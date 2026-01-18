package com.kishultan.persistence.datasource;

import com.kishultan.persistence.config.PersistenceDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

/**
 * 数据源管理器
 * <p>
 * 支持JNDI数据源和本地数据源的管理
 * 线程安全实现，使用StampedLock提供更好的读写性能
 */
public class DataSourceManager {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceManager.class);
    
    // 使用ThreadLocal为每个线程存储独立的JNDI配置
    // 这样每个线程的设置不会影响其他线程
    private static final ThreadLocal<Boolean> threadLocalUseJNDI = new ThreadLocal<>();
    
    // 全局默认值（用于未设置线程本地值的线程）
    private static final AtomicBoolean globalUseJNDI = new AtomicBoolean(true);
    
    // 使用StampedLock提供更好的读写性能（用于全局默认值的读写）
    private static final StampedLock stateLock = new StampedLock();
    private static final Map<String, DataSource> localDSTable = new ConcurrentHashMap<>();
    private static final Map<String, String> dsFlavorsTable = new ConcurrentHashMap<>();
    // 连接池统计信息
    private static final Map<String, ConnectionPoolStatistics> statisticsMap = new ConcurrentHashMap<>();

    /**
     * 获取当前是否使用JNDI
     * <p>
     * 线程安全的方法：优先返回线程本地值，如果没有则返回全局默认值
     * 使用ThreadLocal确保每个线程有独立的配置，互不干扰
     */
    public static boolean isUseJNDI() {
        // 优先检查线程本地值
        Boolean threadLocalValue = threadLocalUseJNDI.get();
        if (threadLocalValue != null) {
            return threadLocalValue;
        }
        // 如果没有线程本地值，返回全局默认值
        return globalUseJNDI.get();
    }

    /**
     * 设置是否使用JNDI（当前线程）
     * <p>
     * 线程安全的方法：设置线程本地值，不影响其他线程
     * 每个线程可以独立设置自己的JNDI模式
     */
    public static void setUseJNDI(boolean useJNDIValue) {
        Boolean oldValue = threadLocalUseJNDI.get();
        threadLocalUseJNDI.set(useJNDIValue);
        
        if (oldValue == null || !oldValue.equals(useJNDIValue)) {
            if (logger.isDebugEnabled()) {
                logger.debug("线程 {} 数据源模式从 {} 更改为: {}",
                        Thread.currentThread().getName(),
                        oldValue != null ? (oldValue ? "JNDI" : "本地") : "默认",
                        useJNDIValue ? "JNDI" : "本地");
            }
        }
    }
    
    /**
     * 清除当前线程的JNDI配置（恢复为使用全局默认值）
     * <p>
     * 在线程结束时调用，避免ThreadLocal内存泄漏
     */
    public static void clearThreadLocalUseJNDI() {
        threadLocalUseJNDI.remove();
    }
    
    /**
     * 设置全局默认的JNDI模式（影响所有未设置线程本地值的线程）
     * <p>
     * 谨慎使用：这会改变全局默认值，影响所有新线程
     */
    public static void setGlobalUseJNDI(boolean useJNDIValue) {
        long stamp = stateLock.writeLock();
        try {
            boolean oldValue = globalUseJNDI.getAndSet(useJNDIValue);
            if (oldValue != useJNDIValue) {
                logger.info("全局数据源模式从 {} 更改为: {}",
                        oldValue ? "JNDI" : "本地",
                        useJNDIValue ? "JNDI" : "本地");
            }
        } finally {
            stateLock.unlockWrite(stamp);
        }
    }

    /**
     * 获取数据库连接（使用默认数据源）
     */
    public static Connection getConnection() throws SQLException, NamingException {
        return getConnection(PersistenceDefaults.getDataSourceName());
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection(String dsName) throws SQLException, NamingException {
        dsName = Optional.ofNullable(dsName).orElse(PersistenceDefaults.getDataSourceName());
        long startTime = System.currentTimeMillis();
        
        try {
            DataSource dataSource = getDataSource(dsName);
            if (dataSource == null) {
                throw new SQLException("数据源不存在: " + dsName);
            }
            
            Connection connection = dataSource.getConnection();
            if (connection == null) {
                throw new SQLException("从数据源获取的连接为 null: " + dsName);
            }
            
            // 记录连接获取统计
            long waitTime = System.currentTimeMillis() - startTime;
            recordConnectionAcquired(dsName, waitTime);
            
            if (logger.isTraceEnabled()) {
                logger.trace("数据库连接获取成功: {}, 等待时间: {}ms", dsName, waitTime);
            }
            
            return connection;
        } catch (SQLException e) {
            // 记录连接超时
            recordConnectionTimeout(dsName);
            logger.error("获取数据库连接失败: {}", dsName, e);
            throw e;
        }
    }
    
    /**
     * 记录连接获取
     */
    private static void recordConnectionAcquired(String dsName, long waitTime) {
        ConnectionPoolStatistics stats = statisticsMap.computeIfAbsent(
            dsName, k -> new ConnectionPoolStatistics());
        stats.recordConnectionAcquired(waitTime);
    }
    
    /**
     * 记录连接超时
     */
    private static void recordConnectionTimeout(String dsName) {
        ConnectionPoolStatistics stats = statisticsMap.computeIfAbsent(
            dsName, k -> new ConnectionPoolStatistics());
        stats.recordConnectionTimeout();
    }
    
    /**
     * 记录连接释放
     */
    public static void recordConnectionReleased(String dsName) {
        ConnectionPoolStatistics stats = statisticsMap.get(dsName);
        if (stats != null) {
            stats.recordConnectionReleased();
        }
    }
    
    /**
     * 获取连接池统计信息
     */
    public static ConnectionPoolStatistics getStatistics(String dsName) {
        return statisticsMap.get(dsName);
    }
    
    /**
     * 获取所有数据源的统计信息
     */
    public static Map<String, ConnectionPoolStatistics> getAllStatistics() {
        return new ConcurrentHashMap<>(statisticsMap);
    }
    
    /**
     * 清除统计信息
     */
    public static void clearStatistics(String dsName) {
        ConnectionPoolStatistics stats = statisticsMap.get(dsName);
        if (stats != null) {
            stats.reset();
        }
    }
    
    /**
     * 清除所有统计信息
     */
    public static void clearAllStatistics() {
        statisticsMap.clear();
    }

    /**
     * 获取数据源
     * <p>
     * 线程安全的方法：使用isUseJNDI()获取当前线程的配置
     * 每个线程根据自己设置的JNDI模式获取对应的数据源
     */
    public static DataSource getDataSource(String name) {
        boolean useJNDIValue = isUseJNDI();
        if (useJNDIValue) {
            return getJNDIDataSource(name);
        } else {
            return getLocalDataSource(name);
        }
    }

    /**
     * 获取JNDI数据源
     */
    private static DataSource getJNDIDataSource(String name) {
        try {
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(name);
//            logger.debug("从JNDI获取数据源: {}", name);
            return ds;
        } catch (NamingException e) {
            logger.error("从JNDI获取数据源失败: {}", name, e);
            throw new RuntimeException("Failed to get JNDI data source: " + name, e);
        }
    }

    /**
     * 获取本地数据源
     */
    private static DataSource getLocalDataSource(String name) {
        DataSource ds = localDSTable.get(name);
        if (ds == null) {
            logger.warn("本地数据源不存在: {}", name);
            throw new RuntimeException("Local data source not found: " + name);
        }
        logger.debug("从本地获取数据源: {}", name);
        return ds;
    }

    /**
     * 添加本地数据源
     * <p>
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static void addLocalDataSource(String name, DataSource dataSource) {
        DataSource oldDataSource = localDSTable.put(name, dataSource);
        if (oldDataSource != null) {
            logger.info("替换本地数据源: {}", name);
        } else {
            logger.info("添加本地数据源: {}", name);
        }
    }

    /**
     * 移除本地数据源
     * <p>
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static void removeLocalDataSource(String name) {
        DataSource removed = localDSTable.remove(name);
        if (removed != null) {
            logger.info("移除本地数据源: {}", name);
        } else {
            logger.warn("尝试移除不存在的本地数据源: {}", name);
        }
    }

    /**
     * 添加数据源类型映射
     * <p>
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static void addDataSourceFlavor(String dsName, String flavor) {
        String oldFlavor = dsFlavorsTable.put(dsName, flavor);
        if (oldFlavor != null && !oldFlavor.equals(flavor)) {
            logger.debug("更新数据源类型映射: {} -> {} (原值: {})", dsName, flavor, oldFlavor);
        } else {
            logger.debug("添加数据源类型映射: {} -> {}", dsName, flavor);
        }
    }

    /**
     * 获取数据源类型
     * <p>
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static String getDataSourceFlavor(String dsName) {
        String flavor = dsFlavorsTable.get(dsName);
        logger.debug("获取数据源类型: {} -> {}", dsName, flavor);
        return flavor;
    }

    /**
     * 检查数据源是否存在
     * <p>
     * 线程安全的方法：使用isUseJNDI()获取当前线程的配置
     * 每个线程根据自己设置的JNDI模式检查对应的数据源
     */
    public static boolean hasDataSource(String name) {
        boolean useJNDIValue = isUseJNDI();
        if (useJNDIValue) {
            try {
                Context ctx = new InitialContext();
                ctx.lookup(name);
                logger.debug("JNDI数据源存在: {}", name);
                return true;
            } catch (NamingException e) {
                logger.debug("JNDI数据源不存在: {}", name);
                return false;
            }
        } else {
            boolean result = localDSTable.containsKey(name);
            logger.debug("本地数据源存在检查: {} = {}", name, result);
            return result;
        }
    }

    /**
     * 获取所有本地数据源名称
     * <p>
     * 线程安全的方法，使用ConcurrentHashMap
     */
    public static String[] getLocalDataSourceNames() {
        String[] names = localDSTable.keySet().toArray(new String[0]);
        logger.debug("获取本地数据源名称列表: {}", String.join(", ", names));
        return names;
    }

    /**
     * 清除所有本地数据源
     * <p>
     * 线程安全的方法，用于测试清理
     */
    public static void clearLocalDataSources() {
        int count = localDSTable.size();
        localDSTable.clear();
        dsFlavorsTable.clear();
        logger.info("清除所有本地数据源，共清除 {} 个数据源", count);
    }
} 