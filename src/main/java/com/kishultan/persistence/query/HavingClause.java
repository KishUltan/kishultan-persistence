package com.kishultan.persistence.query;

/**
 * HAVING子句接口
 * 用于GROUP BY后的过滤
 */
public interface HavingClause<T> extends CommonClause<T>, ClauseBuilder<T> {
    /**
     * AND条件
     */
    HavingClause<T> and();

    /**
     * OR条件
     */
    HavingClause<T> or();

    /**
     * 等于条件
     */
    HavingClause<T> eq(String column, Object value);

    /**
     * 不等于条件
     */
    HavingClause<T> ne(String column, Object value);

    /**
     * 大于条件
     */
    HavingClause<T> gt(String column, Object value);

    /**
     * 大于等于条件
     */
    HavingClause<T> ge(String column, Object value);

    /**
     * 小于条件
     */
    HavingClause<T> lt(String column, Object value);

    /**
     * 小于等于条件
     */
    HavingClause<T> le(String column, Object value);

    /**
     * LIKE条件
     */
    HavingClause<T> like(String column, String value);

    /**
     * IN条件
     */
    HavingClause<T> in(String column, Object... values);

    /**
     * IN条件（集合）
     */
    HavingClause<T> in(String column, java.util.Collection<?> values);

    /**
     * IS NULL条件
     */
    HavingClause<T> isNull(String column);

    /**
     * IS NOT NULL条件
     */
    HavingClause<T> isNotNull(String column);

    /**
     * BETWEEN条件
     */
    HavingClause<T> between(String column, Object start, Object end);

    /**
     * 完成HAVING子句，返回主查询构建器
     */
    Criterion<T> end();
}
