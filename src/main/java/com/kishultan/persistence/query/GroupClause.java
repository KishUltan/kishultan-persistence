package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;

/**
 * GROUP BY子句接口
 * 提供GROUP BY子句功能，遵循SQL书写习惯
 */
public interface GroupClause<T> extends CommonClause<T>, ClauseBuilder<T> {
    // groupBy方法已移至FromClause，符合SQL语法顺序

    /**
     * 添加分组列（不定长字符串）
     * 
     * @param columns 列名数组
     * @return GroupClause实例，支持链式调用
     */
    GroupClause<T> column(String... columns);

    /**
     * 添加分组列（不定长Lambda表达式）
     * 
     * @param columns Lambda表达式数组
     * @param <R> 列类型
     * @return GroupClause实例，支持链式调用
     */
    <R> GroupClause<T> column(Columnable<T, R>... columns);

    /**
     * HAVING子句
     */
    HavingClause<T> having();
    // end方法已移除，GroupClause现在直接继承CommonClause，可以直接调用执行方法
}
