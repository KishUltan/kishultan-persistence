package com.kishultan.persistence.query.cache;

import com.kishultan.persistence.query.cache.QueryCacheImpl;
import com.kishultan.persistence.query.cache.LRUCacheStrategy;
import com.kishultan.persistence.query.cache.TTLCacheStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;

/**
 * 查询缓存测试类
 * 
 * @author Portal Team
 */
public class QueryCacheTest {
    
    private QueryCache cache;
    private CacheConfig config;
    
    @Before
    public void setUp() {
        config = CacheConfig.createDevelopment();
        cache = new QueryCacheImpl(config, new LRUCacheStrategy());
    }
    
    @After
    public void tearDown() {
        if (cache != null) {
            cache.clear();
            if (cache instanceof QueryCacheImpl) {
                ((QueryCacheImpl) cache).shutdown();
            }
        }
    }
    
    @Test
    public void testBasicPutAndGet() {
        String cacheKey = "test_key";
        String testValue = "test_value";
        
        // 测试存储
        cache.put(cacheKey, testValue, 1000);
        
        // 测试获取
        String retrievedValue = cache.get(cacheKey, String.class);
        assertNotNull("获取的值不应为null", retrievedValue);
        assertEquals("获取的值应匹配", testValue, retrievedValue);
        
        // 测试缓存命中
        CacheStatistics stats = cache.getStatistics();
        assertTrue("应该有缓存命中", stats.getHitCount() > 0);
    }
    
    @Test
    public void testCacheMiss() {
        String cacheKey = "non_existent_key";
        
        // 测试获取不存在的键
        String retrievedValue = cache.get(cacheKey, String.class);
        assertNull("不存在的键应返回null", retrievedValue);
        
        // 测试缓存未命中
        CacheStatistics stats = cache.getStatistics();
        assertTrue("应该有缓存未命中", stats.getMissCount() > 0);
    }
    
    @Test
    public void testCacheExpiration() throws InterruptedException {
        String cacheKey = "expiring_key";
        String testValue = "test_value";
        
        // 存储短期过期的缓存
        cache.put(cacheKey, testValue, 100); // 100ms过期
        
        // 立即获取应该成功
        String retrievedValue = cache.get(cacheKey, String.class);
        assertNotNull("立即获取应成功", retrievedValue);
        assertEquals("获取的值应匹配", testValue, retrievedValue);
        
        // 等待过期
        Thread.sleep(150);
        
        // 过期后获取应该失败
        String expiredValue = cache.get(cacheKey, String.class);
        assertNull("过期后应返回null", expiredValue);
    }
    
    @Test
    public void testCacheRemove() {
        String cacheKey = "removable_key";
        String testValue = "test_value";
        
        // 存储缓存
        cache.put(cacheKey, testValue, 1000);
        
        // 验证存在
        assertTrue("缓存应存在", cache.contains(cacheKey));
        
        // 移除缓存
        boolean removed = cache.remove(cacheKey);
        assertTrue("应成功移除", removed);
        
        // 验证已移除
        assertFalse("缓存应不存在", cache.contains(cacheKey));
        String retrievedValue = cache.get(cacheKey, String.class);
        assertNull("移除后应返回null", retrievedValue);
    }
    
    @Test
    public void testCacheClear() {
        // 存储多个缓存
        cache.put("key1", "value1", 1000);
        cache.put("key2", "value2", 1000);
        cache.put("key3", "value3", 1000);
        
        // 验证存在
        assertEquals("缓存大小应为3", 3, cache.size());
        
        // 清除所有缓存
        cache.clear();
        
        // 验证已清除
        assertEquals("清除后缓存大小应为0", 0, cache.size());
        assertFalse("key1应不存在", cache.contains("key1"));
        assertFalse("key2应不存在", cache.contains("key2"));
        assertFalse("key3应不存在", cache.contains("key3"));
    }
    
    @Test
    public void testCacheSizeLimit() {
        // 创建小容量缓存
        CacheConfig smallConfig = new CacheConfig(true, 2, 1000);
        QueryCache smallCache = new QueryCacheImpl(smallConfig, new LRUCacheStrategy());
        
        try {
            // 存储超过容量的缓存
            smallCache.put("key1", "value1", 1000);
            smallCache.put("key2", "value2", 1000);
            smallCache.put("key3", "value3", 1000); // 应该触发淘汰
            
            // 验证容量限制
            assertTrue("缓存大小不应超过限制", smallCache.size() <= 2);
            
            // 验证淘汰机制
            assertTrue("key3应该存在", smallCache.contains("key3"));
            
        } finally {
            smallCache.clear();
            if (smallCache instanceof QueryCacheImpl) {
                ((QueryCacheImpl) smallCache).shutdown();
            }
        }
    }
    
