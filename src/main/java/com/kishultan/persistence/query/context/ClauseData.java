package com.kishultan.persistence.query.context;

import java.util.List;

/**
 * 子句数据统一接口
 * 所有子句的数据对象都实现此接口，用于返回结构化数据
 * 
 * 设计原则：
 * 1. 存储结构化数据，不存储生成的SQL字符串
 * 2. 由 QueryResultBuilder 负责将结构化数据转换为特定格式的查询
 * 3. 支持多种查询构建器（SQL、NoSQL等）复用同一套接口
 */
public interface ClauseData {
    /**
     * 获取子句类型
     */
    String getClauseType();
    
    /**
     * 获取原始字符串数据（用于兼容性）
     * @return 原始字符串数据，如果没有则返回空字符串
     */
    default String getRawString() {
        return "";
    }
    
    /**
     * 获取条件列表（用于 WHERE, HAVING 等）
     * 可以返回混合类型：ConditionInfo、GroupCondition 等
     * @return 条件列表，如果没有则返回空列表
     */
    default List<Object> getConditions() {
        return java.util.Collections.emptyList();
    }
    
    /**
     * 获取排序列表（用于 ORDER BY）
     * @return 排序列表，如果没有则返回空列表
     */
    default List<OrderInfo> getOrders() {
        return java.util.Collections.emptyList();
    }
    
    /**
     * 获取分组条件列表（用于 WHERE/HAVING 的括号分组）
     * @return 分组条件列表，如果没有则返回空列表
     */
    default List<GroupCondition> getGroups() {
        return java.util.Collections.emptyList();
    }
    
    /**
     * 获取 GROUP BY 字段列表
     * @return GROUP BY 字段列表，如果没有则返回空列表
     */
    default List<String> getGroupColumns() {
        return java.util.Collections.emptyList();
    }
    
    /**
     * 获取连接列表（用于 JOIN）
     * @return 连接列表，如果没有则返回空列表
     */
    default List<JoinInfo> getJoins() {
        return java.util.Collections.emptyList();
    }
    
    /**
     * 转换为 Map 结构化数据
     * 用于 NoSQL 查询构建器访问子句的结构化信息
     * @return 包含子句信息的 Map，键为字段名，值为对应的值
     */
    java.util.Map<String, Object> toMap();
}
