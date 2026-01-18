package com.kishultan.persistence.query.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LRU缓存策略实现
 * 基于最近最少使用算法的缓存策略
 */
public class LRUCacheStrategy implements CacheStrategy {
    private final Map<String, Long> accessTimes = new ConcurrentHashMap<>();
    private final StrategyStatistics statistics = new StrategyStatistics();

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.LRU;
    }

    @Override
    public boolean canAddEntry(int currentSize, int maxSize) {
        return currentSize < maxSize;
    }

    @Override
    public List<String> getEvictionCandidates(List<String> cacheKeys, int count) {
        if (cacheKeys == null || cacheKeys.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }
        // 按访问时间排序，选择最久未访问的条目
        List<String> sortedKeys = new ArrayList<>(cacheKeys);
        sortedKeys.sort((key1, key2) -> {
            Long time1 = accessTimes.getOrDefault(key1, 0L);
            Long time2 = accessTimes.getOrDefault(key2, 0L);
            return Long.compare(time1, time2);
        });
        // 返回最久未访问的条目
        int evictCount = Math.min(count, sortedKeys.size());
        List<String> candidates = new ArrayList<>();
        for (int i = 0; i < evictCount; i++) {
            candidates.add(sortedKeys.get(i));
        }
        statistics.recordEviction(System.currentTimeMillis());
        return candidates;
    }

    @Override
    public void recordAccess(String cacheKey, long accessTime) {
        if (cacheKey != null) {
            accessTimes.put(cacheKey, accessTime);
            statistics.recordAccess(accessTime);
        }
    }

    @Override
    public void recordStore(String cacheKey, long storeTime, long ttl) {
        if (cacheKey != null) {
            accessTimes.put(cacheKey, storeTime);
            statistics.recordStore(storeTime);
        }
    }

    @Override
    public boolean isExpired(String cacheKey, long storeTime, long ttl) {
        if (ttl <= 0) {
            return false; // 永不过期
        }
        long currentTime = System.currentTimeMillis();
        boolean expired = (currentTime - storeTime) > ttl;
        if (expired) {
            statistics.recordExpired(currentTime);
        }
        return expired;
    }

    @Override
    public double getWeight(String cacheKey) {
        Long accessTime = accessTimes.get(cacheKey);
        if (accessTime == null) {
            return 0.0;
        }
        // 权重基于访问时间，越久未访问权重越低
        long currentTime = System.currentTimeMillis();
        long timeSinceAccess = currentTime - accessTime;
        // 使用对数函数计算权重，避免权重过小
        return Math.log(Math.max(1, timeSinceAccess));
    }

    @Override
    public void reset() {
        accessTimes.clear();
        statistics.reset();
    }

    @Override
    public StrategyStatistics getStatistics() {
        return statistics;
    }
}
