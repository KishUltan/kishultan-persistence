package com.kishultan.persistence.query.context;

/**
 * 排序信息，用于存储ORDER BY的信息
 */
public class OrderInfo {
    private final String column;
    private final String direction;

    public OrderInfo(String column, String direction) {
        this.column = column;
        this.direction = direction;
    }

    public String getColumn() {
        return column;
    }

    public String getDirection() {
        return direction;
    }
}
