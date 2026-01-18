package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.AbstractClause;
import com.kishultan.persistence.query.ClauseBuilder;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.WhereClause;
import com.kishultan.persistence.query.context.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * WHERE子句实现类
 * 使用新的架构：存储条件信息，通过 buildClause() 方法生成SQL
 */
public class WhereClauseImpl<T> extends AbstractClause<T> implements WhereClause<T>, ClauseBuilder<T>, ClauseData {
    private final List<Object> conditions = new ArrayList<>();
    private boolean hasCondition = false;
    private String logicalOperator = "AND"; // 条件的逻辑操作符

    public WhereClauseImpl(StandardCriterion<T> queryBuilder) {
        super(queryBuilder);
    }

    // ==================== 逻辑操作符 ====================
    @Override
    public WhereClause<T> and() {
        logicalOperator = "AND";
        return this;
    }

    @Override
    public WhereClause<T> or() {
        logicalOperator = "OR";
        return this;
    }

    @Override
    public WhereClause<T> or(Consumer<WhereClause<T>> orBuilder) {
        if (orBuilder != null) {
            // 添加OR分组开始标记
            conditions.add(new GroupCondition("OR", GroupCondition.Type.START));
            // 使用Consumer构建OR条件
            orBuilder.accept(this);
            // 添加OR分组结束标记
            conditions.add(new GroupCondition("OR", GroupCondition.Type.END));
        }
        return this;
    }

    @Override
    public WhereClause<T> and(Consumer<WhereClause<T>> andBuilder) {
        if (andBuilder != null) {
            // 添加AND分组开始标记
            conditions.add(new GroupCondition("AND", GroupCondition.Type.START));
            logicalOperator = "OR";
            // 使用Consumer构建AND条件
            andBuilder.accept(this);
            // 添加AND分组结束标记
            conditions.add(new GroupCondition("AND", GroupCondition.Type.END));
            //恢复默认
            logicalOperator = "AND";
        }
        return this;
    }

