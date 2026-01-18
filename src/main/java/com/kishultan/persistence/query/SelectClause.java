package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.query.expression.SelectExpression;

/**
 * SELECT子句接口
 * 提供SELECT子句功能，支持子查询和lambda表达式，遵循SQL书写习惯
 */
public interface SelectClause<T> extends CommonClause<T>, ClauseBuilder<T> {
    
    // ==================== 列选择方法 ====================
    
    /**
     * 添加列（实体字段）
     */
    SelectClause<T> column(Columnable<T, ?> field);
    
    /**
     * 添加列（实体字段，带别名）
     */
    SelectClause<T> column(Columnable<T, ?> field, String alias);
    
    /**
     * 添加列（字符串列名）
     */
    SelectClause<T> column(String column);
    
    /**
     * 添加列（字符串列名，带别名）
     */
    SelectClause<T> column(String column, String alias);
    
    /**
     * 添加表达式列（聚合函数、表达式函数等）
     */
    SelectClause<T> column(SelectExpression expression);
    
    /**
     * 添加表达式列（带别名）
     */
    SelectClause<T> column(SelectExpression expression, String alias);
    
    /**
     * 添加标量子查询列（Criterion）
     * SQL: (SELECT ...) AS alias
     * 
     * 通过方法重载实现，无需特殊方法名
     */
    SelectClause<T> column(Criterion<?> subquery, String alias);
    
    /**
     * 设置别名（用于链式调用）
     * 注意：此方法需要配合返回支持别名的对象使用
     */
    SelectClause<T> as(String alias);
    
    // ==================== FROM子句方法 ====================
    
    /**
     * 指定FROM子句
     */
    FromClause<T> from();

    /**
     * 指定FROM子句（实体类）
     */
    FromClause<T> from(Class<T> entityClass);

    /**
     * 指定FROM子句（表名）
     */
    FromClause<T> from(String tableName);

    /**
     * 指定FROM子句（表名+别名）
     */
    FromClause<T> from(String tableName, String alias);
    
    /**
     * FROM子查询（Criterion + 别名）
     * SQL: FROM (SELECT ...) AS alias
     * 
     * 通过方法重载实现，无需特殊方法名
     */
    FromClause<T> from(Criterion<?> subquery, String alias);
}
