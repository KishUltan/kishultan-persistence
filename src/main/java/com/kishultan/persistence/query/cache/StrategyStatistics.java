package com.kishultan.persistence.query.cache;

import java.time.LocalDateTime;

/**
 * 策略统计信息类
 * 记录缓存策略的各种统计信息
 */
public class StrategyStatistics {
    private long accessCount = 0;
    private long storeCount = 0;
    private long evictionCount = 0;
    private long expiredCount = 0;
    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime lastAccessTime;
    private LocalDateTime lastStoreTime;
    private LocalDateTime lastEvictionTime;

    /**
     * 记录访问
     *
     * @param accessTime 访问时间
     */
    public void recordAccess(long accessTime) {
        this.accessCount++;
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 记录存储
     *
     * @param storeTime 存储时间
     */
    public void recordStore(long storeTime) {
        this.storeCount++;
        this.lastStoreTime = LocalDateTime.now();
    }

    /**
     * 记录淘汰
     *
     * @param evictionTime 淘汰时间
     */
    public void recordEviction(long evictionTime) {
        this.evictionCount++;
        this.lastEvictionTime = LocalDateTime.now();
    }

    /**
     * 记录过期
     *
     * @param expiredTime 过期时间
     */
    public void recordExpired(long expiredTime) {
        this.expiredCount++;
    }

    // Getter方法
    public long getAccessCount() {
        return accessCount;
    }

    public long getStoreCount() {
        return storeCount;
    }

    public long getEvictionCount() {
        return evictionCount;
    }

    public long getExpiredCount() {
        return expiredCount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public LocalDateTime getLastStoreTime() {
        return lastStoreTime;
    }

    public LocalDateTime getLastEvictionTime() {
        return lastEvictionTime;
    }

    /**
     * 获取淘汰率
     *
     * @return 淘汰率（0-1）
     */
    public double getEvictionRate() {
        return storeCount > 0 ? (double) evictionCount / storeCount : 0.0;
    }

    /**
     * 获取过期率
     *
     * @return 过期率（0-1）
     */
    public double getExpiredRate() {
        return storeCount > 0 ? (double) expiredCount / storeCount : 0.0;
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
        this.accessCount = 0;
        this.storeCount = 0;
        this.evictionCount = 0;
        this.expiredCount = 0;
        this.startTime = LocalDateTime.now();
        this.lastAccessTime = null;
        this.lastStoreTime = null;
        this.lastEvictionTime = null;
    }

    @Override
    public String toString() {
        return String.format("StrategyStatistics{accessCount=%d, storeCount=%d, evictionRate=%.2f%%, expiredRate=%.2f%%, runningTime=%d seconds}",
                accessCount, storeCount, getEvictionRate() * 100, getExpiredRate() * 100, getRunningTimeSeconds());
    }
}
