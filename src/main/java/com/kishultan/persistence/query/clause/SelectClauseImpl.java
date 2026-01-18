package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.*;
import com.kishultan.persistence.query.context.ClauseResult;
import com.kishultan.persistence.query.context.ClauseData;
import com.kishultan.persistence.query.expression.DateFormatExpression;
import com.kishultan.persistence.query.expression.SelectExpression;
import com.kishultan.persistence.query.expression.ScalarSubqueryExpression;
import com.kishultan.persistence.query.utils.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * SELECT子句实现类
 */
public class SelectClauseImpl<T> extends AbstractClause<T> implements SelectClause<T>, ClauseBuilder<T>, ClauseData {
    private boolean selectAll = false;
    private List<String> selectedFields = new ArrayList<>();
    private List<SelectColumn> columns = new ArrayList<>();
    
    /**
     * SELECT列信息
     */
    private static class SelectColumn {
        String sql;
        String alias;
        List<Object> parameters;
        
        SelectColumn(String sql, String alias, List<Object> parameters) {
            this.sql = sql;
            this.alias = alias;
            this.parameters = parameters != null ? new ArrayList<>(parameters) : new ArrayList<>();
        }
        
        String toSql() {
            if (alias != null && !alias.isEmpty()) {
                return sql + " AS " + alias;
            }
            return sql;
        }
    }

    public SelectClauseImpl(Criterion<T> criterion) {
        super(criterion);
    }

    public SelectClauseImpl(Criterion<T> criterion, boolean selectAll) {
        super(criterion);
        this.selectAll = selectAll;
    }
    // ==================== SELECT字段 ====================

    /**
     * 添加字段到SELECT子句
     */
    public void addField(String field) {
        selectedFields.add(field);
    }
    
    // ==================== column() 方法实现 ====================
    
