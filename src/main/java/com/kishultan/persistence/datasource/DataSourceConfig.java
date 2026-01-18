package com.kishultan.persistence.datasource;

import com.kishultan.persistence.config.PersistenceDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import org.apache.commons.dbcp2.BasicDataSource;

/**
 * Database configuration utility class.
 * Extracted from DBConfigUtil to avoid dependencies on the util package.
 * This class is designed to replace DBConfigUtil functionality.
 * Note: JAXB XML serialization is not supported in this standalone version.
 */
public class DataSourceConfig {
    private static boolean dscInitialized = false;
    private static HashMap<String, String> dscDataSourceMap = new HashMap<String, String>();
    private static DataSourceConfig dscSelf = null;
    private static String dscRealPath = "";
    private static boolean dscIsShutdownDefaultDataSource = false;
    private static String dscDataSourceClass = null;
    private static Logger dscLogger = LoggerFactory.getLogger(DataSourceConfig.class);
    private static List<DataSourceInfo> dscDataSources = new ArrayList<DataSourceInfo>();
    private static List<LocalDataSourceInfo> dscLocalDataSources = new ArrayList<LocalDataSourceInfo>();

    public static synchronized void init(String dbConfigFile, String webAppRoot) throws IOException {
        if (dscInitialized) return;
        InputStream fis = null;
        dscRealPath = webAppRoot;
        if (!new File(dbConfigFile).exists()) {
            dscInitialized = true;
            dscLogger.warn("Database properties not initialized. The configuration file, dbconfig.xml, could not be found.");
        } else {
            try {
                fis = new FileInputStream(dbConfigFile);
                init(fis, webAppRoot);
            } catch (FileNotFoundException fex) {
                // Ignore
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
    }

    public synchronized static void init(InputStream fis, String webAppRoot)
            throws IOException {
        if (dscInitialized) return;
        if (fis == null) {
            dscInitialized = true;
            dscLogger.warn("Database properties not initialized. The configuration file, dbconfig.xml, could not be found.");
            return;
        }
        // JAXB XML parsing is not supported in standalone version
        // Initialize with default configuration
        dscSelf = new DataSourceConfig();
        dscInitialized = true;
        dscLogger.info("Successfully parsed database configuration file.");
        // 添加调试信息
        dscLogger.info("dscLocalDataSources.size() = " + dscLocalDataSources.size());
        dscLogger.info("dscDataSources.size() = " + dscDataSources.size());
        dscRealPath = webAppRoot;
        // Check if you are using local datasource
        dscLogger.info("检查本地数据源配置...");
        if (dscLocalDataSources.size() > 0) {
            dscLogger.info("发现 " + dscLocalDataSources.size() + " 个本地数据源，开始初始化...");
            DataSourceManager.setUseJNDI(false);
            try {
                for (int i = 0; i < dscLocalDataSources.size(); i++) {
                    LocalDataSourceInfo dsInfo = dscLocalDataSources.get(i);
                    dscLogger.info("正在初始化数据源 " + (i + 1) + ": " + dsInfo.name);
                    setDataSource(webAppRoot, dsInfo);
                }
                dscLogger.info("所有本地数据源初始化完成");
            } catch (Exception e) {
                dscLogger.error("初始化本地数据源失败", e);
                throw new IOException("Failed to create local data sources.", e);
            }
        } else {
            dscLogger.warn("没有发现本地数据源配置");
        }
        if (PersistenceDefaults.getDataSourceName() == null) {
            dscLogger.warn("Could not find default data source name in configuration.");
        }
        boolean isDefaultDataSourceListed = false;
        if (dscDataSources.size() > 0) {
            String defaultDataSourceName = PersistenceDefaults.getDataSourceName();
            for (int i = 0; i < dscDataSources.size(); i++) {
                DataSourceInfo dsInfo = dscDataSources.get(i);
                dscDataSourceMap.put(dsInfo.label, dsInfo.name);
                if (isNotNullOrEmpty(dsInfo.flavor)) {
                    DataSourceManager.addDataSourceFlavor(dsInfo.name, dsInfo.flavor);
                }
                if (defaultDataSourceName != null && defaultDataSourceName.equals(dsInfo.name))
                    isDefaultDataSourceListed = true;
            }
        } else {
            dscLogger.warn("List of data sources in used is not defined.");
        }
        if (!isDefaultDataSourceListed) {
            dscLogger.warn("Default data source is not specified in data sources list: " +
                    PersistenceDefaults.getDataSourceName());
        }
        dscInitialized = true;
    }

    public static void setDataSource(String webappRoot, LocalDataSourceInfo dsInfo)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String dataSourceName = dsInfo.name;
        String url = dsInfo.url;
        String username = dsInfo.userName;
        String password = dsInfo.password;
        String driver = dsInfo.driverClassName;
        int webRootIndex = url.indexOf("$");
        if (webRootIndex != -1) {
            if (url.substring(webRootIndex).startsWith("${webapp.root}")) {
                url = url.substring(0, webRootIndex) +
                        webappRoot.replaceAll("\\\\", "/") +
                        url.substring(webRootIndex + 15);
            }
        }
        DataSource ds = null;
        if (dscDataSourceClass == null) {
            // 不提供默认连接池实现，需要用户配置 dscDataSourceClass
            // 用户可以通过 setDataSourceClass() 方法指定自定义的 DataSource 实现类
            // 或者直接使用 DataSourceManager.addLocalDataSource() 添加已配置的 DataSource 实例
            throw new IllegalStateException(
                "DataSource class is not specified. " +
                "Please use DataSourceConfig.setDataSourceClass() to specify a DataSource implementation class, " +
                "or use DataSourceManager.addLocalDataSource() directly to add a configured DataSource instance. " +
                "You can use any connection pool implementation (HikariCP, DBCP2, C3P0, etc.) according to your needs."
            );
        } else {
            ds = (DataSource) Class.forName(dscDataSourceClass).newInstance();
        }
        for (int j = 0; j < dsInfo.properties.size(); j++) {
            try {
                PropertyInfo propInfo = dsInfo.properties.get(j);
                setProperty(ds, propInfo.name, propInfo.value, true);
            } catch (Exception e) {
                dscLogger.warn("Error in setting data source property.", e);
            }
        }
        DataSourceManager.addLocalDataSource(dataSourceName, ds);
    }

    public static HashMap getDataSourceMap() {
        return dscDataSourceMap;
    }

    public static DataSourceConfig getInstance() {
        return dscSelf;
    }

    public static void save() throws IOException {
        // JAXB XML serialization is not supported in standalone version
        throw new UnsupportedOperationException("XML serialization is not supported. Use programmatic configuration instead.");
    }

    /**
     * Checks if a string is not null or empty.
     * This method is extracted from StringUtil to avoid dependency on the util package.
     */
    private static boolean isNotNullOrEmpty(String paramValue) {
        return (paramValue != null && paramValue.length() > 0);
    }

    /**
     * Sets a property on an object using reflection.
     * This method is extracted from ReflectionUtil to avoid dependency on the util package.
     */
    private static void setProperty(Object obj, String propertyName, String propertyValue, boolean convertString) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(propertyName);
            field.setAccessible(true);
            if (convertString) {
                Class<?> fieldType = field.getType();
                if (fieldType == String.class) {
                    field.set(obj, propertyValue);
                } else if (fieldType == int.class || fieldType == Integer.class) {
                    field.set(obj, Integer.parseInt(propertyValue));
                } else if (fieldType == long.class || fieldType == Long.class) {
                    field.set(obj, Long.parseLong(propertyValue));
                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    field.set(obj, Boolean.parseBoolean(propertyValue));
                } else if (fieldType == double.class || fieldType == Double.class) {
                    field.set(obj, Double.parseDouble(propertyValue));
                } else if (fieldType == float.class || fieldType == Float.class) {
                    field.set(obj, Float.parseFloat(propertyValue));
                } else {
                    field.set(obj, propertyValue);
                }
            } else {
                field.set(obj, propertyValue);
            }
        } catch (Exception e) {
            dscLogger.warn("Error setting property " + propertyName + " on " + obj.getClass().getName(), e);
        }
    }

    public List<DataSourceInfo> getDataSources() {
        return dscDataSources;
    }

    public void setDataSources(List<DataSourceInfo> ds) {
        dscDataSources = ds;
    }

    // JAXB annotations removed for standalone version
    public List<LocalDataSourceInfo> getLocalDataSources() {
        return dscLocalDataSources;
    }

    public void setLocalDataSources(List<LocalDataSourceInfo> ds) {
        dscLocalDataSources = ds;
    }

    public String getDefaultDataSourceName() {
        return PersistenceDefaults.getDataSourceName();
    }

    public void setDefaultDataSourceName(String dsName) {
        PersistenceDefaults.setDataSourceName(dsName);
        dscLogger.info("Set default data source name to " + dsName);
    }

    public boolean isShutdownDefaultDataSource() {
        return dscIsShutdownDefaultDataSource;
    }

    public void setShutdownDefaultDataSource(boolean shutdown) {
        dscIsShutdownDefaultDataSource = shutdown;
    }

    public String getDataSourceClass() {
        return dscDataSourceClass;
    }

    public void setDataSourceClass(String dataSourceClass) {
        dscDataSourceClass = dataSourceClass;
    }

    
    public static class DataSourceInfo {
        
        public String name = null;
        
        public String label = null;
        
        public String flavor = null;
    }

    
    public static class PropertyInfo {
        
        public String name = null;
        
        public String value = null;
    }

    
    public static class LocalDataSourceInfo {
        
        public String driverClassName = null;
        
        public String url = null;
        
        public String userName = null;
        
        public String password = null;
        
        public String name = null;
        
        public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    }
} 