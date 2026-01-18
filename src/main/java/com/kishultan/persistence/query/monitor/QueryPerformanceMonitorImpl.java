package com.kishultan.persistence.query.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 查询性能监控实现类
 * 提供查询执行性能监控和统计功能
 */
public class QueryPerformanceMonitorImpl implements QueryPerformanceMonitor {
    private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceMonitorImpl.class);
    private final PerformanceConfig config;
    private final QueryMetricsImpl metrics;
    private final Map<String, QueryStatistics> queryStatisticsMap = new ConcurrentHashMap<>();
    private final List<SlowQueryInfo> slowQueries = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, MonitoringContext> activeContexts = new ConcurrentHashMap<>();
    private final AtomicLong contextIdGenerator = new AtomicLong(0);

    /**
     * 构造函数
     *
     * @param config 性能配置
     */
    public QueryPerformanceMonitorImpl(PerformanceConfig config) {
        this.config = config;
        this.metrics = new QueryMetricsImpl();
    }

    @Override
    public String startMonitoring(String sql, Object[] parameters) {
        if (!config.isEnabled()) {
            return null;
        }
        // 采样检查
        if (config.getSamplingRate() < 100 && Math.random() * 100 > config.getSamplingRate()) {
            return null;
        }
        String contextId = String.valueOf(contextIdGenerator.incrementAndGet());
        MonitoringContext context = new MonitoringContext(contextId, sql, parameters);
        activeContexts.put(contextId, context);
        if (config.isEnableDetailedLogging()) {
            logger.debug("开始监控查询: contextId={}, sql={}", contextId, sql);
        }
        return contextId;
    }

    @Override
    public void endMonitoring(String contextId, boolean success, int resultCount) {
        if (contextId == null) {
            return;
        }
        MonitoringContext context = activeContexts.remove(contextId);
        if (context == null) {
            return;
        }
        long executionTime = System.currentTimeMillis() - context.getStartTime();
        // 更新指标
        metrics.recordExecution(executionTime, success, resultCount);
        // 更新查询统计
        String sqlHash = generateSqlHash(context.getSql());
        QueryStatistics stats = queryStatisticsMap.computeIfAbsent(sqlHash,
                k -> new QueryStatistics(sqlHash, context.getSql()));
        stats.recordExecution(executionTime, success, resultCount);
        // 检查慢查询
        if (executionTime >= config.getSlowQueryThreshold()) {
            SlowQueryInfo slowQuery = new SlowQueryInfo(
                    context.getSql(),
                    context.getParameters(),
                    executionTime,
                    resultCount,
                    null
            );
            slowQueries.add(slowQuery);
            // 限制慢查询列表大小
            if (slowQueries.size() > config.getMaxSlowQueries()) {
                slowQueries.remove(0);
            }
            if (config.isEnableSlowQueryLogging()) {
                logger.warn("检测到慢查询: executionTime={}ms, sql={}", executionTime, context.getSql());
            }
        }
        if (config.isEnableDetailedLogging()) {
            logger.debug("结束监控查询: contextId={}, executionTime={}ms, success={}, resultCount={}",
                    contextId, executionTime, success, resultCount);
        }
    }

    @Override
    public void recordError(String contextId, Throwable error) {
        if (contextId == null) {
            return;
        }
        MonitoringContext context = activeContexts.remove(contextId);
        if (context == null) {
            return;
        }
        long executionTime = System.currentTimeMillis() - context.getStartTime();
        // 更新指标
        metrics.recordExecution(executionTime, false, 0);
        // 更新查询统计
        String sqlHash = generateSqlHash(context.getSql());
        QueryStatistics stats = queryStatisticsMap.computeIfAbsent(sqlHash,
                k -> new QueryStatistics(sqlHash, context.getSql()));
        stats.recordExecution(executionTime, false, 0);
        // 记录错误慢查询
        if (executionTime >= config.getSlowQueryThreshold()) {
            SlowQueryInfo slowQuery = new SlowQueryInfo(
                    context.getSql(),
                    context.getParameters(),
                    executionTime,
                    0,
                    error.getMessage()
            );
            slowQueries.add(slowQuery);
            if (slowQueries.size() > config.getMaxSlowQueries()) {
                slowQueries.remove(0);
            }
        }
        logger.error("查询执行错误: contextId={}, executionTime={}ms, error={}",
                contextId, executionTime, error.getMessage(), error);
    }

    @Override
    public QueryMetrics getMetrics() {
        return metrics;
    }

    @Override
    public List<SlowQueryInfo> getSlowQueries(long threshold) {
        return slowQueries.stream()
                .filter(query -> query.getExecutionTime() >= threshold)
                .sorted((q1, q2) -> Long.compare(q2.getExecutionTime(), q1.getExecutionTime()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, QueryStatistics> getQueryStatistics() {
        return new HashMap<>(queryStatisticsMap);
    }

    @Override
    public void clearMetrics() {
        metrics.reset();
        queryStatisticsMap.clear();
        slowQueries.clear();
        activeContexts.clear();
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        config.setEnabled(enabled);
    }

    /**
     * 生成SQL哈希值
     *
     * @param sql SQL语句
     * @return 哈希值
     */
    private String generateSqlHash(String sql) {
        return String.valueOf(sql.hashCode());
    }

    /**
     * 监控上下文类
     */
    private static class MonitoringContext {
        private final String contextId;
        private final String sql;
        private final Object[] parameters;
        private final long startTime;

        public MonitoringContext(String contextId, String sql, Object[] parameters) {
            this.contextId = contextId;
            this.sql = sql;
            this.parameters = parameters;
            this.startTime = System.currentTimeMillis();
        }

        public String getContextId() {
            return contextId;
        }

        public String getSql() {
            return sql;
        }

        public Object[] getParameters() {
            return parameters;
        }

        public long getStartTime() {
            return startTime;
        }
    }
}
