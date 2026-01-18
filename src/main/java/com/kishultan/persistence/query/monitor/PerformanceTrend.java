package com.kishultan.persistence.query.monitor;

import java.time.LocalDateTime;

/**
 * 性能趋势数据类
 * 记录特定时间窗口的性能数据
 */
public class PerformanceTrend {
    private final LocalDateTime timestamp;
    private final long queryCount;
    private final double averageExecutionTime;
    private final long maxExecutionTime;
    private final double successRate;
    private final long totalResultCount;

    /**
     * 构造函数
     *
     * @param timestamp            时间戳
     * @param queryCount           查询次数
     * @param averageExecutionTime 平均执行时间（毫秒）
     * @param maxExecutionTime     最大执行时间（毫秒）
     * @param successRate          成功率（0-1）
     * @param totalResultCount     总结果数量
     */
    public PerformanceTrend(LocalDateTime timestamp, long queryCount,
                            double averageExecutionTime, long maxExecutionTime,
                            double successRate, long totalResultCount) {
        this.timestamp = timestamp;
        this.queryCount = queryCount;
        this.averageExecutionTime = averageExecutionTime;
        this.maxExecutionTime = maxExecutionTime;
        this.successRate = successRate;
        this.totalResultCount = totalResultCount;
    }

    // Getter方法
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long getQueryCount() {
        return queryCount;
    }

    public double getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public long getTotalResultCount() {
        return totalResultCount;
    }

    /**
     * 获取QPS（每秒查询数）
     *
     * @return QPS
     */
    public double getQueriesPerSecond() {
        // 假设时间窗口为1分钟
        return queryCount / 60.0;
    }

    @Override
    public String toString() {
        return String.format("PerformanceTrend{timestamp=%s, queryCount=%d, avgTime=%.2fms, maxTime=%dms, successRate=%.2f%%, qps=%.2f}",
                timestamp, queryCount, averageExecutionTime, maxExecutionTime, successRate * 100, getQueriesPerSecond());
    }
}
