package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.RowMapper;
import com.kishultan.persistence.query.context.QueryBuilder;
import com.kishultan.persistence.query.executor.QueryExecutor;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 通用分页流式查询分割器
 * 支持 SQL 和 NoSQL 数据库
 * 基于 QueryExecutor 和 RowMapper 的统一接口
 *
 * @param <T> 实体类型
 */
public class PaginatedStreamingQuerySpliterator<T> implements Spliterator<T> {
    private final Criterion<T> criterion;
    private final QueryExecutor<T> queryExecutor;
    private final RowMapper<T> rowMapper;
    private final int pageSize;
    private int currentOffset;
    private List<T> currentPage;
    private int currentIndex;
    private boolean hasMorePages;
    private boolean closed = false;

    /**
     * 构造函数
     *
     * @param criterion  查询构建器
     * @param queryExecutor 查询执行器（支持 SQL 和 NoSQL）
     * @param rowMapper     结果集映射器（支持 DefaultRowMapper 和 DocumentRowMapper）
     * @param pageSize      页大小
     * @param offset        初始偏移量
     */
    public PaginatedStreamingQuerySpliterator(Criterion<T> criterion,
                                              QueryExecutor<T> queryExecutor,
                                              RowMapper<T> rowMapper,
                                              int pageSize,
                                              int offset) {
        this.criterion = criterion;
        this.queryExecutor = queryExecutor;
        this.rowMapper = rowMapper;
        this.pageSize = pageSize;
        this.currentOffset = offset;
        this.currentIndex = 0;
        this.hasMorePages = true;
        loadNextPage();
    }

    /**
     * 加载下一页数据
     */
    private void loadNextPage() {
        if (closed) {
            return;
        }
        try {
            // 创建分页查询
            Criterion<T> paginatedQuery = criterion.limit(currentOffset, pageSize);
            
            // 构建查询
            QueryBuilder queryResult = ((StandardCriterion<T>) paginatedQuery).buildQuery();
            
            // 执行查询
            List<T> results = queryExecutor.executeQuery(
                queryResult.getSql(),
                queryResult.getParameters(),
                ((StandardCriterion<T>) criterion).getEntityClass(),
                rowMapper
            );
            
            currentPage = results;
            hasMorePages = results.size() == pageSize;
            currentOffset += pageSize;
            currentIndex = 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load page data", e);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if (closed) {
            return false;
        }
        if (currentIndex >= currentPage.size()) {
            if (hasMorePages) {
                loadNextPage();
                if (currentPage.isEmpty()) {
                    close();
                    return false;
                }
            } else {
                close();
                return false;
            }
        }
        action.accept(currentPage.get(currentIndex++));
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        // 分页流式查询不支持分割，返回null
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