    // ==================== 字符串方式 ====================
    @Override
    public WhereClause<T> eq(String column, Object value) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                addCondition(parts[0], parts[1], "=", value, true);
            } else {
                addCondition(column, "=", value);
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            addCondition(mainTableAlias, column, "=", value, false);
        }
        return this;
    }

    @Override
    public WhereClause<T> ne(String column, Object value) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                addCondition(parts[0], parts[1], "!=", value, true);
            } else {
                addCondition(column, "!=", value);
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            addCondition(mainTableAlias, column, "!=", value, false);
        }
        return this;
    }

    @Override
    public WhereClause<T> gt(String column, Object value) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                addCondition(parts[0], parts[1], ">", value, true);
            } else {
                addCondition(column, ">", value);
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            addCondition(mainTableAlias, column, ">", value, false);
        }
        return this;
    }

    @Override
    public WhereClause<T> ge(String column, Object value) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                addCondition(parts[0], parts[1], ">=", value, true);
            } else {
                addCondition(column, ">=", value);
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            addCondition(mainTableAlias, column, ">=", value, false);
        }
        return this;
    }

    @Override
    public WhereClause<T> lt(String column, Object value) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                addCondition(parts[0], parts[1], "<", value, true);
            } else {
                addCondition(column, "<", value);
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            addCondition(mainTableAlias, column, "<", value, false);
        }
        return this;
    }

    @Override
    public WhereClause<T> le(String column, Object value) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                addCondition(parts[0], parts[1], "<=", value, true);
            } else {
                addCondition(column, "<=", value);
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            addCondition(mainTableAlias, column, "<=", value, false);
        }
        return this;
    }

    @Override
    public WhereClause<T> like(String column, String value) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                conditions.add(new ConditionInfo(parts[0], parts[1], "LIKE", value, true, logicalOperator));
            } else {
                conditions.add(new ConditionInfo(null, column, "LIKE", value, logicalOperator));
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            conditions.add(new ConditionInfo(mainTableAlias, column, "LIKE", value, logicalOperator));
        }
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> in(String column, Object... values) {
        // 检测是否是子查询
        if (values.length == 1 && values[0] instanceof String) {
            String value = (String) values[0];
            if (isSqlSubquery(value)) {
                // 字符串形式的子查询
                addCondition(column, "IN_SUBQUERY", value);
                return this;
            }
        }
        // 检测是否包含QueryBuilder
        for (Object value : values) {
            if (value instanceof Criterion) {
                // QueryBuilder形式的子查询
                addCondition(column, "IN_QUERYBUILDER", values);
                return this;
            }
        }
        // 普通值数组
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                conditions.add(new ConditionInfo(parts[0], parts[1], "IN", values, true, logicalOperator));
            } else {
                conditions.add(new ConditionInfo(null, column, "IN", values, logicalOperator));
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            conditions.add(new ConditionInfo(null, column, "IN", values, logicalOperator));
        }
        hasCondition = true;
        logicalOperator = "AND"; // 重置为默认的AND
        return this;
    }

    @Override
    public WhereClause<T> in(String column, Collection<?> values) {
        conditions.add(new ConditionInfo(null, column, "IN", values.toArray(), logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> notIn(String column, Object... values) {
        // 检测是否是子查询
        if (values.length == 1 && values[0] instanceof String) {
            String value = (String) values[0];
            if (isSqlSubquery(value)) {
                // 字符串形式的子查询
                addCondition(column, "NOT IN_SUBQUERY", value);
                return this;
            }
        }
        // 检测是否包含QueryBuilder
        for (Object value : values) {
            if (value instanceof Criterion) {
                // QueryBuilder形式的子查询
                addCondition(column, "NOT IN_QUERYBUILDER", values);
                return this;
            }
        }
        // 普通值数组
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                conditions.add(new ConditionInfo(parts[0], parts[1], "NOT IN", values, true, logicalOperator));
            } else {
                conditions.add(new ConditionInfo(null, column, "NOT IN", values, logicalOperator));
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            conditions.add(new ConditionInfo(null, column, "NOT IN", values, logicalOperator));
        }
        hasCondition = true;
        logicalOperator = "AND"; // 重置为默认的AND
        return this;
    }

    @Override
    public WhereClause<T> notIn(String column, Collection<?> values) {
        conditions.add(new ConditionInfo(null, column, "NOT IN", values.toArray(), logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> isNull(String column) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                conditions.add(new ConditionInfo(parts[0], parts[1], "IS NULL", null, true, logicalOperator));
            } else {
                conditions.add(new ConditionInfo(null, column, "IS NULL", null, logicalOperator));
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            conditions.add(new ConditionInfo(mainTableAlias, column, "IS NULL", null, logicalOperator));
        }
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> isNotNull(String column) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                conditions.add(new ConditionInfo(parts[0], parts[1], "IS NOT NULL", null, true, logicalOperator));
            } else {
                conditions.add(new ConditionInfo(null, column, "IS NOT NULL", null, logicalOperator));
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            conditions.add(new ConditionInfo(mainTableAlias, column, "IS NOT NULL", null, logicalOperator));
        }
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> between(String column, Object start, Object end) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                conditions.add(new ConditionInfo(parts[0], parts[1], "BETWEEN", new Object[]{start, end}, true, logicalOperator));
            } else {
                conditions.add(new ConditionInfo(null, column, "BETWEEN", new Object[]{start, end}, logicalOperator));
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            conditions.add(new ConditionInfo(mainTableAlias, column, "BETWEEN", new Object[]{start, end}, logicalOperator));
        }
        hasCondition = true;
        return this;
    }

    // ==================== Lambda方式 ====================
    @Override
    public <E, R> WhereClause<T> eq(Columnable<E, R> fieldSelector, R value) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "=", value, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> ne(Columnable<E, R> fieldSelector, R value) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "!=", value, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> gt(Columnable<E, R> fieldSelector, R value) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, ">", value, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> ge(Columnable<E, R> fieldSelector, R value) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, ">=", value, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> lt(Columnable<E, R> fieldSelector, R value) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "<", value, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> le(Columnable<E, R> fieldSelector, R value) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "<=", value, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> like(Columnable<E, R> fieldSelector, String value) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "LIKE", value, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> in(Columnable<E, R> fieldSelector, Object... values) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "IN", values, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> in(Columnable<E, R> fieldSelector, Collection<?> values) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "IN", values.toArray(), logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> notIn(Columnable<E, R> fieldSelector, Object... values) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "NOT IN", values, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> notIn(Columnable<E, R> fieldSelector, Collection<?> values) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "NOT IN", values.toArray(), logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> isNull(Columnable<E, R> fieldSelector) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String mainTableAlias = getMainTableAlias();
        conditions.add(new ConditionInfo(mainTableAlias, fieldName, "IS NULL", null, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> isNotNull(Columnable<E, R> fieldSelector) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String mainTableAlias = getMainTableAlias();
        conditions.add(new ConditionInfo(mainTableAlias, fieldName, "IS NOT NULL", null, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public <E, R> WhereClause<T> between(Columnable<E, R> fieldSelector, R start, R end) {
        String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
        String tableAlias = getTableAlias(fieldSelector);
        conditions.add(new ConditionInfo(tableAlias, fieldName, "BETWEEN", new Object[]{start, end}, logicalOperator));
        hasCondition = true;
        return this;
    }

    // ==================== 子查询支持 ====================
    @Override
    public WhereClause<T> exists(String subQuery) {
        conditions.add(new ConditionInfo(null, subQuery, "EXISTS", null, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> notExists(String subQuery) {
        conditions.add(new ConditionInfo(null, subQuery, "NOT EXISTS", null, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> in(String column, Criterion<?> subQuery) {
        // 处理子查询
        conditions.add(new ConditionInfo(null, column, "IN", subQuery, logicalOperator));
        hasCondition = true;
        return this;
    }

    /**
     * IN条件 - 支持字符串形式的子查询
     */
    public WhereClause<T> in(String column, String subQuery) {
        // 处理字符串形式的子查询
        conditions.add(new ConditionInfo(null, column, "IN_SUBQUERY", subQuery, logicalOperator));
        hasCondition = true;
        return this;
    }

    @Override
    public WhereClause<T> notIn(String column, Criterion<?> subQuery) {
        // TODO: 实现子查询的SQL生成
        conditions.add(new ConditionInfo(null, column, "NOT IN", subQuery, logicalOperator));
        hasCondition = true;
        return this;
    }

    // ==================== 新架构方法 ====================
    @Override
    public ClauseResult buildClause() {
        if (conditions.isEmpty()) {
            return new ClauseResult("", new ArrayList<>());
        }
        StringBuilder sql = new StringBuilder("WHERE ");
        List<Object> parameters = new ArrayList<>();
        //String pendingBracket = null; // 待处理的括号变量
        boolean skipOperator = true;
        for (int i = 0; i < conditions.size(); i++) {
            Object element = conditions.get(i);
            // 检查是否为分组标记，只把括号定义到变量
            if (element instanceof GroupCondition) {
                GroupCondition group = (GroupCondition) element;
                if (group.isGroupStart()) {
                    // 分组开始：添加逻辑操作符 + 左括号
                    if (!skipOperator) {
                        sql.append(" ").append(group.getGroupOperator());
                        skipOperator = true;
                    }
                    sql.append(" ( ");
                } else if (group.isGroupEnd()) {
                    // 分组结束：添加右括号
                    sql.append(" ) ");
                }
                continue;
            }
            // 普通条件的处理
            if (element instanceof ConditionInfo) {
                ConditionInfo condition = (ConditionInfo) element;
                if (!skipOperator) {
                    // 使用条件中存储的逻辑操作符
                    sql.append(" ").append(condition.getLogicalOperator()).append(" ");
                }
                // 构建列引用
                String columnRef = condition.getColumn();
                if (condition.getTableName() != null) {
                    columnRef = condition.getTableName() + "." + columnRef;
                }
                // 构建条件
                if (condition.getOperator().equals("IN") || condition.getOperator().equals("NOT IN")) {
                    Object value = condition.getValue();
                    if (value instanceof Criterion) {
                        // 处理子查询
                        Criterion<?> subQuery = (Criterion<?>) value;
                        sql.append(columnRef).append(" ").append(condition.getOperator()).append(" (");
                        sql.append(subQuery.getGeneratedSql());
                        sql.append(")");
                        // 添加子查询的参数 - 需要先构建子查询
                        if (subQuery instanceof StandardCriterion) {
                            StandardCriterion<?> subQueryImpl =
                                    (StandardCriterion<?>) subQuery;
                            QueryBuilder subQueryResult = subQueryImpl.buildQuery();
                            parameters.addAll(subQueryResult.getParameters());
                        }
                    } else if (value instanceof Object[]) {
                        // 处理数组值
                        Object[] values = (Object[]) value;
                        sql.append(columnRef).append(" ").append(condition.getOperator()).append(" (");
                        for (int j = 0; j < values.length; j++) {
                            if (j > 0) sql.append(", ");
                            sql.append("?");
                            parameters.add(values[j]);
                        }
                        sql.append(")");
                    } else if (value instanceof Collection) {
                        // 处理集合值
                        Collection<?> values = (Collection<?>) value;
                        sql.append(columnRef).append(" ").append(condition.getOperator()).append(" (");
                        int j = 0;
                        for (Object val : values) {
                            if (j > 0) sql.append(", ");
                            sql.append("?");
                            parameters.add(val);
                            j++;
                        }
                        sql.append(")");
                    } else {
                        // 单个值
                        sql.append(columnRef).append(" ").append(condition.getOperator()).append(" (?)");
                        parameters.add(value);
                    }
                } else if (condition.getOperator().equals("IN_SUBQUERY")) {
                    // 处理字符串形式的子查询
                    String subQuery = (String) condition.getValue();
                    sql.append(columnRef).append(" IN (").append(subQuery).append(")");
                    // 字符串形式的子查询没有参数
                } else if (condition.getOperator().equals("IN_QUERYBUILDER")) {
                    // 处理QueryBuilder形式的子查询
                    Object[] values = (Object[]) condition.getValue();
                    for (Object value : values) {
                        if (value instanceof Criterion) {
                            Criterion<?> subQuery = (Criterion<?>) value;
                            sql.append(columnRef).append(" IN (");
                            sql.append(subQuery.getGeneratedSql());
                            sql.append(")");
                            // 添加子查询的参数 - 需要先构建子查询
                            if (subQuery instanceof StandardCriterion) {
                                StandardCriterion<?> subQueryImpl =
                                        (StandardCriterion<?>) subQuery;
                                QueryBuilder subQueryResult = subQueryImpl.buildQuery();
                                parameters.addAll(subQueryResult.getParameters());
                            }
                            break; // 只处理第一个QueryBuilder
                        }
                    }
                } else if (condition.getOperator().equals("BETWEEN") || condition.getOperator().equals("NOT BETWEEN")) {
                    Object[] values = (Object[]) condition.getValue();
                    sql.append(columnRef).append(" ").append(condition.getOperator()).append(" ? AND ?");
                    parameters.add(values[0]);
                    parameters.add(values[1]);
                } else if (condition.getOperator().equals("IS NULL") || condition.getOperator().equals("IS NOT NULL")) {
                    sql.append(columnRef).append(" ").append(condition.getOperator());
                } else {
                    sql.append(columnRef).append(" ").append(condition.getOperator()).append(" ?");
                    if (condition.getValue() != null) {
                        parameters.add(condition.getValue());
                    }
                }
            } // 关闭 if (element instanceof ConditionInfo) 块
            skipOperator = false;
        }
        return new ClauseResult(sql.toString(), parameters);
    }

    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    // ==================== ClauseData 接口实现 ====================
    
    @Override
    public String getClauseType() {
        return "WHERE";
    }
    
    /**
     * 显式重写 getConditions() 方法以解决 WhereClause 和 ClauseData 接口的冲突
     */
    @Override
    public List<Object> getConditions() {
        return conditions;
    }
    
    @Override
    public String getRawString() {
        return getClauseSql();
    }
    
    /**
     * 转换为 Map 结构化数据
     * 
     * @return 包含 WHERE 子句信息的 Map
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("clauseType", "WHERE");
        map.put("conditions", conditions);
        map.put("logicalOperator", logicalOperator);
        return map;
    }
    
    // ==================== CommonClause 接口实现 ====================
    
    @Override
    public ClauseData getClauseData() {
        return this;
    }
    // ==================== 内部方法 ====================

    /**
     * 添加条件的辅助方法
     */
    private void addCondition(String tableName, String column, String operator, Object value, boolean isAlias) {
        if (isAlias) {
            conditions.add(new ConditionInfo(tableName, column, operator, value, true, logicalOperator));
        } else {
            conditions.add(new ConditionInfo(tableName, column, operator, value, logicalOperator));
        }
        hasCondition = true;
        logicalOperator = "AND"; // 重置为默认的AND
    }

    /**
     * 添加条件的辅助方法（无表名）
     */
    private void addCondition(String column, String operator, Object value) {
        conditions.add(new ConditionInfo(null, column, operator, value, logicalOperator));
        hasCondition = true;
        logicalOperator = "AND"; // 重置为默认的AND
    }

    /**
     * 添加条件的辅助方法（包含QueryBuilder）
     */
    private void addCondition(String column, String operator, Object... values) {
        if (column.contains(".")) {
            String[] parts = column.split("\\.");
            if (parts.length == 2) {
                conditions.add(new ConditionInfo(parts[0], parts[1], operator, values, true, logicalOperator));
            } else {
                conditions.add(new ConditionInfo(null, column, operator, values, logicalOperator));
            }
        } else {
            String mainTableAlias = getMainTableAlias();
            conditions.add(new ConditionInfo(null, column, operator, values, logicalOperator));
        }
        hasCondition = true;
        logicalOperator = "AND"; // 重置为默认的AND
    }
    // 注意：getMainTableAlias 方法现在在父类 AbstractClause 中定义

    /**
     * 判断字符串是否是SQL子查询
     */
    private boolean isSqlSubquery(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String upperValue = value.trim().toUpperCase();
        // 检查是否以SELECT开头
        return upperValue.startsWith("SELECT") ||
                upperValue.startsWith("(SELECT") ||
                upperValue.contains("SELECT") && upperValue.contains("FROM");
    }
}
