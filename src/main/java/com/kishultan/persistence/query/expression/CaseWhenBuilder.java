package com.kishultan.persistence.query.expression;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;

import java.util.ArrayList;
import java.util.List;

/**
 * CASE WHEN表达式构建器
 * 用于链式构建CASE WHEN表达式
 */
public class CaseWhenBuilder {
    private final StringBuilder sql = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();
    private boolean isSimpleCase = false;
    private boolean hasWhen = false;
    private boolean hasElse = false;
    private String tableAlias;
    
    /**
     * 开始简单CASE表达式
     * CASE field WHEN value THEN result ...
     * 
     * @param field 要比较的字段
     * @param tableAlias 表别名（可选）
     * @return 构建器自身
     */
    public static <T, R> CaseWhenBuilder simpleCase(Columnable<T, R> field, String tableAlias) {
        CaseWhenBuilder builder = new CaseWhenBuilder();
        builder.isSimpleCase = true;
        builder.tableAlias = tableAlias;
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        builder.sql.append("CASE ").append(qualifiedField);
        return builder;
    }
    
    /**
     * 开始搜索CASE表达式
     * CASE WHEN condition THEN result ...
     * 
     * @return 构建器自身
     */
    public static CaseWhenBuilder searchCase() {
        CaseWhenBuilder builder = new CaseWhenBuilder();
        builder.isSimpleCase = false;
        builder.sql.append("CASE");
        return builder;
    }
    
    /**
     * WHEN条件 - 字段值匹配（简单CASE）
     * 
     * @param value 要匹配的值
     * @return 构建器自身
     */
    public CaseWhenBuilder when(Object value) {
        if (!isSimpleCase) {
            throw new IllegalStateException("字段值匹配只能在简单CASE表达式中使用");
        }
        hasWhen = true;
        sql.append(" WHEN ").append(formatValue(value));
        return this;
    }
    
    /**
     * WHEN条件 - 条件表达式（搜索CASE）
     * 
     * @param condition 条件表达式
     * @return 构建器自身
     */
    public CaseWhenBuilder when(String condition) {
        hasWhen = true;
        sql.append(" WHEN ").append(condition);
        return this;
    }
    
    /**
     * WHEN条件 - Lambda表达式条件（搜索CASE）
     * 
     * @param condition 条件字段
     * @return 构建器自身
     */
    public <T> CaseWhenBuilder when(Columnable<T, Boolean> condition) {
        hasWhen = true;
        String fieldName = ColumnabledLambda.getColumnName(condition);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        sql.append(" WHEN ").append(qualifiedField);
        return this;
    }
    
    /**
     * THEN结果 - 字段值
     * 
     * @param field 字段
     * @return 构建器自身
     */
    public <T, R> CaseWhenBuilder then(Columnable<T, R> field) {
        if (!hasWhen) {
            throw new IllegalStateException("THEN之前必须有WHEN条件");
        }
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        sql.append(" THEN ").append(qualifiedField);
        return this;
    }
    
    /**
     * THEN结果 - 常量值
     * 
     * @param value 值
     * @return 构建器自身
     */
    public CaseWhenBuilder then(Object value) {
        if (!hasWhen) {
            throw new IllegalStateException("THEN之前必须有WHEN条件");
        }
        sql.append(" THEN ").append(formatValue(value));
        return this;
    }
    
    /**
     * THEN结果 - 字符串值
     * 
     * @param value 字符串值
     * @return 构建器自身
     */
    public CaseWhenBuilder then(String value) {
        return then((Object) value);
    }
    
    /**
     * THEN结果 - 数字值
     * 
     * @param value 数字值
     * @return 构建器自身
     */
    public CaseWhenBuilder then(Number value) {
        return then((Object) value);
    }
    
    /**
     * ELSE结果 - 字段值
     * 
     * @param field 字段
     * @return 构建器自身
     */
    public <T, R> CaseWhenBuilder elseResult(Columnable<T, R> field) {
        if (hasElse) {
            throw new IllegalStateException("CASE表达式只能有一个ELSE子句");
        }
        hasElse = true;
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        sql.append(" ELSE ").append(qualifiedField);
        return this;
    }
    
    /**
     * ELSE结果 - 常量值
     * 
     * @param value 值
     * @return 构建器自身
     */
    public CaseWhenBuilder elseResult(Object value) {
        if (hasElse) {
            throw new IllegalStateException("CASE表达式只能有一个ELSE子句");
        }
        hasElse = true;
        sql.append(" ELSE ").append(formatValue(value));
        return this;
    }
    
    /**
     * ELSE结果 - 字符串值
     * 
     * @param value 字符串值
     * @return 构建器自身
     */
    public CaseWhenBuilder elseResult(String value) {
        return elseResult((Object) value);
    }
    
    /**
     * ELSE结果 - 数字值
     * 
     * @param value 数字值
     * @return 构建器自身
     */
    public CaseWhenBuilder elseResult(Number value) {
        return elseResult((Object) value);
    }
    
    /**
     * 完成构建，返回CaseWhenExpression
     * 
     * @return CaseWhenExpression实例
     */
    public CaseWhenExpression end() {
        if (!hasWhen) {
            throw new IllegalStateException("CASE表达式必须至少有一个WHEN条件");
        }
        sql.append(" END");
        return new CaseWhenExpression(sql.toString());
    }
    
    /**
     * 格式化值
     */
    private String formatValue(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value == null) {
            return "NULL";
        } else {
            return "'" + value.toString() + "'";
        }
    }
}