package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.RowMapper;
import com.kishultan.persistence.query.context.QueryBuilder;
import com.kishultan.persistence.query.executor.QueryExecutor;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 通用流式查询分割器
 * 支持 SQL 和 NoSQL 数据库
 * 基于 QueryExecutor 和 RowMapper 的统一接口
 *
 * @param <T> 实体类型
 */
public class StreamingQuerySpliterator<T> implements Spliterator<T> {
    private final Criterion<T> criterion;
    private final QueryExecutor<T> queryExecutor;
    private final RowMapper<T> rowMapper;
    private final int batchSize;
    private List<T> currentBatch;
    private int currentIndex;
    private boolean hasMoreData;
    private boolean closed = false;
    private boolean initialized = false;
    private int totalLoaded = 0; // 已加载的总数

    /**
     * 构造函数
     *
     * @param criterion  查询构建器
     * @param queryExecutor 查询执行器（支持 SQL 和 NoSQL）
     * @param rowMapper     结果集映射器（支持 DefaultRowMapper 和 DocumentRowMapper）
     * @param batchSize     批次大小
     */
    public StreamingQuerySpliterator(Criterion<T> criterion,
                                     QueryExecutor<T> queryExecutor,
                                     RowMapper<T> rowMapper,
                                     int batchSize) {
        this.criterion = criterion;
        this.queryExecutor = queryExecutor;
        this.rowMapper = rowMapper;
        this.batchSize = batchSize;
        this.currentIndex = 0;
        this.hasMoreData = true;
        // 延迟初始化，避免在构造函数中执行数据库操作
    }

    /**
     * 初始化查询
     */
    private void initializeQuery() {
        if (initialized) {
            return;
        }
        initialized = true;
        // 加载第一批数据
        loadNextBatch();
    }

    /**
     * 加载下一批数据
     */
    private void loadNextBatch() {
        if (closed) {
            return;
        }
        try {
            // 计算当前偏移量（基于已加载的总数）
            int offset = totalLoaded;
            
            // 创建分页查询（使用 limit 实现批次读取）
            Criterion<T> paginatedQuery = criterion.limit(offset, batchSize);
            
            // 构建查询
            QueryBuilder queryResult = ((StandardCriterion<T>) paginatedQuery).buildQuery();
            
            // 执行查询
            List<T> results = queryExecutor.executeQuery(
                queryResult.getSql(),
                queryResult.getParameters(),
                ((StandardCriterion<T>) criterion).getEntityClass(),
                rowMapper
            );
            
            currentBatch = results;
            hasMoreData = results.size() == batchSize;
            currentIndex = 0;
            totalLoaded += results.size();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load batch data", e);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (closed) {
            return false;
        }
        
        // 延迟初始化查询
        if (!initialized) {
            initializeQuery();
        }
        
        if (currentIndex >= currentBatch.size()) {
            if (hasMoreData) {
                loadNextBatch();
                if (currentBatch.isEmpty()) {
                    close();
                    return false;
                }
            } else {
                close();
                return false;
            }
        }
        
        action.accept(currentBatch.get(currentIndex++));
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        // 流式查询不支持分割，返回null
        return null;
    }

    @Override
    public long estimateSize() {
        // 无法准确估计大小，返回未知
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
    }

    /**
     * 关闭资源
     */
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        // QueryExecutor 会自动管理资源，这里不需要手动关闭
    }
}

