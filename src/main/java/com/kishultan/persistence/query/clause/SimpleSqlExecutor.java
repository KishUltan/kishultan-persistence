package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.EntityManager;
import com.kishultan.persistence.EntityTransaction;
import com.kishultan.persistence.query.DefaultRowMapper;
import com.kishultan.persistence.query.RowMapper;
import com.kishultan.persistence.query.SqlExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单SQL执行器实现
 * 支持在事务中重用连接，非事务时创建新连接
 */
public class SimpleSqlExecutor implements SqlExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SimpleSqlExecutor.class);
    private final DefaultRowMapper rowMapper = new DefaultRowMapper();
    private final DataSource dataSource;
    private final EntityManager entityManager;

    public SimpleSqlExecutor(DataSource dataSource) {
        this(dataSource, null);
    }

    /**
     * 构造函数（支持传入 EntityManager 以获取事务连接）
     */
    public SimpleSqlExecutor(DataSource dataSource, EntityManager entityManager) {
        this.dataSource = dataSource;
        this.entityManager = entityManager;
    }
    
    /**
     * 获取数据库连接
     * 优先从事务获取连接，如果没有事务则从数据源获取新连接
     * 
     * @return 数据库连接
     * @throws SQLException 如果获取连接失败
     */
    private Connection getConnection() throws SQLException {
        // 优先从 EntityManager 获取事务连接
        if (entityManager != null) {
            EntityTransaction transaction = entityManager.getCurrentTransaction();
            if (transaction != null && transaction.isActive()) {
                Connection txConnection = transaction.getConnection();
                if (txConnection != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("使用事务连接执行查询");
                    }
                    return txConnection;
                } else {
                    logger.warn("事务已激活但连接为 null，将从数据源获取新连接");
                }
            }
        }
        
        // 如果没有事务，从数据源获取新连接
        if (dataSource == null) {
            throw new SQLException("数据源为 null，无法获取连接");
        }
        
        try {
            Connection connection = dataSource.getConnection();
            if (connection == null) {
                throw new SQLException("从数据源获取的连接为 null");
            }
            
            if (logger.isTraceEnabled()) {
                logger.trace("创建新连接执行查询（非事务）");
            }
            
            return connection;
        } catch (SQLException e) {
            logger.error("从数据源获取连接失败", e);
            throw e;
        }
    }

    @Override
    public <T> List<T> executeQuery(String sql, List<Object> parameters, Class<T> resultType) {
        return executeQuery(sql, parameters, resultType, this.rowMapper);
    }

    @Override
    public <T> List<T> executeQuery(String sql, List<Object> parameters, Class<T> resultType, RowMapper<T> mapper) {
        Connection connection = null;
        boolean shouldClose = false;
        try {
            connection = getConnection();
            // 如果是新连接（非事务），需要关闭
            if (entityManager == null || 
                entityManager.getCurrentTransaction() == null || 
                !entityManager.getCurrentTransaction().isActive()) {
                shouldClose = true;
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("执行查询，SQL: {}, 参数数量: {}, 参数: {}", sql, parameters != null ? parameters.size() : 0, parameters);
                }
                setParameters(stmt, parameters);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<T> results = new ArrayList<>();
                    while (rs.next()) {
                        T result = mapper.mapRow(rs, resultType);
                        results.add(result);
                    }
                    //按主键合并对象，解决连接查询主表数据重复的问题
                    return this.rowMapper.mergeList(results, resultType);
                }
            }
        } catch (Exception e) {
            logger.error("执行查询失败: {}, 参数数量: {}, 参数: {}", sql, parameters != null ? parameters.size() : 0, parameters, e);
            throw new RuntimeException("执行查询失败: " + sql, e);
        } finally {
            if (shouldClose && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("关闭连接失败", e);
                }
            }
        }
    }

    @Override
    public <T> T executeAs(String sql, List<Object> parameters, Class<T> resultType) {
        Connection connection = null;
        boolean shouldClose = false;
        try {
            connection = getConnection();
            // 如果是新连接（非事务），需要关闭
            if (entityManager == null || 
                entityManager.getCurrentTransaction() == null || 
                !entityManager.getCurrentTransaction().isActive()) {
                shouldClose = true;
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("执行count查询，SQL: {}, 参数数量: {}, 参数: {}", sql, parameters != null ? parameters.size() : 0, parameters);
                }
                setParameters(stmt, parameters);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return (T) this.rowMapper.mapRow(rs, resultType);
                    }
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error("执行count查询失败: {}, 参数数量: {}, 参数: {}", sql, parameters != null ? parameters.size() : 0, parameters, e);
            throw new RuntimeException("执行count查询失败: " + sql, e);
        } finally {
            if (shouldClose && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("关闭连接失败", e);
                }
            }
        }
    }

    @Override
    public long executeAsLong(String sql, List<Object> parameters) {
        return executeAs(sql, parameters, Long.class);
    }

    @Override
    public int executeUpdate(String sql, List<Object> parameters) {
        Connection connection = null;
        boolean shouldClose = false;
        try {
            connection = getConnection();
            // 如果是新连接（非事务），需要关闭
            if (entityManager == null || 
                entityManager.getCurrentTransaction() == null || 
                !entityManager.getCurrentTransaction().isActive()) {
                shouldClose = true;
            }
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                setParameters(stmt, parameters);
                return stmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("执行更新失败: " + sql, e);
        } finally {
            if (shouldClose && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("关闭连接失败", e);
                }
            }
        }
    }

    @Override
    public int[] executeBatchUpdate(List<String> sqlList, List<List<Object>> parametersList) {
        Connection connection = null;
        boolean shouldClose = false;
        boolean wasAutoCommit = true;
        try {
            connection = getConnection();
            // 如果是新连接（非事务），需要管理事务和关闭
            boolean inTransaction = entityManager != null && 
                entityManager.getCurrentTransaction() != null && 
                entityManager.getCurrentTransaction().isActive();
            
            if (!inTransaction) {
                shouldClose = true;
                wasAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
            }
            
            int[] results = new int[sqlList.size()];
            try {
                for (int i = 0; i < sqlList.size(); i++) {
                    try (PreparedStatement stmt = connection.prepareStatement(sqlList.get(i))) {
                        setParameters(stmt, parametersList.get(i));
                        results[i] = stmt.executeUpdate();
                    }
                }
                
                if (!inTransaction) {
                    connection.commit();
                }
                return results;
            } catch (Exception e) {
                // 失败时回滚事务（仅当不在外部事务中时）
                if (!inTransaction) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        logger.error("批量更新失败且回滚失败", rollbackEx);
                        throw new RuntimeException("批量更新失败且回滚失败", e);
                    }
                }
                throw new RuntimeException("批量更新失败", e);
            } finally {
                // 重置连接状态（仅当不在外部事务中时）
                if (!inTransaction && connection != null) {
                    try {
                        connection.setAutoCommit(wasAutoCommit);
                    } catch (SQLException e) {
                        logger.warn("重置连接自动提交状态失败", e);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("批量更新失败", e);
        } finally {
            // 关闭连接（仅当不在外部事务中时）
            if (shouldClose && connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("关闭连接失败", e);
                }
            }
        }
    }

    private void setParameters(PreparedStatement stmt, List<Object> parameters) throws SQLException {
        if (parameters != null) {
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
        }
    }
}
