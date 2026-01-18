package com.kishultan.persistence;

import java.sql.Connection;

/**
 * 实体事务接口
 * <p>
 * 定义事务操作的抽象接口，避免直接依赖第三方库
 * 采用JPA风格命名
 */
public interface EntityTransaction {
    /**
     * 开始事务
     */
    void begin();

    /**
     * 提交事务
     */
    void commit();

    /**
     * 回滚事务
     */
    void rollback();

    /**
     * 关闭事务
     */
    void close();

    /**
     * 检查事务是否处于活动状态
     *
     * @return 如果事务处于活动状态返回true，否则返回false
     */
    boolean isActive();

    /**
     * 获取事务连接
     *
     * @return 事务连接，如果事务未开始则返回null
     */
    Connection getConnection();
} 