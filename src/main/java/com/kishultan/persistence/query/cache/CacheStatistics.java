package com.kishultan.persistence.query.cache;

import java.time.LocalDateTime;

/**
 * 缓存统计信息类
 * 记录缓存的各种统计信息
 */
public class CacheStatistics {
    private long hitCount = 0;
    private long missCount = 0;
    private long putCount = 0;
    private long removeCount = 0;
    private long evictionCount = 0;
    private long totalAccessCount = 0;
    private long totalMemoryUsage = 0;
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime lastAccessTime;
    private LocalDateTime lastPutTime;
    private LocalDateTime lastRemoveTime;

    /**
     * 记录缓存命中
     */
    public void recordHit() {
        this.hitCount++;
        this.totalAccessCount++;
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 记录缓存未命中
     */
    public void recordMiss() {
        this.missCount++;
        this.totalAccessCount++;
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 记录缓存存储
     *
     * @param memoryUsage 内存使用量
     */
    public void recordPut(long memoryUsage) {
        this.putCount++;
        this.totalMemoryUsage += memoryUsage;
        this.lastPutTime = LocalDateTime.now();
    }

    /**
     * 记录缓存移除
     *
     * @param memoryUsage 释放的内存使用量
     */
    public void recordRemove(long memoryUsage) {
        this.removeCount++;
        this.totalMemoryUsage = Math.max(0, this.totalMemoryUsage - memoryUsage);
        this.lastRemoveTime = LocalDateTime.now();
    }

    /**
     * 记录缓存淘汰
     *
     * @param memoryUsage 释放的内存使用量
     */
    public void recordEviction(long memoryUsage) {
        this.evictionCount++;
        this.totalMemoryUsage = Math.max(0, this.totalMemoryUsage - memoryUsage);
    }

    // Getter方法
    public long getHitCount() {
        return hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public long getPutCount() {
        return putCount;
    }

    public long getRemoveCount() {
        return removeCount;
    }

    public long getEvictionCount() {
        return evictionCount;
    }

    public long getTotalAccessCount() {
        return totalAccessCount;
    }

    public long getTotalMemoryUsage() {
        return totalMemoryUsage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public LocalDateTime getLastPutTime() {
        return lastPutTime;
    }

    public LocalDateTime getLastRemoveTime() {
        return lastRemoveTime;
    }

    /**
     * 获取缓存命中率
     *
     * @return 命中率（0-1）
     */
    public double getHitRate() {
        return totalAccessCount > 0 ? (double) hitCount / totalAccessCount : 0.0;
    }

    /**
     * 获取缓存未命中率
     *
     * @return 未命中率（0-1）
     */
    public double getMissRate() {
        return totalAccessCount > 0 ? (double) missCount / totalAccessCount : 0.0;
    }

    /**
     * 获取平均内存使用量
     *
     * @return 平均内存使用量（字节）
     */
    public double getAverageMemoryUsage() {
        return putCount > 0 ? (double) totalMemoryUsage / putCount : 0.0;
    }

    /**
     * 获取运行时间（秒）
     *
     * @return 运行时间
     */
    public long getRunningTimeSeconds() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        this.hitCount = 0;
        this.missCount = 0;
        this.putCount = 0;
        this.removeCount = 0;
        this.evictionCount = 0;
        this.totalAccessCount = 0;
        this.totalMemoryUsage = 0;
        this.startTime = LocalDateTime.now();
        this.lastAccessTime = null;
        this.lastPutTime = null;
        this.lastRemoveTime = null;
    }

    @Override
    public String toString() {
        return String.format("CacheStatistics{hitRate=%.2f%%, missRate=%.2f%%, totalAccess=%d, memoryUsage=%d bytes, runningTime=%d seconds}",
                getHitRate() * 100, getMissRate() * 100, totalAccessCount, totalMemoryUsage, getRunningTimeSeconds());
    }
}
