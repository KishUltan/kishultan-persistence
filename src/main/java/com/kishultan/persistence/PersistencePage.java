package com.kishultan.persistence;

import java.util.List;

/**
 * 持久化分页结果对象
 * 独立于DSL，用于门面模式
 */
public class PersistencePage<T> {
    private final List<T> data;
    private final long total;
    private final int size;
    private final int pageIndex;

    public PersistencePage(List<T> data, long total, int size, int pageIndex) {
        this.data = data;
        this.total = total;
        this.size = size;
        this.pageIndex = pageIndex;
    }

    public List<T> getData() {
        return data;
    }

    public long getTotal() {
        return total;
    }

    public int getSize() {
        return size;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public boolean hasNext() {
        return (pageIndex * size) < total;
    }

    public boolean hasPrevious() {
        return pageIndex > 1;
    }
} 