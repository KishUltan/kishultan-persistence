package com.kishultan.persistence.query.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TTL缓存策略实现
 * 基于时间过期的缓存策略
 */
public class TTLCacheStrategy implements CacheStrategy {
    private final Map<String, Long> storeTimes = new ConcurrentHashMap<>();
    private final Map<String, Long> ttls = new ConcurrentHashMap<>();
    private final StrategyStatistics statistics = new StrategyStatistics();

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.TTL;
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
        long currentTime = System.currentTimeMillis();
        List<String> expiredKeys = new ArrayList<>();
        // 找出所有过期的条目
        for (String cacheKey : cacheKeys) {
            if (isExpired(cacheKey, storeTimes.getOrDefault(cacheKey, 0L),
                    ttls.getOrDefault(cacheKey, 0L))) {
                expiredKeys.add(cacheKey);
            }
        }
        // 如果过期条目不够，按TTL剩余时间排序
        if (expiredKeys.size() < count) {
            List<String> remainingKeys = new ArrayList<>(cacheKeys);
            remainingKeys.removeAll(expiredKeys);
            // 按TTL剩余时间排序，选择剩余时间最短的
            remainingKeys.sort((key1, key2) -> {
                long ttl1 = ttls.getOrDefault(key1, Long.MAX_VALUE);
                long ttl2 = ttls.getOrDefault(key2, Long.MAX_VALUE);
                long remaining1 = Math.max(0, ttl1 - (currentTime - storeTimes.getOrDefault(key1, 0L)));
                long remaining2 = Math.max(0, ttl2 - (currentTime - storeTimes.getOrDefault(key2, 0L)));
                return Long.compare(remaining1, remaining2);
            });
            int needMore = count - expiredKeys.size();
            for (int i = 0; i < Math.min(needMore, remainingKeys.size()); i++) {
                expiredKeys.add(remainingKeys.get(i));
            }
        }
        statistics.recordEviction(currentTime);
        return expiredKeys;
    }

    @Override
    public void recordAccess(String cacheKey, long accessTime) {
        if (cacheKey != null) {
            statistics.recordAccess(accessTime);
        }
    }

    @Override
    public void recordStore(String cacheKey, long storeTime, long ttl) {
        if (cacheKey != null) {
            storeTimes.put(cacheKey, storeTime);
            ttls.put(cacheKey, ttl);
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
        Long storeTime = storeTimes.get(cacheKey);
        Long ttl = ttls.get(cacheKey);
        if (storeTime == null || ttl == null || ttl <= 0) {
            return 0.0;
        }
        long currentTime = System.currentTimeMillis();
        long remainingTime = Math.max(0, ttl - (currentTime - storeTime));
        // 权重基于剩余TTL时间，剩余时间越长权重越高
        return (double) remainingTime / ttl;
    }

    @Override
    public void reset() {
        storeTimes.clear();
        ttls.clear();
        statistics.reset();
    }

    @Override
    public StrategyStatistics getStatistics() {
        return statistics;
    }
}
