package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.EntityManager;
import com.kishultan.persistence.dialect.DatabaseDialect;
import com.kishultan.persistence.dialect.DialectFactory;
import com.kishultan.persistence.dialect.H2Dialect;
import com.kishultan.persistence.query.*;
import com.kishultan.persistence.query.cache.QueryCache;
import com.kishultan.persistence.query.config.CriterionConfigManager;
import com.kishultan.persistence.query.builder.QueryResultBuilder;
import com.kishultan.persistence.query.builder.SQLQueryResultBuilder;
import com.kishultan.persistence.query.context.QueryBuilder;
import com.kishultan.persistence.query.context.QueryBuildContext;
import com.kishultan.persistence.query.context.TableAliasRegistry;
import com.kishultan.persistence.query.executor.QueryExecutor;
import com.kishultan.persistence.query.executor.SQLQueryExecutor;
import com.kishultan.persistence.query.monitor.QueryMetrics;
import com.kishultan.persistence.query.monitor.QueryPerformanceMonitor;
import com.kishultan.persistence.query.utils.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * 查询构建器实现类（无锁版本）
 * 使用新的架构：存储子句对象，通过 buildClause() 方法生成SQL
 * 
 * <p><b>线程安全性说明：</b></p>
 * <p>此类的实例是完全线程安全的。使用原子操作（CAS）实现无锁并发控制。</p>
 * <p>可以在多线程间安全地共享同一个实例。</p>
 * 
 * <p><b>无锁实现特点：</b></p>
 * <ul>
 *   <li>使用 AtomicReference 和 AtomicInteger 包装所有可变字段</li>
 *   <li>使用 CAS（Compare-And-Swap）操作进行原子更新</li>
 *   <li>查询执行时使用快照（原子读取）</li>
 *   <li>无锁竞争，性能最优</li>
 * </ul>
 * 
 * <p><b>使用方式：</b></p>
 * <pre>
 * // ✅ 正确：每个线程创建自己的实例（推荐）
 * EntityManager em = PersistenceManager.getDefaultManager();
 * Criterion<User> qb = em.createQueryBuilder(User.class);
 * List<User> users = qb.where().eq(User::getName, "John").findList();
 * 
 * // ✅ 也可以：在多线程间共享同一个实例（线程安全）
 * Criterion<User> sharedQb = em.createQueryBuilder(User.class);
 * // 线程 1
 * List<User> users1 = sharedQb.where().eq(User::getName, "John").findList();
 * // 线程 2
 * List<User> users2 = sharedQb.where().eq(User::getEmail, "test@example.com").findList();
 * </pre>
 */
public class StandardCriterion<T> implements Criterion<T> {
    private static final Logger logger = LoggerFactory.getLogger(StandardCriterion.class);
    private final Class<T> entityClass;
    private final TableAliasRegistry aliasRegistry = new TableAliasRegistry();
    private final QueryBuildContext<T> buildContext = new QueryBuildContext<>();
    private final DefaultRowMapper defaultMapper = new DefaultRowMapper();
    private final AtomicReference<RowMapper> customRowMapperRef = new AtomicReference<>(null);
    private Class<?> customResultType;
    
    // ==================== 原子字段（无锁实现）====================
    // 存储各个子句对象（使用 AtomicReference）
    private final AtomicReference<SelectClause<T>> selectClauseRef = new AtomicReference<>(null);
    private final AtomicReference<FromClause<T>> fromClauseRef = new AtomicReference<>(null);
    private final AtomicReference<List<JoinClause<T>>> joinClausesRef = 
        new AtomicReference<>(new ArrayList<>());
    private final AtomicReference<WhereClause<T>> whereClauseRef = new AtomicReference<>(null);
    private final AtomicReference<GroupClause<T>> groupClauseRef = new AtomicReference<>(null);
    private final AtomicReference<HavingClause<T>> havingClauseRef = new AtomicReference<>(null);
    private final AtomicReference<OrderClause<T>> orderClauseRef = new AtomicReference<>(null);
    
    // 子查询引用（使用 AtomicReference）
    private final AtomicReference<StandardCriterion<?>> subqueryRef = new AtomicReference<>(null);
    
    // 分页参数（使用 AtomicInteger）
    private final AtomicInteger offsetValueRef = new AtomicInteger(0);
    private final AtomicInteger limitValueRef = new AtomicInteger(0);
    
