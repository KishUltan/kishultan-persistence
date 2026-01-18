package com.kishultan.persistence;

import com.kishultan.persistence.query.BeanMeta;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.DefaultRowMapper;
import com.kishultan.persistence.query.PropertyAccessor;
import com.kishultan.persistence.query.clause.StandardCriterion;
import com.kishultan.persistence.query.utils.EntityUtils;
import com.zaxxer.sansorm.OrmElf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 实体管理器 - 抽象层
 * <p>
 * 提供实体CRUD操作和事务管理的统一接口
 * 线程安全实现，使用ThreadLocal管理事务状态
 * 不依赖具体的实现类，只依赖接口
 */
public class EntityManager {
    private static final Logger logger = LoggerFactory.getLogger(EntityManager.class);
    // 使用ThreadLocal确保每个线程有独立的事务上下文
    private static final ThreadLocal<EntityTransaction> currentTransactionHolder = new ThreadLocal<>();
    private final EntityManagerFactory entityManagerFactory;
    private final DataSource dataSource;
    private final String dataSourceName;

    public EntityManager(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        // 通过接口获取数据源信息，完全依赖抽象
        this.dataSource = entityManagerFactory.getDataSource();
        this.dataSourceName = entityManagerFactory.getDataSourceName();
//        logger.debug("创建EntityManager实例");
    }

    /**
     * 创建查询
     */
    public <T> EntityQuery<T> createQuery(Class<T> entityClass) {
        logger.debug("创建查询: {}", entityClass.getSimpleName());
        if (dataSource != null) {
            // 创建QueryBuilder实例并传入SimpleEntityQuery
            Criterion<T> criterion = createCriterion(entityClass);
            return new SimpleEntityQuery<>(entityClass, criterion);
        }
        throw new UnsupportedOperationException("Queries are only supported with SansOrm");
    }

    /**
     * 创建标准查询 - 新架构支持
     */
    public <T> Criterion<T> createCriterion(Class<T> entityClass) {
        logger.debug("创建标准查询: {}", entityClass.getSimpleName());
        if (dataSource != null) {
            // 传入 EntityManager 引用，以便 Criterion 可以获取事务连接
            StandardCriterion<T> criterion =
                    new StandardCriterion<>(entityClass, dataSource, this);
            return criterion;
        }
        throw new UnsupportedOperationException("Criterion is only supported with SansOrm");
    }

    /**
     * 保存实体
     */
    public <T> T save(T entity) {
        logger.debug("保存实体: {}", entity.getClass().getSimpleName());
        return executeWithTransactionOrConnection(
                () -> "保存实体",
                connection -> saveWithConnection(entity, connection),
                () -> saveWithConnection(entity, null)
        );
    }

    /**
     * 批量保存实体
     */
    public <T> List<T> saveAll(List<T> entities) {
        logger.debug("批量保存实体，数量: {}", entities.size());
        return executeWithTransactionOrConnection(
                () -> "批量保存实体",
                connection -> saveAllWithConnection(entities, connection),
                () -> saveAllWithConnection(entities, null)
        );
    }

    /**
     * 更新实体
     */
    public <T> T update(T entity) {
        logger.debug("更新实体: {}", entity.getClass().getSimpleName());
        return executeWithTransactionOrConnection(
                () -> "更新实体",
                connection -> updateWithConnection(entity, connection),
                () -> updateWithConnection(entity, null)
        );
    }

    /**
     * 删除实体
     */
    public <T> void delete(T entity) {
        logger.debug("删除实体: {}", entity.getClass().getSimpleName());
        executeWithTransactionOrConnection(
                () -> "删除实体",
                connection -> {
                    deleteWithConnection(entity, connection);
                    return null;
                },
                () -> {
                    deleteWithConnection(entity, null);
                    return null;
                }
        );
    }

    /**
     * 根据ID删除实体
     */
    public <T> void deleteById(Class<T> entityClass, Object id) {
        logger.debug("根据ID删除实体: {} - {}", entityClass.getSimpleName(), id);
        executeWithTransactionOrConnection(
                () -> "根据ID删除实体",
                connection -> {
                    deleteByIdWithConnection(entityClass, id, connection);
                    return null;
                },
                () -> {
                    deleteByIdWithConnection(entityClass, id, null);
                    return null;
                }
        );
    }

    /**
     * 根据ID查找实体
     */
    public <T> T findById(Class<T> entityClass, Object id) {
        logger.debug("根据ID查找实体: {} - {}", entityClass.getSimpleName(), id);
        return findByIdWithConnection(entityClass, id, null);
    }

