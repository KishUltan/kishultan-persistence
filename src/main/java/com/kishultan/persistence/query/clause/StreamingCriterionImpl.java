package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.query.*;
import com.kishultan.persistence.query.config.StreamingCriterionConfig;
import com.kishultan.persistence.query.executor.QueryExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 通用流式查询构建器实现类
 * 支持 SQL 和 NoSQL 数据库
 * 基于 QueryExecutor 和 RowMapper 的统一接口
 *
 * @param <T> 实体类型
 */
public class StreamingCriterionImpl<T> implements StreamingCriterion<T> {
    private final Criterion<T> criterion;
    private final QueryExecutor<T> queryExecutor;
    private final RowMapper<T> rowMapper;

    /**
     * 构造函数
     *
     * @param criterion  查询构建器
     * @param queryExecutor 查询执行器（支持 SQL 和 NoSQL）
     * @param rowMapper     结果集映射器（支持 DefaultRowMapper 和 DocumentRowMapper）
     */
    public StreamingCriterionImpl(Criterion<T> criterion,
                                     QueryExecutor<T> queryExecutor,
                                     RowMapper<T> rowMapper) {
        this.criterion = criterion;
        this.queryExecutor = queryExecutor;
        this.rowMapper = rowMapper;
    }

    // ==================== 基础流式查询 ====================
    @Override
    public Stream<T> stream() {
        return stream(StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public Stream<T> stream(int batchSize) {
        return StreamSupport.stream(
                new StreamingQuerySpliterator<>(criterion, queryExecutor, rowMapper, batchSize),
                false
        );
    }

    // ==================== 分页流式查询 ====================
    @Override
    public Stream<T> streamWithPagination(int pageSize) {
        return streamWithPagination(pageSize, 0);
    }

    @Override
    public Stream<T> streamWithPagination(int pageSize, int offset) {
        return StreamSupport.stream(
                new PaginatedStreamingQuerySpliterator<>(criterion, queryExecutor, rowMapper, pageSize, offset),
                false
        );
    }

    // ==================== 流式处理 ====================
    @Override
    public void streamForEach(Consumer<T> processor) {
        streamForEach(processor, StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public void streamForEach(Consumer<T> processor, int batchSize) {
        try (Stream<T> stream = stream(batchSize)) {
            stream.forEach(processor);
        }
    }

    @Override
    public void streamForEachWithPagination(Consumer<T> processor, int pageSize) {
        try (Stream<T> stream = streamWithPagination(pageSize)) {
            stream.forEach(processor);
        }
    }

    // ==================== 流式转换 ====================
    @Override
    public <R> Stream<R> streamMap(Function<T, R> mapper) {
        return streamMap(mapper, StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public <R> Stream<R> streamMap(Function<T, R> mapper, int batchSize) {
        return stream(batchSize).map(mapper);
    }

    // ==================== 流式过滤 ====================
    @Override
    public Stream<T> streamFilter(Predicate<T> filter) {
        return streamFilter(filter, StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public Stream<T> streamFilter(Predicate<T> filter, int batchSize) {
        return stream(batchSize).filter(filter);
    }

    // ==================== 流式统计 ====================
    @Override
    public CompletableFuture<Long> streamCount() {
        return streamCount(StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public CompletableFuture<Long> streamCount(int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Stream<T> stream = stream(batchSize)) {
                return stream.count();
            }
        });
    }

    // ==================== 流式聚合 ====================
    @Override
    public <R> CompletableFuture<R> streamReduce(R identity, BiFunction<R, T, R> accumulator) {
        return streamReduce(identity, accumulator, StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public <R> CompletableFuture<R> streamReduce(R identity, BiFunction<R, T, R> accumulator, int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Stream<T> stream = stream(batchSize)) {
                R result = identity;
                for (T item : stream.collect(java.util.stream.Collectors.toList())) {
                    result = accumulator.apply(result, item);
                }
                return result;
            }
        });
    }

    // ==================== 流式收集 ====================
    @Override
    public <R, A> CompletableFuture<R> streamCollect(Collector<T, A, R> collector) {
        return streamCollect(collector, StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public <R, A> CompletableFuture<R> streamCollect(Collector<T, A, R> collector, int batchSize) {
        return CompletableFuture.supplyAsync(() -> {
            try (Stream<T> stream = stream(batchSize)) {
                return stream.collect(collector);
            }
        });
    }

    // ==================== 监控和指标 ====================
    @Override
    public Stream<T> streamWithMonitoring(StreamingQueryMetrics metrics) {
        return streamWithMonitoring(metrics, StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public Stream<T> streamWithMonitoring(StreamingQueryMetrics metrics, int batchSize) {
        return stream(batchSize)
                .peek(item -> {
                    if (metrics instanceof StreamingQueryMetricsImpl) {
                        ((StreamingQueryMetricsImpl) metrics).incrementProcessedCount();
                        ((StreamingQueryMetricsImpl) metrics).updateProcessingTime(System.currentTimeMillis());
                    }
                })
                .onClose(() -> {
                    if (metrics instanceof StreamingQueryMetricsImpl) {
                        ((StreamingQueryMetricsImpl) metrics).setCompleted(true);
                    }
                });
    }

    // ==================== 错误处理 ====================
    @Override
    public Stream<T> streamWithErrorHandling(StreamingErrorHandler<T> errorHandler) {
        return streamWithErrorHandling(errorHandler, StreamingCriterionConfig.DEFAULT_BATCH_SIZE);
    }

    @Override
    public Stream<T> streamWithErrorHandling(StreamingErrorHandler<T> errorHandler, int batchSize) {
        return stream(batchSize)
                .filter(item -> {
                    try {
                        return true;
                    } catch (Exception e) {
                        StreamingErrorHandler.ErrorStrategy strategy = errorHandler.handleError(item, e, 0);
                        return strategy == StreamingErrorHandler.ErrorStrategy.CONTINUE ||
                                strategy == StreamingErrorHandler.ErrorStrategy.SKIP;
                    }
                });
    }

    // ==================== 并行处理 ====================
    @Override
    public Stream<T> streamParallel() {
        return streamParallel(StreamingCriterionConfig.DEFAULT_BATCH_SIZE, StreamingCriterionConfig.DEFAULT_PARALLELISM);
    }

    @Override
    public Stream<T> streamParallel(int batchSize) {
        return streamParallel(batchSize, StreamingCriterionConfig.DEFAULT_PARALLELISM);
    }

    @Override
    public Stream<T> streamParallel(int batchSize, int parallelism) {
        return stream(batchSize)
                .parallel()
                .onClose(() -> {
                    // 清理并行处理资源
                });
    }
}

