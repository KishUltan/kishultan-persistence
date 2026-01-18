package com.kishultan.persistence.query;

/**
 * 流式查询指标接口
 * 提供流式查询的性能监控和指标收集功能
 */
public interface StreamingQueryMetrics {
    // ==================== 基础指标 ====================

    /**
     * 获取已处理的记录数
     *
     * @return 已处理的记录数
     */
    long getProcessedCount();

    /**
     * 获取错误记录数
     *
     * @return 错误记录数
     */
    long getErrorCount();

    /**
     * 获取总记录数（如果已知）
     *
     * @return 总记录数，-1表示未知
     */
    long getTotalCount();

    /**
     * 获取处理时间（毫秒）
     *
     * @return 处理时间
     */
    long getProcessingTime();
    // ==================== 计算指标 ====================

    /**
     * 获取处理速率（结果/秒）
     *
     * @return 处理速率
     */
    double getProcessingRate();

    /**
     * 获取错误率（0-1）
     *
     * @return 错误率
     */
    double getErrorRate();

    /**
     * 获取成功率（0-1）
     *
     * @return 成功率
     */
    double getSuccessRate();

    /**
     * 获取平均处理时间（毫秒）
     *
     * @return 平均处理时间
     */
    double getAverageProcessingTime();
    // ==================== 实时指标 ====================

    /**
     * 获取当前批次大小
     *
     * @return 当前批次大小
     */
    long getCurrentBatchSize();

    /**
     * 获取当前偏移量
     *
     * @return 当前偏移量
     */
    long getCurrentOffset();

    /**
     * 是否已完成
     *
     * @return 是否已完成
     */
    boolean isCompleted();
    // ==================== 指标管理 ====================

    /**
     * 重置所有指标
     */
    void reset();

    /**
     * 重置所有指标并设置总记录数
     *
     * @param totalCount 总记录数
     */
    void reset(long totalCount);
}
