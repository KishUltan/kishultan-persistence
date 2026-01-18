package com.kishultan.persistence.query.context;

import java.util.List;

/**
 * 子句构建结果，存储每个子句的SQL片段和参数
 */
public class ClauseResult {
    private final String sql;
    private final List<Object> parameters;

    public ClauseResult(String sql, List<Object> parameters) {
        this.sql = sql;
        this.parameters = parameters;
    }

    public String getSql() {
        return sql;
    }

    public List<Object> getParameters() {
        return parameters;
    }
}
