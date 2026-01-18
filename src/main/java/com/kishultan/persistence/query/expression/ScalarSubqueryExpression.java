package com.kishultan.persistence.query.expression;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.context.QueryBuilder;
import com.kishultan.persistence.query.clause.StandardCriterion;

import java.util.ArrayList;
import java.util.List;

/**
 * 标量子查询表达式
 * SQL: (SELECT ...)
 */
public class ScalarSubqueryExpression extends AbstractSelectExpression {
    private final Criterion<?> subquery;
    
    public ScalarSubqueryExpression(Criterion<?> subquery) {
        this.subquery = subquery;
    }
    
    @Override
    protected String getSqlWithoutAlias() {
        String subquerySql = subquery.getGeneratedSql();
        return "(" + subquerySql + ")";
    }
    
    @Override
    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        if (subquery instanceof StandardCriterion) {
            StandardCriterion<?> subqueryImpl = (StandardCriterion<?>) subquery;
            QueryBuilder queryResult = subqueryImpl.buildQuery();
            params.addAll(queryResult.getParameters());
        }
        return params;
    }
    
    public Criterion<?> getSubquery() {
        return subquery;
    }
}
