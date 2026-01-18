package com.kishultan.persistence.query.expression;

/**
 * CASE WHEN表达式
 * 表示SQL中的CASE WHEN表达式
 */
public class CaseWhenExpression extends AbstractSelectExpression {
    private final String sql;
    
    /**
     * 构造函数
     * 
     * @param sql CASE WHEN表达式的SQL字符串（不含别名）
     */
    public CaseWhenExpression(String sql) {
        this.sql = sql;
    }
    
    @Override
    protected String getSqlWithoutAlias() {
        return sql;
    }
}