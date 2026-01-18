# Kishultan Persistence

一个轻量级、类型安全的ORM框架，提供强大的查询构建器、流式查询支持和性能监控功能。

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-1.8+-green.svg)](https://www.oracle.com/java/)

## ✨ 特性

- 🚀 **轻量级**: 基于SansOrm，无复杂依赖
- 🔒 **类型安全**: 支持Lambda表达式，编译时类型检查
- 📊 **强大的查询能力**: 支持复杂查询、JOIN、聚合、窗口函数等
- 🌊 **流式查询**: 支持大数据量流式处理，避免内存溢出
- 📈 **性能监控**: 内置性能监控和慢查询日志
- 💾 **查询缓存**: 支持LRU和TTL缓存策略
- 🔄 **事务管理**: 完整的事务支持，线程安全
- 📦 **多数据源**: 支持多个数据源的统一管理
- 🎯 **零配置**: 开箱即用，无需复杂配置
- 🗄️ **NoSQL支持**: 通过统一的API支持SQL和NoSQL数据库（MongoDB、CouchDB等）

## 📦 Maven依赖

```xml
<dependency>
    <groupId>com.kishultan</groupId>
    <artifactId>kishultan-persistence</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 🚀 快速开始

### 1. 配置数据源

```java
import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.config.PersistenceDefaults;

// 设置默认数据源名称
PersistenceDefaults.setDataSourceName("default");

// 添加数据源（使用任何连接池实现，如 HikariCP、DBCP2、C3P0 等）
// 示例：使用 HikariCP（需要在 pom.xml 中添加 HikariCP 依赖）
// import com.zaxxer.hikari.HikariDataSource;
// HikariDataSource dataSource = new HikariDataSource();
// dataSource.setJdbcUrl("jdbc:mysql://localhost/test");
// dataSource.setUsername("root");
// dataSource.setPassword("password");

// 或者使用其他连接池，例如：
// javax.sql.DataSource dataSource = ... // 你的连接池实现

DataSourceManager.addLocalDataSource("default", dataSource);
DataSourceManager.addDataSourceFlavor("default", "mysql");
```

### 2. 基本CRUD操作

```java
import com.kishultan.persistence.PersistenceManager;
import com.kishultan.persistence.EntityManager;

// 获取EntityManager
EntityManager em = PersistenceManager.getDefaultManager();

// 保存实体
User user = new User();
user.setName("John");
user.setEmail("john@example.com");
user = em.save(user);

// 查询实体
User found = em.findById(User.class, userId);

// 更新实体
user.setName("John Updated");
user = em.update(user);

// 删除实体
em.delete(user);
```

### 3. 简单查询

```java
import com.kishultan.persistence.EntityQuery;

// 创建查询
EntityQuery<User> query = em.createQuery(User.class);

// 条件查询（类型安全）
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .like(User::getName, "%john%");

// 排序和分页
query.orderBy(User::getCreateTime, false)
     .limit(0, 10);

// 执行查询
List<User> users = query.findList();
```

### 4. 复杂查询（使用 Criterion）

```java
import com.kishultan.persistence.query.Criterion;
import static com.kishultan.persistence.query.expression.Functions.*;

// 创建查询构建器
Criterion<User> criterion = em.createCriterion(User.class);

// 复杂查询
List<User> users = criterion
    .select()
    .column(User::getId)
    .column(User::getName)
    .from("users", "u")
    .leftJoin("departments", "d")
    .on("u.department_id", "d.id")
    .where(w -> {
        w.eq("u.status", "active")
         .and()
         .gt("u.age", 18);
    })
    .orderBy().desc(User::getCreateTime)
    .limit(0, 20)
    .findList();
```

### 5. 聚合函数

```java
import com.kishultan.persistence.query.expression.Functions;

Criterion<User> criterion = em.createCriterion(User.class);

// 使用聚合函数
criterion.select()
    .column(Functions.count(User::getId), "total_count")
    .column(Functions.sum(User::getAmount), "total_amount")
    .column(Functions.avg(User::getAmount), "avg_amount")
    .column(Functions.max(User::getAmount), "max_amount")
    .column(Functions.min(User::getAmount), "min_amount")
    .from("users", "u")
    .findList();
```

### 6. 窗口函数

```java
import static com.kishultan.persistence.query.expression.Functions.*;

Criterion<User> criterion = em.createCriterion(User.class);

// 使用窗口函数
criterion.select()
    .column(User::getId)
    .column(rowNumber().over()).as("row_num")
    .column(rank().over()).as("rank_val")
    .column(denseRank().over()).as("dense_rank_val")
    .from("users", "u")
    .findList();

// 带分区和排序的窗口函数
criterion.select()
    .column(User::getId)
    .column(rowNumber()
        .over(User::getCategory, User::getCreateTime)).as("row_num")
    .from("users", "u")
    .findList();
```

### 7. CASE WHEN 表达式

```java
import com.kishultan.persistence.query.expression.Functions;

Criterion<User> criterion = em.createCriterion(User.class);

// CASE WHEN 表达式
criterion.select()
    .column(User::getId)
    .column(Functions.caseWhen(User::getStatus, "u")
        .when("active").then("启用")
        .when("inactive").then("禁用")
        .elseResult("未知")
        .end()).as("status_text")
    .from("users", "u")
    .findList();
```

### 8. 表达式函数

```java
import com.kishultan.persistence.query.expression.Functions;

Criterion<User> criterion = em.createCriterion(User.class);

// 字符串函数
criterion.select()
    .column(Functions.upper(User::getName), "upper_name")
    .column(Functions.lower(User::getName), "lower_name")
    .column(Functions.length(User::getName), "name_length")
    .from("users", "u")
    .findList();

// 数学函数
criterion.select()
    .column(Functions.abs(User::getAmount), "abs_amount")
    .column(Functions.round(User::getAmount), "round_amount")
    .from("users", "u")
    .findList();
```

### 9. 流式查询（大数据量处理）

```java
import com.kishultan.persistence.query.StreamingCriterion;

Criterion<User> criterion = em.createCriterion(User.class);

// 创建流式查询
StreamingCriterion<User> streamingQuery = criterion.createStreamingCriterion();

// 流式处理
streamingQuery.stream()
    .forEach(user -> {
        // 处理每个用户，避免内存溢出
        processUser(user);
    });

// 分页流式查询
streamingQuery.streamWithPagination(1000)
    .forEach(user -> {
        processUser(user);
    });
```

### 10. NoSQL 数据库支持

框架支持 NoSQL 数据库（如 MongoDB、CouchDB 等），通过统一的 API 查询 SQL 和 NoSQL 数据库。

#### 10.1 MongoDB 集成示例

```java
import com.kishultan.persistence.query.QueryExecutor;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.clause.StandardCriterion;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

// 1. 添加 MongoDB 依赖
// <dependency>
//     <groupId>org.mongodb</groupId>
//     <artifactId>mongodb-driver-sync</artifactId>
//     <version>4.10.0</version>
// </dependency>

// 2. 创建 MongoDB 连接
MongoDatabase database = MongoClients.create("mongodb://localhost:27017")
    .getDatabase("test");

// 3. 实现自定义 MongoDB 执行器（实现 QueryExecutor 接口）
MongoDBQueryExecutor<User> executor = new MongoDBQueryExecutor<>(
    database, 
    "users", 
    User.class
);

// 4. 创建 Criterion 并设置执行器（使用与 SQL 相同的 API）
Criterion<User> criterion = new StandardCriterion<>(User.class);
criterion.setQueryExecutor(executor);

// 5. 使用相同的查询 API（与 SQL 完全一致）
List<User> users = criterion
    .select()
    .from(User.class)
    .where(w -> w.eq(User::getName, "John"))
    .orderBy(o -> o.asc(User::getAge))
    .limit(0, 10)
    .findList();
```

#### 10.2 NoSQL 支持特点

- ✅ **统一 API**：SQL 和 NoSQL 使用相同的 `Criterion` API，代码无需修改
- ✅ **零依赖**：不强制依赖任何 NoSQL 驱动，按需引入
- ✅ **可扩展**：通过实现 `QueryExecutor` 接口支持任意数据库
- ✅ **流式查询**：NoSQL 数据库也支持流式查询，支持大数据量处理
- ✅ **混合使用**：可在同一应用中同时使用 SQL 和 NoSQL 数据库

#### 10.3 支持的 NoSQL 数据库

- **MongoDB** - 文档型数据库（推荐）
- **CouchDB** - 文档型数据库
- **Redis** - 键值型数据库（需要自定义实现）
- **其他**：通过实现 `QueryExecutor` 接口支持任意 NoSQL 数据库

#### 10.4 详细文档

更多 NoSQL 支持信息请参考：[NoSQL 数据库支持文档](docs/PERSISTENCE_COMPLETE_GUIDE.md#nosql-数据库支持)

## 📚 完整文档

详细的文档请参考：[Persistence完整指南](docs/PERSISTENCE_COMPLETE_GUIDE.md)

文档包含：
- 架构设计
- 核心组件详解
- 基础功能示例
- 高级功能（JOIN、子查询、窗口函数等）
- **NoSQL 数据库支持**（MongoDB、CouchDB等）
- 性能优化
- 最佳实践
- 故障排除
- API参考

## 🎯 核心组件

### PersistenceManager

持久化管理器，提供统一的持久化操作入口。

```java
// 获取默认EntityManager
EntityManager em = PersistenceManager.getDefaultManager();

// 获取指定数据源的EntityManager
EntityManager em = PersistenceManager.getManager("myDataSource");
```

### EntityManager

实体管理器，提供CRUD操作和事务管理。

```java
// CRUD操作
User user = em.save(user);
user = em.update(user);
em.delete(user);
User found = em.findById(User.class, id);

// 事务管理
EntityTransaction tx = em.beginTransaction();
try {
    em.save(user1);
    em.save(user2);
    tx.commit();
} catch (Exception e) {
    tx.rollback();
}
```

### EntityQuery

简单查询接口，适用于单表查询。

```java
EntityQuery<User> query = em.createQuery(User.class);
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .orderBy(User::getCreateTime, false)
    .limit(0, 10);
List<User> users = query.findList();
```

### Criterion（查询构建器）

强大的查询构建器，支持复杂查询。原 `QueryBuilder` 已重命名为 `Criterion`。

```java
Criterion<User> criterion = em.createCriterion(User.class);
criterion.select()
    .column(User::getId)
    .column(User::getName)
    .from("users", "u")
    .leftJoin("departments", "d")
    .on("u.department_id", "d.id")
    .where().eq("u.status", "active")
    .groupBy(User::getDepartmentId)
    .orderBy().desc(User::getCreateTime)
    .limit(0, 20);
List<User> users = criterion.findList();
```

### StreamingCriterion（流式查询）

流式查询构建器，支持大数据量处理。原 `StreamingQueryBuilder` 已重命名为 `StreamingCriterion`。

```java
Criterion<User> criterion = em.createCriterion(User.class);
StreamingCriterion<User> streamingQuery = criterion.createStreamingCriterion();

// 流式处理
streamingQuery.stream()
    .forEach(user -> processUser(user));
```

## 🔧 配置

### 数据源配置

```java
import com.kishultan.persistence.config.PersistenceDefaults;
import com.kishultan.persistence.datasource.DataSourceManager;

// 设置默认数据源名称
PersistenceDefaults.setDataSourceName("default");

// 添加数据源
DataSourceManager.addLocalDataSource("default", dataSource);

// 设置数据源类型（用于方言支持）
DataSourceManager.addDataSourceFlavor("default", "mysql");
```

### 性能监控

```java
Criterion<User> criterion = em.createCriterion(User.class);

// 执行查询后获取指标
List<User> users = criterion.findList();
// 性能指标可通过配置自动收集
```

### 查询缓存

```java
Criterion<User> criterion = em.createCriterion(User.class);

// 启用缓存（通过配置）
// 查询结果会自动缓存
List<User> users = criterion.findList();
```

## 📊 支持的功能

### 查询功能

- ✅ 简单查询（单表）
- ✅ 复杂查询（多表JOIN）
- ✅ 子查询
- ✅ 聚合函数（COUNT、SUM、AVG、MAX、MIN）
- ✅ 窗口函数（ROW_NUMBER、RANK、DENSE_RANK、LAG、LEAD等）
- ✅ CASE WHEN表达式
- ✅ 表达式函数（UPPER、LOWER、LENGTH、ABS、ROUND等）
- ✅ 日期格式化函数
- ✅ 分组查询（GROUP BY、HAVING）
- ✅ 流式查询（大数据量处理）

### 条件支持

- ✅ 比较条件（=、!=、>、>=、<、<=）
- ✅ 集合条件（IN、NOT IN）
- ✅ 字符串条件（LIKE、IS NULL、IS NOT NULL）
- ✅ 范围条件（BETWEEN、NOT BETWEEN）
- ✅ 逻辑条件（AND、OR）
- ✅ 复杂嵌套条件

### 数据库支持

**SQL 数据库：**
- ✅ MySQL
- ✅ PostgreSQL
- ✅ Oracle
- ✅ SQL Server
- ✅ H2（测试）
- ✅ 达梦数据库
- ✅ SQLite
- ✅ 其他支持JDBC的数据库

**NoSQL 数据库：**
- ✅ MongoDB（通过实现 `QueryExecutor` 接口）
- ✅ CouchDB（通过实现 `QueryExecutor` 接口）
- ✅ Redis（通过实现 `QueryExecutor` 接口）
- ✅ 其他 NoSQL 数据库（通过实现 `QueryExecutor` 接口）

**统一 API：** SQL 和 NoSQL 数据库使用相同的 `Criterion` 查询构建器 API，代码无需修改即可在不同数据库之间切换。

## 🧪 测试

```bash
mvn test
```

## 📄 许可证

Apache License 2.0 - 详见 [LICENSE](LICENSE) 文件

## 🤝 贡献

欢迎提交Issue和Pull Request！

## 📞 联系方式

- Email: team@kishultan.com
- GitHub: https://github.com/KishUltan/kishultan-persistence
- Gitee: https://gitee.com/kishultan/kishultan-persistence

## 📝 更新日志

### 1.0.0-SNAPSHOT

- ✅ 初始版本
- ✅ 完整的CRUD操作
- ✅ 强大的查询构建器（Criterion）
- ✅ 流式查询支持（StreamingCriterion）
- ✅ 性能监控
- ✅ 查询缓存
- ✅ 多数据源支持
- ✅ 聚合函数、窗口函数、表达式函数支持
- ✅ CASE WHEN 表达式支持

## 🔗 相关链接

- [完整功能指南](docs/PERSISTENCE_COMPLETE_GUIDE.md)

---

**注意**: 本项目已从 Portal 项目中提取，保持 `com.kishultan.persistence.*` 包名，可独立使用。

**重要变更**:
- `QueryBuilder` 已重命名为 `Criterion`
- `StreamingQueryBuilder` 已重命名为 `StreamingCriterion`
- 聚合函数、窗口函数、表达式函数统一通过 `Functions` 类使用
- 使用 `select().column()` 方式构建查询，更加符合SQL语法