    // 统一使用 QueryExecutor 接口（SQL 和 NoSQL 都通过此接口）
    private QueryExecutor<T> queryExecutor;
    // 查询构建器和执行器
    private QueryResultBuilder resultBuilder;
    // 数据源引用
    private DataSource dataSource;
    // EntityManager 引用（用于获取事务连接）
    private EntityManager entityManager;
    // 数据库方言
    private DatabaseDialect dialect;
    // 性能监控和缓存（通过配置管理器获取）
    private QueryPerformanceMonitor performanceMonitor;
    private QueryCache queryCache;
    private boolean performanceMonitoringEnabled = false;
    private boolean cacheEnabled = false;

    // ==================== 构造函数 ====================
    public StandardCriterion(Class<T> entityClass, DataSource dataSource) {
        this(entityClass, dataSource, null);
    }
    
    /**
     * 构造函数（支持传入 EntityManager 以获取事务连接）
     */
    public StandardCriterion(Class<T> entityClass, DataSource dataSource, EntityManager entityManager) {
        this.entityClass = entityClass;
        this.dataSource = dataSource;
        this.entityManager = entityManager;
        
        if (dataSource != null) {
            // 创建 SqlExecutor
            SqlExecutor sqlExecutor;
            if (entityManager != null) {
                sqlExecutor = new SimpleSqlExecutor(dataSource, entityManager);
            } else {
                sqlExecutor = new SimpleSqlExecutor(dataSource);
            }
            // 将 SqlExecutor 适配为 QueryExecutor
            this.queryExecutor = new SQLQueryExecutor<>(sqlExecutor);
            // 从 DataSource 获取数据库方言
            this.dialect = resolveDialect(dataSource);
            defaultMapper.setDialect(dialect);
            // 初始化 SQL 查询结果构建器
            this.resultBuilder = new SQLQueryResultBuilder();
        }
        
        // 注册主表到ResultSetMapper
        String tableName = EntityUtils.getTableName(entityClass);
        defaultMapper.register(entityClass, tableName);
    }

    /**
     * 从 DataSource 解析数据库方言
     * 优化：使用 DialectFactory 的统一方法，避免重复的连接获取逻辑
     */
    private DatabaseDialect resolveDialect(DataSource dataSource) {
        if (dataSource == null) {
            logger.debug("数据源为 null，使用默认H2方言");
            return new H2Dialect();
        }
        
        try {
            // 使用 DialectFactory 的统一方法，它会处理连接异常
            return DialectFactory.createDialect(dataSource);
        } catch (Exception e) {
            // 仅在调试级别记录，避免在测试环境中产生大量警告
            logger.debug("无法从数据源解析数据库方言，使用默认H2方言: {}", e.getMessage());
            return new H2Dialect();
        }
    }

    /**
     * 设置数据源
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 设置SQL执行器（自动适配为 QueryExecutor）
     */
    /*public void setSqlExecutor(SqlExecutor sqlExecutor) {
        this.queryExecutor = new com.kishultan.persistence.orm.query.executor.SQLQueryExecutor<>(sqlExecutor);
    }*/
    
    /**
     * 设置查询执行器（统一接口，支持 SQL 和 NoSQL）
     * 
     * @param executor 查询执行器
     */
    public void setExecutor(QueryExecutor<T> executor) {
        this.queryExecutor = executor;
    }
    
    /**
     * 设置查询结果构建器
     * 
     * @param resultBuilder 查询结果构建器
     */
    public void setResultBuilder(QueryResultBuilder resultBuilder) {
        this.resultBuilder = resultBuilder;
    }

    // ==================== 别名注册表管理 ====================
    public TableAliasRegistry getAliasRegistry() {
        return aliasRegistry;
    }

    public void registerTable(Class<?> entityClass, String tableName, String alias) {
        aliasRegistry.registerTable(tableName, alias);
        defaultMapper.register(entityClass, alias);
    }

    public String getTableAlias(String tableName) {
        return aliasRegistry.getAlias(tableName);
    }

    public DefaultRowMapper getResultSetMapper() {
        return defaultMapper;
    }

    public QueryBuildContext<T> getBuildContext() {
        return buildContext;
    }
    
    // ==================== 智能展开辅助方法 ====================

    /**
     * 检查是否有JOIN子句
     */
    public boolean hasJoinClause() {
        return !joinClausesRef.get().isEmpty();
    }

