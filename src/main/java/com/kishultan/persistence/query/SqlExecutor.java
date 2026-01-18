package com.kishultan.persistence.query;

import java.util.List;

/**
 * SQL执行器接口
 * 负责执行SQL查询并返回结果
 */
public interface SqlExecutor {
    /**
     * 执行查询并返回结果列表
     */
    <T> List<T> executeQuery(String sql, List<Object> parameters, Class<T> resultType);

    /**
     * 执行查询并返回结果列表（使用指定的RowMapper）
     */
    <T> List<T> executeQuery(String sql, List<Object> parameters, Class<T> resultType, RowMapper<T> mapper);
    /**
     * 执行查询并返回结果列表（使用指定的ResultSetMapper）
     */
    //<T> List<T> executeQuery(String sql, List<Object> parameters, Class<T> resultType, DefaultRowMapper<T> mapper);

    /**
     * 执行查询并返回单个结果
     */
    <T> T executeAs(String sql, List<Object> parameters, Class<T> resultType);

    /**
     * 执行查询并返回结果数量
     */
    long executeAsLong(String sql, List<Object> parameters);

    /**
     * 执行更新操作（INSERT、UPDATE、DELETE）
     */
    int executeUpdate(String sql, List<Object> parameters);

    /**
     * 批量执行更新操作
     */
    int[] executeBatchUpdate(List<String> sqlList, List<List<Object>> parametersList);
}
