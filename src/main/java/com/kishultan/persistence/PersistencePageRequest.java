package com.kishultan.persistence;

/**
 * 持久化分页请求对象
 * 独立于DSL，用于门面模式
 */
public class PersistencePageRequest {
    private final int offset;
    private final int size;

    public PersistencePageRequest(int offset, int size) {
        this.offset = offset;
        this.size = size;
    }

    public static PersistencePageRequest of(int offset, int size) {
        return new PersistencePageRequest(Math.max(0, offset), Math.max(0, size));
    }

    public static PersistencePageRequest ofPage(int pageIndex, int pageSize) {
        int offset = (pageIndex - 1) * pageSize;
        return new PersistencePageRequest(offset, pageSize);
    }

    public int getOffset() {
        return offset;
    }

    public int getSize() {
        return size;
    }
} 