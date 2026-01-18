package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.query.PaginationSupport;

import java.util.List;

/**
 * 分页结果实现类
 */
public class PaginatedResultImpl<T> implements PaginationSupport.PaginatedResult<T> {
    private final List<T> data;
    private final long total;
    private final int page;
    private final int size;
    private final int totalPages;

    public PaginatedResultImpl(List<T> data, long total, int page, int size) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    @Override
    public List<T> getData() {
        return data;
    }

    @Override
    public long getTotal() {
        return total;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public boolean hasNext() {
        return page < totalPages;
    }

    @Override
    public boolean hasPrevious() {
        return page > 1;
    }
}
