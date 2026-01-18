package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;

/**
 * CASE WHEN表达式子句接口
 * 提供CASE WHEN表达式功能，与SELECT、FROM等子句同级
 */
public interface CaseWhenClause<T> extends SelectClause<T>, FunctionQuery {
    // ==================== WHEN条件 ====================

    /**
     * WHEN条件 - 字段值匹配（简单CASE）
     */
    CaseWhenClause<T> when(Object value);

    /**
     * WHEN条件 - 条件表达式（搜索CASE）
     */
    CaseWhenClause<T> when(String condition);

    /**
     * WHEN条件 - Lambda表达式条件（搜索CASE）
     */
    CaseWhenClause<T> when(Columnable<T, Boolean> condition);
    // ==================== THEN结果 ====================

    /**
     * THEN结果 - 字段值
     */
    CaseWhenClause<T> then(Columnable<T, ?> field);

    /**
     * THEN结果 - 常量值
     */
    CaseWhenClause<T> then(Object value);

    /**
     * THEN结果 - 字符串值
     */
    CaseWhenClause<T> then(String value);

    /**
     * THEN结果 - 数字值
     */
    CaseWhenClause<T> then(Number value);
    // ==================== ELSE结果 ====================

    /**
     * ELSE结果 - 字段值
     */
    CaseWhenClause<T> elseResult(Columnable<T, ?> field);

    /**
     * ELSE结果 - 常量值
     */
    CaseWhenClause<T> elseResult(Object value);

    /**
     * ELSE结果 - 字符串值
     */
    CaseWhenClause<T> elseResult(String value);

    /**
     * ELSE结果 - 数字值
     */
    CaseWhenClause<T> elseResult(Number value);
    // ==================== FROM子句 ====================
//    /**
//     * 指定FROM子句
//     */
//    FromClause<T> from();
//
//    /**
//     * 指定FROM子句（实体类）
//     */
//    FromClause<T> from(Class<T> entityClass);
//
//    /**
//     * 指定FROM子句（表名）
//     */
//    FromClause<T> from(String tableName);
//
//    /**
//     * 指定FROM子句（表名+别名）
//     */
//    FromClause<T> from(String tableName, String alias);
}
