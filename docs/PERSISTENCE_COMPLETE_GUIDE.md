# Persistence 模块完整功能说明和使用指南

**文档版本**: 1.0  
**最后更新**: 2026年1月18日  
**维护者**: Portal Team

---

## 📋 目录

1. [概述](#概述)
2. [架构设计](#架构设计)
3. [核心组件](#核心组件)
4. [基础功能](#基础功能)
5. [高级功能](#高级功能)
6. [NoSQL 数据库支持](#nosql-数据库支持)
7. [查询构建器](#查询构建器)
8. [性能优化](#性能优化)
9. [最佳实践](#最佳实践)
10. [故障排除](#故障排除)
11. [API参考](#api参考)

---

## 概述

### 什么是 Persistence 模块

Persistence 模块是一个独立的轻量级 ORM 框架，提供了完整的对象关系映射（ORM）功能。它基于 SansOrm 库，通过门面模式封装了底层实现细节，为应用程序提供了统一、类型安全、高性能的持久化操作接口。

### 核心特性

- ✅ **统一的持久化API**: 提供一致的 CRUD 操作接口
- ✅ **类型安全**: 支持 Lambda 表达式，编译时类型检查
- ✅ **多数据源支持**: 支持多个数据源的统一管理
- ✅ **事务管理**: 完整的事务支持，线程安全
- ✅ **强大的查询能力**: 支持简单查询、复杂查询、流式查询
- ✅ **性能优化**: 内置查询缓存、性能监控、慢查询日志
- ✅ **灵活的分页**: 支持多种分页策略
- ✅ **数据库方言支持**: 支持 MySQL、H2 等多种数据库

### 设计原则

1. **门面模式**: 隐藏第三方库实现细节
2. **依赖倒置**: 依赖接口而非具体实现
3. **模块化设计**: 清晰的包结构，职责分离
4. **配置驱动**: 通过配置控制行为
5. **线程安全**: 所有核心组件都是线程安全的

---

## 架构设计

### 包结构

```
persistence/
├── PersistenceManager          # 持久化管理器（入口）
├── config/                     # 配置类
│   ├── PersistenceConfig       # 持久化配置
│   └── PersistenceDefaults     # 默认配置
├── datasource/                 # 数据源管理
│   ├── DataSourceManager      # 数据源管理器
│   └── DataSourceConfig         # 数据源配置
├── EntityManager               # 实体管理器
├── EntityManagerFactory        # 实体管理器工厂
├── EntityTransaction           # 事务接口
├── EntityQuery                 # 实体查询接口
├── QueryCondition              # 查询条件接口
├── Columnable                  # Lambda表达式接口
├── ColumnabledLambda           # Lambda工具类
├── PersistenceQueryWrapper     # 查询包装器（门面）
├── SimpleEntityQuery           # 简单查询实现
├── delegate/                   # 实现委托
│   ├── SansOrmEntityManagerFactory
│   ├── SansOrmEntityTransaction
│   └── SansOrmFactoryProvider
├── dialect/                    # 数据库方言
│   ├── DatabaseDialect
│   ├── DialectFactory
│   ├── MySQLDialect
│   └── H2Dialect
└── query/                      # 查询构建器
    ├── Criterion               # 查询构建器接口（原QueryBuilder）
    ├── SelectClause            # SELECT子句
    ├── WhereClause             # WHERE子句
    ├── JoinClause              # JOIN子句
    ├── GroupClause             # GROUP BY子句
    ├── HavingClause            # HAVING子句
    ├── OrderClause             # ORDER BY子句
    ├── StreamingCriterion      # 流式查询（原StreamingQueryBuilder）
    ├── expression/             # 表达式类
    │   ├── Functions           # 函数工厂类
    │   ├── AggregateExpression # 聚合表达式
    │   ├── WindowExpression    # 窗口表达式
    │   ├── CaseWhenExpression  # CASE WHEN表达式
    │   └── FunctionExpression  # 函数表达式
    ├── clause/                 # 实现类（原impl）
    │   ├── StandardCriterion   # 标准查询构建器实现
    │   └── StreamingCriterionImpl # 流式查询实现
    ├── cache/                  # 查询缓存
    ├── monitor/                # 性能监控
    └── config/                 # 查询配置
```

### 依赖关系

```
PersistenceManager (上层)
  ↓
EntityManager (核心接口)
  ↓
EntityManagerFactory (工厂接口)
  ↓
SansOrmEntityManagerFactory (具体实现)
```

### 数据流

```
应用程序
  ↓
PersistenceManager.getDefaultManager()
  ↓
EntityManager
  ↓
EntityQuery / Criterion
  ↓
SQL执行
  ↓
结果映射
  ↓
返回实体对象
```

---

## 核心组件

### 1. PersistenceManager

持久化管理器，提供统一的持久化操作入口。

#### 主要方法

```java
// 获取默认的EntityManager
EntityManager em = PersistenceManager.getDefaultManager();

// 获取指定数据源的EntityManager
EntityManager em = PersistenceManager.getManager("myDataSource");

// 关闭管理器
PersistenceManager.shutdown();
PersistenceManager.shutdown("myDataSource");
PersistenceManager.shutdownAll();

// 检查数据源可用性
boolean available = PersistenceManager.isDefaultDataSourceAvailable();
boolean available = PersistenceManager.isDataSourceAvailable("myDataSource");
```

#### 特性

- **线程安全**: 使用原子引用和并发缓存
- **多数据源支持**: 支持多个数据源的统一管理
- **自动缓存**: 自动缓存 EntityManagerFactory 实例
- **资源管理**: 提供完整的资源关闭方法

### 2. EntityManager

实体管理器，提供实体 CRUD 操作和事务管理。

#### 主要方法

```java
// CRUD操作
<T> T save(T entity);
<T> List<T> saveAll(List<T> entities);
<T> T update(T entity);
<T> void delete(T entity);
<T> void deleteById(Class<T> entityClass, Object id);
<T> T findById(Class<T> entityClass, Object id);

// 查询创建
<T> EntityQuery<T> createQuery(Class<T> entityClass);
<T> Criterion<T> createCriterion(Class<T> entityClass);

// 事务管理
EntityTransaction beginTransaction();
void commit();
void rollback();
boolean isTransactionActive();

// 原生SQL执行
<T> List<T> executeQuery(String sql, Class<T> resultClass, Object... params);
int executeUpdate(String sql, Object... params);
```

#### 特性

- **线程安全**: 使用 ThreadLocal 管理事务状态
- **自动事务**: 支持自动事务管理
- **连接管理**: 自动管理数据库连接
- **异常处理**: 完善的异常处理机制

### 3. EntityQuery

实体查询接口，提供简单的单表查询功能。

#### 主要方法

```java
// 条件查询
QueryCondition<T> where();
EntityQuery<T> where(Consumer<QueryCondition<T>> whereBuilder);

// 字段选择
EntityQuery<T> select(String... columns);
EntityQuery<T> select(Columnable<T, ?>... columns);
EntityQuery<T> selectAll();

// 排序
EntityQuery<T> orderBy(String column, boolean ascending);
EntityQuery<T> orderBy(Columnable<T, ?> column, boolean ascending);

// 分页
EntityQuery<T> limit(int offset, int size);

// 执行查询
List<T> findList();
T findFirst();
long count();
```

### 4. Criterion（原 QueryBuilder）

查询构建器，提供强大的查询构建能力。

#### 主要功能

- **SELECT子句**: 支持字段选择、聚合函数
- **FROM子句**: 支持表、子查询、JOIN
- **WHERE子句**: 支持复杂条件、子查询
- **GROUP BY**: 支持分组查询
- **HAVING**: 支持分组后过滤
- **ORDER BY**: 支持排序
- **窗口函数**: 支持窗口函数
- **CASE WHEN**: 支持条件表达式
- **表达式函数**: 支持自定义表达式
- **子查询**: 支持子查询
- **流式查询**: 支持大数据量流式处理

### 5. PersistenceQueryWrapper

查询包装器门面类，提供统一的查询接口。

#### 主要特性

- **多种查询策略**: BASIC、JOIN_AGGREGATE、BATCH
- **类型安全**: 支持 Lambda 表达式
- **链式调用**: 流畅的 API
- **分页支持**: 完整的分页功能

---

## 基础功能

### 1. 实体CRUD操作

#### 保存实体

```java
EntityManager em = PersistenceManager.getDefaultManager();

// 保存单个实体
User user = new User();
user.setName("John");
user.setEmail("john@example.com");
user = em.save(user);

// 批量保存
List<User> users = Arrays.asList(user1, user2, user3);
users = em.saveAll(users);
```

#### 更新实体

```java
// 更新实体
user.setName("John Updated");
user = em.update(user);
```

#### 删除实体

```java
// 删除实体
em.delete(user);

// 根据ID删除
em.deleteById(User.class, userId);
```

#### 查找实体

```java
// 根据ID查找
User user = em.findById(User.class, userId);
```

### 2. 简单查询

#### 基础查询

```java
EntityManager em = PersistenceManager.getDefaultManager();
EntityQuery<User> query = em.createQuery(User.class);

// 条件查询
query.where()
    .eq("status", "active")
    .gt("age", 18)
    .like("name", "%john%");

// 排序
query.orderBy("createTime", false);

// 分页
query.limit(0, 10);

// 执行查询
List<User> users = query.findList();
```

#### Lambda表达式查询

```java
EntityQuery<User> query = em.createQuery(User.class);

// 使用Lambda表达式（类型安全）
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .like(User::getName, "%john%");

query.orderBy(User::getCreateTime, false);
List<User> users = query.findList();
```

#### 条件构建器模式

```java
// 使用Consumer构建条件
query.where(condition -> {
    condition.eq(User::getStatus, "active")
             .and()
             .gt(User::getAge, 18)
             .or()
             .like(User::getName, "%john%");
});

List<User> users = query.findList();
```

### 3. 查询条件

#### 比较条件

```java
query.where()
    .eq("status", "active")      // 等于
    .ne("status", "inactive")    // 不等于
    .gt("age", 18)               // 大于
    .ge("age", 18)               // 大于等于
    .lt("age", 65)               // 小于
    .le("age", 65);              // 小于等于
```

#### 集合条件

```java
// IN条件
query.where().in("status", "active", "pending", "completed");
query.where().in("status", Arrays.asList("active", "pending"));

// NOT IN条件
query.where().notIn("status", "deleted", "archived");
```

#### 字符串条件

```java
query.where()
    .like("name", "%john%")      // 模糊查询
    .isNull("description")        // 空值
    .isNotNull("description");    // 非空值
```

#### 范围条件

```java
query.where()
    .between("age", 18, 65)      // 范围查询
    .notBetween("age", 0, 17);   // 不在范围内
```

#### 逻辑条件

```java
// AND条件
query.where()
    .eq("status", "active")
    .and()
    .gt("age", 18);

// OR条件
query.where()
    .eq("status", "active")
    .or()
    .eq("status", "pending");

// 复杂逻辑
query.where(condition -> {
    condition.eq("status", "active")
             .and(andCondition -> {
                 andCondition.gt("age", 18)
                            .or()
                            .lt("age", 65);
             });
});
```

### 4. 排序和分页

#### 排序

```java
// 单字段排序
query.orderBy("createTime", false);  // 降序
query.orderBy("name", true);         // 升序

// Lambda表达式排序
query.orderBy(User::getCreateTime, false);

// 多字段排序（使用Criterion）
Criterion<User> criterion = em.createCriterion(User.class);
criterion.selectAll()
  .from(User.class)
  .orderBy().desc(User::getCreateTime).asc(User::getName);
```

#### 分页

```java
// 简单分页
query.limit(0, 10);  // offset=0, size=10

// 使用PersistencePageRequest
PersistencePageRequest pageRequest = PersistencePageRequest.ofPage(1, 10);
PersistencePage<User> page = query.findPage(pageRequest);

// 获取分页信息
List<User> data = page.getData();
long total = page.getTotal();
int size = page.getSize();
int pageIndex = page.getPageIndex();
boolean hasNext = page.hasNext();
boolean hasPrevious = page.hasPrevious();
```

### 5. 聚合查询

```java
import static com.kishultan.persistence.query.expression.Functions.*;

Criterion<User> criterion = em.createCriterion(User.class);

// 计数
criterion.select()
  .column(count(User::getId), "total")
  .from(User.class);
long count = criterion.findList().get(0).getTotal();

// 求和
criterion.select()
  .column(sum(User::getAmount), "total_amount")
  .from(User.class);
Number sum = criterion.findList().get(0).getTotalAmount();

// 平均值
criterion.select()
  .column(avg(User::getAmount), "avg_amount")
  .from(User.class);
Number avg = criterion.findList().get(0).getAvgAmount();

// 最大值
criterion.select()
  .column(max(User::getAmount), "max_amount")
  .from(User.class);
Number max = criterion.findList().get(0).getMaxAmount();

// 最小值
criterion.select()
  .column(min(User::getAmount), "min_amount")
  .from(User.class);
Number min = criterion.findList().get(0).getMinAmount();

// 组合聚合
criterion.select()
  .column(count(User::getId), "total")
  .column(sum(User::getAmount), "total_amount")
  .column(avg(User::getAmount), "avg_amount")
  .from(User.class);
```

---

## 高级功能

### 1. JOIN查询

#### 基本JOIN

```java
Criterion<User> criterion = em.createCriterion(User.class);

criterion.selectAll()
  .from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId)
  .where()
    .eq("u.status", "active")
    .eq("d.status", "active");

List<User> users = criterion.findList();
```

#### 多表JOIN

```java
criterion.selectAll()
  .from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId)
  .leftJoin(Role.class, "r")
  .onEq(User::getRoleId, Role::getId)
  .where()
    .eq("u.status", "active");
```

#### JOIN类型

```java
// INNER JOIN
criterion.from(User.class, "u")
  .innerJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId);

// LEFT JOIN
criterion.from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId);

// RIGHT JOIN
criterion.from(User.class, "u")
  .rightJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId);
```

### 2. 子查询

```java
Criterion<User> criterion = em.createCriterion(User.class);

// 创建子查询（使用 EntityManager 创建新的 Criterion）
Criterion<User> subQuery = em.createCriterion(User.class);
subQuery.select()
        .column(User::getId)
        .from(User.class)
        .where().eq(User::getStatus, "active");

// 使用子查询作为 FROM 子句
Criterion<User> mainQuery = em.createCriterion(User.class);
mainQuery.selectAll()
  .from(subQuery, "active_users")
  .where().gt("active_users.age", 18);
```

### 3. 窗口函数

```java
import static com.kishultan.persistence.query.expression.Functions.*;

Criterion<Order> criterion = em.createCriterion(Order.class);

criterion.select()
  .column(Order::getId)
  .column(rowNumber().over(Order::getUserId, Order::getCreateTime), "row_num")
  .from(Order.class)
  .where().eq(Order::getStatus, "completed");
```

### 4. CASE WHEN表达式

```java
import static com.kishultan.persistence.query.expression.Functions.*;

Criterion<User> criterion = em.createCriterion(User.class);

criterion.select()
  .column(User::getId)
  .column(caseWhen(User::getStatus, "u")
    .when("active").then("正常")
    .when("inactive").then("停用")
    .elseResult("未知")
    .end(), "status_text")
  .from(User.class, "u");
```

### 5. 表达式函数

```java
import static com.kishultan.persistence.query.expression.Functions.*;

Criterion<User> criterion = em.createCriterion(User.class);

// 字符串函数示例
criterion.select()
  .column(upper(User::getFirstName), "upper_first_name")
  .column(lower(User::getLastName), "lower_last_name")
  .column(length(User::getEmail), "email_length")
  .from(User.class);

// 数学函数示例
criterion.select()
  .column(abs(User::getAmount), "abs_amount")
  .column(round(User::getAmount, 2), "rounded_amount")
  .from(User.class);
```

### 6. 分组查询

```java
import static com.kishultan.persistence.query.expression.Functions.*;

Criterion<Order> criterion = em.createCriterion(Order.class);

criterion.select()
  .column(Order::getUserId)
  .column(count(Order::getId), "order_count")
  .column(sum(Order::getAmount), "total_amount")
  .from(Order.class)
  .groupBy(Order::getUserId)
  .having()
    .gt("order_count", 10)
    .gt("total_amount", 1000);
```

### 7. 流式查询

流式查询适用于大数据量处理，避免内存溢出。

```java
Criterion<User> criterion = em.createCriterion(User.class);

// 先构建查询
criterion.selectAll()
  .from(User.class)
  .where().eq(User::getStatus, "active");

// 创建流式查询
StreamingCriterion<User> streamingQuery = criterion.createStreamingCriterion();

// 流式处理
streamingQuery.stream()
  .forEach(user -> {
      // 处理每个用户
      processUser(user);
  });

// 分批处理
streamingQuery.stream(1000)
  .forEach(user -> {
      // 处理每批数据
      processUser(user);
  });
```

---

## NoSQL 数据库支持

Kishultan Persistence 框架支持 NoSQL 数据库（如 MongoDB、CouchDB 等）。通过统一的抽象层，开发者可以使用相同的 API 查询 SQL 和 NoSQL 数据库。

### 架构设计

#### 核心组件

1. **QueryExecutor 接口** - 统一的查询执行器接口（SQL 和 NoSQL 都通过此接口）
2. **RowMapper 接口** - 统一的结果映射器接口
   - **DefaultRowMapper** - SQL 数据库结果映射器
   - **DocumentRowMapper** - NoSQL 文档数据库结果映射器（可选实现）
3. **Criterion 接口** - 统一的查询构建器接口（支持 SQL 和 NoSQL）

#### 设计原则

- **零依赖**: NoSQL 支持不强制依赖任何 NoSQL 驱动
- **可扩展**: 通过实现 `QueryExecutor` 接口支持特定数据库
- **统一API**: SQL 和 NoSQL 使用相同的查询构建器 API（`Criterion`）
- **可选功能**: NoSQL 支持是可选的，不影响现有 SQL 功能

### 使用方式

#### 1. MongoDB 集成示例

##### 1.1 添加 Maven 依赖

在项目的 `pom.xml` 中添加 MongoDB 驱动依赖：

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.10.0</version>
</dependency>
```

##### 1.2 实现自定义 MongoDB 执行器

实现 `QueryExecutor` 接口，提供 MongoDB 查询执行逻辑：

```java
package com.example.mongodb;

import com.kishultan.persistence.query.QueryExecutor;
import com.kishultan.persistence.query.RowMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB 查询执行器
 */
public class MongoDBQueryExecutor<T> implements QueryExecutor<T> {
    
    private final MongoCollection<T> collection;
    private final RowMapper<T> rowMapper;
    
    public MongoDBQueryExecutor(MongoDatabase database, String collectionName, Class<T> entityClass) {
        this.collection = database.getCollection(collectionName, entityClass);
        this.rowMapper = new DocumentRowMapper<>(entityClass);
    }
    
    @Override
    public List<T> executeQuery(String query, List<Object> parameters) {
        // query 参数是 Criterion 生成的 JSON 字符串或 MongoDB 查询对象
        // 例如：{"filter":{"name":"John"},"limit":10}
        
        // 将 JSON 字符串解析为 MongoDB 的 Bson 对象
        Bson filter = parseQueryToBson(query, parameters);
        
        // 执行查询
        FindIterable<T> iterable = collection.find(filter);
        
        // 使用 RowMapper 映射结果
        List<T> results = new ArrayList<>();
        int index = 0;
        for (T document : iterable) {
            if (rowMapper != null) {
                results.add(rowMapper.mapRow(document, index++));
            } else {
                results.add(document);
            }
        }
        
        return results;
    }
    
    @Override
    public long executeCount(String query, List<Object> parameters) {
        Bson filter = parseQueryToBson(query, parameters);
        return collection.countDocuments(filter);
    }
    
    @Override
    public int executeUpdate(String query, List<Object> parameters) {
        // MongoDB 不支持直接更新，使用特定方法
        throw new UnsupportedOperationException(
            "Use MongoDB specific update methods: updateOne, updateMany, replaceOne"
        );
    }
    
    /**
     * 解析查询字符串为 Bson 对象
     */
    private Bson parseQueryToBson(String query, List<Object> parameters) {
        if (query == null || query.isEmpty()) {
            return new Document();
        }
        
        try {
            // 解析 JSON 为 Document
            Document doc = Document.parse(query);
            Object filterObj = doc.get("filter");
            if (filterObj instanceof Bson) {
                return (Bson) filterObj;
            } else if (filterObj instanceof Document) {
                return (Document) filterObj;
            }
            return new Document();
        } catch (Exception e) {
            return new Document();
        }
    }
}
```

##### 1.3 使用 Criterion 查询 MongoDB

```java
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.clause.StandardCriterion;

public class MongoDBExample {
    
    public static void main(String[] args) {
        // 1. 创建 MongoDB 连接
        MongoDatabase database = MongoClients.create("mongodb://localhost:27017")
            .getDatabase("test");
        
        // 2. 创建 MongoDB 执行器
        MongoDBQueryExecutor<User> executor = new MongoDBQueryExecutor<>(
            database, 
            "users", 
            User.class
        );
        
        // 3. 创建 Criterion 并设置执行器
        Criterion<User> criterion = new StandardCriterion<>(User.class);
        criterion.setQueryExecutor(executor);
        
        // 4. 构建查询（使用与 SQL 相同的 API）
        List<User> users = criterion
            .select()
            .from(User.class)
            .where(w -> w.eq(User::getName, "John"))
            .orderBy(o -> o.asc(User::getAge))
            .limit(0, 10)
            .findList();
        
        System.out.println("Found " + users.size() + " users");
    }
}
```

#### 2. 统一 API 使用

SQL 和 NoSQL 使用完全相同的 `Criterion` API：

```java
// SQL 数据库查询
Criterion<User> sqlQuery = em.createCriterion(User.class);
List<User> sqlUsers = sqlQuery
    .where(w -> w.eq(User::getStatus, "active"))
    .findList();

// NoSQL 数据库查询（使用相同的 API）
Criterion<User> nosqlQuery = new StandardCriterion<>(User.class);
nosqlQuery.setQueryExecutor(mongoExecutor);
List<User> nosqlUsers = nosqlQuery
    .where(w -> w.eq(User::getStatus, "active"))
    .findList();
```

#### 3. 流式查询支持

NoSQL 数据库也支持流式查询：

```java
// 创建流式查询
Criterion<User> criterion = new StandardCriterion<>(User.class);
criterion.setQueryExecutor(mongoExecutor);

criterion.selectAll()
  .from(User.class)
  .where().eq(User::getStatus, "active");

StreamingCriterion<User> streamingQuery = criterion.createStreamingCriterion();

// 流式处理
streamingQuery.stream()
  .forEach(user -> {
      // 处理每个用户
      processUser(user);
  });
```

### 高级功能

#### 1. 混合查询支持

在同一个应用中同时使用 SQL 和 NoSQL 数据库：

```java
// SQL 查询
EntityManager em = PersistenceManager.getDefaultManager();
Criterion<User> sqlQuery = em.createCriterion(User.class);
List<User> sqlUsers = sqlQuery
    .where(w -> w.eq(User::getStatus, "active"))
    .findList();

// NoSQL 查询
Criterion<User> nosqlQuery = new StandardCriterion<>(User.class);
nosqlQuery.setQueryExecutor(mongoExecutor);
List<User> nosqlUsers = nosqlQuery
    .where(w -> w.eq(User::getStatus, "active"))
    .findList();
```

#### 2. 缓存集成

NoSQL 查询也可以使用缓存：

```java
Criterion<User> criterion = new StandardCriterion<>(User.class);
criterion.setQueryExecutor(mongoExecutor);
criterion.setCacheEnabled(true);

List<User> users = criterion
    .where(w -> w.eq(User::getName, "John"))
    .findList();
```

#### 3. 性能监控

NoSQL 查询支持性能监控：

```java
Criterion<User> criterion = new StandardCriterion<>(User.class);
criterion.setQueryExecutor(mongoExecutor);

List<User> users = criterion.findList();

// 获取性能指标
QueryMetrics metrics = criterion.getPerformanceMetrics();
System.out.println("Execution time: " + metrics.getExecutionTime() + "ms");
System.out.println("Query SQL: " + criterion.getGeneratedSql());
```

### 最佳实践

#### 1. 事务处理

- **SQL 数据库**: 使用标准的数据库事务（`EntityTransaction`）
- **NoSQL 数据库**: 使用事务 API（如 MongoDB 4.0+ 的事务支持）

```java
// MongoDB 事务示例（需要自行实现）
try (ClientSession session = mongoClient.startSession()) {
    session.startTransaction();
    try {
        // 执行多个操作
        collection1.insertOne(session, document1);
        collection2.insertOne(session, document2);
        
        session.commitTransaction();
    } catch (Exception e) {
        session.abortTransaction();
    }
}
```

#### 2. 错误处理

```java
try {
    List<User> users = criterion.findList();
} catch (UnsupportedOperationException e) {
    // 处理不支持的操作（如文档数据库的某些更新操作）
    logger.error("Operation not supported for document database", e);
} catch (Exception e) {
    // 处理其他异常
    logger.error("Query execution failed", e);
}
```

#### 3. 性能优化

- **索引**: 为常用查询字段创建索引
- **批量操作**: 使用批量插入和更新
- **投影**: 只查询需要的字段
- **限制结果**: 使用 limit 限制返回结果数量

```java
// 只查询需要的字段
criterion.select(User::getName, User::getEmail).findList();

// 限制结果数量
criterion.limit(0, 100).findList();
```

### 限制和注意事项

1. **功能差异**: 不同 NoSQL 数据库的功能特性不同，某些 SQL 功能可能无法直接映射
2. **事务支持**: 不是所有 NoSQL 数据库都支持事务
3. **查询复杂度**: 复杂的 SQL 查询可能难以转换为 NoSQL 查询
4. **性能考虑**: NoSQL 数据库的查询性能可能与 SQL 数据库不同
5. **JOIN 操作**: NoSQL 数据库通常不支持 JOIN，需要在应用层处理关联

### 支持的 NoSQL 数据库

- **MongoDB** - 文档型数据库（推荐）
- **CouchDB** - 文档型数据库
- **Redis** - 键值型数据库（需要自定义实现）
- **其他**: 通过实现 `QueryExecutor` 接口支持任意数据库

### 参考实现

以下是来自 `simple-test` 项目的完整 MongoDB 实现，可作为参考代码：

#### MongoDBQueryExecutor 实现

```java
package com.test.mongodb;

import com.kishultan.persistence.query.RowMapper;
import com.kishultan.persistence.query.executor.DocumentQueryExecutor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB 查询执行器实现
 * 用于测试 NoSQL 支持
 */
public class MongoDBQueryExecutor<T> extends DocumentQueryExecutor<T> {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBQueryExecutor.class);
    
    private final MongoCollection<Document> collection;
    
    public MongoDBQueryExecutor(MongoDatabase database, String collectionName, Class<T> entityClass) {
        super(null);
        this.collection = database.getCollection(collectionName, Document.class);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <R> List<R> executeQuery(
        String query,
        List<Object> parameters,
        Class<R> resultType,
        RowMapper<R> rowMapper
    ) {
        // query 参数是由 DocumentQueryResultBuilder.buildQuery() 生成的 JSON 字符串
        // 例如：{"filter":{"name":"John"},"limit":10}
        
        // 解析 JSON 字符串为 Bson 对象
        Bson filter = parseJsonToBson(query, parameters);
        
        // 应用限制和分页
        int skip = 0;
        int limit = 0;
        
        if (query != null && !query.isEmpty()) {
            try {
                Document queryDoc = Document.parse(query);
                if (queryDoc.containsKey("skip")) {
                    skip = queryDoc.getInteger("skip", 0);
                }
                if (queryDoc.containsKey("limit")) {
                    limit = queryDoc.getInteger("limit", 0);
                }
            } catch (Exception e) {
                // 忽略解析错误
            }
        }
        
        // 执行查询
        FindIterable<Document> iterable = collection.find(filter);
        
        // 应用分页
        if (skip > 0) {
            iterable = iterable.skip(skip);
        }
        if (limit > 0) {
            iterable = iterable.limit(limit);
        }
        
        // 使用 RowMapper 进行结果映射
        RowMapper<R> mapper = rowMapper;
        if (mapper == null) {
            mapper = new com.kishultan.persistence.query.DocumentRowMapper<>();
        }
        
        List<R> results = new ArrayList<>();
        for (Document document : iterable) {
            try {
                // 使用 DocumentRowMapper 的 mapDocument 方法
                if (mapper instanceof com.kishultan.persistence.query.DocumentRowMapper) {
                    @SuppressWarnings("unchecked")
                    com.kishultan.persistence.query.DocumentRowMapper<R> docMapper = 
                        (com.kishultan.persistence.query.DocumentRowMapper<R>) mapper;
                    R mapped = docMapper.mapDocument(document, resultType);
                    if (mapped != null) {
                        results.add(mapped);
                    }
                } else {
                    logger.warn("Custom RowMapper provided but not a DocumentRowMapper, using default mapping");
                    com.kishultan.persistence.query.DocumentRowMapper<R> docMapper = 
                        new com.kishultan.persistence.query.DocumentRowMapper<>();
                    R mapped = docMapper.mapDocument(document, resultType);
                    if (mapped != null) {
                        results.add(mapped);
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to map document", e);
            }
        }
        
        return results;
    }
    
    @Override
    public long executeCount(String query, List<Object> parameters) {
        Bson filter = parseJsonToBson(query, parameters);
        return collection.countDocuments(filter);
    }
    
    @Override
    public int executeUpdate(String query, List<Object> parameters) {
        // MongoDB 不支持直接更新，使用特定方法
        throw new UnsupportedOperationException(
            "MongoDB does not support executeUpdate. Use updateOne(), updateMany(), or replaceOne() methods."
        );
    }
    
    /**
     * 解析 JSON 字符串为 Bson 对象
     */
    private Bson parseJsonToBson(String json, List<Object> parameters) {
        if (json == null || json.isEmpty()) {
            return new Document();
        }
        
        try {
            // DocumentQueryResultBuilder 返回的 JSON 格式：
            // {"filter":{...}, "sort":{...}, "limit":10, "skip":0}
            
            // 解析 JSON 为 Document
            Document doc = Document.parse(json);
            
            // 从 Document 中提取 filter 部分
            Object filterObj = doc.get("filter");
            if (filterObj instanceof Bson) {
                return (Bson) filterObj;
            } else if (filterObj instanceof Document) {
                return (Document) filterObj;
            } else if (filterObj instanceof String) {
                try {
                    return Document.parse((String) filterObj);
                } catch (Exception e) {
                    logger.warn("Failed to parse filter string: " + filterObj);
                }
            }
            
            return new Document();
        } catch (Exception e) {
            logger.error("Failed to parse query JSON: " + json, e);
            return new Document();
        }
    }
    
    @Override
    public String getExecutorType() {
        return "mongodb";
    }
}
```

#### MongoDBQueryResultBuilder 实现

```java
package com.test.mongodb;

import com.kishultan.persistence.query.WhereClause;
import com.kishultan.persistence.query.builder.DocumentQueryResultBuilder;
import com.kishultan.persistence.query.context.QueryBuildContext;
import com.kishultan.persistence.query.context.ConditionInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MongoDB 查询结果构建器实现
 * 直接基于 ConditionInfo 构建 MongoDB 查询，无需解析 SQL 字符串
 */
public class MongoDBQueryResultBuilder extends DocumentQueryResultBuilder {
    
    @Override
    public String buildQuery(QueryBuildContext<?> context) {
        Map<String, Object> query = new HashMap<>();
        
        // WHERE（从 WhereClause 获取结构化条件）
        if (context.getWhereClause() != null) {
            query.putAll(buildMongoFilter(context.getWhereClause()));
        }
        
        // 处理 LIMIT 子句
        if (context.hasLimit()) {
            int limitValue = context.getLimitValue();
            int offsetValue = context.getOffsetValue();
            if (limitValue > 0) {
                query.put("limit", limitValue);
            }
            if (offsetValue > 0) {
                query.put("skip", offsetValue);
            }
        }
        
        return mapToJson(query);
    }
    
    /**
     * 从 WhereClause 获取结构化条件，构建 MongoDB 查询
     */
    private Map<String, Object> buildMongoFilter(WhereClause<?> whereClause) {
        List<Object> conditions = whereClause.getClauseData().getConditions();
        
        if (conditions.isEmpty()) {
            return new HashMap<>();
        }
        
        // 多个条件：构建 AND 条件
        List<Map<String, Object>> mongoConditions = new ArrayList<>();
        for (Object element : conditions) {
            if (element instanceof ConditionInfo) {
                ConditionInfo info = (ConditionInfo) element;
                Map<String, Object> mongoCondition = buildMongoCondition(info);
                if (mongoCondition != null && !mongoCondition.isEmpty()) {
                    mongoConditions.add(mongoCondition);
                }
            }
        }
        
        if (mongoConditions.isEmpty()) {
            return new HashMap<>();
        } else if (mongoConditions.size() == 1) {
            return mongoConditions.get(0);
        } else {
            // 使用 $and 组合多个条件
            Map<String, Object> result = new HashMap<>();
            result.put("$and", mongoConditions);
            return result;
        }
    }
    
    /**
     * 从 ConditionInfo 构建 MongoDB 条件
     */
    private Map<String, Object> buildMongoCondition(ConditionInfo info) {
        String field = info.getColumn();
        Object value = info.getValue();
        String operator = info.getOperator();
        
        Map<String, Object> condition = new HashMap<>();
        
        switch (operator) {
            case "=":
            case "eq":
                condition.put(field, value);
                break;
            case ">":
            case "gt":
                Map<String, Object> gtCondition = new HashMap<>();
                gtCondition.put("$gt", value);
                condition.put(field, gtCondition);
                break;
            case "<":
            case "lt":
                Map<String, Object> ltCondition = new HashMap<>();
                ltCondition.put("$lt", value);
                condition.put(field, ltCondition);
                break;
            case "LIKE":
            case "like":
                // SQL LIKE → MongoDB 正则表达式
                String pattern = value.toString().replace("%", ".*").replace("_", ".");
                Map<String, Object> regexCondition = new HashMap<>();
                regexCondition.put("$regex", pattern);
                regexCondition.put("$options", "i");
                condition.put(field, regexCondition);
                break;
            // ... 其他操作符类似处理
        }
        
        return condition;
    }
    
    @Override
    public String getBuilderType() {
        return "mongodb";
    }
}
```

**说明：**

- 这些实现来自 `simple-test` 项目，展示了如何完整实现 MongoDB 支持
- `MongoDBQueryExecutor` 继承 `DocumentQueryExecutor`，实现了查询执行逻辑
- `MongoDBQueryResultBuilder` 继承 `DocumentQueryResultBuilder`，将 `ConditionInfo` 转换为 MongoDB 查询
- 所有代码都是可工作的示例，可以直接使用或作为参考进行扩展

完整代码请参考：[simple-test 项目](https://github.com/KishUltan/kishultan-persistence/tree/main/portlets/simple-test)

### 迁移指南

#### 从 SQL 到 NoSQL

1. **评估需求**: 确认 NoSQL 数据库是否适合您的应用场景
2. **数据建模**: 重新设计数据模型以适应 NoSQL 的特点
3. **查询转换**: 转换 SQL 查询为 NoSQL 查询（通过自定义 `QueryExecutor`）
4. **测试**: 充分测试功能正确性和性能
5. **监控**: 监控 NoSQL 数据库的性能和资源使用

---

## 查询构建器

### Criterion（原 QueryBuilder） 完整示例

```java
EntityManager em = PersistenceManager.getDefaultManager();
Criterion<User> criterion = em.createCriterion(User.class);

// 复杂查询
List<User> users = qb
  .select(User::getId, User::getName, User::getEmail)
  .from(User.class, "u")
  .leftJoin(Department.class, "d")
  .onEq(User::getDepartmentId, Department::getId)
  .where(where -> {
      where.eq("u.status", "active")
           .and()
           .gt("u.age", 18)
           .or(orCondition -> {
               orCondition.like("u.name", "%admin%")
                         .or()
                         .like("u.email", "%admin%");
           });
  })
  .groupBy(User::getDepartmentId)
  .having(having -> {
      having.gt("COUNT(u.id)", 10);
  })
  .orderBy().desc(User::getCreateTime).asc(User::getName)
  .limit(0, 20)
  .findList();
```

### PersistenceQueryWrapper 使用

```java
EntityManager em = PersistenceManager.getDefaultManager();
PersistenceQueryWrapper<User> wrapper = 
    new PersistenceQueryWrapper<>(User.class, em);

// 基础查询
List<User> users = wrapper
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18)
    .like(User::getName, "%john%")
    .orderBy(User::getCreateTime, false)
    .findList();

// 分页查询
PersistencePageRequest pageRequest = PersistencePageRequest.ofPage(1, 10);
PersistencePage<User> page = wrapper
    .eq(User::getStatus, "active")
    .findPage(pageRequest);

// 复杂查询（使用Criterion）
wrapper.setFetchStrategy(PersistenceQueryWrapper.FetchStrategy.QUERY_BUILDER)
       .setQueryConfigurer(qb -> {
           criterion.selectAll()
             .from(User.class, "u")
             .leftJoin(Department.class, "d")
             .onEq(User::getDepartmentId, Department::getId)
             .where().eq("u.status", "active");
       });

List<User> users = wrapper.findList();
```

---

## 性能优化

### 1. 查询缓存

```java
Criterion<User> criterion = em.createCriterion(User.class);

// 获取查询缓存
QueryCache cache = criterion.getQueryCache();

// 启用缓存
CacheConfig cacheConfig = new CacheConfig();
cacheConfig.setEnabled(true);
cacheConfig.setStrategy(CacheStrategy.LRU);
cacheConfig.setMaxSize(1000);
cacheConfig.setTtl(3600); // 1小时

// 使用缓存
List<User> users = criterion.selectAll()
                     .from(User.class)
                     .where().eq(User::getStatus, "active")
                     .findList(); // 结果会被缓存
```

### 2. 性能监控

```java
Criterion<User> criterion = em.createCriterion(User.class);

// 获取性能监控器
QueryPerformanceMonitor monitor = criterion.getPerformanceMonitor();

// 执行查询
List<User> users = criterion.findList();

// 获取性能指标
QueryMetrics metrics = criterion.getPerformanceMetrics();
long executionTime = metrics.getExecutionTime();
long rowCount = metrics.getRowCount();
String sql = metrics.getSql();

// 获取统计信息
QueryStatistics stats = monitor.getStatistics();
long totalQueries = stats.getTotalQueries();
long slowQueries = stats.getSlowQueries();
double avgExecutionTime = stats.getAverageExecutionTime();
```

### 3. 慢查询日志

```java
// 配置慢查询日志
PersistenceConfig config = PersistenceConfig.getDevelopmentConfig();
config.setSlowQueryLogging(true);
config.setSlowQueryThreshold(500); // 500ms

// 慢查询会自动记录到日志
```

### 4. 批量操作

```java
// 批量保存
List<User> users = Arrays.asList(user1, user2, user3, ...);
users = em.saveAll(users); // 比循环save更高效

// 批量更新
users = em.updateAll(users);
```

### 5. 流式处理

```java
// 大数据量流式处理
Criterion<User> criterion = em.createCriterion(User.class);
StreamingCriterion<User> streamingQuery = criterion.createStreamingCriterion();

streamingQuery.stream()
              .forEach(user -> {
                  // 处理每个用户，避免一次性加载所有数据
                  processUser(user);
              });
```

---

## 最佳实践

### 1. 实体类设计

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "email")
    private String email;
    
    // getters and setters
}
```

### 2. 查询构建

```java
// ✅ 推荐：使用Lambda表达式，类型安全
EntityQuery<User> query = em.createQuery(User.class);
query.where()
    .eq(User::getStatus, "active")
    .gt(User::getAge, 18);

// ❌ 不推荐：使用字符串，容易出错
query.where()
    .eq("status", "active")
    .gt("age", 18);
```

### 3. 事务管理

```java
// ✅ 推荐：使用try-with-resources或显式管理
EntityTransaction tx = em.beginTransaction();
try {
    em.save(user1);
    em.save(user2);
    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
}

// ✅ 推荐：使用自动事务（简单操作）
User user = em.save(user); // 自动提交
```

### 4. 异常处理

```java
try {
    User user = em.findById(User.class, userId);
    if (user == null) {
        throw new EntityNotFoundException("User not found: " + userId);
    }
    return user;
} catch (Exception e) {
    logger.error("Failed to find user: " + userId, e);
    throw new PersistenceException("Database error", e);
}
```

### 5. 性能优化

```java
// ✅ 推荐：使用批量操作
List<User> users = em.saveAll(userList);

// ❌ 不推荐：循环单个保存
for (User user : userList) {
    em.save(user); // 多次数据库交互
}

// ✅ 推荐：使用分页查询
PersistencePageRequest pageRequest = PersistencePageRequest.ofPage(1, 20);
PersistencePage<User> page = query.findPage(pageRequest);

// ❌ 不推荐：一次性查询所有数据
List<User> allUsers = query.findList(); // 可能内存溢出
```

---

## 故障排除

### 常见问题

#### 1. 连接问题

```java
// 检查数据源配置
boolean available = PersistenceManager.isDefaultDataSourceAvailable();
if (!available) {
    // 数据源未配置
}

// 检查数据源名称
String dataSourceName = PersistenceDefaults.getDataSourceName();
```

#### 2. 事务问题

```java
// 检查事务状态
boolean active = em.isTransactionActive();
if (!active) {
    // 没有活动的事务
}

// 检查事务是否已提交
EntityTransaction tx = em.beginTransaction();
// ... 操作
if (tx.isActive()) {
    tx.commit();
}
```

#### 3. 查询问题

```java
// 获取生成的SQL（调试用）
Criterion<User> criterion = em.createCriterion(User.class);
String sql = criterion.selectAll()
               .from(User.class)
               .where().eq(User::getStatus, "active")
               .getGeneratedSql();
logger.debug("Generated SQL: {}", sql);
```

#### 4. 性能问题

```java
// 启用性能监控
QueryPerformanceMonitor monitor = criterion.getPerformanceMonitor();

// 检查慢查询
QueryStatistics stats = monitor.getStatistics();
List<SlowQueryInfo> slowQueries = stats.getSlowQueries();
for (SlowQueryInfo info : slowQueries) {
    logger.warn("Slow query: {}ms - {}", 
                info.getExecutionTime(), 
                info.getSql());
}
```

---

## API参考

### PersistenceManager

| 方法 | 说明 |
|------|------|
| `getDefaultManager()` | 获取默认EntityManager |
| `getManager(String dataSourceName)` | 获取指定数据源的EntityManager |
| `shutdown()` | 关闭默认管理器 |
| `shutdown(String dataSourceName)` | 关闭指定数据源的管理器 |
| `shutdownAll()` | 关闭所有管理器 |
| `isDefaultDataSourceAvailable()` | 检查默认数据源是否可用 |
| `isDataSourceAvailable(String name)` | 检查指定数据源是否可用 |

### EntityManager

| 方法 | 说明 |
|------|------|
| `save(T entity)` | 保存实体 |
| `saveAll(List<T> entities)` | 批量保存 |
| `update(T entity)` | 更新实体 |
| `delete(T entity)` | 删除实体 |
| `deleteById(Class<T> clazz, Object id)` | 根据ID删除 |
| `findById(Class<T> clazz, Object id)` | 根据ID查找 |
| `createQuery(Class<T> clazz)` | 创建查询 |
| `createCriterion(Class<T> clazz)` | 创建查询构建器（Criterion） |
| `beginTransaction()` | 开始事务 |
| `executeQuery(String sql, Class<T> clazz, Object... params)` | 执行查询 |
| `executeUpdate(String sql, Object... params)` | 执行更新 |

### EntityQuery

| 方法 | 说明 |
|------|------|
| `where()` | 创建查询条件 |
| `where(Consumer<QueryCondition<T>>)` | 条件构建器 |
| `select(String... columns)` | 选择字段 |
| `select(Columnable<T, ?>... columns)` | 选择字段（Lambda） |
| `selectAll()` | 选择所有字段 |
| `orderBy(String column, boolean ascending)` | 排序 |
| `limit(int offset, int size)` | 分页 |
| `findList()` | 查询列表 |
| `findFirst()` | 查询第一条 |
| `count()` | 计数 |

### Criterion（原 QueryBuilder）

| 方法 | 说明 |
|------|------|
| `select()` | SELECT子句 |
| `from(Class<T> clazz)` | FROM子句 |
| `where(Consumer<WhereClause<T>>)` | WHERE子句 |
| `leftJoin(Class<?> clazz, String alias)` | LEFT JOIN |
| `innerJoin(Class<?> clazz, String alias)` | INNER JOIN |
| `groupBy(Columnable<T, ?> column)` | GROUP BY |
| `having(Consumer<HavingClause<T>>)` | HAVING子句 |
| `orderBy()` | ORDER BY子句 |
| `column(SelectExpression)` | 添加表达式列（聚合、窗口、CASE WHEN等） |
| `column(SelectExpression, String alias)` | 添加表达式列（带别名） |
| `limit(int offset, int size)` | 分页 |
| `findList()` | 查询列表 |
| `findFirst()` | 查询第一条 |
| `count()` | 计数 |
| `getGeneratedSql()` | 获取生成的SQL |

---

## 总结

Persistence 模块提供了完整、强大、易用的持久化功能：

- ✅ **完整的CRUD操作**: 支持实体的保存、更新、删除、查询
- ✅ **强大的查询能力**: 从简单查询到复杂JOIN查询
- ✅ **类型安全**: Lambda表达式支持，编译时类型检查
- ✅ **性能优化**: 查询缓存、性能监控、慢查询日志
- ✅ **灵活的分页**: 多种分页策略，适应不同场景
- ✅ **流式处理**: 支持大数据量流式处理
- ✅ **事务管理**: 完整的事务支持
- ✅ **多数据源**: 支持多个数据源的统一管理

通过本指南，您应该能够充分利用 Persistence 模块的强大功能，构建高效、可靠的持久化应用。

---

**文档维护**: 如有问题或建议，请联系 Portal Team

