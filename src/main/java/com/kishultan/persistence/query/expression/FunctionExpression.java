package com.kishultan.persistence.query.expression;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;

import java.util.ArrayList;
import java.util.List;

/**
 * 函数表达式
 * 如 YEAR(field), UPPER(field), ABS(field) 等
 */
public class FunctionExpression extends AbstractSelectExpression {
    private final String functionName;
    private final List<String> arguments = new ArrayList<>();
    private final String tableAlias;
    
    public <T, R> FunctionExpression(String functionName, Columnable<T, R> field, String tableAlias) {
        this.functionName = functionName;
        this.tableAlias = tableAlias;
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        this.arguments.add(qualifiedField);
    }
    
    public FunctionExpression(String functionName, String... args) {
        this.functionName = functionName;
        this.tableAlias = null;
        for (String arg : args) {
            this.arguments.add(arg);
        }
    }
    
    public <T, R1, R2> FunctionExpression(String functionName, Columnable<T, R1> field1, Columnable<T, R2> field2, String tableAlias) {
        this.functionName = functionName;
        this.tableAlias = tableAlias;
        String fieldName1 = ColumnabledLambda.getColumnName(field1);
        String fieldName2 = ColumnabledLambda.getColumnName(field2);
        String qualifiedField1 = tableAlias != null ? tableAlias + "." + fieldName1 : fieldName1;
        String qualifiedField2 = tableAlias != null ? tableAlias + "." + fieldName2 : fieldName2;
        this.arguments.add(qualifiedField1);
        this.arguments.add(qualifiedField2);
    }
    
    @Override
    protected String getSqlWithoutAlias() {
        StringBuilder sql = new StringBuilder(functionName + "(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(arguments.get(i));
        }
        sql.append(")");
        return sql.toString();
    }
}
