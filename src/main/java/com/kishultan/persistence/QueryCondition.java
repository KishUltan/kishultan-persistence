package com.kishultan.persistence;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * 查询条件接口
 * 提供各种查询条件的构建方法
 */
public interface QueryCondition<T> extends CommonQuery<T> {
    // ==================== 基础比较条件 ====================

    /**
     * 等于条件
     */
    QueryCondition eq(String column, Object value);

    /**
     * 不等于条件
     */
    QueryCondition ne(String column, Object value);

    /**
     * 大于条件
     */
    QueryCondition gt(String column, Object value);

    /**
     * 大于等于条件
     */
    QueryCondition ge(String column, Object value);

    /**
     * 小于条件
     */
    QueryCondition lt(String column, Object value);

    /**
     * 小于等于条件
     */
    QueryCondition le(String column, Object value);

    /**
     * LIKE条件
     */
    QueryCondition like(String column, String value);

    /**
     * IN条件
     */
    QueryCondition in(String column, Object... values);

    /**
     * IN条件（集合）
     */
    QueryCondition in(String column, Collection<?> values);

    /**
     * NOT IN条件
     */
    QueryCondition notIn(String column, Object... values);

    /**
     * NOT IN条件（集合）
     */
    QueryCondition notIn(String column, Collection<?> values);

    /**
     * IS NULL条件
     */
    QueryCondition isNull(String column);

    /**
     * IS NOT NULL条件
     */
    QueryCondition isNotNull(String column);

    /**
     * BETWEEN条件
     */
    QueryCondition between(String column, Object start, Object end);
    // ==================== Lambda表达式条件 ====================

    /**
     * 等于条件（Lambda）
     */
    <E, R> QueryCondition<T> eq(Columnable<E, R> fieldSelector, R value);

    /**
     * 不等于条件（Lambda）
     */
    <E, R> QueryCondition<T> ne(Columnable<E, R> fieldSelector, R value);

    /**
     * 大于条件（Lambda）
     */
    <E, R> QueryCondition<T> gt(Columnable<E, R> fieldSelector, R value);

    /**
     * 大于等于条件（Lambda）
     */
    <E, R> QueryCondition<T> ge(Columnable<E, R> fieldSelector, R value);

    /**
     * 小于条件（Lambda）
     */
    <E, R> QueryCondition<T> lt(Columnable<E, R> fieldSelector, R value);

    /**
     * 小于等于条件（Lambda）
     */
    <E, R> QueryCondition<T> le(Columnable<E, R> fieldSelector, R value);

    /**
     * LIKE条件（Lambda）
     */
    <E, R> QueryCondition<T> like(Columnable<E, R> fieldSelector, String value);

    /**
     * IN条件（Lambda）
     */
    <E, R> QueryCondition<T> in(Columnable<E, R> fieldSelector, Object... values);

    /**
     * IN条件（Lambda，集合）
     */
    <E, R> QueryCondition<T> in(Columnable<E, R> fieldSelector, Collection<?> values);

    /**
     * NOT IN条件（Lambda）
     */
    <E, R> QueryCondition<T> notIn(Columnable<E, R> fieldSelector, Object... values);

    /**
     * NOT IN条件（Lambda，集合）
     */
    <E, R> QueryCondition<T> notIn(Columnable<E, R> fieldSelector, Collection<?> values);

    /**
     * IS NULL条件（Lambda）
     */
    <E, R> QueryCondition<T> isNull(Columnable<E, R> fieldSelector);

    /**
     * IS NOT NULL条件（Lambda）
     */
    <E, R> QueryCondition<T> isNotNull(Columnable<E, R> fieldSelector);

    /**
     * BETWEEN条件（Lambda）
     */
    <E, R> QueryCondition<T> between(Columnable<E, R> fieldSelector, R start, R end);
    // ==================== 逻辑操作符 ====================

    /**
     * AND条件
     */
    QueryCondition and();

    /**
     * AND条件分组 组内条件默认or，可通过QueryCondition重新指定
     */
    QueryCondition<T> and(Consumer<QueryCondition<T>> andBuilder);

    /**
     * OR条件
     */
    QueryCondition or();

    /**
     * OR条件分组 组内条件默认and，可通过QueryCondition重新指定
     */
    QueryCondition<T> or(Consumer<QueryCondition<T>> orBuilder);
    /**
     * 左括号
     *
     * @deprecated 当前WhereClause架构不支持leftParen，请使用QueryBuilder的where子句
     */
    // QueryCondition leftParen();
    /**
     * 右括号
     *
     * @deprecated 当前WhereClause架构不支持rightParen，请使用QueryBuilder的where子句
     */
    // QueryCondition rightParen();
    // ==================== 子查询条件 ====================

    /**
     * EXISTS子查询
     */
    QueryCondition exists(String subQuery);

    /**
     * NOT EXISTS子查询
     */
    QueryCondition notExists(String subQuery);

    /**
     * IN子查询
     */
    QueryCondition in(String column, String subQuery);
    // ==================== 结束条件构建 ====================
    // end()方法已移除，QueryCondition现在直接继承CommonQuery，可以直接调用执行方法
} 