    /**
     * 获取所有相关表的字段（带表别名和字段别名）
     * 包括主表和所有JOIN表的字段，避免歧义和重复展开
     * 字段别名规则：表别名__列名
     */
    public String[] getAllTableFields() {
        List<String> allFields = new ArrayList<>();
        Set<Class<?>> processedEntityClasses = new HashSet<>();
        
        // 1. 添加主表字段
        String mainTableAlias = getCurrentTableAlias();
        if (mainTableAlias == null) {
            mainTableAlias = EntityUtils.getTableName(entityClass);
        }
        String[] mainTableFields = EntityUtils.getColumnNames(entityClass);
        for (String field : mainTableFields) {
            String fieldWithAlias = mainTableAlias + "." + field + " AS " + mainTableAlias + "__" + field;
            allFields.add(fieldWithAlias);
        }
        processedEntityClasses.add(entityClass);
        
        // 2. 添加所有JOIN表的字段（避免重复展开）
        List<JoinClause<T>> joinClauses = joinClausesRef.get();
        for (JoinClause<T> joinClause : joinClauses) {
            if (joinClause instanceof JoinClauseImpl) {
                JoinClauseImpl<T> joinImpl = (JoinClauseImpl<T>) joinClause;
                String joinTableAlias = joinImpl.getCurrentTableAlias();
                Class<?> joinEntityClass = joinImpl.getJoinEntityClass();
                
                if (joinTableAlias != null && joinEntityClass != null &&
                        !processedEntityClasses.contains(joinEntityClass)) {
                    String[] joinTableFields = EntityUtils.getColumnNames(joinEntityClass);
                    for (String field : joinTableFields) {
                        String fieldWithAlias = joinTableAlias + "." + field + " AS " + joinTableAlias + "__" + field;
                        allFields.add(fieldWithAlias);
                    }
                    processedEntityClasses.add(joinEntityClass);
                }
            }
        }
        return allFields.toArray(new String[0]);
    }

    // ==================== 主查询构建 ====================
    @Override
    public SelectClause<T> select() {
        // 🔧 修复：清除之前的状态，避免状态污染
        resetQueryState();
        SelectClause<T> newSelectClause = new SelectClauseImpl<>(this);
        selectClauseRef.set(newSelectClause);
        return newSelectClause;
    }

    @Override
    public SelectClause<T> select(String... columns) {
        // 🔧 修复：清除之前的状态，避免状态污染
        resetQueryState();
        SelectClause<T> newSelectClause = new SelectClauseImpl<T>(this);
        // 将选择的字段传递给SelectClauseImpl
        if (columns != null && columns.length > 0) {
            ((SelectClauseImpl<T>) newSelectClause).setSelectedFields(columns);
        }
        selectClauseRef.set(newSelectClause);
        return newSelectClause;
    }

