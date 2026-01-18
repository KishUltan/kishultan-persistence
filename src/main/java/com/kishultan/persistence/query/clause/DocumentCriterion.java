package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.query.builder.QueryResultBuilder;
import com.kishultan.persistence.query.executor.DocumentQueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文档查询构建器 - NoSQL 数据库专用
 * <p>
 * 继承 StandardCriterion，但是不依赖 DataSource
 * 专用于 MongoDB、Cassandra、Redis、Elasticsearch 等 NoSQL 数据库
 * <p>
 * 与 StandardCriterion 的区别：
 * - StandardCriterion: 需要 DataSource，用于 SQL 数据库
 * - DocumentCriterion: 不需要 DataSource，用于 NoSQL 数据库
 * <p>
 * 使用示例：
 * <pre>
 * DataManager dataManager = PersistenceManager.getDataManager("mongodb");
 * Criterion<User> qb = dataManager.createQueryBuilder(User.class);
 * // 内部使用 DocumentCriterion
 * List<User> users = qb.findList();
 * </pre>
 */
public class DocumentCriterion<T> extends StandardCriterion<T> {
    private static final Logger logger = LoggerFactory.getLogger(DocumentCriterion.class);
    
    /**
     * 构造函数 - NoSQL 数据库专用
     * <p>
     * 不需要 DataSource 参数，通过 setExecutor() 设置 DocumentQueryExecutor
     * 
     * @param entityClass 实体类
     */
    public DocumentCriterion(Class<T> entityClass) {
        super(entityClass, (javax.sql.DataSource) null);
        logger.debug("创建文档查询构建器: {}", entityClass.getSimpleName());
    }
    
    /**
     * 设置文档查询执行器
     * 
     * @param executor DocumentQueryExecutor 实例
     */
    public void setDocumentExecutor(DocumentQueryExecutor<T> executor) {
        setExecutor(executor);
    }
    
    /**
     * 设置文档查询结果构建器
     * 
     * @param resultBuilder DocumentQueryResultBuilder 实例
     */
    public void setDocumentResultBuilder(QueryResultBuilder resultBuilder) {
        setResultBuilder(resultBuilder);
    }
}
