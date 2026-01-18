package com.kishultan.persistence.query.config;

/**
 * 流式查询配置类
 * 提供流式查询的默认配置和常量
 */
public class StreamingCriterionConfig {
    // ==================== 默认配置 ====================
    /**
     * 默认批次大小
     */
    public static final int DEFAULT_BATCH_SIZE = 1000;
    /**
     * 默认页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 1000;
    /**
     * 默认并行度（CPU核心数）
     */
    public static final int DEFAULT_PARALLELISM = Runtime.getRuntime().availableProcessors();
    /**
     * 默认连接超时时间（毫秒）
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    /**
     * 默认查询超时时间（毫秒）
     */
    public static final int DEFAULT_QUERY_TIMEOUT = 60000;
    /**
     * 默认最大重试次数
     */
    public static final int DEFAULT_MAX_RETRIES = 3;
    // ==================== 批次大小配置 ====================
    /**
     * 小数据集批次大小
     */
    public static final int SMALL_DATASET_BATCH_SIZE = 2000;
    /**
     * 大数据集批次大小
     */
    public static final int LARGE_DATASET_BATCH_SIZE = 500;
    /**
     * 内存受限批次大小
     */
    public static final int MEMORY_LIMITED_BATCH_SIZE = 100;
    // ==================== 并行度配置 ====================
    /**
     * 最小并行度
     */
    public static final int MIN_PARALLELISM = 1;
    /**
     * 最大并行度
     */
    public static final int MAX_PARALLELISM = 16;
    /**
     * 默认并行度（CPU核心数的2倍）
     */
    public static final int DEFAULT_PARALLELISM_MULTIPLIER = 2;
    // ==================== 超时配置 ====================
    /**
     * 短超时时间（毫秒）
     */
    public static final int SHORT_TIMEOUT = 10000;
    /**
     * 长超时时间（毫秒）
     */
    public static final int LONG_TIMEOUT = 300000;
    // ==================== 工具方法 ====================

    /**
     * 根据数据集大小获取推荐的批次大小
     *
     * @param datasetSize 数据集大小
     * @return 推荐的批次大小
     */
    public static int getRecommendedBatchSize(long datasetSize) {
        if (datasetSize < 10000) {
            return SMALL_DATASET_BATCH_SIZE;
        } else if (datasetSize < 1000000) {
            return DEFAULT_BATCH_SIZE;
        } else {
            return LARGE_DATASET_BATCH_SIZE;
        }
    }

    /**
     * 根据可用内存获取推荐的批次大小
     *
     * @param availableMemoryMB 可用内存（MB）
     * @return 推荐的批次大小
     */
    public static int getRecommendedBatchSizeByMemory(long availableMemoryMB) {
        if (availableMemoryMB < 512) {
            return MEMORY_LIMITED_BATCH_SIZE;
        } else if (availableMemoryMB < 2048) {
            return LARGE_DATASET_BATCH_SIZE;
        } else {
            return DEFAULT_BATCH_SIZE;
        }
    }

    /**
     * 获取推荐的并行度
     *
     * @param batchSize 批次大小
     * @return 推荐的并行度
     */
    public static int getRecommendedParallelism(int batchSize) {
        int parallelism = DEFAULT_PARALLELISM * DEFAULT_PARALLELISM_MULTIPLIER;
        // 根据批次大小调整并行度
        if (batchSize < 100) {
            parallelism = Math.max(MIN_PARALLELISM, parallelism / 2);
        } else if (batchSize > 5000) {
            parallelism = Math.min(MAX_PARALLELISM, parallelism * 2);
        }
        return parallelism;
    }
}
