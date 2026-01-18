package com.kishultan.persistence.query.builder;

import com.kishultan.persistence.query.context.QueryBuildContext;

/**
 * 查询结果构建器接口
 * 负责将 QueryBuildContext 转换为数据库原生查询
 */
public interface QueryResultBuilder {
    
    /**
     * 构建主查询
     * 
     * @param context 查询构建上下文
     * @return 数据库原生查询语句
     */
    String buildQuery(QueryBuildContext<?> context);
    
    /**
     * 构建计数查询
     * 
     * @param context 查询构建上下文
     * @return 计数查询语句
     */
    String buildCountQuery(QueryBuildContext<?> context);
    
    /**
     * 获取构建器类型
     * 返回 "sql"、"mongodb"、"redis" 等
     * 
     * @return 构建器类型
     */
    String getBuilderType();
}
