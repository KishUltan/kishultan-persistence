package com.kishultan.persistence;

import com.kishultan.persistence.query.builder.QueryResultBuilder;
import com.kishultan.persistence.query.executor.DocumentQueryExecutor;

/**
 * 文档数据源接口
 * <p>
 * 与 javax.sql.DataSource 类似，用于非关系型数据库（NoSQL）
 * <p>
 * 统一 MongoDB、Cassandra、Redis、Elasticsearch 等 NoSQL 数据库的访问接口
 */
public interface DocumentDataSource {
    
    /**
     * 获取数据源名称
     * 
     * @return 数据源名称
     */
    String getDataSourceName();
    
    /**
     * 获取数据源类型
     * 
     * @return 数据源类型（mongodb, cassandra, redis, elasticsearch 等）
     */
    String getDataSourceType();
    
    /**
     * 创建文档查询执行器
     * 
     * @param entityClass 实体类
     * @param <T> 实体类型
     * @return DocumentQueryExecutor 实例
     */
    <T> DocumentQueryExecutor<T> createExecutor(Class<T> entityClass);
    
    /**
     * 创建查询结果构建器
     * 
     * @return QueryResultBuilder 实例
     */
    QueryResultBuilder createResultBuilder();
    
    /**
     * 保存实体
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 保存后的实体
     */
    <T> T save(T entity);
    
    /**
     * 更新实体
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 更新后的实体
     */
    <T> T update(T entity);
    
    /**
     * 删除实体
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     */
    <T> void delete(T entity);
    
    /**
     * 根据ID删除实体
     * 
     * @param entityClass 实体类
     * @param id 实体ID
     * @param <T> 实体类型
     */
    <T> void deleteById(Class<T> entityClass, Object id);
    
    /**
     * 根据ID查找实体
     * 
     * @param entityClass 实体类
     * @param id 实体ID
     * @param <T> 实体类型
     * @return 实体对象
     */
    <T> T findById(Class<T> entityClass, Object id);
    
    /**
     * 关闭数据源
     */
    void close();
}
