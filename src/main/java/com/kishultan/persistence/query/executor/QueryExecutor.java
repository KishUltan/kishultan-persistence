package com.kishultan.persistence.query.executor;

import com.kishultan.persistence.query.RowMapper;

import java.util.List;

/**
 * 查询执行器接口
 * 支持多种数据库：SQL、MongoDB、Redis 等
 * 
 * @param <T> 实体类型
 */
public interface QueryExecutor<T> {
    
    /**
     * 执行查询并返回列表
     * 
     * @param query 查询语句（SQL、JSON 等）
     * @param parameters 查询参数
     * @param resultType 结果类型
     * @param rowMapper 行映射器
     * @param <R> 返回类型
     * @return 查询结果列表
     */
    <R> List<R> executeQuery(
        String query,
        List<Object> parameters,
        Class<R> resultType,
        RowMapper<R> rowMapper
    );
    
    /**
     * 执行查询并返回单个结果
     * 
     * @param query 查询语句
     * @param parameters 查询参数
     * @param resultType 结果类型
     * @param rowMapper 行映射器
     * @param <R> 返回类型
     * @return 单个查询结果，如果没有则返回 null
     */
    <R> R executeSingle(
        String query,
        List<Object> parameters,
        Class<R> resultType,
        RowMapper<R> rowMapper
    );
    
    /**
     * 执行计数查询
     * 
     * @param query 计数查询语句
     * @param parameters 查询参数
     * @return 计数结果
     */
    long executeCount(String query, List<Object> parameters);
    
    /**
     * 执行更新操作
     * 
     * @param query 更新语句
     * @param parameters 更新参数
     * @return 影响的行数
     */
    int executeUpdate(String query, List<Object> parameters);
    
    /**
     * 获取执行器类型
     * 返回 "sql"、"mongodb"、"redis" 等
     * 
     * @return 执行器类型
     */
    String getExecutorType();
}
