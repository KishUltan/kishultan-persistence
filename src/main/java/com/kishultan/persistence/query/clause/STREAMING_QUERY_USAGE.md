# PaginatedStreamingQuerySpliterator 使用指南

## 概述

`PaginatedStreamingQuerySpliterator` 是一个通用的分页流式查询分割器，支持 SQL 和 NoSQL 数据库。它通过分页查询的方式实现流式数据读取，适合处理大数据集。

## 工作原理

1. **分页查询**: 使用 `QueryBuilder.limit(offset, pageSize)` 创建分页查询
2. **自动加载**: 当当前页数据消费完毕后，自动加载下一页
3. **资源管理**: 通过 `QueryExecutor` 自动管理数据库连接和资源

## 使用方式

### 1. 通过 StreamingCriterion 使用（推荐）

`PaginatedStreamingQuerySpliterator` 通常不直接使用，而是通过 `StreamingCriterion` 接口使用：

```java
import com.kishultan.persistence.EntityManager;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.StreamingCriterion;

import java.util.stream.Stream;

// 创建 QueryBuilder
EntityManager entityManager = PersistenceManager.getEntityManager();
        Criterion<User> criterion = entityManager.createCriterion(User.class);

// 添加查询条件
queryBuilder.

        where(w ->w.

        gt("age",18));

        // 创建流式查询构建器
        StreamingCriterion<User> streamingBuilder = criterion.createStreamingCriterion();

        // 方式1: 从偏移量0开始分页流式查询（每页1000条）
        Stream<User> stream = streamingBuilder.streamWithPagination(1000);

        // 方式2: 从指定偏移量开始分页流式查询（从第5000条开始，每页1000条）
        Stream<User> stream = streamingBuilder.streamWithPagination(1000, 5000);
```

### 2. 完整示例

#### SQL 数据库示例

```java
import com.kishultan.persistence.PersistenceManager;
import com.kishultan.persistence.EntityManager;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.StreamingCriterion;

public class PaginatedStreamingExample {
    public static void main(String[] args) {
        // 1. 获取 EntityManager
        EntityManager entityManager = PersistenceManager.getEntityManager();

        // 2. 创建 Criterion
        Criterion<User> criterion = entityManager.createCriterion(User.class);

        // 3. 添加查询条件
        criterion
                .where(w -> w
                        .gt("age", 18)
                        .eq("status", "active")
                )
                .orderBy(o -> o.desc("createdTime"));

        // 4. 创建流式查询构建器
        StreamingCriterion<User> streamingBuilder = criterion.createStreamingCriterion();

        // 5. 使用分页流式查询（每页1000条）
        try (Stream<User> stream = streamingBuilder.streamWithPagination(1000)) {
            stream.forEach(user -> {
                // 处理每个用户
                System.out.println("处理用户: " + user.getName());
                processUser(user);
            });
        }

        // 6. 从指定偏移量开始（跳过前5000条，每页1000条）
        try (Stream<User> stream = streamingBuilder.streamWithPagination(1000, 5000)) {
            stream.forEach(user -> {
                System.out.println("处理用户: " + user.getName());
                processUser(user);
            });
        }
    }

    private static void processUser(User user) {
        // 业务处理逻辑
    }
}
```

#### MongoDB 示例

```java
import com.kishultan.persistence.PersistenceManager;
import com.kishultan.persistence.DataManager;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.StreamingCriterion;

public class MongoDBPaginatedStreamingExample {
    public static void main(String[] args) {
        // 1. 获取 DataManager
        DataManager dataManager = PersistenceManager.getDataManager("mongodb");

        // 2. 创建 Criterion（与 SQL 完全一致）
        Criterion<User> criterion = dataManager.createQueryBuilder(User.class);

        // 3. 添加查询条件
        criterion
                .where(w -> w
                        .gt("age", 18)
                        .eq("status", "active")
                );

        // 4. 创建流式查询构建器
        StreamingCriterion<User> streamingBuilder = criterion.createStreamingCriterion();

        // 5. 使用分页流式查询（每页1000条）
        try (Stream<User> stream = streamingBuilder.streamWithPagination(1000)) {
            stream.forEach(user -> {
                System.out.println("处理用户: " + user.getName());
                processUser(user);
            });
        }
    }

    private static void processUser(User user) {
        // 业务处理逻辑
    }
}
```

### 3. 使用 streamForEachWithPagination 简化代码

```java
// 简化版本：直接处理，无需手动管理 Stream
streamingBuilder.streamForEachWithPagination(user -> {
    System.out.println("处理用户: " + user.getName());
    processUser(user);
}, 1000); // 每页1000条
```

### 4. 结合 Stream API 使用

