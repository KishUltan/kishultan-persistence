package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.CaseWhenClause;
import com.kishultan.persistence.query.ClauseBuilder;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.context.ClauseResult;

import java.util.ArrayList;
import java.util.List;

/**
 * CASE WHEN表达式子句实现类
 */
public class CaseWhenClauseImpl<T> extends SelectClauseImpl<T> implements CaseWhenClause<T>, ClauseBuilder<T> {
    // CASE表达式字段
    private final List<String> caseExpressions = new ArrayList<>();
    // 当前构建状态
    private StringBuilder currentCaseExpression;
    private String currentAlias;
    private boolean isSimpleCase = false;
    private boolean hasWhen = false;
    private boolean hasElse = false;
    private boolean isComplete = false;

    public CaseWhenClauseImpl(Criterion<T> criterion) {
        super(criterion);
    }

    // 构造函数 - 简单CASE
    public CaseWhenClauseImpl(Criterion<T> criterion, Columnable<T, ?> field, String alias) {
        super(criterion);
        startNewCase(alias);
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        currentCaseExpression.append("CASE ").append(qualifiedField);
        isSimpleCase = true;
    }

    // 构造函数 - 搜索CASE
    public CaseWhenClauseImpl(Criterion<T> criterion, String alias) {
        super(criterion);
        startNewCase(alias);
        currentCaseExpression.append("CASE");
        isSimpleCase = false;
    }

    // ==================== WHEN条件 ====================
    @Override
    public CaseWhenClause<T> when(Object value) {
        if (isComplete) {
            throw new IllegalStateException("CASE表达式已完成，请开始新的CASE表达式");
        }
        if (!isSimpleCase) {
            throw new IllegalStateException("字段值匹配只能在简单CASE表达式中使用");
        }
        hasWhen = true;
        currentCaseExpression.append(" WHEN ").append(formatValue(value));
        return this;
    }

    @Override
    public CaseWhenClause<T> when(String condition) {
        if (isComplete) {
            throw new IllegalStateException("CASE表达式已完成，请开始新的CASE表达式");
        }
        hasWhen = true;
        currentCaseExpression.append(" WHEN ").append(condition);
        return this;
    }

    @Override
    public CaseWhenClause<T> when(Columnable<T, Boolean> condition) {
        if (isComplete) {
            throw new IllegalStateException("CASE表达式已完成，请开始新的CASE表达式");
        }
        hasWhen = true;
        String fieldName = ColumnabledLambda.getColumnName(condition);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        currentCaseExpression.append(" WHEN ").append(qualifiedField);
        return this;
    }

    // ==================== THEN结果 ====================
    @Override
    public CaseWhenClause<T> then(Columnable<T, ?> field) {
        if (!hasWhen) {
            throw new IllegalStateException("THEN之前必须有WHEN条件");
        }
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        currentCaseExpression.append(" THEN ").append(qualifiedField);
        return this;
    }

    @Override
    public CaseWhenClause<T> then(Object value) {
        if (!hasWhen) {
            throw new IllegalStateException("THEN之前必须有WHEN条件");
        }
        currentCaseExpression.append(" THEN ").append(formatValue(value));
        return this;
    }

    @Override
    public CaseWhenClause<T> then(String value) {
        return then((Object) value);
    }

    @Override
    public CaseWhenClause<T> then(Number value) {
        return then((Object) value);
    }

    // ==================== ELSE结果 ====================
    @Override
    public CaseWhenClause<T> elseResult(Columnable<T, ?> field) {
        if (hasElse) {
            throw new IllegalStateException("CASE表达式只能有一个ELSE子句");
        }
        hasElse = true;
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        currentCaseExpression.append(" ELSE ").append(qualifiedField);
        return this;
    }

    @Override
    public CaseWhenClause<T> elseResult(Object value) {
        if (hasElse) {
            throw new IllegalStateException("CASE表达式只能有一个ELSE子句");
        }
        hasElse = true;
        currentCaseExpression.append(" ELSE ").append(formatValue(value));
        // 不在这里完成CASE表达式，让用户决定何时完成
        return this;
    }

    @Override
    public CaseWhenClause<T> elseResult(String value) {
        return elseResult((Object) value);
    }

    @Override
    public CaseWhenClause<T> elseResult(Number value) {
        return elseResult((Object) value);
    }

    // ==================== FROM子句 ====================
    /*@Override
    public FromClause<T> from() {
        completeCurrentCase();
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion);
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }
    @Override
    public FromClause<T> from(Class<T> entityClass) {
        completeCurrentCase();
        String tableName = EntityUtils.getTableName(entityClass);
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion, entityClass, tableName, tableName);
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }
    @Override
    public FromClause<T> from(String tableName) {
        completeCurrentCase();
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion, tableName, tableName);
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }
    @Override
    public FromClause<T> from(String tableName, String alias) {
        completeCurrentCase();
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion, tableName, alias);
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }*/
    // ==================== 构建子句 ====================
    @Override
    public ClauseResult buildClause() {
        completeCurrentCase();
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        if (caseExpressions.isEmpty()) {
            sql.append("*");
        } else {
            for (int i = 0; i < caseExpressions.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(caseExpressions.get(i));
            }
        }
        return new ClauseResult(sql.toString(), parameters);
    }

    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }

    @Override
    public String toFunctionSql() {
        // 确保当前CASE表达式被完成
        completeCurrentCase();
        if (caseExpressions.isEmpty()) {
            return "1";
        }
        return String.join(", ", caseExpressions);
    }

    // ==================== 辅助方法 ====================
    private void startNewCase(String alias) {
        completeCurrentCase();
        currentCaseExpression = new StringBuilder();
        currentAlias = alias;
        isSimpleCase = false;
        hasWhen = false;
        hasElse = false;
        isComplete = false;
    }

    private void completeCurrentCase() {
        if (currentCaseExpression != null && !isComplete) {
            if (!hasWhen) {
                throw new IllegalStateException("CASE表达式必须至少有一个WHEN条件");
            }
            currentCaseExpression.append(" END");
            if (currentAlias != null) {
                currentCaseExpression.append(" AS ").append(currentAlias);
            }
            caseExpressions.add(currentCaseExpression.toString());
            isComplete = true;
        }
    }

    private String getCurrentTableAlias() {
        if (criterion instanceof StandardCriterion) {
            StandardCriterion<T> qb = (StandardCriterion<T>) criterion;
            return qb.getCurrentTableAlias();
        }
        return null;
    }

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
