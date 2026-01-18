package com.kishultan.persistence.query.config;

import com.kishultan.persistence.query.cache.CacheConfig;
import com.kishultan.persistence.query.cache.QueryCache;
import com.kishultan.persistence.query.cache.LRUCacheStrategy;
import com.kishultan.persistence.query.cache.QueryCacheImpl;
import com.kishultan.persistence.query.monitor.PerformanceConfig;
import com.kishultan.persistence.query.monitor.QueryPerformanceMonitor;
import com.kishultan.persistence.query.monitor.QueryPerformanceMonitorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueryBuilder配置管理器
 * 统一管理性能监控和缓存的初始化
 */
public class CriterionConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(CriterionConfigManager.class);
    private static volatile QueryPerformanceMonitor performanceMonitor;
    private static volatile QueryCache queryCache;
    private static volatile boolean initialized = false;

    /**
     * 初始化配置
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        try {
            // 初始化性能监控
            initializePerformanceMonitoring();
            // 初始化缓存
            initializeCache();
            initialized = true;
            logger.info("QueryBuilder配置管理器初始化完成");
        } catch (Exception e) {
            logger.error("QueryBuilder配置管理器初始化失败", e);
        }
    }

    /**
     * 初始化性能监控
     */
    private static void initializePerformanceMonitoring() {
        try {
            String monitoringEnabled = System.getProperty("querybuilder.performance.monitor.enabled", "false");
            if ("true".equalsIgnoreCase(monitoringEnabled)) {
                PerformanceConfig config = PerformanceConfig.createDefault();
                performanceMonitor = new QueryPerformanceMonitorImpl(config);
                logger.info("性能监控已启用");
            } else {
                logger.debug("性能监控未启用");
            }
        } catch (Exception e) {
            logger.warn("初始化性能监控失败: {}", e.getMessage());
        }
    }

    /**
     * 初始化缓存
     */
    private static void initializeCache() {
        try {
            String cacheEnabled = System.getProperty("querybuilder.cache.enabled", "false");
            if ("true".equalsIgnoreCase(cacheEnabled)) {
                CacheConfig config = CacheConfig.createDefault();
                LRUCacheStrategy strategy = new LRUCacheStrategy();
                queryCache = new QueryCacheImpl(config, strategy);
                logger.info("查询缓存已启用");
            } else {
                logger.debug("查询缓存未启用");
            }
        } catch (Exception e) {
            logger.warn("初始化查询缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 获取性能监控器
     */
    public static QueryPerformanceMonitor getPerformanceMonitor() {
        if (!initialized) {
            initialize();
        }
        return performanceMonitor;
    }

    /**
     * 获取查询缓存
     */
    public static QueryCache getQueryCache() {
        if (!initialized) {
            initialize();
        }
        return queryCache;
    }

    /**
     * 检查性能监控是否启用
     */
    public static boolean isPerformanceMonitoringEnabled() {
        return getPerformanceMonitor() != null;
    }

    /**
     * 检查缓存是否启用
     */
    public static boolean isCacheEnabled() {
        return getQueryCache() != null;
    }

    /**
     * 重置配置（主要用于测试）
     */
    public static synchronized void reset() {
        performanceMonitor = null;
        queryCache = null;
        initialized = false;
    }
}