    @Override
    @SafeVarargs
    public final SelectClause<T> select(Columnable<T, ?>... fields) {
        // 🔧 修复：清除之前的状态，避免状态污染
        resetQueryState();
        SelectClause<T> newSelectClause = new SelectClauseImpl<T>(this);
        // 将选择的字段传递给SelectClauseImpl
        if (fields != null && fields.length > 0) {
            String[] fieldNames = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].isField()) {
                    fieldNames[i] = fields[i].columnName();
                } else {
                    fieldNames[i] = fields[i].toSql();
                }
            }
            ((SelectClauseImpl<T>) newSelectClause).setSelectedFields(fieldNames);
        }
        selectClauseRef.set(newSelectClause);
        return newSelectClause;
    }

    @Override
    public SelectClause<T> selectAll() {
        // 🔧 修复：清除之前的状态，避免状态污染
        resetQueryState();
        SelectClause<T> newSelectClause = new SelectClauseImpl<T>(this, true);
        selectClauseRef.set(newSelectClause);
        return newSelectClause;
    }

    // ==================== 状态管理 ====================
    /**
     * 重置查询状态，清除所有子句，避免状态污染
     * 当用户开始新的查询时调用此方法
     */
    private void resetQueryState() {
        selectClauseRef.set(null);
        fromClauseRef.set(null);
        joinClausesRef.set(new ArrayList<>());
        whereClauseRef.set(null);
        groupClauseRef.set(null);
        havingClauseRef.set(null);
        orderClauseRef.set(null);
        subqueryRef.set(null);
        offsetValueRef.set(0);
        limitValueRef.set(0);
        // 清空构建上下文
        buildContext.clear();
    }

    // ==================== 子查询构建 ====================
    // 已移动到select子句中实现
    /*@Override
    public Criterion<T> subquery() {
        StandardCriterion<T> subquery = new StandardCriterion<>(entityClass, dataSource, entityManager);
        return subquery;
    }*/

    // ==================== 执行方法 ====================
    @Override
    public List<T> findList() {
        if (queryExecutor == null) {
            throw new IllegalStateException("查询执行器未设置");
        }
        
        // 如果启用了缓存，先尝试从缓存获取
        if (CriterionConfigManager.isCacheEnabled()) {
            QueryCache cache = getQueryCache();
            if (cache != null) {
                String cacheKey = generateCacheKey("findList");
                @SuppressWarnings("unchecked")
                List<T> cachedResult = cache.get(cacheKey, List.class);
                if (cachedResult != null) {
                    logger.debug("从缓存获取查询结果: cacheKey={}", cacheKey);
                    return cachedResult;
                }
            }
        }
        
        // 开始性能监控
        String contextId = startPerformanceMonitoring();
        try {
            QueryBuilder queryResult = buildQuery();
            if(logger.isDebugEnabled()){
                logger.debug("-------------------------------------");
                logger.debug("findList->SQL : {}", queryResult.getSql());
                logger.debug("findList->parameters: {}", queryResult.getParameters());
                logger.debug("-------------------------------------");
            }
            
            // 统一使用 QueryExecutor 执行查询
            List<T> result;
            RowMapper customRowMapper = customRowMapperRef.get();
            if (customRowMapper != null) {
                // 使用自定义RowMapper
                @SuppressWarnings("unchecked")
                RowMapper<T> typedRowMapper = customRowMapper;
                @SuppressWarnings("unchecked")
                Class<T> typedResultType = (Class<T>) customResultType;
                result = queryExecutor.executeQuery(queryResult.getSql(), queryResult.getParameters(), typedResultType, typedRowMapper);
            } else {
                // 使用默认的ResultSetMapper
                @SuppressWarnings("unchecked")
                DefaultRowMapper typedDefaultMapper = defaultMapper;
                result = queryExecutor.executeQuery(queryResult.getSql(), queryResult.getParameters(), entityClass, typedDefaultMapper);
            }
            
            // 结束性能监控
            endPerformanceMonitoring(contextId, true, result != null ? result.size() : 0);
            
            // 如果启用了缓存，存储结果到缓存
            if (CriterionConfigManager.isCacheEnabled() && result != null && !result.isEmpty()) {
                QueryCache cache = getQueryCache();
                if (cache != null) {
                    String cacheKey = generateCacheKey("findList");
                    cache.put(cacheKey, result, 300000); // 5分钟TTL
                    logger.debug("查询结果已缓存: cacheKey={}, resultSize={}", cacheKey, result.size());
                }
            }
            return result;
        } catch (Exception e) {
            // 记录性能监控错误
            recordPerformanceError(contextId, e);
            throw e;
        }
    }

    @Override
    public T findFirst() {
        List<T> list = findList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public long count() {
        if (queryExecutor == null) {
            throw new IllegalStateException("查询执行器未设置，请先设置数据源或执行器");
        }
        
        // 如果启用了缓存，先尝试从缓存获取
        if (CriterionConfigManager.isCacheEnabled()) {
            QueryCache cache = getQueryCache();
            if (cache != null) {
                String cacheKey = generateCacheKey("count");
                Long cachedResult = cache.get(cacheKey, Long.class);
                if (cachedResult != null) {
                    logger.debug("从缓存获取计数结果: cacheKey={}, count={}", cacheKey, cachedResult);
                    return cachedResult;
                }
            }
        }
        
        // 开始性能监控
        String contextId = startPerformanceMonitoring();
        try {
            QueryBuilder queryResult = buildQuery();
            if(logger.isDebugEnabled()){
                logger.debug("-------------------------------------");
                logger.debug("count->SQL : {}", queryResult.getCountSql());
                logger.debug("count->parameters: {}", queryResult.getParameters());
                logger.debug("-------------------------------------");
            }
            
            // 统一使用 QueryExecutor 执行计数查询
            long result = queryExecutor.executeCount(queryResult.getCountSql(), queryResult.getCountParameters());
            
            // 结束性能监控
            endPerformanceMonitoring(contextId, true, 1); // count查询结果数量为1
            
            // 如果启用了缓存，存储结果到缓存
            if (CriterionConfigManager.isCacheEnabled()) {
                QueryCache cache = getQueryCache();
                if (cache != null) {
                    String cacheKey = generateCacheKey("count");
                    cache.put(cacheKey, result, 60000); // 1分钟TTL
                    logger.debug("计数结果已缓存: cacheKey={}, count={}", cacheKey, result);
                }
            }
            return result;
        } catch (Exception e) {
            // 记录性能监控错误
            recordPerformanceError(contextId, e);
            throw e;
        }
    }

    // 分页查询方法（不在接口中，但提供便利方法）
    public PaginationSupport.PaginatedResult<T> findPage(int page, int size) {
        offsetValueRef.set((page - 1) * size);
        limitValueRef.set(size);
        long total = count();
        List<T> list = findList();
        return new PaginatedResultImpl<>(list, total, page, size);
    }

    // ==================== 实现接口要求的方法 ====================
    @Override
    public String getGeneratedSql() {
        QueryBuilder queryResult = buildQuery();
        return queryResult.getSql();
    }

    @Override
    public boolean isSubquery() {
        // TODO: 实现子查询检测逻辑
        return false;
    }

    void setSubquery(StandardCriterion<?> subquery) {
        subqueryRef.set(subquery);
    }

    // ==================== 新架构方法 ====================
    @Override
    public String getSubquerySql() {
        return isSubquery() ? getGeneratedSql() : "";
    }

    /**
     * 构建查询结果
     * 使用新的架构：将子句对象设置到 QueryBuildContext，然后使用 SQLQueryResultBuilder 生成 SQL
     */
    public QueryBuilder buildQuery() {
        // 清空构建上下文
        buildContext.clear();
        
        // 🔧 自动初始化必要的子句，确保无条件查询也能正常工作
        SelectClause<T> selectClause = selectClauseRef.get();
        FromClause<T> fromClause = fromClauseRef.get();
        
        if (selectClause == null) {
            SelectClause<T> newSelectClause = new SelectClauseImpl<>(this);
            selectClauseRef.set(newSelectClause);
            selectClause = newSelectClause;
        }
        
        if (fromClause == null) {
            // 使用实体类和表名初始化FromClause
            String tableName = EntityUtils.getTableName(entityClass);
            FromClause<T> newFromClause = new FromClauseImpl<>(this, entityClass, tableName, tableName);
            fromClauseRef.set(newFromClause);
            fromClause = newFromClause;
        }
        
        // 设置数据库方言
        buildContext.setDialect(dialect);
        
        // 将子句对象设置到构建上下文
        buildClauses();
        
        // 设置分页信息
        buildContext.setOffsetValue(offsetValueRef.get());
        buildContext.setLimitValue(limitValueRef.get());
        
        // 使用 SQLQueryResultBuilder 生成 SQL 和参数
        if (resultBuilder == null) {
            resultBuilder = new SQLQueryResultBuilder();
        }
        
        SQLQueryResultBuilder sqlBuilder = (SQLQueryResultBuilder) resultBuilder;
        
        // 先构建子查询（如果有），以便收集子查询的参数
        List<Object> subqueryParameters = new ArrayList<>();
        StandardCriterion<?> subquery = subqueryRef.get();
        if (subquery != null) {
            QueryBuilder subQueryResult = subquery.buildQuery();
            subqueryParameters.addAll(subQueryResult.getParameters());
        }
        
        SQLQueryResultBuilder.QueryResultWithParams queryResult = sqlBuilder.buildQueryWithParams(buildContext);
        SQLQueryResultBuilder.QueryResultWithParams countResult = sqlBuilder.buildCountQueryWithParams(buildContext);
        
        // 合并子查询的参数（子查询参数放在前面，因为子查询在 FROM 子句中）
        // 主查询和 count 查询使用相同的参数列表（因为它们有相同的 WHERE 条件和子查询）
        List<Object> allParameters = new ArrayList<>();
        allParameters.addAll(subqueryParameters);
        allParameters.addAll(queryResult.getParameters());
        
        // 验证 count 查询的参数是否与主查询一致（应该一致，因为它们有相同的 WHERE 条件）
        // 如果 count 查询的参数与主查询不同，需要单独处理
        List<Object> allCountParameters = new ArrayList<>();
        allCountParameters.addAll(subqueryParameters);
        allCountParameters.addAll(countResult.getParameters());
        
        // 检查参数数量是否匹配（用于调试）
        /*if (logger.isDebugEnabled()) {
            logger.debug("=== Criterion 生成的SQL ===");
            logger.debug("查询SQL：{}", queryResult.getSql());
            logger.debug("计数SQL：{}", countResult.getSql());
            logger.debug("子查询参数：{}", subqueryParameters);
            logger.debug("主查询参数：{}", queryResult.getParameters());
            logger.debug("count查询参数：{}", countResult.getParameters());
            logger.debug("合并后参数（主查询）：{}", allParameters);
            logger.debug("合并后参数（count查询）：{}", allCountParameters);
            logger.debug("================================");
        }*/
        
        // 使用合并后的参数（主查询和 count 查询应该使用相同的参数列表）
        // 如果 count 查询的参数不同，需要修改 QueryBuilder 以支持独立的参数列表
        return new QueryBuilder(queryResult.getSql(), countResult.getSql(), allParameters, allCountParameters);
    }
    
    /**
     * 构建子句：将各个子句对象设置到 QueryBuildContext
     */
    private void buildClauses() {
        // SELECT 子句
        SelectClause<T> selectClause = selectClauseRef.get();
        FromClause<T> fromClause = fromClauseRef.get();
        List<JoinClause<T>> joinClauses = joinClausesRef.get();
        WhereClause<T> whereClause = whereClauseRef.get();
        GroupClause<T> groupClause = groupClauseRef.get();
        HavingClause<T> havingClause = havingClauseRef.get();
        OrderClause<T> orderClause = orderClauseRef.get();
        
        if (selectClause != null) {
            buildContext.setSelectClause(selectClause);
        }
        
        // FROM 子句
        if (fromClause != null) {
            buildContext.setFromClause(fromClause);
        }
        
        // JOIN 子句
        for (JoinClause<T> joinClause : joinClauses) {
            buildContext.addJoinClause(joinClause);
        }
        
        // WHERE 子句
        if (whereClause != null) {
            buildContext.setWhereClause(whereClause);
        }
        
        // GROUP BY 子句
        if (groupClause != null) {
            buildContext.setGroupClause(groupClause);
        }
        
        // HAVING 子句
        if (havingClause != null) {
            buildContext.setHavingClause(havingClause);
        }
        
        // ORDER BY 子句
        if (orderClause != null) {
            buildContext.setOrderClause(orderClause);
        }
        
        // 处理子查询（如果有）
        StandardCriterion<?> subquery = subqueryRef.get();
        if (subquery != null) {
            QueryBuilder subQueryResult = subquery.buildQuery();
            // 子查询的参数会在 SQLQueryResultBuilder 中处理
            // 这里可以添加子查询处理逻辑
        }
    }

    // ==================== 内部方法 ====================
    public Class<T> getEntityClass() {
        return entityClass;
    }

    public int getOffsetValue() {
        return offsetValueRef.get();
    }

    public int getLimitValue() {
        return limitValueRef.get();
    }

    public boolean hasPagination() {
        return limitValueRef.get() > 0;
    }

    // ==================== 必要的方法 ====================
    @Override
    public Criterion<T> limit(int offset, int size) {
        offsetValueRef.set(offset);
        limitValueRef.set(size);
        return this;
    }

    // ==================== StreamingQueryBuilder 支持 ====================
    
    /**
     * 创建流式查询构建器
     * 支持 SQL 和 NoSQL 数据库
     * 
     * @return StreamingQueryBuilder 实例
     */
    public StreamingCriterion<T> createStreamingCriterion() {
        if (queryExecutor == null) {
            throw new IllegalStateException("查询执行器未设置，无法创建流式查询构建器");
        }
        
        // 获取 RowMapper（优先使用自定义的，否则使用默认的）
        RowMapper customRowMapper = customRowMapperRef.get();
        @SuppressWarnings("unchecked")
        RowMapper<T> mapper = customRowMapper != null ? (RowMapper<T>) customRowMapper : (RowMapper<T>) defaultMapper;
        
        // 使用通用实现，支持 SQL 和 NoSQL
        return new StreamingCriterionImpl<>(this, queryExecutor, mapper);
    }
    
    // ==================== 子句设置方法 ====================
    void setFromClause(FromClause<T> fromClause) {
        fromClauseRef.set(fromClause);
    }

    void addJoinClause(JoinClause<T> joinClause) {
        List<JoinClause<T>> currentJoinClauses = joinClausesRef.get();
        List<JoinClause<T>> newJoinClauses = new ArrayList<>(currentJoinClauses);
        newJoinClauses.add(joinClause);
        joinClausesRef.set(newJoinClauses);
    }

    void setWhereClause(WhereClause<T> whereClause) {
        whereClauseRef.set(whereClause);
    }

    /**
     * 条件构建器模式 - 支持 Consumer 的 where 方法
     * 允许在 Criterion 构建完成后，通过 Consumer 动态添加 where 条件
     */
    @Override
    public Criterion<T> where(Consumer<WhereClause<T>> whereBuilder) {
        if (whereBuilder != null) {
            WhereClause<T> currentWhereClause = whereClauseRef.get();
            WhereClause<T> newWhereClause;
            
            if (currentWhereClause == null) {
                newWhereClause = new WhereClauseImpl<>(this);
                if (!whereClauseRef.compareAndSet(null, newWhereClause)) {
                    // CAS 失败，重试
                    return where(whereBuilder);
                }
            } else {
                newWhereClause = currentWhereClause;
            }
            
            // 使用 Consumer 构建 where 条件
            whereBuilder.accept(newWhereClause);
        }
        return this;
    }

    void setGroupClause(GroupClause<T> groupClause) {
        groupClauseRef.set(groupClause);
    }

    void setHavingClause(HavingClause<T> havingClause) {
        havingClauseRef.set(havingClause);
    }

    public void setOrderClause(OrderClause<T> orderClause) {
        orderClauseRef.set(orderClause);
    }

    @Override
    public OrderClause<T> createOrderClause() {
        // 🔧 修复：如果已存在，返回现有实例，避免多次调用时丢失之前的排序条件
        OrderClause<T> currentOrderClause = orderClauseRef.get();
        if (currentOrderClause == null) {
            OrderClause<T> newOrderClause = new OrderClauseImpl<>(this);
            if (!orderClauseRef.compareAndSet(null, newOrderClause)) {
                // CAS 失败，重试
                return createOrderClause();
            }
            return newOrderClause;
        }
        return currentOrderClause;
    }

    @Override
    public GroupClause<T> createGroupClause() {
        // 🔧 修复：如果已存在，返回现有实例，避免多次调用时丢失之前的分组条件
        GroupClause<T> currentGroupClause = groupClauseRef.get();
        if (currentGroupClause == null) {
            GroupClause<T> newGroupClause = new GroupClauseImpl<>(this);
            if (!groupClauseRef.compareAndSet(null, newGroupClause)) {
                // CAS 失败，重试
                return createGroupClause();
            }
            return newGroupClause;
        }
        return currentGroupClause;
    }

    // ==================== 子查询字段引用 ====================
    @Override
    public String selfField(Columnable<T, ?> fieldSelector) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String currentTableAlias = getCurrentTableAlias();
        return currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
    }

    @Override
    public String subqueryField(Columnable<T, ?> fieldSelector) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        return "subquery." + fieldName;
    }

    public String getCurrentTableAlias() {
        // 主表的别名就是表名
        if (entityClass != null) {
            return EntityUtils.getTableName(entityClass);
        }
        return null;
    }

    /**
     * 尝试从当前线程获取数据库连接
     */
    private Connection getCurrentConnection() {
        try {
            // 如果有数据源，从数据源获取连接
            if (dataSource != null) {
                return dataSource.getConnection();
            }
            // 如果没有数据源，返回null
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建分页子句，使用数据库方言
     */
    private String buildLimitClause(DatabaseDialect dialect, int offset, int limit) {
        if (dialect == null) {
            // 默认使用标准 LIMIT/OFFSET 语法
            return "LIMIT " + limit + (offset > 0 ? " OFFSET " + offset : "");
        }

        // 直接调用方言的 limit 方法
        if (offset > 0) {
            return dialect.limit(offset, limit);
        } else {
            return dialect.limit(limit);
        }
    }

    /**
     * 获取数据库方言
     */
    public DatabaseDialect getDialect() {
        return dialect;
    }

    /**
     * 为标识符添加引号（表名、列名）
     */
    public String quoteIdentifier(String identifier) {
        if (dialect == null || identifier == null) {
            return identifier;
        }
        return dialect.getTableNameQuoteLeft() + identifier + dialect.getTableNameQuoteRight();
    }

    // ==================== 性能监控支持 ====================
    @Override
    public QueryMetrics getPerformanceMetrics() {
        QueryPerformanceMonitor monitor = getPerformanceMonitor();
        return monitor != null ? monitor.getMetrics() : null;
    }

    @Override
    public QueryPerformanceMonitor getPerformanceMonitor() {
        if (performanceMonitor == null) {
            performanceMonitor = CriterionConfigManager.getPerformanceMonitor();
            performanceMonitoringEnabled = CriterionConfigManager.isPerformanceMonitoringEnabled();
        }
        return performanceMonitor;
    }

    // ==================== 缓存支持 ====================
    @Override
    public QueryCache getQueryCache() {
        if (queryCache == null) {
            queryCache = CriterionConfigManager.getQueryCache();
            cacheEnabled = CriterionConfigManager.isCacheEnabled();
        }
        return queryCache;
    }

    @Override
    public Criterion setRowMapper(RowMapper rowMapper) {
        customRowMapperRef.set(rowMapper);
        //this.customResultType = getRowMapperResultType(rowMapper);
        return this;
    }

    @Override
    public RowMapper<?> getRowMapper() {
        return customRowMapperRef.get();
    }
    
    // ==================== 辅助方法 ====================

    /**
     * 生成缓存键
     * 使用MD5哈希算法避免哈希冲突
     *
     * @param operation 操作类型
     * @return 缓存键
     */
    private String generateCacheKey(String operation) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append("querybuilder:").append(entityClass.getSimpleName().toLowerCase());
        keyBuilder.append(":").append(operation);
        
        // 使用MD5获得更好的哈希分布，避免哈希冲突
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            // 添加SQL
            md.update(getGeneratedSql().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // 添加参数（从构建的查询中获取）
            QueryBuilder queryBuilder = buildQuery();
            if (queryBuilder.getParameters() != null && !queryBuilder.getParameters().isEmpty()) {
                for (Object param : queryBuilder.getParameters()) {
                    md.update(String.valueOf(param).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            keyBuilder.append(":").append(hexString.toString());
        } catch (Exception e) {
            // 如果MD5失败，回退到更简单的方法
            logger.warn("生成MD5哈希失败，使用简单方法: {}", e.getMessage());
            keyBuilder.append(":").append(getGeneratedSql());
            QueryBuilder queryBuilder = buildQuery();
            if (queryBuilder.getParameters() != null) {
                keyBuilder.append(":").append(queryBuilder.getParameters());
            }
        }
        return keyBuilder.toString();
    }

    /**
     * 开始性能监控
     *
     * @return 监控上下文ID
     */
    private String startPerformanceMonitoring() {
        if (!CriterionConfigManager.isPerformanceMonitoringEnabled()) {
            return null;
        }
        try {
            QueryPerformanceMonitor monitor = getPerformanceMonitor();
            if (monitor != null) {
                QueryBuilder queryBuilder = buildQuery();
                String sql = queryBuilder.getSql();
                Object[] parameters = queryBuilder.getParameters().toArray();
                return monitor.startMonitoring(sql, parameters);
            }
        } catch (Exception e) {
            logger.warn("开始性能监控失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 结束性能监控
     *
     * @param contextId   监控上下文ID
     * @param success     是否成功
     * @param resultCount 结果数量
     */
    private void endPerformanceMonitoring(String contextId, boolean success, int resultCount) {
        if (contextId != null) {
            QueryPerformanceMonitor monitor = getPerformanceMonitor();
            if (monitor != null) {
                monitor.endMonitoring(contextId, success, resultCount);
            }
        }
    }

    /**
     * 记录性能监控错误
     *
     * @param contextId 监控上下文ID
     * @param error     错误
     */
    private void recordPerformanceError(String contextId, Throwable error) {
        if (contextId != null) {
            QueryPerformanceMonitor monitor = getPerformanceMonitor();
            if (monitor != null) {
                monitor.recordError(contextId, error);
            }
        }
    }
}