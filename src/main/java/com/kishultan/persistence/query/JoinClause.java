package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;

/**
 * JOIN子句接口
 * 只负责设置JOIN条件，完成后返回FromClause以继续链式调用
 */
public interface JoinClause<T> extends ClauseBuilder<T> {
    /**
     * 设置JOIN条件（字符串）
     */
    FromClause<T> on(String condition);

    /**
     * 设置JOIN条件（左右列）
     */
    FromClause<T> on(String leftColumn, String rightColumn);

    /**
     * 设置JOIN的ON条件
     * 🔧 强制：参数顺序必须正确，不允许颠倒
     *
     * @param leftField  左表（主表）的关联字段，必须是主表类型T，如 Order::getClinic
     * @param rightField 右表（JOIN表）的主键字段，可以是任意类型，如 Clinic::getId
     * @return FromClause
     * @throws IllegalArgumentException 如果参数顺序错误
     */
    <R, E, F> FromClause<T> on(
            Columnable<T, R> leftField,
            Columnable<E, F> rightField
    );

    /**
     * 设置JOIN的ON条件（Lambda + String版本）
     * 🔧 强制：参数顺序必须正确
     *
     * @param leftField   左表（主表）的关联字段
     * @param rightColumn 右表（JOIN表）的列名
     * @return FromClause
     */
    <R> FromClause<T> on(Columnable<T, R> leftField, String rightColumn);

    /**
     * 设置JOIN的ON条件（String + Lambda版本）
     * 🔧 强制：参数顺序必须正确
     *
     * @param leftColumn 左表（主表）的列名
     * @param rightField 右表（JOIN表）的主键字段
     * @return FromClause
     */
    <E, F> FromClause<T> on(String leftColumn, Columnable<E, F> rightField);
}
