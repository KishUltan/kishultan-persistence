package com.kishultan.persistence.delegate;

import com.kishultan.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SansOrm事务实现
 * <p>
 * 使用SansOrm进行轻量级的事务管理
 */
public class SansOrmEntityTransaction implements EntityTransaction {
    private static final Logger logger = LoggerFactory.getLogger(SansOrmEntityTransaction.class);
    private final DataSource dataSource;
    private Connection connection;
    private boolean isActive = false;

    public SansOrmEntityTransaction(DataSource dataSource) {
        this.dataSource = dataSource;
        logger.debug("创建SansOrmEntityTransaction实例");
    }

    /**
     * 开始事务
     */
    public void begin() {
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            isActive = true;
            logger.info("事务开始");
        } catch (SQLException e) {
            logger.error("开始事务失败", e);
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }

    @Override
    public void commit() {
        if (!isActive) {
            logger.warn("尝试提交非活动事务");
            return;
        }
        try {
            connection.commit();
            // 重置连接状态
            try {
                connection.setAutoCommit(true);
                logger.debug("连接状态已重置为auto-commit=true");
            } catch (SQLException e) {
                logger.warn("提交后无法重置auto-commit", e);
            }
            isActive = false;
            logger.info("事务提交成功");
        } catch (SQLException e) {
            logger.error("事务提交失败", e);
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }

    @Override
    public void rollback() {
        if (!isActive) {
            logger.warn("尝试回滚非活动事务");
            return;
        }
        try {
            connection.rollback();
            // 重置连接状态
            try {
                connection.setAutoCommit(true);
                logger.debug("连接状态已重置为auto-commit=true");
            } catch (SQLException e) {
                logger.warn("回滚后无法重置auto-commit", e);
            }
            isActive = false;
            logger.info("事务回滚成功");
        } catch (SQLException e) {
            logger.error("事务回滚失败", e);
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }

    @Override
    public void close() {
        if (isActive) {
            logger.warn("关闭活动事务，自动回滚");
            rollback();
        }
        if (connection != null) {
            try {
                connection.close();
                logger.debug("事务连接已关闭");
            } catch (SQLException e) {
                logger.error("关闭事务连接失败", e);
            }
        }
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    /**
     * 获取事务连接
     */
    @Override
    public Connection getConnection() {
        return connection;
    }
}