    /**
     * 查找所有实体
     */
    public <T> List<T> findAll(Class<T> entityClass) {
        logger.debug("查找所有实体: {}", entityClass.getSimpleName());
        return findAllWithConnection(entityClass, null);
    }

    /**
     * 执行原生SQL查询
     */
    public List<Object> executeSql(String sql, Object... params) {
        logger.debug("执行原生SQL查询: {}", sql);
        return executeSqlWithConnection(sql, null, params);
    }

    /**
     * 开始事务
     */
    public EntityTransaction beginTransaction() {
        logger.debug("开始事务");
        // 检查是否已经有活动事务
        if (isTransactionActive()) {
            throw new RuntimeException("Transaction is already active");
        }
        EntityTransaction transaction = entityManagerFactory.createTransaction();
        transaction.begin(); // 确保事务开始
        currentTransactionHolder.set(transaction);
        return transaction;
    }

    /**
     * 获取当前事务
     */
    public EntityTransaction getCurrentTransaction() {
        return currentTransactionHolder.get();
    }

    /**
     * 关闭EntityManager
     */
    public void close() {
        logger.debug("关闭EntityManager");
        EntityTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction != null && currentTransaction.isActive()) {
            logger.warn("关闭EntityManager时发现活动事务，将回滚");
            currentTransaction.rollback();
        }
        currentTransactionHolder.remove();
    }
    // ==================== 私有辅助方法 ====================

    /**
     * 统一的执行策略：优先使用事务连接，否则使用新连接
     *
     * @param operationName      操作名称提供者
     * @param withTransaction    使用事务连接的执行逻辑
     * @param withoutTransaction 不使用事务的执行逻辑
     * @return 执行结果
     */
    private <T> T executeWithTransactionOrConnection(
            Supplier<String> operationName,
            Function<Connection, T> withTransaction,
            Supplier<T> withoutTransaction) {
        EntityTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction != null && currentTransaction.isActive()) {
            logger.debug("在事务中执行: {}", operationName.get());
            Connection connection = currentTransaction.getConnection();
            if (connection != null) {
                return withTransaction.apply(connection);
            }
        }
        logger.debug("非事务执行: {}", operationName.get());
        return withoutTransaction.get();
    }

    private <T> T saveWithConnection(T entity, Connection connection) {
        try {
            if (connection != null) {
                return OrmElf.insertObject(connection, entity);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    return OrmElf.insertObject(conn, entity);
                }
            }
        } catch (Exception e) {
            logger.error("保存实体失败: {}", entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }

    private <T> List<T> saveAllWithConnection(List<T> entities, Connection connection) {
        if (entities == null || entities.isEmpty()) {
            return entities;
        }
        
        try {
            // 对于小批量（少于10条），使用原有方式（OrmElf 处理主键生成等）
            // 对于大批量，尝试使用 JDBC Batch 优化
            if (entities.size() < 10) {
                return saveAllWithOrmElf(entities, connection);
            } else {
                // 尝试使用批量插入优化
                try {
                    return saveAllWithBatch(entities, connection);
                } catch (Exception e) {
                    // 如果批量插入失败，回退到原有方式
                    logger.debug("批量插入优化失败，回退到原有方式: {}", e.getMessage());
                    return saveAllWithOrmElf(entities, connection);
                }
            }
        } catch (Exception e) {
            logger.error("批量保存实体失败", e);
            throw new RuntimeException("Failed to save entities", e);
        }
    }
    
    /**
     * 使用 OrmElf 批量保存（原有方式，处理主键生成等）
     */
    private <T> List<T> saveAllWithOrmElf(List<T> entities, Connection connection) {
        try {
            if (connection != null) {
                for (T entity : entities) {
                    OrmElf.insertObject(connection, entity);
                }
                return entities;
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    for (T entity : entities) {
                        OrmElf.insertObject(conn, entity);
                    }
                    return entities;
                }
            }
        } catch (Exception e) {
            logger.error("使用 OrmElf 批量保存失败", e);
            throw new RuntimeException("Failed to save entities with OrmElf", e);
        }
    }
    
    /**
     * 使用 JDBC Batch 批量保存（性能优化版本）
     * 优化：支持自增主键（IDENTITY），并回填ID
     */
    private <T> List<T> saveAllWithBatch(List<T> entities, Connection connection) throws Exception {
        Connection conn = connection;
        boolean shouldClose = false;
        boolean wasAutoCommit = true;
        
        try {
            // 获取连接
            if (conn == null) {
                EntityTransaction tx = getCurrentTransaction();
                if (tx != null && tx.isActive()) {
                    conn = tx.getConnection();
                    logger.debug("批量插入: 重用事务连接");
                } else {
                    conn = dataSource.getConnection();
                    shouldClose = true;
                    wasAutoCommit = conn.getAutoCommit();
                    if (wasAutoCommit) {
                        conn.setAutoCommit(false);
                    }
                    logger.debug("批量插入: 创建新连接");
                }
            }
            
            // 准备批量插入
            Class<?> entityClass = entities.get(0).getClass();
            String tableName = EntityUtils.getTableName(entityClass);
            // 获取所有列，排除 IDENTITY 主键
            String[] columns = EntityUtils.getColumnNames(entityClass, true);
            
            if (columns.length == 0) {
                throw new IllegalStateException("实体类 " + entityClass.getName() + " 没有可插入的列");
            }
            
            // 查找 Identity 字段（用于回填主键）
            java.lang.reflect.Field identityField = EntityUtils.getIdentityField(entityClass);
            
            // 构建 INSERT SQL
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(tableName).append(" (");
            sql.append(String.join(", ", columns));
            sql.append(") VALUES (");
            for (int i = 0; i < columns.length; i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");
            
            // 使用 BeanMeta 获取字段访问器
            BeanMeta beanMeta =
                new BeanMeta(entityClass);
            
            // 获取字段映射（列名 -> Field）
            java.util.Map<String, java.lang.reflect.Field> columnToFieldMap = new java.util.HashMap<>();
            for (java.lang.reflect.Field field : entityClass.getDeclaredFields()) {
                String columnName = EntityUtils.getColumnName(field);
                columnToFieldMap.put(columnName, field);
            }
            
            // 准备 Statement (如果有Identity字段，请求返回生成的主键)
            int autoGeneratedKeys = identityField != null ? java.sql.Statement.RETURN_GENERATED_KEYS : java.sql.Statement.NO_GENERATED_KEYS;
            
            try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql.toString(), autoGeneratedKeys)) {
                for (T entity : entities) {
                    // 设置参数
                    for (int i = 0; i < columns.length; i++) {
                        String columnName = columns[i];
                        java.lang.reflect.Field field = columnToFieldMap.get(columnName);
                        if (field != null) {
                            PropertyAccessor accessor =
                                beanMeta.getPropertyAccessor(field);
                            Object value = accessor.get(entity);
                            pstmt.setObject(i + 1, value);
                        } else {
                            pstmt.setObject(i + 1, null);
                        }
                    }
                    pstmt.addBatch();
                }
                
                // 执行批量插入
                int[] results = pstmt.executeBatch();
                logger.debug("批量插入执行完成: 表={}, 数量={}, 结果={}", tableName, entities.size(), results.length);
                
                // 回填主键
                if (identityField != null) {
                    DefaultRowMapper<?> rowMapper = new DefaultRowMapper<>();
                    try (java.sql.ResultSet rs = pstmt.getGeneratedKeys()) {
                        int index = 0;
                        while (rs.next() && index < entities.size()) {
                            // 使用 RowMapper 转换主键类型
                            Object idValue = rowMapper.mapRow(rs, (Class) identityField.getType());
                            if (idValue != null) {
                                T entity = entities.get(index);
                                PropertyAccessor accessor =
                                    beanMeta.getPropertyAccessor(identityField);
                                accessor.set(entity, idValue);
                            }
                            index++;
                        }
                    } catch (Exception ex) {
                        logger.warn("回填主键失败: {}", ex.getMessage());
                    }
                }
                
                // 提交（仅当不在事务中时）
                if (shouldClose && wasAutoCommit) {
                    conn.commit();
                }
            }
            
            return entities;
        } catch (Exception e) {
            // 回滚（仅当不在事务中时）
            if (shouldClose && conn != null) {
                try {
                    conn.rollback();
                } catch (java.sql.SQLException rollbackEx) {
                    logger.warn("批量插入回滚失败", rollbackEx);
                }
            }
            throw e;
        } finally {
            // 恢复自动提交并关闭连接（仅当不在事务中时）
            if (shouldClose && conn != null) {
                try {
                    if (wasAutoCommit) {
                        conn.setAutoCommit(true);
                    }
                    conn.close();
                } catch (java.sql.SQLException e) {
                    logger.warn("关闭连接失败", e);
                }
            }
        }
    }

    private <T> T updateWithConnection(T entity, Connection connection) {
        try {
            if (connection != null) {
                return OrmElf.updateObject(connection, entity);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    return OrmElf.updateObject(conn, entity);
                }
            }
        } catch (Exception e) {
            logger.error("更新实体失败: {}", entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    private <T> void deleteWithConnection(T entity, Connection connection) {
        try {
            if (connection != null) {
                OrmElf.deleteObject(connection, entity);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    OrmElf.deleteObject(conn, entity);
                }
            }
        } catch (Exception e) {
            logger.error("删除实体失败: {}", entity.getClass().getSimpleName(), e);
            throw new RuntimeException("Failed to delete entity", e);
        }
    }

    private <T> void deleteByIdWithConnection(Class<T> entityClass, Object id, Connection connection) {
        try {
            if (connection != null) {
                OrmElf.deleteObjectById(connection, entityClass, id);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    OrmElf.deleteObjectById(conn, entityClass, id);
                }
            }
        } catch (Exception e) {
            logger.error("根据ID删除实体失败: {} - {}", entityClass.getSimpleName(), id, e);
            throw new RuntimeException("Failed to delete entity by ID", e);
        }
    }

    private <T> T findByIdWithConnection(Class<T> entityClass, Object id, Connection connection) {
        try {
            Connection conn = connection;
            boolean shouldClose = false;
            
            if (conn == null) {
                EntityTransaction tx = getCurrentTransaction();
                if (tx != null && tx.isActive()) {
                    // 重用事务连接
                    conn = tx.getConnection();
                    logger.debug("findById: 重用事务连接");
                } else {
                    // 创建新连接
                    conn = dataSource.getConnection();
                    shouldClose = true;
                    logger.debug("findById: 创建新连接");
                }
            }
            
            try {
                // 使用SansOrm的正确方法
                return OrmElf.objectById(conn, entityClass, id);
            } finally {
                if (shouldClose && conn != null) {
                    conn.close();
                    logger.debug("findById: 关闭新连接");
                }
            }
        } catch (Exception e) {
            logger.error("根据ID查找实体失败: {} - {}", entityClass.getSimpleName(), id, e);
            throw new RuntimeException("Failed to find entity by ID", e);
        }
    }

    private <T> List<T> findAllWithConnection(Class<T> entityClass, Connection connection) {
        try {
            Connection conn = connection;
            boolean shouldClose = false;
            
            if (conn == null) {
                EntityTransaction tx = getCurrentTransaction();
                if (tx != null && tx.isActive()) {
                    // 重用事务连接
                    conn = tx.getConnection();
                    logger.debug("findAll: 重用事务连接");
                } else {
                    // 创建新连接
                    conn = dataSource.getConnection();
                    shouldClose = true;
                    logger.debug("findAll: 创建新连接");
                }
            }
            
            try {
                return OrmElf.listFromClause(conn, entityClass, "");
            } finally {
                if (shouldClose && conn != null) {
                    conn.close();
                    logger.debug("findAll: 关闭新连接");
                }
            }
        } catch (Exception e) {
            logger.error("查找所有实体失败: {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("Failed to find all entities", e);
        }
    }

    private List<Object> executeSqlWithConnection(String sql, Connection connection, Object... params) {
        try {
            if (connection != null) {
                return OrmElf.listFromClause(connection, Object.class, sql, params);
            } else {
                try (Connection conn = dataSource.getConnection()) {
                    return OrmElf.listFromClause(conn, Object.class, sql, params);
                }
            }
        } catch (Exception e) {
            logger.error("执行SQL失败: {}", sql, e);
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }

    /**
     * 回滚当前事务
     */
    public void rollbackTransaction() {
        executeTransactionOperation("回滚事务", EntityTransaction::rollback);
    }

    /**
     * 提交当前事务
     */
    public void commitTransaction() {
        executeTransactionOperation("提交事务", EntityTransaction::commit);
    }

    /**
     * 检查事务是否处于活动状态
     */
    public boolean isTransactionActive() {
        EntityTransaction currentTransaction = getCurrentTransaction();
        return currentTransaction != null && currentTransaction.isActive();
    }

    /**
     * 关闭当前事务
     */
    public void closeTransaction() {
        executeTransactionOperation("关闭事务", transaction -> {
            if (transaction.isActive()) {
                logger.warn("关闭活动事务，自动回滚");
                transaction.rollback();
            }
        });
    }

    /**
     * 清除当前事务
     * 线程安全的方法，使用ThreadLocal管理事务状态
     */
    private void clearCurrentTransaction() {
        currentTransactionHolder.remove();
    }

    /**
     * 统一的事务操作执行器
     *
     * @param operationName 操作名称
     * @param operation     事务操作
     */
    private void executeTransactionOperation(String operationName, java.util.function.Consumer<EntityTransaction> operation) {
        EntityTransaction currentTransaction = getCurrentTransaction();
        if (currentTransaction == null || !currentTransaction.isActive()) {
            logger.warn("尝试{}不存在或已结束的事务", operationName);
            return;
        }
        try {
            logger.info(operationName);
            operation.accept(currentTransaction);
        } finally {
            clearCurrentTransaction();
        }
    }
}