```java
// 分页流式查询 + 过滤 + 转换
List<String> activeUserNames = streamingBuilder
    .streamWithPagination(1000)
    .filter(user -> "active".equals(user.getStatus()))
    .map(User::getName)
    .collect(Collectors.toList());

// 分页流式查询 + 统计
long activeUserCount = streamingBuilder
    .streamWithPagination(1000)
    .filter(user -> "active".equals(user.getStatus()))
    .count();

// 分页流式查询 + 聚合
int totalAge = streamingBuilder
    .streamWithPagination(1000)
    .mapToInt(User::getAge)
    .sum();
```

### 5. 处理大数据集

```java
// 处理百万级数据
public void processLargeDataset() {
    QueryBuilder<Order> criterion = entityManager.createQueryBuilder(Order.class);
    criterion.where(w -> w.ge("createdTime", startDate));
    
    StreamingCriterion<Order> streamingBuilder = criterion.createStreamingCriterion();
    
    // 每页5000条，避免内存溢出
    AtomicInteger processedCount = new AtomicInteger(0);
    
    try (Stream<Order> stream = streamingBuilder.streamWithPagination(5000)) {
        stream.forEach(order -> {
            processOrder(order);
            int count = processedCount.incrementAndGet();
            if (count % 10000 == 0) {
                System.out.println("已处理: " + count);
            }
        });
    }
    
    System.out.println("总共处理: " + processedCount.get());
}
```

## 与普通流式查询的区别

### 普通流式查询 (`stream()`)

```java
// 使用批次大小控制，内部使用偏移量累加
Stream<User> stream = streamingBuilder.stream(1000);
```

**特点：**
- 使用批次大小（batchSize）
- 内部自动管理偏移量
- 适合顺序处理所有数据

### 分页流式查询 (`streamWithPagination()`)

```java
// 使用页大小和偏移量控制
Stream<User> stream = streamingBuilder.streamWithPagination(1000, 5000);
```

**特点：**
- 使用页大小（pageSize）和偏移量（offset）
- 可以指定起始位置
- 适合跳过部分数据或从特定位置开始处理

## 参数说明

### `streamWithPagination(int pageSize)`

- **pageSize**: 每页的大小（记录数）
- **offset**: 默认为 0（从第一条记录开始）

### `streamWithPagination(int pageSize, int offset)`

- **pageSize**: 每页的大小（记录数）
- **offset**: 起始偏移量（跳过多少条记录）

## 性能考虑

1. **页大小选择**:
   - 太小（如 100）：查询次数多，性能差
   - 太大（如 100000）：内存占用高，可能溢出
   - 推荐：1000-5000 之间

2. **内存管理**:
   - 每页数据加载到内存后立即处理
   - 处理完一页后自动释放，加载下一页
   - 适合处理大数据集

3. **数据库连接**:
   - 每次分页查询都会执行新的 SQL/NoSQL 查询
   - 连接由 `QueryExecutor` 自动管理

## 注意事项

1. **资源管理**: 使用 try-with-resources 确保 Stream 正确关闭
2. **异常处理**: 在 `forEach` 中处理异常，避免中断整个流
3. **事务管理**: 分页查询在每次加载新页时都会执行新的查询，注意事务边界
4. **数据一致性**: 如果数据在查询过程中发生变化，可能看到重复或遗漏的数据

## 完整示例：数据迁移

```java
public void migrateData() {
    // 源数据库
    EntityManager sourceEm = PersistenceManager.getEntityManager("source");
    QueryBuilder<Order> sourceQb = sourceEm.createQueryBuilder(Order.class);
    sourceQb.where(w -> w.ge("createdTime", startDate));
    StreamingCriterion<Order> sourceStreaming = sourceQb.createStreamingCriterion();
    
    // 目标数据库
    EntityManager targetEm = PersistenceManager.getEntityManager("target");
    
    // 分页流式迁移（每页2000条）
    try (Stream<Order> stream = sourceStreaming.streamWithPagination(2000)) {
        List<Order> batch = new ArrayList<>();
        
        stream.forEach(order -> {
            batch.add(order);
            
            // 每2000条批量保存
            if (batch.size() >= 2000) {
                targetEm.saveAll(batch);
                batch.clear();
                System.out.println("已迁移: " + batch.size() + " 条");
            }
        });
        
        // 处理剩余数据
        if (!batch.isEmpty()) {
            targetEm.saveAll(batch);
        }
    }
}
```

## 总结

`PaginatedStreamingQuerySpliterator` 通过 `StreamingCriterion.streamWithPagination()` 方法使用，提供了：

1. **统一接口**: SQL 和 NoSQL 使用相同的 API
2. **内存友好**: 分页加载，避免一次性加载大量数据
3. **灵活控制**: 可以指定页大小和起始偏移量
4. **易于使用**: 与 Java Stream API 完全兼容

通过合理使用分页流式查询，可以高效处理大数据集，同时保持较低的内存占用。

