package com.kishultan.persistence.query.cache;

import java.util.List;

/**
 * 缓存策略接口
 * 定义缓存的各种策略和算法
 */
public interface CacheStrategy {
    /**
     * 获取策略类型
     *
     * @return 策略类型
     */
    StrategyType getStrategyType();

    /**
     * 检查是否可以添加新条目
     *
     * @param currentSize 当前缓存大小
     * @param maxSize     最大缓存大小
     * @return 是否可以添加
     */
    boolean canAddEntry(int currentSize, int maxSize);

    /**
     * 获取需要淘汰的缓存键
     *
     * @param cacheKeys 当前缓存键列表
     * @param count     需要淘汰的数量
     * @return 需要淘汰的缓存键列表
     */
    List<String> getEvictionCandidates(List<String> cacheKeys, int count);

    /**
     * 记录缓存访问
     *
     * @param cacheKey   缓存键
     * @param accessTime 访问时间
     */
    void recordAccess(String cacheKey, long accessTime);

    /**
     * 记录缓存存储
     *
     * @param cacheKey  缓存键
     * @param storeTime 存储时间
     * @param ttl       生存时间
     */
    void recordStore(String cacheKey, long storeTime, long ttl);

    /**
     * 检查缓存是否过期
     *
     * @param cacheKey  缓存键
     * @param storeTime 存储时间
     * @param ttl       生存时间
     * @return 是否过期
     */
    boolean isExpired(String cacheKey, long storeTime, long ttl);

    /**
     * 获取缓存权重
     *
     * @param cacheKey 缓存键
     * @return 缓存权重
     */
    double getWeight(String cacheKey);

    /**
     * 重置策略状态
     */
    void reset();

    /**
     * 获取策略统计信息
     *
     * @return 策略统计信息
     */
    StrategyStatistics getStatistics();

    /**
     * 缓存策略类型枚举
     */
    enum StrategyType {
        LRU,    // 最近最少使用
        LFU,    // 最少频率使用
        TTL,    // 基于时间过期
        SIZE,   // 基于大小限制
        CUSTOM  // 自定义策略
    }
}
