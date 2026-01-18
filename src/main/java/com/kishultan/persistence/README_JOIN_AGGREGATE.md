# JOIN_AGGREGATE 策略使用指南

## 概述

`JOIN_AGGREGATE` 策略是 `PersistenceQueryWrapper` 中用于处理复杂连接查询的分页策略。该策略经过优化，能够自动处理总数计算，无需手动设置 `joinCountSupplier`。

## 主要特性

### 1. 自动总数计算
当 `joinCountSupplier` 未设置时，系统会根据 `joinConfigurer` 自动计算总数：
- 创建新的 `TypeSafeJoinQueryBuilder` 实例
- 应用相同的连接配置
- 执行 COUNT 查询获取总数
- 自动回退到 BASIC 策略（如果自动计算失败）

### 2. 智能回退机制
- **第一优先级**：显式设置的 `joinCountSupplier`
- **第二优先级**：根据 `joinConfigurer` 自动计算
- **第三优先级**：回退到 BASIC 策略

### 3. 完整的 JOIN 支持
- 支持实体表连接
- 支持子查询表达式连接
- 支持复杂的 WHERE 条件
- 自动处理表别名和连接条件

## 使用示例

### 基本用法

```java
// 创建查询包装器
PersistenceQueryWrapper<User> wrapper = new PersistenceQueryWrapper<>(User.class, entityManager);

// 设置 JOIN_AGGREGATE 策略
wrapper.setFetchStrategy(PersistenceQueryWrapper.FetchStrategy.JOIN_AGGREGATE)
       .setJoinConfigurer(qb -> {
           // 配置连接查询
           qb.from("u")
              .leftJoin(Department.class, "d")
              .onEq(User::getDepartmentId, Department::getId)
              .where().eq(User::getStatus, "ACTIVE");
       });

// 执行分页查询（总数会自动计算）
PersistencePage<User> result = wrapper.findPage(new PersistencePageRequest(0, 10));
```

### 使用显式计数提供者

```java
wrapper.setFetchStrategy(PersistenceQueryWrapper.FetchStrategy.JOIN_AGGREGATE)
       .setJoinConfigurer(qb -> {
           // 配置连接查询
           qb.from("u")
              .leftJoin(Department.class, "d")
              .onEq(User::getDepartmentId, Department::getId);
       })
       .setJoinCountSupplier(() -> {
           // 自定义计数逻辑
           return customCountService.getUserCount();
       });
```

### 复杂连接查询

```java
wrapper.setFetchStrategy(PersistenceQueryWrapper.FetchStrategy.JOIN_AGGREGATE)
       .setJoinConfigurer(qb -> {
           // 主表
           qb.from("u")
              // 左连接部门表
              .leftJoin(Department.class, "d")
              .onEq(User::getDepartmentId, Department::getId)
              // 左连接角色表（多对多）
              .leftJoin(UserRole.class, "ur")
              .onEq(User::getId, UserRole::getUserId)
              .leftJoin(Role.class, "r")
              .onEq(UserRole::getRoleId, Role::getId)
              // 子查询连接
              .leftJoin(qb.as("sub").select(User.class).where().eq(User::getStatus, "INACTIVE"), "inactive")
              .onEq(User::getId, "inactive.id")
              // WHERE 条件
              .where()
                  .eq(User::getStatus, "ACTIVE")
                  .like(User::getName, "%admin%")
                  .eq("d.status", "ACTIVE")
                  .end();
       });
```

## 配置选项

### 调试模式
```java
// 启用调试模式（输出详细日志和错误堆栈）
wrapper.setDebugMode(true);

// 获取当前策略信息
String info = wrapper.getStrategyInfo();
System.out.println(info);
```

### 分页设置
```java
// 设置分页参数
wrapper.setPage(new PersistencePageRequest(20, 10));

// 分页参数会自动应用到查询中
```

## 性能优化建议

### 1. 合理使用连接
- 避免不必要的连接
- 使用适当的连接类型（INNER JOIN vs LEFT JOIN）
- 确保连接条件有索引支持

### 2. 计数查询优化
- 对于复杂查询，考虑使用显式的 `joinCountSupplier`
- 利用缓存减少重复计算
- 监控自动计算的性能

### 3. 错误处理
```java
try {
    PersistencePage<User> result = wrapper.findPage(pageRequest);
    // 处理结果
} catch (Exception e) {
    // 检查是否为计数计算失败
    if (e.getMessage().contains("Failed to automatically calculate total")) {
        // 使用备用策略
        wrapper.setFetchStrategy(PersistenceQueryWrapper.FetchStrategy.BASIC);
        result = wrapper.findPage(pageRequest);
    }
}
```

## 注意事项

### 1. 表别名
- 必须为所有连接的表设置别名
- 别名必须唯一
- 在 WHERE 条件中正确引用别名

### 2. 连接条件
- 确保连接条件正确
- 避免笛卡尔积
- 使用适当的操作符（=, <>, >, < 等）

### 3. 性能考虑
- 复杂连接查询可能影响性能
- 考虑使用数据库视图或物化视图
- 监控查询执行计划

## 故障排除

### 常见问题

1. **总数计算失败**
   - 检查 `joinConfigurer` 配置
   - 启用调试模式查看详细错误
   - 验证表结构和连接条件

2. **性能问题**
   - 检查数据库索引
   - 优化连接顺序
   - 考虑使用显式计数提供者

3. **内存问题**
   - 减少一次性查询的数据量
   - 使用流式处理
   - 调整批处理大小

### 调试技巧

```java
// 启用调试模式
wrapper.setDebugMode(true);

// 获取策略信息
System.out.println(wrapper.getStrategyInfo());

// 检查生成的SQL（如果支持）
// 注意：这需要 TypeSafeJoinQueryBuilder 支持 getGeneratedSql() 方法
```

## 版本历史

- **v1.0**: 基础 JOIN_AGGREGATE 策略
- **v2.0**: 添加自动总数计算
- **v3.0**: 优化错误处理和回退机制
- **v4.0**: 支持 TableExpression 和复杂子查询


