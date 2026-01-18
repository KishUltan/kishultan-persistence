package com.kishultan.persistence.query;

import java.util.List;
import java.util.Map;

/**
 * 流式查询错误处理接口
 * 提供流式查询过程中的错误处理策略
 *
 * @param <T> 实体类型
 */
public interface StreamingErrorHandler<T> {
    /**
     * 处理单个记录的错误
     *
     * @param item     出错的记录
     * @param error    错误信息
     * @param position 记录位置
     * @return 错误处理策略
     */
    ErrorStrategy handleError(T item, Throwable error, long position);

    /**
     * 处理批量记录的错误
     *
     * @param batch         出错的批次
     * @param error         错误信息
     * @param startPosition 批次起始位置
     * @return 错误处理策略
     */
    ErrorStrategy handleBatchError(List<T> batch, Throwable error, long startPosition);

    /**
     * 处理连接错误
     *
     * @param error 连接错误
     * @return 错误处理策略
     */
    ErrorStrategy handleConnectionError(Throwable error);

    /**
     * 获取错误统计信息
     *
     * @return 错误类型和数量的映射
     */
    Map<Class<? extends Throwable>, Long> getErrorStatistics();

    /**
     * 错误处理策略
     */
    enum ErrorStrategy {
        /**
         * 继续处理
         */
        CONTINUE,
        /**
         * 跳过错误项
         */
        SKIP,
        /**
         * 重试
         */
        RETRY,
        /**
         * 停止处理
         */
        STOP
    }
}
