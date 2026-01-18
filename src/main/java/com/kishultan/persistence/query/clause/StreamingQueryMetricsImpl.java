package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.query.StreamingQueryMetrics;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 流式查询指标实现类
 * 提供线程安全的指标收集和计算功能
 */
public class StreamingQueryMetricsImpl implements StreamingQueryMetrics {
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong errorCount = new AtomicLong(0);
    private final AtomicLong totalCount = new AtomicLong(-1);
    private final AtomicLong processingTime = new AtomicLong(0);
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicLong currentBatchSize = new AtomicLong(0);
    private final AtomicLong currentOffset = new AtomicLong(0);

    // ==================== 基础指标 ====================
    @Override
    public long getProcessedCount() {
        return processedCount.get();
    }

    @Override
    public long getErrorCount() {
        return errorCount.get();
    }

    @Override
    public long getTotalCount() {
        return totalCount.get();
    }

    /**
     * 设置总记录数
     *
     * @param totalCount 总记录数
     */
    public void setTotalCount(long totalCount) {
        this.totalCount.set(totalCount);
    }

    @Override
    public long getProcessingTime() {
        return processingTime.get();
    }

    // ==================== 计算指标 ====================
    @Override
    public double getProcessingRate() {
        long processed = processedCount.get();
        long time = processingTime.get();
        return time > 0 ? (double) processed / (time / 1000.0) : 0.0;
    }

    @Override
    public double getErrorRate() {
        long processed = processedCount.get();
        long errors = errorCount.get();
        return processed > 0 ? (double) errors / processed : 0.0;
    }

    @Override
    public double getSuccessRate() {
        return 1.0 - getErrorRate();
    }

    @Override
    public double getAverageProcessingTime() {
        long processed = processedCount.get();
        long time = processingTime.get();
        return processed > 0 ? (double) time / processed : 0.0;
    }

    // ==================== 实时指标 ====================
    @Override
    public long getCurrentBatchSize() {
        return currentBatchSize.get();
    }

    @Override
    public long getCurrentOffset() {
        return currentOffset.get();
    }

    @Override
    public boolean isCompleted() {
        return completed.get();
    }

    /**
     * 设置完成状态
     *
     * @param completed 是否完成
     */
    public void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    // ==================== 内部方法 ====================
    // ==================== 指标管理 ====================
    @Override
    public void reset() {
        processedCount.set(0);
        errorCount.set(0);
        totalCount.set(-1);
        processingTime.set(0);
        startTime.set(System.currentTimeMillis());
        completed.set(false);
        currentBatchSize.set(0);
        currentOffset.set(0);
    }

    @Override
    public void reset(long totalCount) {
        reset();
        this.totalCount.set(totalCount);
    }

    /**
     * 增加已处理记录数
     */
    public void incrementProcessedCount() {
        processedCount.incrementAndGet();
    }

    /**
     * 增加错误记录数
     */
    public void incrementErrorCount() {
        errorCount.incrementAndGet();
    }

    /**
     * 更新处理时间
     *
     * @param currentTime 当前时间
     */
    public void updateProcessingTime(long currentTime) {
        processingTime.set(currentTime - startTime.get());
    }

    /**
     * 更新批次信息
     *
     * @param batchSize 批次大小
     * @param offset    偏移量
     */
    public void updateBatchInfo(long batchSize, long offset) {
        this.currentBatchSize.set(batchSize);
        this.currentOffset.set(offset);
    }
}
