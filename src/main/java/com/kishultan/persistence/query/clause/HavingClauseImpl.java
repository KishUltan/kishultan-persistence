package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.query.AbstractClause;
import com.kishultan.persistence.query.ClauseBuilder;
import com.kishultan.persistence.query.HavingClause;
import com.kishultan.persistence.query.context.ClauseResult;
import com.kishultan.persistence.query.context.ClauseData;
import com.kishultan.persistence.query.context.ConditionInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * HAVING子句实现类
 * 使用新的架构：存储HAVING条件信息，通过 buildClause() 方法生成SQL
 */
public class HavingClauseImpl<T> extends AbstractClause<T> implements HavingClause<T>, ClauseBuilder<T>, ClauseData {
    private final List<ConditionInfo> conditions = new ArrayList<>();
    private String logicalOperator = "AND"; // 条件的逻辑操作符

    public HavingClauseImpl(StandardCriterion<T> queryBuilder) {
        super(queryBuilder);
    }

    // ==================== 条件方法 ====================
    @Override
    public HavingClause<T> eq(String column, Object value) {
        conditions.add(new ConditionInfo(null, column, "=", value, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> ne(String column, Object value) {
        conditions.add(new ConditionInfo(null, column, "!=", value, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> gt(String column, Object value) {
        conditions.add(new ConditionInfo(null, column, ">", value, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> ge(String column, Object value) {
        conditions.add(new ConditionInfo(null, column, ">=", value, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> lt(String column, Object value) {
        conditions.add(new ConditionInfo(null, column, "<", value, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> le(String column, Object value) {
        conditions.add(new ConditionInfo(null, column, "<=", value, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> like(String column, String value) {
        conditions.add(new ConditionInfo(null, column, "LIKE", value, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> in(String column, Object... values) {
        conditions.add(new ConditionInfo(null, column, "IN", values, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> isNull(String column) {
        conditions.add(new ConditionInfo(null, column, "IS NULL", null, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> isNotNull(String column) {
        conditions.add(new ConditionInfo(null, column, "IS NOT NULL", null, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> between(String column, Object start, Object end) {
        conditions.add(new ConditionInfo(null, column, "BETWEEN", new Object[]{start, end}, logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> in(String column, Collection<?> values) {
        conditions.add(new ConditionInfo(null, column, "IN", values.toArray(), logicalOperator));
        return this;
    }

    @Override
    public HavingClause<T> and() {
        // 逻辑操作符在 buildClause() 中处理
        this.logicalOperator = "AND";
        return this;
    }

    @Override
    public HavingClause<T> or() {
        // 逻辑操作符在 buildClause() 中处理
        this.logicalOperator = "OR";
        return this;
    }

    // ==================== 新架构方法 ====================
    @Override
    public ClauseResult buildClause() {
        if (conditions.isEmpty()) {
            return new ClauseResult("", new ArrayList<>());
        }
        StringBuilder sql = new StringBuilder("HAVING ");
        List<Object> parameters = new ArrayList<>();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            ConditionInfo condition = conditions.get(i);
            String columnRef = condition.getColumn();
            // 构建条件
            if (condition.getOperator().equals("IN") || condition.getOperator().equals("NOT IN")) {
                Object[] values = (Object[]) condition.getValue();
                sql.append(columnRef).append(" ").append(condition.getOperator()).append(" (");
                for (int j = 0; j < values.length; j++) {
                    if (j > 0) sql.append(", ");
                    sql.append("?");
                    parameters.add(values[j]);
                }
                sql.append(")");
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
        }
        return new ClauseResult(sql.toString(), parameters);
    }

    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    // ==================== CommonClause 接口实现 ====================
    
    @Override
    public ClauseData getClauseData() {
        return this;
    }
    
    // ==================== ClauseData 接口实现 ====================
    
    @Override
    public String getClauseType() {
        return "HAVING";
    }
    
    @Override
    public String getRawString() {
        return getClauseSql();
    }
    
    /**
     * 转换为 Map 结构化数据
     * 
     * @return 包含 HAVING 子句信息的 Map
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("clauseType", "HAVING");
        map.put("conditions", conditions);
        map.put("logicalOperator", logicalOperator);
        return map;
    }
}
