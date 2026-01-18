package com.kishultan.persistence.query.monitor;

import com.kishultan.persistence.query.monitor.QueryPerformanceMonitorImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * 查询性能监控测试类
 * 
 * @author Portal Team
 */
public class QueryPerformanceTest {
    
    private QueryPerformanceMonitor monitor;
    private PerformanceConfig config;
    
    @Before
    public void setUp() {
        config = PerformanceConfig.createDevelopment();
        monitor = new QueryPerformanceMonitorImpl(config);
    }
    
    @After
    public void tearDown() {
        if (monitor != null) {
            monitor.clearMetrics();
        }
    }
    
    @Test
    public void testStartMonitoring() {
        String sql = "SELECT * FROM users WHERE id = ?";
        Object[] parameters = {1};
        
        String contextId = monitor.startMonitoring(sql, parameters);
        assertNotNull("监控上下文ID不应为null", contextId);
        assertTrue("监控上下文ID不应为空", !contextId.isEmpty());
    }
    
    @Test
    public void testEndMonitoring() {
        String sql = "SELECT * FROM users WHERE id = ?";
        Object[] parameters = {1};
        
        String contextId = monitor.startMonitoring(sql, parameters);
        assertNotNull("监控上下文ID不应为null", contextId);
        
        // 模拟执行时间
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        monitor.endMonitoring(contextId, true, 5);
        
        QueryMetrics metrics = monitor.getMetrics();
        assertNotNull("性能指标不应为null", metrics);
        assertEquals("查询次数应为1", 1, metrics.getTotalQueryCount());
        assertEquals("成功查询次数应为1", 1, metrics.getSuccessQueryCount());
        assertEquals("失败查询次数应为0", 0, metrics.getFailedQueryCount());
        assertTrue("平均执行时间应大于0", metrics.getAverageExecutionTime() > 0);
    }
    
    @Test
    public void testRecordError() {
        String sql = "SELECT * FROM users WHERE id = ?";
        Object[] parameters = {1};
        
        String contextId = monitor.startMonitoring(sql, parameters);
        assertNotNull("监控上下文ID不应为null", contextId);
        
        // 模拟执行时间
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Exception error = new RuntimeException("数据库连接失败");
        monitor.recordError(contextId, error);
        
        QueryMetrics metrics = monitor.getMetrics();
        assertNotNull("性能指标不应为null", metrics);
        assertEquals("查询次数应为1", 1, metrics.getTotalQueryCount());
        assertEquals("成功查询次数应为0", 0, metrics.getSuccessQueryCount());
        assertEquals("失败查询次数应为1", 1, metrics.getFailedQueryCount());
        assertTrue("平均执行时间应大于0", metrics.getAverageExecutionTime() > 0);
    }
    
    @Test
    public void testSlowQueryDetection() {
        // 设置较短的慢查询阈值
        config.setSlowQueryThreshold(50);
        
        String sql = "SELECT * FROM users WHERE id = ?";
        Object[] parameters = {1};
        
        String contextId = monitor.startMonitoring(sql, parameters);
        assertNotNull("监控上下文ID不应为null", contextId);
        
        // 模拟慢查询
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        monitor.endMonitoring(contextId, true, 5);
        
        List<SlowQueryInfo> slowQueries = monitor.getSlowQueries(50);
        assertNotNull("慢查询列表不应为null", slowQueries);
        assertTrue("应该检测到慢查询", slowQueries.size() > 0);
        
        SlowQueryInfo slowQuery = slowQueries.get(0);
        assertEquals("SQL语句应匹配", sql, slowQuery.getSql());
        assertTrue("执行时间应超过阈值", slowQuery.getExecutionTime() >= 50);
    }
    
    @Test
    public void testQueryStatistics() {
        String sql1 = "SELECT * FROM users WHERE id = ?";
        String sql2 = "SELECT * FROM orders WHERE user_id = ?";
        
        // 执行第一个查询
        String contextId1 = monitor.startMonitoring(sql1, new Object[]{1});
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        monitor.endMonitoring(contextId1, true, 5);
        
        // 执行第二个查询
        String contextId2 = monitor.startMonitoring(sql2, new Object[]{1});
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        monitor.endMonitoring(contextId2, true, 3);
        
        Map<String, QueryStatistics> statistics = monitor.getQueryStatistics();
        assertNotNull("查询统计不应为null", statistics);
        assertEquals("应该有2个查询统计", 2, statistics.size());
        
        // 验证统计信息
        for (QueryStatistics stats : statistics.values()) {
            assertTrue("执行次数应大于0", stats.getExecutionCount() > 0);
            assertTrue("平均执行时间应大于0", stats.getAverageExecutionTime() > 0);
            assertEquals("成功率应为1.0", 1.0, stats.getSuccessRate(), 0.01);
        }
    }
    
    @Test
    public void testMetricsCalculation() {
        // 执行多个查询
        for (int i = 0; i < 5; i++) {
            String contextId = monitor.startMonitoring("SELECT * FROM users WHERE id = ?", new Object[]{i});
            try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            monitor.endMonitoring(contextId, i % 2 == 0, i + 1);
        }
        
        QueryMetrics metrics = monitor.getMetrics();
        assertNotNull("性能指标不应为null", metrics);
        
        assertEquals("总查询次数应为5", 5, metrics.getTotalQueryCount());
        assertEquals("成功查询次数应为3", 3, metrics.getSuccessQueryCount());
        assertEquals("失败查询次数应为2", 2, metrics.getFailedQueryCount());
        assertTrue("平均执行时间应大于0", metrics.getAverageExecutionTime() > 0);
        assertTrue("成功率应大于0", metrics.getSuccessRate() > 0);
        assertTrue("失败率应大于0", metrics.getFailureRate() > 0);
    }
    
    @Test
    public void testClearMetrics() {
        // 执行一些查询
        String contextId = monitor.startMonitoring("SELECT * FROM users", new Object[]{});
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        monitor.endMonitoring(contextId, true, 5);
        
        QueryMetrics metrics = monitor.getMetrics();
        assertTrue("应该有查询记录", metrics.getTotalQueryCount() > 0);
        
        // 清除指标
        monitor.clearMetrics();
        
        QueryMetrics clearedMetrics = monitor.getMetrics();
        assertEquals("清除后查询次数应为0", 0, clearedMetrics.getTotalQueryCount());
        assertEquals("清除后成功查询次数应为0", 0, clearedMetrics.getSuccessQueryCount());
        assertEquals("清除后失败查询次数应为0", 0, clearedMetrics.getFailedQueryCount());
    }
    
    @Test
    public void testEnabledDisabled() {
        assertTrue("默认应启用监控", monitor.isEnabled());
        
        monitor.setEnabled(false);
        assertFalse("应禁用监控", monitor.isEnabled());
        
        // 禁用状态下不应记录监控数据
        String contextId = monitor.startMonitoring("SELECT * FROM users", new Object[]{});
        assertNull("禁用状态下应返回null", contextId);
        
        monitor.setEnabled(true);
        assertTrue("应重新启用监控", monitor.isEnabled());
    }
}