    @Override
    public SelectClause<T> column(Columnable<T, ?> field) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        columns.add(new SelectColumn(qualifiedField, null, null));
        return this;
    }
    
    @Override
    public SelectClause<T> column(Columnable<T, ?> field, String alias) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String currentTableAlias = getCurrentTableAlias();
        String qualifiedField = currentTableAlias != null ? currentTableAlias + "." + fieldName : fieldName;
        columns.add(new SelectColumn(qualifiedField, alias, null));
        return this;
    }
    
    @Override
    public SelectClause<T> column(String column) {
        columns.add(new SelectColumn(column, null, null));
        return this;
    }
    
    @Override
    public SelectClause<T> column(String column, String alias) {
        columns.add(new SelectColumn(column, alias, null));
        return this;
    }
    
    @Override
    public SelectClause<T> column(SelectExpression expression) {
        // 如果expression是DateFormatExpression且没有设置queryBuilder，则设置它
        if (expression instanceof DateFormatExpression) {
            DateFormatExpression dateFormatExpr =
                (DateFormatExpression) expression;
            // 如果DateFormatExpression没有criterion，则设置当前的criterion
            if (dateFormatExpr.getCriterion() == null) {
                dateFormatExpr = new DateFormatExpression(
                    dateFormatExpr.getField(),
                    dateFormatExpr.getFormat(),
                    dateFormatExpr.getTableAlias(),
                    dateFormatExpr.getDialect(),
                        criterion
                );
                expression = dateFormatExpr;
            }
        }
        String sql = expression.toSql();
        List<Object> params = expression.getParameters();
        columns.add(new SelectColumn(sql, null, params));
        return this;
    }
    
    @Override
    public SelectClause<T> column(SelectExpression expression, String alias) {
        // 如果expression是DateFormatExpression且没有设置queryBuilder，则设置它
        if (expression instanceof DateFormatExpression) {
            DateFormatExpression dateFormatExpr =
                (DateFormatExpression) expression;
            // 如果DateFormatExpression没有criterion，则设置当前的criterion
            if (dateFormatExpr.getCriterion() == null) {
                dateFormatExpr = new DateFormatExpression(
                    dateFormatExpr.getField(),
                    dateFormatExpr.getFormat(),
                    dateFormatExpr.getTableAlias(),
                    dateFormatExpr.getDialect(),
                        criterion
                );
                expression = dateFormatExpr;
            }
        }
        String sql = expression.toSql();
        // 如果expression已经有别名，需要去掉，使用新的别名
        String sqlWithoutAlias = sql;
        if (expression.getAlias() != null) {
            sqlWithoutAlias = sql.substring(0, sql.lastIndexOf(" AS " + expression.getAlias()));
        }
        List<Object> params = expression.getParameters();
        columns.add(new SelectColumn(sqlWithoutAlias, alias, params));
        return this;
    }
    
    @Override
    public SelectClause<T> column(Criterion<?> subquery, String alias) {
        ScalarSubqueryExpression scalarSubquery = new ScalarSubqueryExpression(subquery);
        // 获取不带别名的SQL（标量子查询本身不包含别名）
        String subquerySql = subquery.getGeneratedSql();
        String sql = "(" + subquerySql + ")";
        List<Object> params = scalarSubquery.getParameters();
        columns.add(new SelectColumn(sql, alias, params));
        return this;
    }
    
    @Override
    public SelectClause<T> as(String alias) {
        // 为最后一个列设置别名
        if (!columns.isEmpty()) {
            SelectColumn lastColumn = columns.get(columns.size() - 1);
            if (lastColumn.alias == null) {
                columns.set(columns.size() - 1, new SelectColumn(lastColumn.sql, alias, lastColumn.parameters));
            }
        }
        return this;
    }
    
    /**
     * 获取当前表别名
     */
    private String getCurrentTableAlias() {
        if (criterion instanceof StandardCriterion) {
            StandardCriterion<T> qb = (StandardCriterion<T>) criterion;
            return qb.getCurrentTableAlias();
        }
        return null;
    }

    // ==================== FROM子句 ====================
    @Override
    public FromClause<T> from() {
        // 获取实体类信息
        Class<T> entityClass = null;
        if (criterion instanceof StandardCriterion) {
            entityClass = ((StandardCriterion<T>) criterion).getEntityClass();
        }
        
        // 创建FromClause并设置表名
        String tableName = null;
        if (entityClass != null) {
            tableName = EntityUtils.getTableName(entityClass);
        }
        
        FromClauseImpl<T> fromClause;
        if (entityClass != null && tableName != null) {
            fromClause = new FromClauseImpl<>(criterion, entityClass, tableName, tableName);
        } else {
            fromClause = new FromClauseImpl<>(criterion);
        }
        
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }

    @Override
    public FromClause<T> from(Class<T> entityClass) {
        String tableName = EntityUtils.getTableName(entityClass);
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion, entityClass, tableName, tableName);
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }

    @Override
    public FromClause<T> from(String tableName) {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion, tableName, tableName);
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }

    @Override
    public FromClause<T> from(String tableName, String alias) {
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion, tableName, alias);
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }

    @Override
    public FromClause<T> from(Criterion<?> subquery, String alias) {
        // 获取子查询SQL，但不立即合并参数
        String subquerySql = subquery.getGeneratedSql();
        // 创建FROM子句，并保存子查询引用以便后续动态收集参数
        FromClauseImpl<T> fromClause = new FromClauseImpl<>(criterion, "(" + subquerySql + ")", alias);
        // 如果是QueryBuilderImpl，保存子查询引用以便后续参数收集
        if (criterion instanceof StandardCriterion && subquery instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setSubquery((StandardCriterion<?>) subquery);
        }
        if (criterion instanceof StandardCriterion) {
            ((StandardCriterion<T>) criterion).setFromClause(fromClause);
        }
        return fromClause;
    }

    // ==================== 新架构方法 ====================
    @Override
    public ClauseResult buildClause() {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        // 优先使用column()方法添加的列
        if (!columns.isEmpty()) {
            sql.append("SELECT ");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                SelectColumn column = columns.get(i);
                sql.append(column.toSql());
                if (column.parameters != null) {
                    parameters.addAll(column.parameters);
                }
            }
        } else if (selectAll || selectedFields.isEmpty()) {
            // 🔧 智能展开：如果有JOIN，自动展开为所有相关表字段避免歧义
            if (hasJoinClause()) {
                sql.append("SELECT ");
                String[] allTableFields = getQueryBuilderTableFields();
                for (int i = 0; i < allTableFields.length; i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    sql.append(allTableFields[i]);
                }
            } else {
                sql.append("SELECT *");
            }
        } else {
            sql.append("SELECT ");
            // 🔧 如果有JOIN，为用户选择的字段也添加别名
            if (hasJoinClause()) {
                for (int i = 0; i < selectedFields.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    String field = selectedFields.get(i);
                    // 为字段添加别名：将.替换为__，避免字段名冲突
                    String fieldAlias = field.replace(".", "__");
                    sql.append(field).append(" AS ").append(fieldAlias);
                }
            } else {
                for (int i = 0; i < selectedFields.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    sql.append(selectedFields.get(i));
                }
            }
        }
        return new ClauseResult(sql.toString(), parameters);
    }
    // ==================== 智能展开辅助方法 ====================

    /**
     * 检查是否有JOIN子句
     */
    private boolean hasJoinClause() {
        if (criterion instanceof StandardCriterion) {
            StandardCriterion<T> qb = (StandardCriterion<T>) criterion;
            return qb.hasJoinClause();
        }
        return false;
    }

    /**
     * 获取所有相关表字段（带表别名）
     */
    private String[] getQueryBuilderTableFields() {
        if (criterion instanceof StandardCriterion) {
            StandardCriterion<T> qb = (StandardCriterion<T>) criterion;
            return qb.getAllTableFields();
        }
        return new String[0];
    }

    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }

    public List<String> getSelectedFields() {
        return selectedFields;
    }
    
    // ==================== CommonClause 接口实现 ====================
    
    @Override
    public ClauseData getClauseData() {
        return this;
    }

    // ==================== SELECT字段设置 ====================
    public void setSelectedFields(String... fields) {
        selectedFields.clear();
        if (fields != null) {
            for (String field : fields) {
                selectedFields.add(field);
            }
        }
    }
    
    // ==================== ClauseData 接口实现 ====================
    
    @Override
    public String getClauseType() {
        return "SELECT";
    }
    
    @Override
    public String getRawString() {
        return getClauseSql();
    }
    
    /**
     * 转换为 Map 结构化数据
     * 
     * @return 包含 SELECT 子句信息的 Map
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("clauseType", "SELECT");
        map.put("selectAll", selectAll);
        map.put("selectedFields", selectedFields);
        return map;
    }
}
