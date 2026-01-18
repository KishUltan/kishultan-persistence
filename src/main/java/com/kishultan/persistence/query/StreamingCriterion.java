package com.kishultan.persistence.query;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * 流式查询构建器接口
 * 基于Criterion提供流式查询功能，支持大数据集处理
 *
 * @param <T> 实体类型
 */
public interface StreamingCriterion<T> {
    // ==================== 基础流式查询 ====================

    /**
     * 开始流式查询（使用默认批次大小）
     *
     * @return 流式查询结果流
     */
    Stream<T> stream();

    /**
     * 开始流式查询（指定批次大小）
     *
     * @param batchSize 批次大小
     * @return 流式查询结果流
     */
    Stream<T> stream(int batchSize);
    // ==================== 分页流式查询 ====================

    /**
     * 分页流式查询（从偏移量0开始）
     *
     * @param pageSize 页大小
     * @return 流式查询结果流
     */
    Stream<T> streamWithPagination(int pageSize);

    /**
     * 分页流式查询（指定偏移量）
     *
     * @param pageSize 页大小
     * @param offset   偏移量
     * @return 流式查询结果流
     */
    Stream<T> streamWithPagination(int pageSize, int offset);
    // ==================== 流式处理 ====================

    /**
     * 流式处理每个结果（使用默认批次大小）
     *
     * @param processor 处理器
     */
    void streamForEach(Consumer<T> processor);

    /**
     * 流式处理每个结果（指定批次大小）
     *
     * @param processor 处理器
     * @param batchSize 批次大小
     */
    void streamForEach(Consumer<T> processor, int batchSize);

    /**
     * 分页流式处理
     *
     * @param processor 处理器
     * @param pageSize  页大小
     */
    void streamForEachWithPagination(Consumer<T> processor, int pageSize);
    // ==================== 流式转换 ====================

    /**
     * 流式转换（使用默认批次大小）
     *
     * @param mapper 转换函数
     * @param <R>    转换结果类型
     * @return 转换后的流
     */
    <R> Stream<R> streamMap(Function<T, R> mapper);

    /**
     * 流式转换（指定批次大小）
     *
     * @param mapper    转换函数
     * @param batchSize 批次大小
     * @param <R>       转换结果类型
     * @return 转换后的流
     */
    <R> Stream<R> streamMap(Function<T, R> mapper, int batchSize);
    // ==================== 流式过滤 ====================

    /**
     * 流式过滤（使用默认批次大小）
     *
     * @param filter 过滤条件
     * @return 过滤后的流
     */
    Stream<T> streamFilter(Predicate<T> filter);

    /**
     * 流式过滤（指定批次大小）
     *
     * @param filter    过滤条件
     * @param batchSize 批次大小
     * @return 过滤后的流
     */
    Stream<T> streamFilter(Predicate<T> filter, int batchSize);
    // ==================== 流式统计 ====================

    /**
     * 流式计数（使用默认批次大小）
     *
     * @return 异步计数结果
     */
    CompletableFuture<Long> streamCount();

    /**
     * 流式计数（指定批次大小）
     *
     * @param batchSize 批次大小
     * @return 异步计数结果
     */
    CompletableFuture<Long> streamCount(int batchSize);
    // ==================== 流式聚合 ====================

    /**
     * 流式聚合（使用默认批次大小）
     *
     * @param identity    初始值
     * @param accumulator 聚合函数
     * @param <R>         聚合结果类型
     * @return 异步聚合结果
     */
    <R> CompletableFuture<R> streamReduce(R identity, BiFunction<R, T, R> accumulator);

    /**
     * 流式聚合（指定批次大小）
     *
     * @param identity    初始值
     * @param accumulator 聚合函数
     * @param batchSize   批次大小
     * @param <R>         聚合结果类型
     * @return 异步聚合结果
     */
    <R> CompletableFuture<R> streamReduce(R identity, BiFunction<R, T, R> accumulator, int batchSize);
    // ==================== 流式收集 ====================

    /**
     * 流式收集（使用默认批次大小）
     *
     * @param collector 收集器
     * @param <R>       收集结果类型
     * @param <A>       中间累积类型
     * @return 异步收集结果
     */
    <R, A> CompletableFuture<R> streamCollect(Collector<T, A, R> collector);

    /**
     * 流式收集（指定批次大小）
     *
     * @param collector 收集器
     * @param batchSize 批次大小
     * @param <R>       收集结果类型
     * @param <A>       中间累积类型
     * @return 异步收集结果
     */
    <R, A> CompletableFuture<R> streamCollect(Collector<T, A, R> collector, int batchSize);
    // ==================== 监控和指标 ====================

    /**
     * 带监控的流式查询（使用默认批次大小）
     *
     * @param metrics 指标收集器
     * @return 带监控的流
     */
    Stream<T> streamWithMonitoring(StreamingQueryMetrics metrics);

    /**
     * 带监控的流式查询（指定批次大小）
     *
     * @param metrics   指标收集器
     * @param batchSize 批次大小
     * @return 带监控的流
     */
    Stream<T> streamWithMonitoring(StreamingQueryMetrics metrics, int batchSize);
    // ==================== 错误处理 ====================

    /**
     * 带错误处理的流式查询（使用默认批次大小）
     *
     * @param errorHandler 错误处理器
     * @return 带错误处理的流
     */
    Stream<T> streamWithErrorHandling(StreamingErrorHandler<T> errorHandler);

    /**
     * 带错误处理的流式查询（指定批次大小）
     *
     * @param errorHandler 错误处理器
     * @param batchSize    批次大小
     * @return 带错误处理的流
     */
    Stream<T> streamWithErrorHandling(StreamingErrorHandler<T> errorHandler, int batchSize);
    // ==================== 并行处理 ====================

    /**
     * 并行流式查询（使用默认配置）
     *
     * @return 并行流
     */
    Stream<T> streamParallel();

    /**
     * 并行流式查询（指定批次大小）
     *
     * @param batchSize 批次大小
     * @return 并行流
     */
    Stream<T> streamParallel(int batchSize);

    /**
     * 并行流式查询（指定批次大小和并行度）
     *
     * @param batchSize   批次大小
     * @param parallelism 并行度
     * @return 并行流
     */
    Stream<T> streamParallel(int batchSize, int parallelism);
}
