package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * WHERE子句接口
 * 支持字符串和lambda表达式两种方式
 */
public interface WhereClause<T> extends CommonClause<T>, ClauseBuilder<T> {
    /**
     * AND条件
     */
    WhereClause<T> and();

    /**
     * AND条件分组，组内条件默认or，可通过QueryCondition重新指定
     */
    WhereClause<T> and(Consumer<WhereClause<T>> andBuilder);

    /**
     * OR条件
     */
    WhereClause<T> or();

    /**
     * OR条件分组，组内条件默认and，可通过QueryCondition重新指定
     */
    WhereClause<T> or(Consumer<WhereClause<T>> orBuilder);
    // ==================== 字符串方式 ====================

    /**
     * 等于条件
     */
    WhereClause<T> eq(String column, Object value);

    /**
     * 不等于条件
     */
    WhereClause<T> ne(String column, Object value);

    /**
     * 大于条件
     */
    WhereClause<T> gt(String column, Object value);

    /**
     * 大于等于条件
     */
    WhereClause<T> ge(String column, Object value);

    /**
     * 小于条件
     */
    WhereClause<T> lt(String column, Object value);

    /**
     * 小于等于条件
     */
    WhereClause<T> le(String column, Object value);

    /**
     * LIKE条件
     */
    WhereClause<T> like(String column, String value);

    /**
     * IN条件
     */
    WhereClause<T> in(String column, Object... values);

    /**
     * IN条件（集合）
     */
    WhereClause<T> in(String column, Collection<?> values);

    /**
     * NOT IN条件
     */
    WhereClause<T> notIn(String column, Object... values);

    /**
     * NOT IN条件（集合）
     */
    WhereClause<T> notIn(String column, Collection<?> values);

    /**
     * IS NULL条件
     */
    WhereClause<T> isNull(String column);

    /**
     * IS NOT NULL条件
     */
    WhereClause<T> isNotNull(String column);

    /**
     * BETWEEN条件
     */
    WhereClause<T> between(String column, Object start, Object end);
    // ==================== Lambda方式 ====================

    /**
     * 等于条件（Lambda）
     */
    <E, R> WhereClause<T> eq(Columnable<E, R> fieldSelector, R value);

    /**
     * 不等于条件（Lambda）
     */
    <E, R> WhereClause<T> ne(Columnable<E, R> fieldSelector, R value);

    /**
     * 大于条件（Lambda）
     */
    <E, R> WhereClause<T> gt(Columnable<E, R> fieldSelector, R value);

    /**
     * 大于等于条件（Lambda）
     */
    <E, R> WhereClause<T> ge(Columnable<E, R> fieldSelector, R value);

    /**
     * 小于条件（Lambda）
     */
    <E, R> WhereClause<T> lt(Columnable<E, R> fieldSelector, R value);

    /**
     * 小于等于条件（Lambda）
     */
    <E, R> WhereClause<T> le(Columnable<E, R> fieldSelector, R value);

    /**
     * LIKE条件（Lambda）
     */
    <E, R> WhereClause<T> like(Columnable<E, R> fieldSelector, String value);

    /**
     * IN条件（Lambda）
     */
    <E, R> WhereClause<T> in(Columnable<E, R> fieldSelector, Object... values);

    /**
     * IN条件（Lambda，集合）
     */
    <E, R> WhereClause<T> in(Columnable<E, R> fieldSelector, Collection<?> values);

    /**
     * NOT IN条件（Lambda）
     */
    <E, R> WhereClause<T> notIn(Columnable<E, R> fieldSelector, Object... values);

    /**
     * NOT IN条件（Lambda，集合）
     */
    <E, R> WhereClause<T> notIn(Columnable<E, R> fieldSelector, Collection<?> values);

    /**
     * IS NULL条件（Lambda）
     */
    <E, R> WhereClause<T> isNull(Columnable<E, R> fieldSelector);

    /**
     * IS NOT NULL条件（Lambda）
     */
    <E, R> WhereClause<T> isNotNull(Columnable<E, R> fieldSelector);

    /**
     * BETWEEN条件（Lambda）
     */
    <E, R> WhereClause<T> between(Columnable<E, R> fieldSelector, R start, R end);
    // ==================== 子查询支持 ====================

    /**
     * EXISTS子查询
     */
    WhereClause<T> exists(String subQuery);

    /**
     * NOT EXISTS子查询
     */
    WhereClause<T> notExists(String subQuery);

    /**
     * IN子查询
     */
    WhereClause<T> in(String column, Criterion<?> subQuery);

    /**
     * NOT IN子查询
     */
    WhereClause<T> notIn(String column, Criterion<?> subQuery);
    
    /**
     * 获取结构化的条件列表
     * 默认实现：返回空列表
     * 子类可以重写此方法以提供结构化条件信息
     * 
     * @return 条件列表（可能为空）
     */
    /*default List<Object> getConditions() {
        return java.util.Collections.emptyList();
    }*/
}
