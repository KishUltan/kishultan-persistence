package com.kishultan.persistence.query.monitor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 查询性能指标实现类
 * 提供查询执行的各种性能指标
 */
public class QueryMetricsImpl implements QueryMetrics {
    private final AtomicLong totalQueryCount = new AtomicLong(0);
    private final AtomicLong successQueryCount = new AtomicLong(0);
    private final AtomicLong failedQueryCount = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong totalResultSetSize = new AtomicLong(0);
    private final AtomicReference<Long> maxExecutionTime = new AtomicReference<>(0L);
    private final AtomicReference<Long> minExecutionTime = new AtomicReference<>(Long.MAX_VALUE);
    private final AtomicReference<Long> lastExecutionTime = new AtomicReference<>(0L);
    private final AtomicReference<LocalDateTime> startTime = new AtomicReference<>(LocalDateTime.now());

    /**
     * 记录查询执行
     *
     * @param executionTime 执行时间（毫秒）
     * @param success       是否成功
     * @param resultCount   结果数量
     */
    public void recordExecution(long executionTime, boolean success, int resultCount) {
        totalQueryCount.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        totalResultSetSize.addAndGet(resultCount);
        if (success) {
            successQueryCount.incrementAndGet();
        } else {
            failedQueryCount.incrementAndGet();
        }
        // 更新最大执行时间
        maxExecutionTime.updateAndGet(current -> Math.max(current, executionTime));
        // 更新最小执行时间
        minExecutionTime.updateAndGet(current -> Math.min(current, executionTime));
        // 更新最后执行时间
        lastExecutionTime.set(executionTime);
    }

    @Override
    public long getTotalQueryCount() {
        return totalQueryCount.get();
    }

    @Override
    public long getSuccessQueryCount() {
        return successQueryCount.get();
    }

    @Override
    public long getFailedQueryCount() {
        return failedQueryCount.get();
    }

    @Override
    public double getAverageExecutionTime() {
        long count = totalQueryCount.get();
        return count > 0 ? (double) totalExecutionTime.get() / count : 0.0;
    }

    @Override
    public long getMaxExecutionTime() {
        return maxExecutionTime.get();
    }

    @Override
    public long getMinExecutionTime() {
        long min = minExecutionTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }

    @Override
    public long getLastExecutionTime() {
        return lastExecutionTime.get();
    }

    @Override
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }

    @Override
    public double getSuccessRate() {
        long total = totalQueryCount.get();
        return total > 0 ? (double) successQueryCount.get() / total : 0.0;
    }

    @Override
    public double getFailureRate() {
        long total = totalQueryCount.get();
        return total > 0 ? (double) failedQueryCount.get() / total : 0.0;
    }

    @Override
    public double getQueriesPerSecond() {
        long runningTimeSeconds = java.time.Duration.between(startTime.get(), LocalDateTime.now()).getSeconds();
        return runningTimeSeconds > 0 ? (double) totalQueryCount.get() / runningTimeSeconds : 0.0;
    }

    @Override
    public double getAverageResultSetSize() {
        long count = totalQueryCount.get();
        return count > 0 ? (double) totalResultSetSize.get() / count : 0.0;
    }

    @Override
    public long getTotalResultSetSize() {
        return totalResultSetSize.get();
    }

    @Override
    public long getSlowQueryCount(long threshold) {
        // 这里简化实现，实际应该记录每个查询的执行时间
        // 在真实实现中，需要维护一个执行时间列表
        return 0;
    }

    @Override
    public double getSlowQueryRate(long threshold) {
        long total = totalQueryCount.get();
        return total > 0 ? (double) getSlowQueryCount(threshold) / total : 0.0;
    }

    @Override
    public List<PerformanceTrend> getPerformanceTrend(int timeWindow) {
        // 这里简化实现，实际应该按时间窗口记录性能数据
        List<PerformanceTrend> trends = new ArrayList<>();
        // 创建当前时间窗口的趋势数据
        PerformanceTrend currentTrend = new PerformanceTrend(
                LocalDateTime.now(),
                totalQueryCount.get(),
                getAverageExecutionTime(),
                getMaxExecutionTime(),
                getSuccessRate(),
                totalResultSetSize.get()
        );
        trends.add(currentTrend);
        return trends;
    }

    @Override
    public void reset() {
        totalQueryCount.set(0);
        successQueryCount.set(0);
        failedQueryCount.set(0);
        totalExecutionTime.set(0);
        totalResultSetSize.set(0);
        maxExecutionTime.set(0L);
        minExecutionTime.set(Long.MAX_VALUE);
        lastExecutionTime.set(0L);
        startTime.set(LocalDateTime.now());
    }
}
