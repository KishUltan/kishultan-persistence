package com.kishultan.persistence.query;

import java.util.List;

/**
 * 分页查询支持接口
 */
public interface PaginationSupport<T> {
    /**
     * 分页查询（页码从1开始）
     */
    PaginatedResult<T> findPage(int page, int size);

    /**
     * 分页查询（带偏移量）
     */
    PaginatedResult<T> findPageWithOffset(int offset, int size);

    /**
     * 获取总数
     */
    long getTotalCount();

    /**
     * 分页结果
     */
    interface PaginatedResult<T> {
        List<T> getData();

        long getTotal();

        int getPage();

        int getSize();

        int getTotalPages();

        boolean hasNext();

        boolean hasPrevious();
    }
}
