package com.kishultan.persistence.query.expression;

/**
 * 窗口函数表达式
 * 表示SQL中的窗口函数表达式
 */
public class WindowExpression extends AbstractSelectExpression {
    private final String sql;
    
    /**
     * 构造函数
     * 
     * @param sql 窗口函数表达式的SQL字符串（不含别名）
     */
    public WindowExpression(String sql) {
        this.sql = sql;
    }
    
    @Override
    protected String getSqlWithoutAlias() {
        return sql;
    }
}