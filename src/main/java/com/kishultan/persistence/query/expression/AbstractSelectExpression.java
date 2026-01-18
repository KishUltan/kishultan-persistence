package com.kishultan.persistence.query.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * SELECT表达式抽象基类
 * 提供通用的表达式实现
 */
public abstract class AbstractSelectExpression implements SelectExpression {
    protected String alias;
    protected List<Object> parameters = new ArrayList<>();
    
    @Override
    public SelectExpression as(String alias) {
        this.alias = alias;
        return this;
    }
    
    @Override
    public String getAlias() {
        return alias;
    }
    
    @Override
    public List<Object> getParameters() {
        return new ArrayList<>(parameters);
    }
    
    /**
     * 获取SQL字符串（不含别名）
     * 
     * @return SQL字符串
     */
    protected abstract String getSqlWithoutAlias();
    
    @Override
    public String toSql() {
        String sql = getSqlWithoutAlias();
        if (alias != null && !alias.isEmpty()) {
            sql += " AS " + alias;
        }
        return sql;
    }
}