    @Test
    public void testAsyncOperations() {
        String cacheKey = "async_key";
        String testValue = "async_value";
        
        // 异步存储
        CompletableFuture<Void> putFuture = cache.putAsync(cacheKey, testValue, 1000);
        assertNotNull("异步存储应返回Future", putFuture);
        
        // 等待存储完成
        putFuture.join();
        
        // 异步获取
        CompletableFuture<String> getFuture = cache.getAsync(cacheKey, String.class);
        assertNotNull("异步获取应返回Future", getFuture);
        
        // 等待获取完成并验证结果
        String retrievedValue = getFuture.join();
        assertNotNull("异步获取的值不应为null", retrievedValue);
        assertEquals("异步获取的值应匹配", testValue, retrievedValue);
    }
    
    @Test
    public void testCacheWarmUp() {
        Map<String, Object> warmUpData = new HashMap<>();
        warmUpData.put("warm_key1", "warm_value1");
        warmUpData.put("warm_key2", "warm_value2");
        warmUpData.put("warm_key3", "warm_value3");
        
        // 预热缓存
        cache.warmUp(warmUpData);
        
        // 验证预热数据
        assertEquals("预热后缓存大小应为3", 3, cache.size());
        assertTrue("warm_key1应存在", cache.contains("warm_key1"));
        assertTrue("warm_key2应存在", cache.contains("warm_key2"));
        assertTrue("warm_key3应存在", cache.contains("warm_key3"));
        
        // 验证预热数据值
        String value1 = cache.get("warm_key1", String.class);
        assertEquals("预热值1应匹配", "warm_value1", value1);
    }
    
    @Test
    public void testCacheStatistics() {
        // 执行一些缓存操作
        cache.put("key1", "value1", 1000);
        cache.get("key1", String.class); // 命中
        cache.get("key2", String.class); // 未命中
        cache.put("key2", "value2", 1000);
        cache.remove("key1");
        
        // 验证统计信息
        CacheStatistics stats = cache.getStatistics();
        assertNotNull("统计信息不应为null", stats);
        assertTrue("应该有命中记录", stats.getHitCount() > 0);
        assertTrue("应该有未命中记录", stats.getMissCount() > 0);
        assertTrue("应该有存储记录", stats.getPutCount() > 0);
        assertTrue("应该有移除记录", stats.getRemoveCount() > 0);
        assertTrue("命中率应大于0", stats.getHitRate() > 0);
    }
    
    @Test
    public void testEnabledDisabled() {
        assertTrue("默认应启用缓存", cache.isEnabled());
        
        cache.setEnabled(false);
        assertFalse("应禁用缓存", cache.isEnabled());
        
        // 禁用状态下不应存储缓存
        cache.put("disabled_key", "disabled_value", 1000);
        String retrievedValue = cache.get("disabled_key", String.class);
        assertNull("禁用状态下应返回null", retrievedValue);
        
        cache.setEnabled(true);
        assertTrue("应重新启用缓存", cache.isEnabled());
    }
    
    @Test
    public void testTTLStrategy() {
        // 使用TTL策略
        QueryCache ttlCache = new QueryCacheImpl(config, new TTLCacheStrategy());
        
        try {
            String cacheKey = "ttl_key";
            String testValue = "ttl_value";
            
            // 存储短期过期的缓存
            ttlCache.put(cacheKey, testValue, 100); // 100ms过期
            
            // 立即获取应该成功
            String retrievedValue = ttlCache.get(cacheKey, String.class);
            assertNotNull("立即获取应成功", retrievedValue);
            assertEquals("获取的值应匹配", testValue, retrievedValue);
            
            // 等待过期
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 过期后获取应该失败
            String expiredValue = ttlCache.get(cacheKey, String.class);
            assertNull("过期后应返回null", expiredValue);
            
        } finally {
            ttlCache.clear();
            if (ttlCache instanceof QueryCacheImpl) {
                ((QueryCacheImpl) ttlCache).shutdown();
            }
        }
    }
}
