package com.kishultan.persistence;

/**
 * 实体管理器工厂接口
 * <p>
 * 负责创建EntityManager实例，采用JPA风格命名
 */
public interface EntityManagerFactory {
    /**
     * 创建EntityManager实例
     *
     * @return EntityManager实例
     */
    //EntityManager createEntityManager();
    /**
     * 创建支持事务的EntityManager实例
     *
     * @return EntityManager实例
     */
    //EntityManager createEntityManager(EntityTransaction transaction);

    /**
     * 检查是否支持特定功能
     *
     * @return 是否支持Lambda表达式
     */
    boolean isLambdaSupported();

    /**
     * 检查是否支持高级查询
     *
     * @return 是否支持高级查询
     */
    boolean isAdvancedQuerySupported();

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    javax.sql.DataSource getDataSource();

    /**
     * 获取数据源名称
     *
     * @return 数据源名称
     */
    String getDataSourceName();

    /**
     * 创建事务实例
     *
     * @return EntityTransaction实例
     */
    EntityTransaction createTransaction();

    /**
     * 关闭工厂
     */
    void shutdown();
} 