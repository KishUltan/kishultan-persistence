package com.kishultan.persistence.query.monitor;

/**
 * 性能监控配置类
 * 提供性能监控的各种配置选项
 */
public class PerformanceConfig {
    // 默认配置常量
    public static final boolean DEFAULT_ENABLED = true;
    public static final long DEFAULT_SLOW_QUERY_THRESHOLD = 1000; // 1秒
    public static final int DEFAULT_MAX_SLOW_QUERIES = 100;
    public static final int DEFAULT_SAMPLING_RATE = 100; // 100%采样
    public static final int DEFAULT_TREND_WINDOW = 60; // 60分钟
    public static final int DEFAULT_METRICS_RETENTION_HOURS = 24; // 24小时
    // 配置属性
    private boolean enabled = DEFAULT_ENABLED;
    private long slowQueryThreshold = DEFAULT_SLOW_QUERY_THRESHOLD;
    private int maxSlowQueries = DEFAULT_MAX_SLOW_QUERIES;
    private int samplingRate = DEFAULT_SAMPLING_RATE;
    private int trendWindow = DEFAULT_TREND_WINDOW;
    private int metricsRetentionHours = DEFAULT_METRICS_RETENTION_HOURS;
    private boolean enableDetailedLogging = false;
    private boolean enableSlowQueryLogging = true;

    /**
     * 默认构造函数
     */
    public PerformanceConfig() {
    }

    /**
     * 构造函数
     *
     * @param enabled            是否启用监控
     * @param slowQueryThreshold 慢查询阈值（毫秒）
     */
    public PerformanceConfig(boolean enabled, long slowQueryThreshold) {
        this.enabled = enabled;
        this.slowQueryThreshold = slowQueryThreshold;
    }

    /**
     * 创建默认配置
     *
     * @return 默认配置实例
     */
    public static PerformanceConfig createDefault() {
        return new PerformanceConfig();
    }

    /**
     * 创建生产环境配置
     *
     * @return 生产环境配置实例
     */
    public static PerformanceConfig createProduction() {
        PerformanceConfig config = new PerformanceConfig();
        config.setSamplingRate(10); // 10%采样
        config.setSlowQueryThreshold(500); // 500ms慢查询阈值
        config.setEnableDetailedLogging(false);
        config.setEnableSlowQueryLogging(true);
        return config;
    }

    /**
     * 创建开发环境配置
     *
     * @return 开发环境配置实例
     */
    public static PerformanceConfig createDevelopment() {
        PerformanceConfig config = new PerformanceConfig();
        config.setSamplingRate(100); // 100%采样
        config.setSlowQueryThreshold(100); // 100ms慢查询阈值
        config.setEnableDetailedLogging(true);
        config.setEnableSlowQueryLogging(true);
        return config;
    }

    // Getter和Setter方法
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    public void setSlowQueryThreshold(long slowQueryThreshold) {
        this.slowQueryThreshold = slowQueryThreshold;
    }

    public int getMaxSlowQueries() {
        return maxSlowQueries;
    }

    public void setMaxSlowQueries(int maxSlowQueries) {
        this.maxSlowQueries = maxSlowQueries;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = Math.max(0, Math.min(100, samplingRate));
    }

    public int getTrendWindow() {
        return trendWindow;
    }

    public void setTrendWindow(int trendWindow) {
        this.trendWindow = trendWindow;
    }

    public int getMetricsRetentionHours() {
        return metricsRetentionHours;
    }

    public void setMetricsRetentionHours(int metricsRetentionHours) {
        this.metricsRetentionHours = metricsRetentionHours;
    }

    public boolean isEnableDetailedLogging() {
        return enableDetailedLogging;
    }

    public void setEnableDetailedLogging(boolean enableDetailedLogging) {
        this.enableDetailedLogging = enableDetailedLogging;
    }

    public boolean isEnableSlowQueryLogging() {
        return enableSlowQueryLogging;
    }

    public void setEnableSlowQueryLogging(boolean enableSlowQueryLogging) {
        this.enableSlowQueryLogging = enableSlowQueryLogging;
    }
}
