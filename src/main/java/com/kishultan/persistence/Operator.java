package com.kishultan.persistence;

/**
 * 操作符枚举
 * <p>
 * 定义各种SQL比较操作符
 */
public enum Operator {
    EQUAL("="),
    NOT_EQUAL("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    LIKE("LIKE"),
    IN("IN"),
    IS_NULL("IS NULL");
    private final String sqlOperator;

    Operator(String sqlOperator) {
        this.sqlOperator = sqlOperator;
    }

    public String getSqlOperator() {
        return sqlOperator;
    }

    @Override
    public String toString() {
        return sqlOperator;
    }
} 