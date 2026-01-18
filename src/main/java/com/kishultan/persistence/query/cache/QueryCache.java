package com.kishultan.persistence.query.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 查询缓存接口
 * 提供查询结果的缓存功能
 */
public interface QueryCache {
    /**
     * 获取缓存结果
     *
     * @param <T>        结果类型
     * @param cacheKey   缓存键
     * @param resultType 结果类型
     * @return 缓存结果，如果不存在返回null
     */
    <T> T get(String cacheKey, Class<T> resultType);

    /**
     * 异步获取缓存结果
     *
     * @param <T>        结果类型
     * @param cacheKey   缓存键
     * @param resultType 结果类型
     * @return 异步缓存结果
     */
    <T> CompletableFuture<T> getAsync(String cacheKey, Class<T> resultType);

    /**
     * 存储缓存结果
     *
     * @param cacheKey 缓存键
     * @param result   结果对象
     * @param ttl      生存时间（毫秒），-1表示永不过期
     */
    void put(String cacheKey, Object result, long ttl);

    /**
     * 异步存储缓存结果
     *
     * @param cacheKey 缓存键
     * @param result   结果对象
     * @param ttl      生存时间（毫秒），-1表示永不过期
     * @return 异步操作结果
     */
    CompletableFuture<Void> putAsync(String cacheKey, Object result, long ttl);

    /**
     * 移除缓存
     *
     * @param cacheKey 缓存键
     * @return 是否成功移除
     */
    boolean remove(String cacheKey);

    /**
     * 批量移除缓存
     *
     * @param cacheKeys 缓存键列表
     * @return 成功移除的数量
     */
    int removeAll(List<String> cacheKeys);

    /**
     * 清除所有缓存
     */
    void clear();

    /**
     * 检查缓存是否存在
     *
     * @param cacheKey 缓存键
     * @return 是否存在
     */
    boolean contains(String cacheKey);

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    int size();

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    CacheStatistics getStatistics();

    /**
     * 预热缓存
     *
     * @param cacheEntries 缓存条目
     */
    void warmUp(Map<String, Object> cacheEntries);

    /**
     * 异步预热缓存
     *
     * @param cacheEntries 缓存条目
     * @return 异步操作结果
     */
    CompletableFuture<Void> warmUpAsync(Map<String, Object> cacheEntries);

    /**
     * 是否启用缓存
     *
     * @return 是否启用
     */
    boolean isEnabled();

    /**
     * 设置缓存开关
     *
     * @param enabled 是否启用
     */
    void setEnabled(boolean enabled);
}
