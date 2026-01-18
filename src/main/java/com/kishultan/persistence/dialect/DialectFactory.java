package com.kishultan.persistence.dialect;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库方言工厂
 * 负责根据数据源自动检测并创建对应的方言实例
 */
public class DialectFactory {
    // 方言缓存，避免重复创建
    private static final Map<String, DatabaseDialect> DIALECT_CACHE = new ConcurrentHashMap<>();
    
    // 数据库产品名称到方言的映射
    private static final Map<String, String> DIALECT_MAPPING = new HashMap<>();
    
    static {
        // 初始化数据库产品名称到方言的映射
        DIALECT_MAPPING.put("MySQL", MySQLDialect.class.getName());
        DIALECT_MAPPING.put("H2", H2Dialect.class.getName());
        DIALECT_MAPPING.put("PostgreSQL", PostgreSQLDialect.class.getName());
        DIALECT_MAPPING.put("Oracle", OracleDialect.class.getName());
        DIALECT_MAPPING.put("Microsoft SQL Server", SQLServerDialect.class.getName());
        DIALECT_MAPPING.put("SQLite", SQLiteDialect.class.getName());
        DIALECT_MAPPING.put("MariaDB", MariaDBDialect.class.getName());
        DIALECT_MAPPING.put("DB2", DB2Dialect.class.getName());
        DIALECT_MAPPING.put("Sybase", SybaseDialect.class.getName());
        
        // 国产数据库映射
        DIALECT_MAPPING.put("OceanBase", OceanBaseDialect.class.getName());
        DIALECT_MAPPING.put("TiDB", TiDBDialect.class.getName());
        DIALECT_MAPPING.put("GaussDB", GaussDBDialect.class.getName());
        DIALECT_MAPPING.put("KingbaseES", KingbaseESDialect.class.getName());
        DIALECT_MAPPING.put("Dameng", DamengDialect.class.getName());
        DIALECT_MAPPING.put("TDSQL", TDSQLDialect.class.getName());
        DIALECT_MAPPING.put("PolarDB", PolarDBDialect.class.getName());
        DIALECT_MAPPING.put("GBase", GBaseDialect.class.getName());
        
        // 支持更多数据库产品名称的变体
        DIALECT_MAPPING.put("mariadb", MariaDBDialect.class.getName());
        DIALECT_MAPPING.put("h2 database", H2Dialect.class.getName());
        DIALECT_MAPPING.put("postgresql", PostgreSQLDialect.class.getName());
        DIALECT_MAPPING.put("oracle", OracleDialect.class.getName());
        DIALECT_MAPPING.put("sql server", SQLServerDialect.class.getName());
        DIALECT_MAPPING.put("sqlite", SQLiteDialect.class.getName());
        DIALECT_MAPPING.put("db2 udb", DB2Dialect.class.getName());
        DIALECT_MAPPING.put("db2/400", DB2Dialect.class.getName());
        DIALECT_MAPPING.put("adaptive server enterprise", SybaseDialect.class.getName());
        DIALECT_MAPPING.put("ase", SybaseDialect.class.getName());
        
        // 国产数据库产品名称变体
        DIALECT_MAPPING.put("oceanbase", OceanBaseDialect.class.getName());
        DIALECT_MAPPING.put("tidb", TiDBDialect.class.getName());
        DIALECT_MAPPING.put("gaussdb", GaussDBDialect.class.getName());
        DIALECT_MAPPING.put("kingbase", KingbaseESDialect.class.getName());
        DIALECT_MAPPING.put("kingbasees", KingbaseESDialect.class.getName());
        DIALECT_MAPPING.put("dameng", DamengDialect.class.getName());
        DIALECT_MAPPING.put("dm", DamengDialect.class.getName());
        DIALECT_MAPPING.put("tdsql", TDSQLDialect.class.getName());
        DIALECT_MAPPING.put("polardb", PolarDBDialect.class.getName());
        DIALECT_MAPPING.put("gbase", GBaseDialect.class.getName());
    }
    
    /**
     * 根据数据源自动检测并创建方言实例
     * 
     * @param dataSource 数据源
     * @return 数据库方言实例
     * @throws SQLException 如果数据库连接失败
     */
    public static DatabaseDialect createDialect(DataSource dataSource) throws SQLException {
        if (dataSource == null) {
            // 数据源为 null 时返回默认方言，不抛出异常
            return new H2Dialect();
        }
        
        try {
            String databaseProductName = getDatabaseProductName(dataSource);
            String cacheKey = databaseProductName.toLowerCase();
            
            // 从缓存获取
            DatabaseDialect dialect = DIALECT_CACHE.get(cacheKey);
            if (dialect != null) {
                return dialect;
            }
            
            // 创建新实例
            dialect = createDialectByProductName(databaseProductName);
            
            // 缓存
            DIALECT_CACHE.put(cacheKey, dialect);
            
            return dialect;
        } catch (SQLException e) {
            // 连接失败时返回默认方言，不抛出异常
            // 这样可以在测试环境中优雅降级
            return new H2Dialect();
        }
    }
    
    /**
     * 根据数据库产品名称创建方言实例
     * 
     * @param productName 数据库产品名称
     * @return 数据库方言实例
     */
    public static DatabaseDialect createDialect(String productName) {
        // 从缓存获取
        String cacheKey = productName.toLowerCase();
        DatabaseDialect dialect = DIALECT_CACHE.get(cacheKey);
        if (dialect != null) {
            return dialect;
        }
        
        // 创建新实例
        dialect = createDialectByProductName(productName);
        
        // 缓存
        DIALECT_CACHE.put(cacheKey, dialect);
        
        return dialect;
    }
    
    /**
     * 获取数据库产品名称
     * 
     * @param dataSource 数据源
     * @return 数据库产品名称
     * @throws SQLException 如果数据库连接失败
     */
    private static String getDatabaseProductName(DataSource dataSource) throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据源为 null");
        }
        
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            if (connection == null) {
                throw new SQLException("无法从数据源获取连接");
            }
            
            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData == null) {
                throw new SQLException("无法获取数据库元数据");
            }
            
            return metaData.getDatabaseProductName();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // 忽略关闭连接时的异常
                }
            }
        }
    }
    
    /**
     * 根据数据库产品名称创建方言实例
     * 
     * @param productName 数据库产品名称
     * @return 数据库方言实例
     */
    private static DatabaseDialect createDialectByProductName(String productName) {
        String dialectClassName = null;
        
        // 精确匹配
        dialectClassName = DIALECT_MAPPING.get(productName);
        
        // 模糊匹配（不区分大小写）
        if (dialectClassName == null) {
            String lowerProductName = productName.toLowerCase();
            for (Map.Entry<String, String> entry : DIALECT_MAPPING.entrySet()) {
                if (lowerProductName.contains(entry.getKey().toLowerCase())) {
                    dialectClassName = entry.getValue();
                    break;
                }
            }
        }
        
        // 如果没有找到匹配的方言，使用默认的 H2 方言
        if (dialectClassName == null) {
            return new H2Dialect();
        }
        
        // 使用反射创建方言实例
        try {
            Class<?> dialectClass = Class.forName(dialectClassName);
            return (DatabaseDialect) dialectClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            // 如果创建失败，使用默认的 H2 方言
            return new H2Dialect();
        }
    }
    
    /**
     * 清除方言缓存
     */
    public static void clearCache() {
        DIALECT_CACHE.clear();
    }
}
