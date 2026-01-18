package com.kishultan.persistence.config;

/**
 * 持久化配置类
 * <p>
 * 简化的持久化层配置，只保留核心配置
 */
public class PersistenceConfig {
    /**
     * ORM类型 - 现在只支持sansorm
     */
    private String ormType = "sansorm";
    /**
     * 是否启用慢查询日志
     */
    private boolean slowQueryLogging = true;
    /**
     * 慢查询阈值（毫秒）
     */
    private long slowQueryThreshold = 1000;

    /**
     * 默认构造函数
     */
    public PersistenceConfig() {
        // 默认配置
    }

    /**
     * 获取默认配置
     *
     * @return 默认的PersistenceConfig实例
     */
    public static PersistenceConfig getDefaultConfig() {
        return new PersistenceConfig();
    }

    /**
     * 创建开发环境配置
     *
     * @return 开发环境配置
     */
    public static PersistenceConfig getDevelopmentConfig() {
        PersistenceConfig config = new PersistenceConfig();
        config.setSlowQueryLogging(true);
        config.setSlowQueryThreshold(500);
        return config;
    }

    /**
     * 创建生产环境配置
     *
     * @return 生产环境配置
     */
    public static PersistenceConfig getProductionConfig() {
        PersistenceConfig config = new PersistenceConfig();
        config.setSlowQueryLogging(true);
        config.setSlowQueryThreshold(2000);
        return config;
    }
    // Getters and Setters

    /**
     * 获取ORM类型
     *
     * @return ORM类型
     */
    public String getOrmType() {
        return ormType;
    }

    /**
     * 设置ORM类型
     *
     * @param ormType ORM类型
     */
    public void setOrmType(String ormType) {
        this.ormType = ormType;
    }

    /**
     * 是否启用慢查询日志
     *
     * @return 是否启用慢查询日志
     */
    public boolean isSlowQueryLogging() {
        return slowQueryLogging;
    }

    /**
     * 设置是否启用慢查询日志
     *
     * @param slowQueryLogging 是否启用慢查询日志
     */
    public void setSlowQueryLogging(boolean slowQueryLogging) {
        this.slowQueryLogging = slowQueryLogging;
    }

    /**
     * 获取慢查询阈值
     *
     * @return 慢查询阈值（毫秒）
     */
    public long getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    /**
     * 设置慢查询阈值
     *
     * @param slowQueryThreshold 慢查询阈值（毫秒）
     */
    public void setSlowQueryThreshold(long slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
    }
} 