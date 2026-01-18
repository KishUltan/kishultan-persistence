package com.kishultan.persistence.query.cache;

/**
 * 缓存配置类
 * 提供缓存的各种配置选项
 */
public class CacheConfig {
    // 默认配置常量
    public static final boolean DEFAULT_ENABLED = true;
    public static final int DEFAULT_MAX_SIZE = 1000;
    public static final long DEFAULT_DEFAULT_TTL = 300000; // 5分钟
    public static final int DEFAULT_CLEANUP_INTERVAL = 60000; // 1分钟
    public static final boolean DEFAULT_ENABLE_ASYNC = true;
    public static final int DEFAULT_THREAD_POOL_SIZE = 4;
    // 配置属性
    private boolean enabled = DEFAULT_ENABLED;
    private int maxSize = DEFAULT_MAX_SIZE;
    private long defaultTtl = DEFAULT_DEFAULT_TTL;
    private int cleanupInterval = DEFAULT_CLEANUP_INTERVAL;
    private boolean enableAsync = DEFAULT_ENABLE_ASYNC;
    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    private CacheStrategy.StrategyType strategyType = CacheStrategy.StrategyType.LRU;
    private boolean enableStatistics = true;
    private boolean enableWarmUp = false;
    private long maxMemoryUsage = 100 * 1024 * 1024; // 100MB

    /**
     * 默认构造函数
     */
    public CacheConfig() {
    }

    /**
     * 构造函数
     *
     * @param enabled    是否启用缓存
     * @param maxSize    最大缓存大小
     * @param defaultTtl 默认TTL（毫秒）
     */
    public CacheConfig(boolean enabled, int maxSize, long defaultTtl) {
        this.enabled = enabled;
        this.maxSize = maxSize;
        this.defaultTtl = defaultTtl;
    }

    /**
     * 创建默认配置
     *
     * @return 默认配置实例
     */
    public static CacheConfig createDefault() {
        return new CacheConfig();
    }

    /**
     * 创建生产环境配置
     *
     * @return 生产环境配置实例
     */
    public static CacheConfig createProduction() {
        CacheConfig config = new CacheConfig();
        config.setMaxSize(10000);
        config.setDefaultTtl(600000); // 10分钟
        config.setCleanupInterval(300000); // 5分钟
        config.setStrategyType(CacheStrategy.StrategyType.LRU);
        config.setMaxMemoryUsage(500 * 1024 * 1024); // 500MB
        return config;
    }

    /**
     * 创建开发环境配置
     *
     * @return 开发环境配置实例
     */
    public static CacheConfig createDevelopment() {
        CacheConfig config = new CacheConfig();
        config.setMaxSize(100);
        config.setDefaultTtl(60000); // 1分钟
        config.setCleanupInterval(30000); // 30秒
        config.setStrategyType(CacheStrategy.StrategyType.TTL);
        config.setMaxMemoryUsage(50 * 1024 * 1024); // 50MB
        return config;
    }

    /**
     * 创建高性能配置
     *
     * @return 高性能配置实例
     */
    public static CacheConfig createHighPerformance() {
        CacheConfig config = new CacheConfig();
        config.setMaxSize(50000);
        config.setDefaultTtl(1800000); // 30分钟
        config.setCleanupInterval(120000); // 2分钟
        config.setStrategyType(CacheStrategy.StrategyType.LFU);
        config.setMaxMemoryUsage(1024 * 1024 * 1024); // 1GB
        config.setThreadPoolSize(8);
        return config;
    }

    // Getter和Setter方法
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
    }

    public long getDefaultTtl() {
        return defaultTtl;
    }

    public void setDefaultTtl(long defaultTtl) {
        this.defaultTtl = Math.max(0, defaultTtl);
    }

    public int getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(int cleanupInterval) {
        this.cleanupInterval = Math.max(1000, cleanupInterval);
    }

    public boolean isEnableAsync() {
        return enableAsync;
    }

    public void setEnableAsync(boolean enableAsync) {
        this.enableAsync = enableAsync;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = Math.max(1, threadPoolSize);
    }

    public CacheStrategy.StrategyType getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(CacheStrategy.StrategyType strategyType) {
        this.strategyType = strategyType;
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    public boolean isEnableWarmUp() {
        return enableWarmUp;
    }

    public void setEnableWarmUp(boolean enableWarmUp) {
        this.enableWarmUp = enableWarmUp;
    }

    public long getMaxMemoryUsage() {
        return maxMemoryUsage;
    }

    public void setMaxMemoryUsage(long maxMemoryUsage) {
        this.maxMemoryUsage = Math.max(1024 * 1024, maxMemoryUsage); // 最小1MB
    }
}
