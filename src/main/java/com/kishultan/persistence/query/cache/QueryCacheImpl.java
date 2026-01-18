package com.kishultan.persistence.query.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 查询缓存实现类
 * 提供查询结果的缓存功能
 */
public class QueryCacheImpl implements QueryCache {
    private static final Logger logger = LoggerFactory.getLogger(QueryCacheImpl.class);
    private final CacheConfig config;
    private final CacheStrategy strategy;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final CacheStatistics statistics = new CacheStatistics();
    private final ScheduledExecutorService cleanupExecutor;
    private volatile boolean enabled = true;

    /**
     * 构造函数
     *
     * @param config   缓存配置
     * @param strategy 缓存策略
     */
    public QueryCacheImpl(CacheConfig config, CacheStrategy strategy) {
        this.config = config;
        this.strategy = strategy;
        this.enabled = config.isEnabled();
        // 启动清理任务
        if (config.isEnableAsync()) {
            this.cleanupExecutor = Executors.newScheduledThreadPool(config.getThreadPoolSize());
            this.cleanupExecutor.scheduleAtFixedRate(
                    this::cleanupExpiredEntries,
                    config.getCleanupInterval(),
                    config.getCleanupInterval(),
                    TimeUnit.MILLISECONDS
            );
        } else {
            this.cleanupExecutor = null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheKey, Class<T> resultType) {
        if (!enabled || cacheKey == null) {
            return null;
        }
        CacheEntry entry = cache.get(cacheKey);
        if (entry == null) {
            statistics.recordMiss();
            return null;
        }
        // 检查是否过期
        if (strategy.isExpired(cacheKey, entry.getStoreTime(), entry.getTtl())) {
            cache.remove(cacheKey);
            statistics.recordMiss();
            return null;
        }
        // 记录访问
        strategy.recordAccess(cacheKey, System.currentTimeMillis());
        statistics.recordHit();
        try {
            return (T) entry.getValue();
        } catch (ClassCastException e) {
            logger.warn("缓存类型转换失败: cacheKey={}, expectedType={}, actualType={}",
                    cacheKey, resultType.getSimpleName(), entry.getValue().getClass().getSimpleName());
            cache.remove(cacheKey);
            statistics.recordMiss();
            return null;
        }
    }

    @Override
    public <T> CompletableFuture<T> getAsync(String cacheKey, Class<T> resultType) {
        if (!config.isEnableAsync()) {
            return CompletableFuture.completedFuture(get(cacheKey, resultType));
        }
        return CompletableFuture.supplyAsync(() -> get(cacheKey, resultType));
    }

    @Override
    public void put(String cacheKey, Object result, long ttl) {
        if (!enabled || cacheKey == null || result == null) {
            return;
        }
        // 检查容量限制
        if (!strategy.canAddEntry(cache.size(), config.getMaxSize())) {
            evictEntries();
        }
        long storeTime = System.currentTimeMillis();
        long actualTtl = ttl > 0 ? ttl : config.getDefaultTtl();
        CacheEntry entry = new CacheEntry(result, storeTime, actualTtl);
        cache.put(cacheKey, entry);
        // 记录存储
        strategy.recordStore(cacheKey, storeTime, actualTtl);
        statistics.recordPut(estimateMemoryUsage(result));
        if (logger.isDebugEnabled()) {
            logger.debug("缓存存储: cacheKey={}, ttl={}ms", cacheKey, actualTtl);
        }
    }

    @Override
    public CompletableFuture<Void> putAsync(String cacheKey, Object result, long ttl) {
        if (!config.isEnableAsync()) {
            put(cacheKey, result, ttl);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> put(cacheKey, result, ttl));
    }

    @Override
    public boolean remove(String cacheKey) {
        if (cacheKey == null) {
            return false;
        }
        CacheEntry entry = cache.remove(cacheKey);
        if (entry != null) {
            statistics.recordRemove(estimateMemoryUsage(entry.getValue()));
            return true;
        }
        return false;
    }

    @Override
    public int removeAll(List<String> cacheKeys) {
        if (cacheKeys == null || cacheKeys.isEmpty()) {
            return 0;
        }
        int removedCount = 0;
        for (String cacheKey : cacheKeys) {
            if (remove(cacheKey)) {
                removedCount++;
            }
        }
        return removedCount;
    }

    @Override
    public void clear() {
        cache.clear();
        statistics.reset();
        strategy.reset();
        if (logger.isDebugEnabled()) {
            logger.debug("缓存已清空");
        }
    }

    @Override
    public boolean contains(String cacheKey) {
        if (cacheKey == null) {
            return false;
        }
        CacheEntry entry = cache.get(cacheKey);
        if (entry == null) {
            return false;
        }
        // 检查是否过期
        if (strategy.isExpired(cacheKey, entry.getStoreTime(), entry.getTtl())) {
            cache.remove(cacheKey);
            return false;
        }
        return true;
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public CacheStatistics getStatistics() {
        return statistics;
    }

    @Override
    public void warmUp(Map<String, Object> cacheEntries) {
        if (cacheEntries == null || cacheEntries.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : cacheEntries.entrySet()) {
            put(entry.getKey(), entry.getValue(), config.getDefaultTtl());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("缓存预热完成: {} 个条目", cacheEntries.size());
        }
    }

    @Override
    public CompletableFuture<Void> warmUpAsync(Map<String, Object> cacheEntries) {
        if (!config.isEnableAsync()) {
            warmUp(cacheEntries);
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> warmUp(cacheEntries));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 清理过期条目
     */
    private void cleanupExpiredEntries() {
        if (!enabled) {
            return;
        }
        List<String> expiredKeys = new ArrayList<>();
        for (Map.Entry<String, CacheEntry> entry : cache.entrySet()) {
            String cacheKey = entry.getKey();
            CacheEntry cacheEntry = entry.getValue();
            if (strategy.isExpired(cacheKey, cacheEntry.getStoreTime(), cacheEntry.getTtl())) {
                expiredKeys.add(cacheKey);
            }
        }
        if (!expiredKeys.isEmpty()) {
            removeAll(expiredKeys);
            logger.debug("清理过期缓存条目: {} 个", expiredKeys.size());
        }
    }

    /**
     * 淘汰缓存条目
     */
    private void evictEntries() {
        List<String> allKeys = new ArrayList<>(cache.keySet());
        int evictCount = Math.max(1, allKeys.size() / 10); // 淘汰10%
        List<String> evictCandidates = strategy.getEvictionCandidates(allKeys, evictCount);
        for (String cacheKey : evictCandidates) {
            CacheEntry entry = cache.remove(cacheKey);
            if (entry != null) {
                statistics.recordEviction(estimateMemoryUsage(entry.getValue()));
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("淘汰缓存条目: {} 个", evictCandidates.size());
        }
    }

    /**
     * 估算内存使用量
     *
     * @param obj 对象
     * @return 估算的内存使用量（字节）
     */
    private long estimateMemoryUsage(Object obj) {
        if (obj == null) {
            return 0;
        }
        // 简单的内存估算
        if (obj instanceof String) {
            return ((String) obj).length() * 2; // 每个字符2字节
        } else if (obj instanceof List) {
            return ((List<?>) obj).size() * 64; // 每个元素估算64字节
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).size() * 128; // 每个条目估算128字节
        } else {
            return 64; // 默认估算64字节
        }
    }

    /**
     * 关闭缓存
     */
    public void shutdown() {
        if (cleanupExecutor != null) {
            cleanupExecutor.shutdown();
            try {
                if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cleanupExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cleanupExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 对象被垃圾回收时的清理
     * 作为额外的保护，确保线程池被正确关闭
     */
    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        try {
            if (cleanupExecutor != null && !cleanupExecutor.isShutdown()) {
                logger.warn("QueryCacheImpl对象被GC时线程池未关闭，强制关闭线程池");
                cleanupExecutor.shutdownNow();
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * 缓存条目类
     */
    private static class CacheEntry {
        private final Object value;
        private final long storeTime;
        private final long ttl;

        public CacheEntry(Object value, long storeTime, long ttl) {
            this.value = value;
            this.storeTime = storeTime;
            this.ttl = ttl;
        }

        public Object getValue() {
            return value;
        }

        public long getStoreTime() {
            return storeTime;
        }

        public long getTtl() {
            return ttl;
        }
    }
}
