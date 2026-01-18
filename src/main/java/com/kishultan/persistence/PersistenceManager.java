package com.kishultan.persistence;

import com.kishultan.persistence.config.PersistenceDefaults;
import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.delegate.SansOrmFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 持久化管理器
 * <p>
 * 提供统一的持久化操作入口，支持关系型数据库和非关系型数据库
 * <p>
 * 统一接口设计：
 * - EntityManager: 关系型数据库（SQL） - 使用 PersistenceManager.getEntityManager()
 * - DataManager: 非关系型数据库（NoSQL） - 使用 PersistenceManager.getDataManager()
 * <p>
 * 使用示例：
 * <pre>
 * // SQL 数据库（关系型）
 * EntityManager entityManager = PersistenceManager.getEntityManager();
 * Criterion<User> qb = entityManager.createQueryBuilder(User.class);
 * List<User> users = qb.where().eq(User::getName, "张三").findList();
 * 
 * // MongoDB（非关系型）
 * DataManager dataManager = PersistenceManager.getDataManager("mongodb");
 * Criterion<User> qb = dataManager.createQueryBuilder(User.class);
 * List<User> users = qb.where().eq(User::getName, "张三").findList();
 * </pre>
 * <p>
 * 线程安全实现，使用原子引用和并发缓存
 * <p>
 * 位于persistence包（上层），可以依赖orm包和delegate包，打破循环依赖
 */
