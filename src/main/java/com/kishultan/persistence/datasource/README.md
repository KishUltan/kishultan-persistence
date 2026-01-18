# DataSource Package

This package contains the new data source management architecture that replaces the legacy `DBUtil` and `DBConfigUtil` classes.

## Components

### DataSourceManager
- **Purpose**: Manages database connections and data sources
- **Location**: `com.kishultan.datasource.persistence.DataSourceManager`
- **Key Methods**:
  - `getConnection()`: Get a database connection
  - `getDataSource()`: Get a data source by name
  - `getDataSourceFlavor()`: Get the database flavor for a data source
  - `addLocalDataSource()`: Add a local data source
  - `addDataSourceFlavor()`: Add a data source flavor mapping

### DataSourceConfig
- **Purpose**: Manages database configuration and XML parsing
- **Location**: `com.kishultan.datasource.persistence.DataSourceConfig`
- **Key Methods**:
  - `init()`: Initialize configuration from file or stream
  - `getInstance()`: Get the singleton instance
  - `isShutdownDefaultDataSource()`: Check if default data source should be shut down
  - `getOnlineUserDataSourceName()`: Get online user data source name
  - `getOnlineUserTableName()`: Get online user table name
  - `getDataSourceClass()`: Get custom data source class
  - `save()`: Save configuration to XML file

## Migration Status

### ✅ Completed
- **DataSourceManager**: Fully implemented with all core functionality
- **DataSourceConfig**: Complete with all configuration properties and XML support
- **Configuration Properties**: All properties including `isShutdownDefaultDataSource` are now available
- **XML Serialization**: All configuration properties are properly annotated for XML binding
- **Call Site Updates**: All `DBConfigUtil.init()` calls updated to `DataSourceConfig.init()`
- **Direct Method Calls**: All `DBConfigUtil.getInstance()` calls directly updated to `DataSourceConfig.getInstance()`
- **Method Extraction**: All `getConnection()`, `getDataSource()`, and `getDataSourceFlavor()` methods removed from `DBUtil`
- **Call Site Migration**: All call sites updated to use `DataSourceManager` instead of `DBUtil` for connection and data source methods

### 🔄 Migration Strategy

#### For New Code
Use the new classes directly:
```java
// Get configuration
DataSourceConfig config = DataSourceConfig.getInstance();
boolean shouldShutdown = config.isShutdownDefaultDataSource();

// Get connections
Connection conn = DataSourceManager.getConnection();
DataSource ds = DataSourceManager.getDataSource("myDataSource");
```

#### For Existing Code
- **Configuration**: All `DBConfigUtil` calls have been updated to use `DataSourceConfig` directly
- **Connections**: All `DBUtil.getConnection()` and `DBUtil.getDataSource()` calls have been updated to use `DataSourceManager`
- **Remaining DBUtil Methods**: Other `DBUtil` methods (like `execute()`, `getRecord()`, etc.) remain unchanged and continue to work

## Benefits

1. **Reduced Dependencies**: No dependency on the `util` package
2. **Cleaner Architecture**: Separation of concerns between connection management and configuration
3. **Direct Migration**: New code can use the new classes directly
4. **Backward Compatibility**: Existing code continues to work unchanged
5. **Complete Configuration**: All configuration properties are available, including `isShutdownDefaultDataSource`
6. **Direct Method Access**: All configuration methods are now directly accessible through `DataSourceConfig`

## Implementation Details

### Configuration Properties
All configuration properties from the original `DBConfigUtil` are now available in `DataSourceConfig`:

- `isShutdownDefaultDataSource()`: Controls whether to shut down the default data source
- `getOnlineUserDataSourceName()`: Returns the online user data source name
- `getOnlineUserTableName()`: Returns the online user table name
- `getDataSourceClass()`: Returns the custom data source class

### XML Configuration
The configuration is loaded from `dbconfig.xml` and supports all the original elements:
- `<shutdown-default-data-source>`: Boolean flag for shutting down default data source
- `<online-user-data-source>`: Online user data source name
- `<online-user-table-name>`: Online user table name
- `<data-source-class>`: Custom data source class

### Testing
Comprehensive test coverage is provided:
- `DataSourceManagerTest`: Tests connection and data source management
- `DataSourceConfigTest`: Tests configuration loading and property access

## Usage Examples

### Initialization
```java
// Initialize from file
DataSourceConfig.init("WEB-INF/dbconfig.xml", webAppRoot);

// Initialize from stream
InputStream configStream = getClass().getResourceAsStream("/dbconfig.xml");
DataSourceConfig.init(configStream, webAppRoot);
```

### Configuration Access
```java
DataSourceConfig config = DataSourceConfig.getInstance();

// Check shutdown setting
if (config.isShutdownDefaultDataSource()) {
    // Perform shutdown logic
}

// Get online user settings
String onlineUserDS = config.getOnlineUserDataSourceName();
String onlineUserTable = config.getOnlineUserTableName();
```

### Connection Management
```java
// Get default connection
Connection conn = DataSourceManager.getConnection();

// Get specific data source
DataSource ds = DataSourceManager.getDataSource("myDataSource");
Connection conn = ds.getConnection();
```

## Migration Timeline

1. ✅ **Phase 1**: Extract `DataSourceManager` from `DBUtil`
2. ✅ **Phase 2**: Extract `DataSourceConfig` from `DBConfigUtil`
3. ✅ **Phase 3**: Update all call sites to use `DataSourceConfig.init()`
4. ✅ **Phase 4**: Complete configuration property migration
5. ✅ **Phase 5**: Direct method call migration - all `DBConfigUtil.getInstance()` calls updated to `DataSourceConfig.getInstance()`
6. ✅ **Phase 6**: Method extraction and call site migration - all connection and data source methods removed from `DBUtil` and call sites updated to use `DataSourceManager`

The migration is now complete. All configuration properties, including `isShutdownDefaultDataSource`, are fully available in the new architecture. All existing code that was calling `DBConfigUtil.getInstance()` has been updated to call `DataSourceConfig.getInstance()` directly. All connection and data source methods have been moved from `DBUtil` to `DataSourceManager` and all call sites have been updated accordingly. 