package com.kishultan.persistence.datasource;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 连接池统计信息
 * 记录连接池的使用情况
 */
public class ConnectionPoolStatistics {
    private final AtomicLong totalConnectionsCreated = new AtomicLong(0);
    private final AtomicLong totalConnectionsAcquired = new AtomicLong(0);
    private final AtomicLong totalConnectionsReleased = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);
    private final AtomicLong peakActiveConnections = new AtomicLong(0);
    private final AtomicLong connectionWaitTime = new AtomicLong(0);
    private final AtomicLong connectionTimeoutCount = new AtomicLong(0);
    private final LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime lastConnectionTime;
    private LocalDateTime lastReleaseTime;

    /**
     * 记录连接创建
     */
    public void recordConnectionCreated() {
        totalConnectionsCreated.incrementAndGet();
        long active = activeConnections.incrementAndGet();
        updatePeak(active);
        lastConnectionTime = LocalDateTime.now();
    }

    /**
     * 记录连接获取
     */
    public void recordConnectionAcquired(long waitTime) {
        totalConnectionsAcquired.incrementAndGet();
        connectionWaitTime.addAndGet(waitTime);
        long active = activeConnections.incrementAndGet();
        updatePeak(active);
        lastConnectionTime = LocalDateTime.now();
    }

    /**
     * 记录连接释放
     */
    public void recordConnectionReleased() {
        totalConnectionsReleased.incrementAndGet();
        activeConnections.decrementAndGet();
        lastReleaseTime = LocalDateTime.now();
    }

    /**
     * 记录连接超时
     */
    public void recordConnectionTimeout() {
        connectionTimeoutCount.incrementAndGet();
    }

    /**
     * 更新峰值连接数
     */
    private void updatePeak(long current) {
        long currentPeak = peakActiveConnections.get();
        while (current > currentPeak) {
            if (peakActiveConnections.compareAndSet(currentPeak, current)) {
                break;
            }
            currentPeak = peakActiveConnections.get();
        }
    }

    /**
     * 获取平均等待时间（毫秒）
     */
    public long getAverageWaitTime() {
        long acquired = totalConnectionsAcquired.get();
        if (acquired == 0) {
            return 0;
        }
        return connectionWaitTime.get() / acquired;
    }

    /**
     * 获取连接池使用率（0-100）
     */
    public double getUsageRate(long maxConnections) {
        if (maxConnections == 0) {
            return 0.0;
        }
        return (activeConnections.get() * 100.0) / maxConnections;
    }

    // Getter 方法
    public long getTotalConnectionsCreated() {
        return totalConnectionsCreated.get();
    }

    public long getTotalConnectionsAcquired() {
        return totalConnectionsAcquired.get();
    }

    public long getTotalConnectionsReleased() {
        return totalConnectionsReleased.get();
    }

    public long getActiveConnections() {
        return activeConnections.get();
    }

    public long getPeakActiveConnections() {
        return peakActiveConnections.get();
    }

    public long getConnectionTimeoutCount() {
        return connectionTimeoutCount.get();
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getLastConnectionTime() {
        return lastConnectionTime;
    }

    public LocalDateTime getLastReleaseTime() {
        return lastReleaseTime;
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        totalConnectionsCreated.set(0);
        totalConnectionsAcquired.set(0);
        totalConnectionsReleased.set(0);
        activeConnections.set(0);
        peakActiveConnections.set(0);
        connectionWaitTime.set(0);
        connectionTimeoutCount.set(0);
    }

    @Override
    public String toString() {
        return String.format(
            "ConnectionPoolStatistics{active=%d, peak=%d, created=%d, acquired=%d, released=%d, timeouts=%d, avgWaitTime=%dms}",
            activeConnections.get(),
            peakActiveConnections.get(),
            totalConnectionsCreated.get(),
            totalConnectionsAcquired.get(),
            totalConnectionsReleased.get(),
            connectionTimeoutCount.get(),
            getAverageWaitTime()
        );
    }
}

