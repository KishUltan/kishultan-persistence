package com.kishultan.persistence;

import com.kishultan.persistence.query.Criterion;

import java.util.function.Consumer;

/**
 * 实体查询接口 - 新架构的简单查询入口
 * <p>
 * 提供基本的单表查询功能，支持Lambda表达式
 * 对于复杂查询，建议使用QueryBuilder
 */
public interface EntityQuery<T> extends CommonQuery<T> {
    // ==================== 基础查询条件 ====================

    /**
     * 创建查询条件
     *
     * @return 查询条件对象
     */
    QueryCondition<T> where();

    /**
     * 条件构建器模式 - 支持 Consumer 的 where 方法
     * 允许在 EntityQuery 构建完成后，通过 Consumer 动态添加 where 条件
     *
     * @param whereBuilder Consumer 函数，用于构建 where 条件
     * @return 实体查询
     */
    EntityQuery<T> where(Consumer<QueryCondition<T>> whereBuilder);
    // ==================== 字段选择 ====================

    /**
     * 设置查询字段
     *
     * @param columns 字段名数组
     * @return 实体查询
     */
    EntityQuery<T> select(String... columns);

    /**
     * 设置查询字段（Lambda表达式）
     *
     * @param columns Lambda表达式数组
     * @return 实体查询
     */
    EntityQuery<T> select(Columnable<T, ?>... columns);

    /**
     * 选择所有字段
     *
     * @return 实体查询
     */
    EntityQuery<T> selectAll();
    /**
     * 设置去重查询
     *
     * @return 实体查询
     *
     * @deprecated 当前QueryBuilder架构不支持distinct，请使用QueryBuilder.select().distinct()
     */
    // EntityQuery<T> distinct();
    // ==================== 排序 ====================
    // 排序方法已移至CommonQuery接口
    // ==================== 分页 ====================
    // 分页方法已移至CommonQuery接口
    // ==================== 分组和聚合 ====================
    // 分组方法已移至CommonQuery接口

    /**
     * 设置HAVING条件
     *
     * @return 查询条件
     */
    QueryCondition having();
    // ==================== 内部访问方法 ====================

    /**
     * 获取实体类（供内部类使用）
     */
    Class<T> getEntityClass();

    /**
     * 获取查询构建器（供内部类使用）
     */
    Criterion<T> getCriterion();
    // ==================== 查询执行 ====================
    // 执行方法已移至CommonQuery接口
    // ==================== 流式查询 ====================
    // 流式查询方法已移至CommonQuery接口
    // ==================== 分页控制 ====================
    // 分页控制方法已移至CommonQuery接口
    // ==================== 高级功能升级 ====================
    // 注意：如果需要更复杂的查询功能，请直接使用 Criterion
    // EntityQuery 设计为简单查询的完整接口，不暴露底层实现
} 