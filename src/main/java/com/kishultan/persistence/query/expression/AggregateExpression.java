package com.kishultan.persistence.query.expression;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;

/**
 * 聚合函数表达式
 * 如 COUNT(field), SUM(field), AVG(field) 等
 */
public class AggregateExpression extends AbstractSelectExpression {
    private final String functionName;
    private final Columnable<?, ?> field;
    private final String qualifiedField;
    
    public <T, R> AggregateExpression(String functionName, Columnable<T, R> field) {
        this(functionName, field, null);
    }
    
    public <T, R> AggregateExpression(String functionName, Columnable<T, R> field, String tableAlias) {
        this.functionName = functionName;
        this.field = field;
        String fieldName = ColumnabledLambda.getColumnName(field);
        this.qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
    }
    
    @Override
    protected String getSqlWithoutAlias() {
        return functionName + "(" + qualifiedField + ")";
    }
    
    public String getFunctionName() {
        return functionName;
    }
    
    public Columnable<?, ?> getField() {
        return field;
    }
}
