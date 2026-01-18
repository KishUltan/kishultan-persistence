package com.kishultan.persistence;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.clause.DocumentCriterion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据管理器 - 非关系型数据库
 * <p>
 * 与 EntityManager 对应：
 * - EntityManager: 关系型数据库（SQL）
 * - DataManager: 非关系型数据库（NoSQL: MongoDB、Cassandra、Redis、Elasticsearch 等）
 * <p>
 * 提供与 EntityManager 相同的接口，确保使用端完全一致
 * <p>
 * 使用示例：
 * <pre>
 * // SQL 数据库（关系型）
 * EntityManager entityManager = PersistenceManager.getEntityManager();
 * Criterion<User> qb = entityManager.createQueryBuilder(User.class);
 * 
 * // MongoDB（非关系型）
 * DataManager dataManager = PersistenceManager.getDataManager("mongodb");
 * Criterion<User> qb = dataManager.createQueryBuilder(User.class);
 * 
 * // 后续操作完全一致
 * List<User> users = qb.where().eq(User::getName, "张三").findList();
 * </pre>
 */
public class DataManager {
    private static final Logger logger = LoggerFactory.getLogger(DataManager.class);
    
    private final DocumentDataSource dataSource;
    private final String dataSourceName;
    private final String dataSourceType; // mongodb, cassandra, redis, elasticsearch, etc.
    
    public DataManager(DocumentDataSource dataSource, String dataSourceType) {
        this.dataSource = dataSource;
        this.dataSourceName = dataSource.getDataSourceName();
        this.dataSourceType = dataSourceType;
        logger.debug("创建DataManager实例: type={}, name={}", dataSourceType, dataSourceName);
    }
    
    /**
     * 创建查询构建器
     * <p>
     * 与 EntityManager.createQueryBuilder() 接口完全一致
     * 内部使用 DocumentCriterion（NoSQL 专用）
     * 
     * @param entityClass 实体类
     * @param <T> 实体类型
     * @return Criterion 实例
     */
    public <T> Criterion<T> createQueryBuilder(Class<T> entityClass) {
        logger.debug("创建查询构建器: {} (NoSQL: {})", entityClass.getSimpleName(), dataSourceType);
        
        // 创建 DocumentCriterion 实例（NoSQL 专用）
        DocumentCriterion<T> queryBuilder = new DocumentCriterion<>(entityClass);
        
        // 设置 NoSQL 执行器和构建器
        queryBuilder.setExecutor(dataSource.createExecutor(entityClass));
        queryBuilder.setResultBuilder(dataSource.createResultBuilder());
        
        return queryBuilder;
    }
    
    /**
     * 保存实体
     * <p>
     * 与 EntityManager.save() 接口完全一致
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 保存后的实体
     */
    public <T> T save(T entity) {
        logger.debug("保存实体: {} (NoSQL: {})", entity.getClass().getSimpleName(), dataSourceType);
        return dataSource.save(entity);
    }
    
    /**
     * 更新实体
     * <p>
     * 与 EntityManager.update() 接口完全一致
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     * @return 更新后的实体
     */
    public <T> T update(T entity) {
        logger.debug("更新实体: {} (NoSQL: {})", entity.getClass().getSimpleName(), dataSourceType);
        return dataSource.update(entity);
    }
    
    /**
     * 删除实体
     * <p>
     * 与 EntityManager.delete() 接口完全一致
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     */
    public <T> void delete(T entity) {
        logger.debug("删除实体: {} (NoSQL: {})", entity.getClass().getSimpleName(), dataSourceType);
        dataSource.delete(entity);
    }
    
    /**
     * 根据ID删除实体
     * <p>
     * 与 EntityManager.deleteById() 接口完全一致
     * 
     * @param entityClass 实体类
     * @param id 实体ID
     * @param <T> 实体类型
     */
    public <T> void deleteById(Class<T> entityClass, Object id) {
        logger.debug("根据ID删除实体: {} - {} (NoSQL: {})", entityClass.getSimpleName(), id, dataSourceType);
        dataSource.deleteById(entityClass, id);
    }
    
    /**
     * 根据ID查找实体
     * <p>
     * 与 EntityManager.findById() 接口完全一致
     * 
     * @param entityClass 实体类
     * @param id 实体ID
     * @param <T> 实体类型
     * @return 实体对象
     */
    public <T> T findById(Class<T> entityClass, Object id) {
        logger.debug("根据ID查找实体: {} - {} (NoSQL: {})", entityClass.getSimpleName(), id, dataSourceType);
        return dataSource.findById(entityClass, id);
    }
    
    /**
     * 获取数据源名称
     * 
     * @return 数据源名称
     */
    public String getDataSourceName() {
        return dataSourceName;
    }
    
    /**
     * 获取数据源类型
     * 
     * @return 数据源类型（mongodb, cassandra, redis, elasticsearch 等）
     */
    public String getDataSourceType() {
        return dataSourceType;
    }
    
    /**
     * 关闭 DataManager
     */
    public void close() {
        logger.debug("关闭DataManager: {}", dataSourceName);
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
