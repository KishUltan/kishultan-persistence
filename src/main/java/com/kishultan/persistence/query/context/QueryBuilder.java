package com.kishultan.persistence.query.context;

import java.util.List;

/**
 * 构建好的查询定义，包含查询语句、计数查询语句和参数
 * 这是查询条件构建的结果，而不是查询执行的结果
 */
public class QueryBuilder {
    private final String sql;
    private final String countSql;
    private final List<Object> parameters;
    private final List<Object> countParameters;

    public QueryBuilder(String sql, String countSql, List<Object> parameters) {
        this(sql, countSql, parameters, parameters);
    }

    public QueryBuilder(String sql, String countSql, List<Object> parameters, List<Object> countParameters) {
        this.sql = sql;
        this.countSql = countSql;
        this.parameters = parameters;
        this.countParameters = countParameters;
    }

    public String getSql() {
        return sql;
    }

    public String getCountSql() {
        return countSql;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public List<Object> getCountParameters() {
        return countParameters;
    }
}

