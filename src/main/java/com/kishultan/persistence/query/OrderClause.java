package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;

/**
 * ORDER BY子句接口
 * 支持字符串和lambda表达式两种方式
 */
public interface OrderClause<T> extends CommonClause<T>, ClauseBuilder<T> {
    // ==================== 字符串方式 ====================

    /**
     * 升序排序
     */
    OrderClause<T> asc(String column);

    /**
     * 降序排序
     */
    OrderClause<T> desc(String column);

    /**
     * 升序排序（指定表别名）
     */
    OrderClause<T> asc(String tableAlias, String column);

    /**
     * 降序排序（指定表别名）
     */
    OrderClause<T> desc(String tableAlias, String column);
    // ==================== Lambda方式 ====================

    /**
     * 升序排序（Lambda）
     */
    <R> OrderClause<T> asc(Columnable<T, R> fieldSelector);

    /**
     * 降序排序（Lambda）
     */
    <R> OrderClause<T> desc(Columnable<T, R> fieldSelector);

    /**
     * 升序排序（Lambda，指定表别名）
     */
    <R> OrderClause<T> asc(String tableAlias, Columnable<T, R> fieldSelector);

    /**
     * 降序排序（Lambda，指定表别名）
     */
    <R> OrderClause<T> desc(String tableAlias, Columnable<T, R> fieldSelector);
}