public class PersistenceManager {
    private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);
    private static final String DEFAULT_DATASOURCE_NAME = "default";
    // 使用原子引用确保线程安全
    private static final AtomicReference<EntityManagerFactory> defaultManagerRef = new AtomicReference<>();
    // 使用并发缓存存储不同数据源的EntityManagerFactory实例
    private static final ConcurrentHashMap<String, EntityManagerFactory> managerCache = new ConcurrentHashMap<>();
    // 使用并发缓存存储不同NoSQL数据源的DataManager实例
    private static final ConcurrentHashMap<String, DocumentDataSource> documentDataSourceCache = new ConcurrentHashMap<>();
    // 工厂提供者（直接依赖delegate包，无循环依赖）
    private static final SansOrmFactoryProvider factoryProvider = new SansOrmFactoryProvider();

    /**
     * 获取默认的EntityManager
     * <p>
     * 使用原子操作确保线程安全，避免双重检查锁定的复杂性
     */
    public static EntityManager getDefaultManager() {
        EntityManagerFactory manager = defaultManagerRef.get();
        if (manager == null) {
            // 直接使用工厂提供者创建EntityManagerFactory实例
            EntityManagerFactory newManager = createEntityManagerFactory(
                    DataSourceManager.getDataSource(PersistenceDefaults.getDataSourceName())
            );
            // 原子性地设置默认管理器
            if (defaultManagerRef.compareAndSet(null, newManager)) {
                logger.info("初始化默认EntityManager");
            } else {
                // 如果另一个线程已经设置了，使用已设置的值
                manager = defaultManagerRef.get();
            }
        }
        // 每次都创建新的EntityManager实例，确保线程安全
        return new EntityManager(manager != null ? manager : defaultManagerRef.get());
    }

    /**
     * 获取指定数据源的EntityManager
     * <p>
     * 使用缓存避免重复创建EntityManagerFactory实例
     */
    public static EntityManager getManager(String dataSourceName) {
        logger.debug("获取EntityManager，数据源: {}", dataSourceName);
        // 从缓存中获取或创建EntityManagerFactory
        EntityManagerFactory factory = managerCache.computeIfAbsent(dataSourceName, name -> {
            logger.debug("为数据源创建新的EntityManagerFactory: {}", name);
            return createEntityManagerFactory(DataSourceManager.getDataSource(name));
        });
        // 每次都创建新的EntityManager实例，确保线程安全
        return new EntityManager(factory);
    }

    /**
     * 关闭默认管理器
     * <p>
     * 线程安全的方法，使用原子操作
     */
    public static void shutdown() {
        EntityManagerFactory manager = defaultManagerRef.get();
        if (manager != null && defaultManagerRef.compareAndSet(manager, null)) {
            logger.info("关闭默认EntityManager");
            manager.shutdown();
        }
    }

    /**
     * 关闭指定数据源的管理器
     * <p>
     * 线程安全的方法，从缓存中移除
     */
    public static void shutdown(String dataSourceName) {
        EntityManagerFactory manager = managerCache.remove(dataSourceName);
        if (manager != null) {
            logger.info("关闭数据源EntityManager: {}", dataSourceName);
            manager.shutdown();
        }
    }

    /**
     * 关闭所有管理器
     * <p>
     * 线程安全的方法，清理所有缓存的管理器和NoSQL数据源
     */
    public static void shutdownAll() {
        // 关闭默认管理器
        shutdown();
        // 关闭所有缓存的管理器
        managerCache.forEach((name, manager) -> {
            logger.info("关闭数据源EntityManager: {}", name);
            manager.shutdown();
        });
        managerCache.clear();
        
        // 关闭所有NoSQL数据源
        clearDocumentDataSourceCache();
    }

    /**
     * 检查默认数据源是否可用
     */
    public static boolean isDefaultDataSourceAvailable() {
        boolean available = DataSourceManager.hasDataSource(DEFAULT_DATASOURCE_NAME);
        logger.debug("默认数据源可用性检查: {}", available);
        return available;
    }

    /**
     * 检查指定数据源是否可用
     */
    public static boolean isDataSourceAvailable(String dataSourceName) {
        boolean available = DataSourceManager.hasDataSource(dataSourceName);
        logger.debug("数据源可用性检查: {} = {}", dataSourceName, available);
        return available;
    }

    /**
     * 获取缓存的管理器数量
     * <p>
     * 用于监控和调试
     */
    public static int getCachedManagerCount() {
        return managerCache.size();
    }
    
    /**
     * 获取缓存的NoSQL数据源数量
     * <p>
     * 用于监控和调试
     */
    public static int getCachedDocumentDataSourceCount() {
        return documentDataSourceCache.size();
    }

    /**
     * 获取指定NoSQL数据源的DataManager
     * <p>
     * 与getEntityManager()方法对应，提供统一的接口
     * 
     * @param dataSourceName 数据源名称（如 "mongodb"、"cassandra" 等）
     * @return DataManager实例
     */
    public static DataManager getDataManager(String dataSourceName) {
        logger.debug("获取DataManager，数据源: {}", dataSourceName);
        
        // 从缓存中获取或创建DocumentDataSource
        DocumentDataSource dataSource = documentDataSourceCache.get(dataSourceName);
        if (dataSource == null) {
            // 这里需要根据dataSourceName创建相应的DocumentDataSource
            // 实际实现中可以通过配置或工厂模式创建
            // 这里只是一个示例，实际需要更复杂的实现
            throw new UnsupportedOperationException(
                "尚未配置NoSQL数据源: " + dataSourceName + 
                "，请通过PersistenceManager.registerDocumentDataSource()注册"
            );
        }
        
        // 创建DataManager实例
        return new DataManager(dataSource, dataSource.getDataSourceType());
    }
    
    /**
     * 注册NoSQL文档数据源
     * <p>
     * 用于在运行时注册MongoDB、Cassandra等NoSQL数据源
     * 
     * @param dataSourceName 数据源名称
     * @param dataSource DocumentDataSource实例
     */
    public static void registerDocumentDataSource(String dataSourceName, DocumentDataSource dataSource) {
        logger.info("注册NoSQL文档数据源: {} -> {}", dataSourceName, dataSource.getDataSourceName());
        documentDataSourceCache.put(dataSourceName, dataSource);
    }
    
    /**
     * 注销NoSQL文档数据源
     * 
     * @param dataSourceName 数据源名称
     */
    public static void unregisterDocumentDataSource(String dataSourceName) {
        DocumentDataSource dataSource = documentDataSourceCache.remove(dataSourceName);
        if (dataSource != null) {
            logger.info("注销NoSQL文档数据源: {}", dataSourceName);
            dataSource.close();
        }
    }
    
    /**
     * 清理缓存的管理器
     * <p>
     * 用于测试或内存管理
     */
    public static void clearCache() {
        int count = managerCache.size();
        managerCache.clear();
        logger.info("清理EntityManagerFactory缓存，共清理 {} 个管理器", count);
    }
    
    /**
     * 清理所有NoSQL数据源
     * <p>
     * 用于测试或内存管理
     */
    public static void clearDocumentDataSourceCache() {
        int count = documentDataSourceCache.size();
        documentDataSourceCache.forEach((name, dataSource) -> {
            logger.info("关闭NoSQL数据源: {}", name);
            dataSource.close();
        });
        documentDataSourceCache.clear();
        logger.info("清理DocumentDataSource缓存，共清理 {} 个数据源", count);
    }

    /**
     * 创建EntityManagerFactory实例
     * 直接使用工厂提供者，无需SPI或反射
     *
     * @param dataSource 数据源
     * @return EntityManagerFactory实例
     */
    private static EntityManagerFactory createEntityManagerFactory(javax.sql.DataSource dataSource) {
        return factoryProvider.createFactory(dataSource);
    }
}
