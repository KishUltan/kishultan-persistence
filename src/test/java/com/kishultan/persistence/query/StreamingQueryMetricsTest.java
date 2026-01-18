package com.kishultan.persistence.query;

import com.kishultan.persistence.query.clause.StreamingQueryMetricsImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 流式查询指标测试类
 */
public class StreamingQueryMetricsTest {
    
    private StreamingQueryMetrics metrics;
    
    @Before
    public void setUp() {
        metrics = new StreamingQueryMetricsImpl();
    }
    
    @Test
    public void testInitialState() {
        // 测试初始状态
        assertEquals("Initial processed count should be 0", 0, metrics.getProcessedCount());
        assertEquals("Initial error count should be 0", 0, metrics.getErrorCount());
        assertEquals("Initial total count should be -1", -1, metrics.getTotalCount());
        assertEquals("Initial processing time should be 0", 0, metrics.getProcessingTime());
        assertFalse("Initial completed state should be false", metrics.isCompleted());
    }
    
    @Test
    public void testProcessedCount() {
        // 测试已处理记录数
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        impl.incrementProcessedCount();
        assertEquals("Processed count should be 1", 1, metrics.getProcessedCount());
        
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        assertEquals("Processed count should be 3", 3, metrics.getProcessedCount());
    }
    
    @Test
    public void testErrorCount() {
        // 测试错误记录数
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        impl.incrementErrorCount();
        assertEquals("Error count should be 1", 1, metrics.getErrorCount());
        
        impl.incrementErrorCount();
        impl.incrementErrorCount();
        assertEquals("Error count should be 3", 3, metrics.getErrorCount());
    }
    
    @Test
    public void testTotalCount() {
        // 测试总记录数
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        impl.setTotalCount(1000);
        assertEquals("Total count should be 1000", 1000, metrics.getTotalCount());
    }
    
    @Test
    public void testProcessingTime() {
        // 测试处理时间
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        // 等待一小段时间确保时间差大于0
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long currentTime = System.currentTimeMillis();
        impl.updateProcessingTime(currentTime);
        
        assertTrue("Processing time should be greater than 0", metrics.getProcessingTime() > 0);
    }
    
    @Test
    public void testCompleted() {
        // 测试完成状态
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        impl.setCompleted(true);
        assertTrue("Completed state should be true", metrics.isCompleted());
        
        impl.setCompleted(false);
        assertFalse("Completed state should be false", metrics.isCompleted());
    }
    
    @Test
    public void testBatchInfo() {
        // 测试批次信息
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        impl.updateBatchInfo(1000, 5000);
        assertEquals("Current batch size should be 1000", 1000, metrics.getCurrentBatchSize());
        assertEquals("Current offset should be 5000", 5000, metrics.getCurrentOffset());
    }
    
    @Test
    public void testProcessingRate() {
        // 测试处理速率
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        // 设置处理时间和记录数
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        
        long currentTime = System.currentTimeMillis() + 1000; // 1秒后
        impl.updateProcessingTime(currentTime);
        
        double rate = metrics.getProcessingRate();
        assertTrue("Processing rate should be greater than 0", rate > 0);
    }
    
    @Test
    public void testErrorRate() {
        // 测试错误率
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        // 设置处理记录数和错误数
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        impl.incrementErrorCount();
        
        double errorRate = metrics.getErrorRate();
        assertEquals("Error rate should be 1/3", 1.0/3.0, errorRate, 0.001);
    }
    
    @Test
    public void testSuccessRate() {
        // 测试成功率
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        // 设置处理记录数和错误数
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        impl.incrementErrorCount();
        
        double successRate = metrics.getSuccessRate();
        assertEquals("Success rate should be 2/3", 2.0/3.0, successRate, 0.001);
    }
    
    @Test
    public void testAverageProcessingTime() {
        // 测试平均处理时间
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        // 设置处理记录数和时间
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        
        long currentTime = System.currentTimeMillis() + 3000; // 3秒后
        impl.updateProcessingTime(currentTime);
        
        double avgTime = metrics.getAverageProcessingTime();
        assertTrue("Average processing time should be greater than 0", avgTime > 0);
    }
    
    @Test
    public void testReset() {
        // 测试重置
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        // 设置一些值
        impl.incrementProcessedCount();
        impl.incrementErrorCount();
        impl.setTotalCount(1000);
        impl.setCompleted(true);
        impl.updateBatchInfo(500, 2500);
        
        // 重置
        metrics.reset();
        
        // 验证重置后的状态
        assertEquals("Processed count should be 0 after reset", 0, metrics.getProcessedCount());
        assertEquals("Error count should be 0 after reset", 0, metrics.getErrorCount());
        assertEquals("Total count should be -1 after reset", -1, metrics.getTotalCount());
        assertEquals("Processing time should be 0 after reset", 0, metrics.getProcessingTime());
        assertFalse("Completed state should be false after reset", metrics.isCompleted());
        assertEquals("Current batch size should be 0 after reset", 0, metrics.getCurrentBatchSize());
        assertEquals("Current offset should be 0 after reset", 0, metrics.getCurrentOffset());
    }
    
    @Test
    public void testResetWithTotalCount() {
        // 测试带总记录数的重置
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        // 设置一些值
        impl.incrementProcessedCount();
        impl.incrementErrorCount();
        impl.setCompleted(true);
        
        // 重置并设置总记录数
        metrics.reset(2000);
        
        // 验证重置后的状态
        assertEquals("Processed count should be 0 after reset", 0, metrics.getProcessedCount());
        assertEquals("Error count should be 0 after reset", 0, metrics.getErrorCount());
        assertEquals("Total count should be 2000 after reset", 2000, metrics.getTotalCount());
        assertEquals("Processing time should be 0 after reset", 0, metrics.getProcessingTime());
        assertFalse("Completed state should be false after reset", metrics.isCompleted());
    }
    
    @Test
    public void testZeroErrorRate() {
        // 测试零错误率
        StreamingQueryMetricsImpl impl = (StreamingQueryMetricsImpl) metrics;
        
        impl.incrementProcessedCount();
        impl.incrementProcessedCount();
        
        double errorRate = metrics.getErrorRate();
        assertEquals("Error rate should be 0 when no errors", 0.0, errorRate, 0.001);
    }
    
    @Test
    public void testZeroProcessingRate() {
        // 测试零处理速率
        double rate = metrics.getProcessingRate();
        assertEquals("Processing rate should be 0 when no processing time", 0.0, rate, 0.001);
    }
    
    @Test
    public void testZeroAverageProcessingTime() {
        // 测试零平均处理时间
        double avgTime = metrics.getAverageProcessingTime();
        assertEquals("Average processing time should be 0 when no processed count", 0.0, avgTime, 0.001);
    }
}
