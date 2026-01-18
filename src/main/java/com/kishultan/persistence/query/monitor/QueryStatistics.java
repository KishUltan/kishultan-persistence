package com.kishultan.persistence.query.monitor;

import java.time.LocalDateTime;

/**
 * 查询统计信息类
 * 记录特定查询的统计信息
 */
public class QueryStatistics {
    private final String sqlHash;
    private final String sql;
    private long executionCount = 0;
    private long totalExecutionTime = 0;
    private long maxExecutionTime = 0;
    private long minExecutionTime = Long.MAX_VALUE;
    private long successCount = 0;
    private long failureCount = 0;
    private long totalResultCount = 0;
    private LocalDateTime firstExecution;
    private LocalDateTime lastExecution;

    /**
     * 构造函数
     *
     * @param sqlHash SQL哈希值
     * @param sql     SQL语句
     */
    public QueryStatistics(String sqlHash, String sql) {
        this.sqlHash = sqlHash;
        this.sql = sql;
        this.firstExecution = LocalDateTime.now();
        this.lastExecution = LocalDateTime.now();
    }

    /**
     * 记录查询执行
     *
     * @param executionTime 执行时间（毫秒）
     * @param success       是否成功
     * @param resultCount   结果数量
     */
    public void recordExecution(long executionTime, boolean success, int resultCount) {
        this.executionCount++;
        this.totalExecutionTime += executionTime;
        this.maxExecutionTime = Math.max(this.maxExecutionTime, executionTime);
        this.minExecutionTime = Math.min(this.minExecutionTime, executionTime);
        this.totalResultCount += resultCount;
        this.lastExecution = LocalDateTime.now();
        if (success) {
            this.successCount++;
        } else {
            this.failureCount++;
        }
    }

    // Getter方法
    public String getSqlHash() {
        return sqlHash;
    }

    public String getSql() {
        return sql;
    }

    public long getExecutionCount() {
        return executionCount;
    }

    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }

    public long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public long getMinExecutionTime() {
        return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public long getTotalResultCount() {
        return totalResultCount;
    }

    public LocalDateTime getFirstExecution() {
        return firstExecution;
    }

    public LocalDateTime getLastExecution() {
        return lastExecution;
    }

    /**
     * 获取平均执行时间
     *
     * @return 平均执行时间（毫秒）
     */
    public double getAverageExecutionTime() {
        return executionCount > 0 ? (double) totalExecutionTime / executionCount : 0.0;
    }

    /**
     * 获取成功率
     *
     * @return 成功率（0-1）
     */
    public double getSuccessRate() {
        return executionCount > 0 ? (double) successCount / executionCount : 0.0;
    }

    /**
     * 获取平均结果集大小
     *
     * @return 平均结果集大小
     */
    public double getAverageResultSetSize() {
        return executionCount > 0 ? (double) totalResultCount / executionCount : 0.0;
    }
}
