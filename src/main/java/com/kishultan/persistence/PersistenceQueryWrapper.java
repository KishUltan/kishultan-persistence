package com.kishultan.persistence;

import com.kishultan.persistence.query.Criterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

/**
 * 持久化查询包装器门面 - 支持字符串属性名和Lambda表达式查询
 * 采用JPA风格命名
 */
public class PersistenceQueryWrapper<T> {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceQueryWrapper.class);
    private final EntityQuery<T> query;
    private final EntityManager entityManager;
    private final Class<T> entityClass;
    private FetchStrategy fetchStrategy = FetchStrategy.BASIC;
    private java.util.function.Consumer<Criterion<T>> queryConfigurer;
    private java.util.function.LongSupplier countSupplier;  // 自定义计数提供者
    private int batchSize = 1000;
    private int pageOffset = 0;
    private int pageSize = 20;

    public PersistenceQueryWrapper(Class<T> entityClass, EntityManager entityManager) {
        this.entityClass = entityClass;
        this.entityManager = entityManager;
        this.query = entityManager.createQuery(entityClass);
    }

    /**
     * 设置查询配置器 - 支持QueryBuilder
     */
    public PersistenceQueryWrapper<T> setQueryConfigurer(java.util.function.Consumer<Criterion<T>> config) {
        this.queryConfigurer = config;
        this.fetchStrategy = FetchStrategy.QUERY_BUILDER;
        return this;
    }

    /**
     * 设置自定义计数提供者 - 用于复杂查询的精确计数
     * 如果设置了CountSupplier，将使用它来执行计数查询
     * 否则使用默认的findCount()方法
     */
    public PersistenceQueryWrapper<T> setCountSupplier(java.util.function.LongSupplier countSupplier) {
        this.countSupplier = countSupplier;
        return this;
    }

    /**
     * 等于条件
     */
    public PersistenceQueryWrapper<T> eq(String property, Object value) {
        query.where().eq(property, value);
        return this;
    }

    /**
     * 等于条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> eq(Columnable<T, ?> property, Object value) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().eq(columnName, value);
        return this;
    }
    /*public PersistenceQueryWrapper<T> setBatchSize(int size) {
        if (size > 0) this.batchSize = size;
        return this;
    }*/
    // ==================== 基础查询条件 ====================

    /**
     * IN条件
     */
    public PersistenceQueryWrapper<T> in(String property, Object... values) {
        query.where().in(property, values);
        return this;
    }

    /**
     * IN条件 - List版本
     */
    public PersistenceQueryWrapper<T> in(String property, List<?> values) {
        query.where().in(property, values);
        return this;
    }

    /**
     * IN条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> in(Columnable<T, ?> property, Object... values) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().in(columnName, values);
        return this;
    }

    /**
     * IN条件 - 支持Lambda，List版本
     */
    public PersistenceQueryWrapper<T> in(Columnable<T, ?> property, List<?> values) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().in(columnName, values);
        return this;
    }

    /**
     * 模糊查询
     */
    public PersistenceQueryWrapper<T> like(String property, String value) {
        query.where().like(property, value);
        return this;
    }

    /**
     * 模糊查询 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> like(Columnable<T, ?> property, String value) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().like(columnName, value);
        return this;
    }

    /**
     * 大于条件
     */
    public PersistenceQueryWrapper<T> gt(String property, Object value) {
        query.where().gt(property, value);
        return this;
    }

    /**
     * 大于条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> gt(Columnable<T, ?> property, Object value) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().gt(columnName, value);
        return this;
    }

    /**
     * 大于等于条件
     */
    public PersistenceQueryWrapper<T> ge(String property, Object value) {
        query.where().ge(property, value);
        return this;
    }

    /**
     * 大于等于条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> ge(Columnable<T, ?> property, Object value) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().ge(columnName, value);
        return this;
    }

    /**
     * 小于条件
     */
    public PersistenceQueryWrapper<T> lt(String property, Object value) {
        query.where().lt(property, value);
        return this;
    }

    /**
     * 小于条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> lt(Columnable<T, ?> property, Object value) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().lt(columnName, value);
        return this;
    }

    /**
     * 小于等于条件
     */
    public PersistenceQueryWrapper<T> le(String property, Object value) {
        query.where().le(property, value);
        return this;
    }

    /**
     * 小于等于条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> le(Columnable<T, ?> property, Object value) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().le(columnName, value);
        return this;
    }

    /**
     * 空值条件
     */
    public PersistenceQueryWrapper<T> isNull(String property) {
        query.where().isNull(property);
        return this;
    }

    /**
     * 空值条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> isNull(Columnable<T, ?> property) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().isNull(columnName);
        return this;
    }

    /**
     * 非空值条件
     */
    public PersistenceQueryWrapper<T> isNotNull(String property) {
        query.where().isNotNull(property);
        return this;
    }

    /**
     * 非空值条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> isNotNull(Columnable<T, ?> property) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().isNotNull(columnName);
        return this;
    }

    /**
     * 范围查询条件
     */
    public PersistenceQueryWrapper<T> between(String property, Object start, Object end) {
        query.where().between(property, start, end);
        return this;
    }

    /**
     * 范围查询条件 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> between(Columnable<T, ?> property, Object start, Object end) {
        String columnName = ColumnabledLambda.getColumnName(property);
        query.where().between(columnName, start, end);
        return this;
    }

    /**
     * AND条件
     */
    public PersistenceQueryWrapper<T> and() {
        query.where().and();
        return this;
    }

    /**
     * AND条件分组 组内条件默认or，可通过QueryCondition重新指定
     */
    public PersistenceQueryWrapper<T> and(java.util.function.Consumer<QueryCondition<T>> andBuilder) {
        query.where().and(andBuilder);
        return this;
    }
    // ==================== 逻辑操作符 ====================

    /**
     * OR条件
     */
    public PersistenceQueryWrapper<T> or() {
        query.where().or();
        return this;
    }

    /**
     * OR条件分组 组内条件默认and，可通过QueryCondition重新指定
     */
    public PersistenceQueryWrapper<T> or(java.util.function.Consumer<QueryCondition<T>> orBuilder) {
        query.where().or(orBuilder);
        return this;
    }

    /**
     * 设置排序（升序）
     */
    public PersistenceQueryWrapper<T> orderBy(String property) {
        query.orderBy(property);
        return this;
    }

    /**
     * 设置排序（升序）- 支持Lambda
     */
    public PersistenceQueryWrapper<T> orderBy(Columnable<T, ?> property) {
        query.orderBy(property);
        return this;
    }
    // ==================== 排序 ====================

    /**
     * 设置排序（升序）- 明确指定ASC
     */
    public PersistenceQueryWrapper<T> orderByAsc(String property) {
        query.orderByAsc(property);
        return this;
    }

    /**
     * 设置排序（升序）- 明确指定ASC，支持Lambda
     */
    public PersistenceQueryWrapper<T> orderByAsc(Columnable<T, ?> property) {
        query.orderByAsc(property);
        return this;
    }

    /**
     * 设置排序（降序）
     */
    public PersistenceQueryWrapper<T> orderByDesc(String property) {
        query.orderByDesc(property);
        return this;
    }

    /**
     * 设置排序（降序）- 支持Lambda
     */
    public PersistenceQueryWrapper<T> orderByDesc(Columnable<T, ?> property) {
        query.orderByDesc(property);
        return this;
    }

    /**
     * 设置排序（支持升序/降序）
     */
    public PersistenceQueryWrapper<T> orderBy(String property, boolean ascending) {
        if (ascending) {
            query.orderByAsc(property);
        } else {
            query.orderByDesc(property);
        }
        return this;
    }

    /**
     * 设置排序（支持升序/降序）- Lambda版本
     */
    public PersistenceQueryWrapper<T> orderBy(Columnable<T, ?> property, boolean ascending) {
        if (ascending) {
            query.orderByAsc(property);
        } else {
            query.orderByDesc(property);
        }
        return this;
    }

    /**
     * 设置分页
     */
    public PersistenceQueryWrapper<T> setPage(PersistencePageRequest pageRequest) {
        this.pageOffset = pageRequest.getOffset();
        this.pageSize = pageRequest.getSize();
        // 仅为 BASIC 路径预置分页参数（JOIN 路径由 joinConfigurer 决定如何注入分页）
        if (fetchStrategy == FetchStrategy.BASIC) {
            query.limit(this.pageOffset, this.pageSize);
        }
        return this;
    }

    /**
     * 设置分页参数
     */
    public PersistenceQueryWrapper<T> setPage(int offset, int size) {
        this.pageOffset = offset;
        this.pageSize = size;
        if (fetchStrategy == FetchStrategy.BASIC) {
            query.limit(this.pageOffset, this.pageSize);
        }
        return this;
    }
    // ==================== 分页 ====================

    /**
     * 设置分页起始位置
     */
    public PersistenceQueryWrapper<T> setFirstRow(int firstRow) {
        this.pageOffset = firstRow;
        if (fetchStrategy == FetchStrategy.BASIC) {
            query.limit(this.pageOffset, this.pageSize);
        }
        // 注意：ADVANCED模式不支持setFirstRow，必须在配置器中明确指定LIMIT位置
        return this;
    }

    /**
     * 设置查询字段
     */
    public PersistenceQueryWrapper<T> select(String... columns) {
        query.select(columns);
        return this;
    }

    /**
     * 设置查询字段 - 支持Lambda
     */
    public PersistenceQueryWrapper<T> select(Columnable<T, ?>... columns) {
        String[] columnNames = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            columnNames[i] = ColumnabledLambda.getColumnName(columns[i]);
        }
        query.select(columnNames);
        return this;
    }

    /**
     * 选择所有字段
     */
    public PersistenceQueryWrapper<T> selectAll() {
        query.selectAll();
        return this;
    }
    // ==================== 字段选择 ====================

    /**
     * 设置去重查询
     */
    public PersistenceQueryWrapper<T> distinct() {
        // TODO: distinct方法已被注释掉，暂时不实现
        // query.distinct();
        return this;
    }

    /**
     * 设置分组
     */
    public PersistenceQueryWrapper<T> groupBy(String... columns) {
        query.groupBy(columns);
        return this;
    }

    /**
     * 设置分组 - 支持Lambda
     */
    public <R> PersistenceQueryWrapper<T> groupBy(Columnable<T, R>... columns) {
        query.groupBy(columns);
        return this;
    }

    /**
     * 执行查询并返回列表
     */
    public List<T> findList() {
        logger.debug("findList() - fetchStrategy: {}, queryConfigurer: {}",
                fetchStrategy, queryConfigurer != null);
        if (fetchStrategy == FetchStrategy.QUERY_BUILDER && queryConfigurer != null) {
            // 使用查询构建器
            Criterion<T> criterion = entityManager.createCriterion(entityClass);
            queryConfigurer.accept(criterion);
            return criterion.findList();
        } else {
            // 使用基础查询
            return query.findList();
        }
    }
    // ==================== 分组 ====================

    /**
     * 执行查询并返回单个结果
     */
    public T findOne() {
        logger.debug("findOne() - fetchStrategy: {}, queryConfigurer: {}",fetchStrategy, queryConfigurer != null);
        if (fetchStrategy == FetchStrategy.QUERY_BUILDER && queryConfigurer != null) {
            Criterion<T> criterion = entityManager.createCriterion(entityClass);
            queryConfigurer.accept(criterion);
            return criterion.findFirst();
        } else {
            return query.findOne();
        }
    }

    /**
     * 执行计数查询
     */
    public long count() {
        logger.debug("count() - fetchStrategy: {}, queryConfigurer: {}",fetchStrategy, queryConfigurer != null);
        if (fetchStrategy == FetchStrategy.QUERY_BUILDER && queryConfigurer != null) {
            Criterion<T> criterion = entityManager.createCriterion(entityClass);
            queryConfigurer.accept(criterion);
            return criterion.count();
        } else {
            return query.count();
        }
    }
    // ==================== 查询执行 ====================

    /**
     * 执行分页查询
     * 确保总是返回有效的分页对象，data字段永远不会为null
     */
    public PersistencePage<T> findPage() {
        long total;
        // ✅ 初始化为空列表而不是null，避免NullPointerException
        List<T> data = java.util.Collections.emptyList();
        if (fetchStrategy == FetchStrategy.QUERY_BUILDER && queryConfigurer != null) {
            Criterion<T> criterion = entityManager.createCriterion(entityClass);
            queryConfigurer.accept(criterion);
            // 🔧 支持自定义计数提供者，用于复杂查询的精确计数
            if (countSupplier != null) {
                // 使用自定义的CountSupplier
                total = countSupplier.getAsLong();
            } else {
                // 使用默认的count()方法
                total = criterion.count();
            }
            if (total > 0) {
                // 🔧 简化：直接调用QueryBuilder的方法，让QueryBuilder自己处理分页
                data = criterion.findList();
                if (data == null) {
                    logger.warn("查询返回null，使用空列表");
                    data = java.util.Collections.emptyList();
                }
            }
        } else {
            // 🔧 支持自定义计数提供者，用于复杂查询的精确计数
            if (countSupplier != null) {
                // 使用自定义的CountSupplier
                total = countSupplier.getAsLong();
            } else {
                // 使用默认的count()方法
                total = query.count();
            }
            if (total > 0) {
                // 🔧 简化：直接调用QueryBuilder的方法，让QueryBuilder自己处理分页
                data = query.findList();
                if (data == null) {
                    logger.warn("查询返回null，使用空列表");
                    data = java.util.Collections.emptyList();
                }
            }
        }
        int pageIndex = (pageOffset / pageSize) + 1;
        return new PersistencePage<>(data, total, pageSize, pageIndex);
    }

    /**
     * 执行查询并返回流
     */
    public java.util.stream.Stream<T> stream() {
        if (fetchStrategy == FetchStrategy.QUERY_BUILDER && queryConfigurer != null) {
            Criterion<T> criterion = entityManager.createCriterion(entityClass);
            queryConfigurer.accept(criterion);
            return criterion.findList().stream();
        } else {
            return query.stream();
        }
    }

    /**
     * 执行查询并返回分页流
     */
    public java.util.stream.Stream<T> streamWithPagination(int pageSize) {
        if (fetchStrategy == FetchStrategy.QUERY_BUILDER && queryConfigurer != null) {
            Criterion<T> criterion = entityManager.createCriterion(entityClass);
            queryConfigurer.accept(criterion);
            return criterion.findList().stream();
        } else {
            // TODO: streamWithPagination方法已被注释掉，暂时不实现
            // return query.streamWithPagination(pageSize);
            return Stream.empty();
        }
    }

    /**
     * 选择表达式 - 支持复杂SQL表达式
     */
    public Criterion<T> selectExpression() {
        if (fetchStrategy == FetchStrategy.QUERY_BUILDER && queryConfigurer != null) {
            Criterion<T> criterion = entityManager.createCriterion(entityClass);
            queryConfigurer.accept(criterion);
            return criterion;
        } else {
            throw new UnsupportedOperationException("selectExpression() only supported with QUERY_BUILDER fetch strategy");
        }
    }

    // ==================== 流式查询 ====================
    // ==================== 获取器方法 ====================
    public EntityQuery<T> getQuery() {
        return query;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public FetchStrategy getFetchStrategy() {
        return fetchStrategy;
    }

    public PersistenceQueryWrapper<T> setFetchStrategy(FetchStrategy strategy) {
        this.fetchStrategy = strategy != null ? strategy : FetchStrategy.BASIC;
        return this;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置分页大小
     */
    public PersistenceQueryWrapper<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        if (fetchStrategy == FetchStrategy.BASIC) {
            query.limit(this.pageOffset, this.pageSize);
        }
        // 注意：ADVANCED模式不支持setMaxRows，必须在配置器中明确指定LIMIT位置
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public enum FetchStrategy {BASIC, QUERY_BUILDER}
}
