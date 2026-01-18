package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;

import java.util.function.Consumer;

/**
 * FROM子句接口
 * 指定查询的数据源
 */
public interface FromClause<T> extends CommonClause<T>, ClauseBuilder<T> {
    /**
     * 内连接
     */
    JoinClause<T> innerJoin(Class<?> entityClass);

    /**
     * 内连接
     */
    JoinClause<T> innerJoin(Class<?> entityClass, String alias);

    /**
     * 内连接（自动别名）
     */
    JoinClause<T> join(Class<?> entityClass);

    /**
     * 内连接（字符串表名）
     */
    JoinClause<T> innerJoin(String tableName, String alias);
    
    /**
     * 内连接子查询（Criterion + 别名）
     * SQL: INNER JOIN (SELECT ...) AS alias ON condition
     * 
     * 通过方法重载实现，无需特殊方法名
     */
    JoinClause<T> innerJoin(Criterion<?> subquery, String alias);

    /**
     * 左连接
     */
    JoinClause<T> leftJoin(Class<?> entityClass, String alias);

    /**
     * 左连接（自动别名）
     */
    JoinClause<T> leftJoin(Class<?> entityClass);

    /**
     * 左连接（字符串表名）
     */
    JoinClause<T> leftJoin(String tableName, String alias);
    
    /**
     * 左连接子查询（Criterion + 别名）
     * SQL: LEFT JOIN (SELECT ...) AS alias ON condition
     * 
     * 通过方法重载实现，无需特殊方法名
     */
    JoinClause<T> leftJoin(Criterion<?> subquery, String alias);

    /**
     * 右连接
     */
    JoinClause<T> rightJoin(Class<?> entityClass, String alias);
    
    /**
     * 右连接子查询（Criterion + 别名）
     * SQL: RIGHT JOIN (SELECT ...) AS alias ON condition
     * 
     * 通过方法重载实现，无需特殊方法名
     */
    JoinClause<T> rightJoin(Criterion<?> subquery, String alias);

    /**
     * 全连接
     */
    JoinClause<T> fullJoin(Class<?> entityClass, String alias);
    
    /**
     * 全连接子查询（Criterion + 别名）
     * SQL: FULL JOIN (SELECT ...) AS alias ON condition
     * 
     * 通过方法重载实现，无需特殊方法名
     */
    JoinClause<T> fullJoin(Criterion<?> subquery, String alias);

    /**
     * 交叉连接
     */
    JoinClause<T> crossJoin(Class<?> entityClass, String alias);
    
    /**
     * 交叉连接子查询（Criterion + 别名）
     * SQL: CROSS JOIN (SELECT ...) AS alias ON condition
     * 
     * 通过方法重载实现，无需特殊方法名
     */
    JoinClause<T> crossJoin(Criterion<?> subquery, String alias);

    /**
     * WHERE子句
     */
    WhereClause<T> where();

    /**
     * WHERE子句 - 支持Consumer模式
     * 提供更简洁的条件构建方式
     *
     * @param whereClause WHERE条件构建器
     * @return WhereClause实例，支持链式调用
     */
    WhereClause<T> where(Consumer<WhereClause<T>> whereClause);

    /**
     * GROUP BY子句（无参数）
     * 返回GroupClause后，可以通过column()方法添加分组字段
     */
    GroupClause<T> groupBy();

    /**
     * GROUP BY子句（直接指定分组字段）
     */
    GroupClause<T> groupBy(String... columns);

    /**
     * GROUP BY子句（Lambda表达式）
     */
    <R> GroupClause<T> groupBy(Columnable<T, R>... columns);

    /**
     * ORDER BY子句
     */
    OrderClause<T> orderBy();
    // end方法已移除，FromClause现在直接继承CommonClause，可以直接调用执行方法
}
