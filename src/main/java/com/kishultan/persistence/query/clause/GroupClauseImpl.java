package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.AbstractClause;
import com.kishultan.persistence.query.ClauseBuilder;
import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.query.GroupClause;
import com.kishultan.persistence.query.HavingClause;
import com.kishultan.persistence.query.context.ClauseResult;
import com.kishultan.persistence.query.context.ClauseData;

import java.util.ArrayList;
import java.util.List;

/**
 * GROUP BY子句实现
 */
public class GroupClauseImpl<T> extends AbstractClause<T> implements GroupClause<T>, ClauseBuilder<T>, ClauseData {
    private final List<String> groupColumns = new ArrayList<>();

    public GroupClauseImpl(StandardCriterion<T> queryBuilder) {
        super(queryBuilder);
    }
    // ==================== 公开的column方法 ====================
    // 实现GroupClause接口中定义的column重载方法

    @Override
    public GroupClause<T> column(String... columns) {
        if (columns != null) {
            for (String col : columns) {
                if (col != null && !col.isEmpty()) {
                    groupColumns.add(col);
                }
            }
        }
        return this;
    }

    @Override
    public <R> GroupClause<T> column(Columnable<T, R>... columns) {
        if (columns != null) {
            for (Columnable<T, R> col : columns) {
                if (col != null) {
                    String columnName = ColumnabledLambda.getColumnName(col);
                    if (columnName != null && !columnName.isEmpty()) {
                        groupColumns.add(columnName);
                    }
                }
            }
        }
        return this;
    }

    /**
     * 添加分组字段（供FromClause直接调用）
     * @deprecated 请使用column(String)方法
     */
    @Deprecated
    public void addColumn(String column) {
        groupColumns.add(column);
    }

    @Override
    public HavingClause<T> having() {
        HavingClauseImpl<T> havingClause = new HavingClauseImpl<T>((StandardCriterion<T>) criterion);
        ((StandardCriterion<T>) criterion).setHavingClause(havingClause);
        return havingClause;
    }

    @Override
    public ClauseResult buildClause() {
        if (groupColumns.isEmpty()) {
            return new ClauseResult("", new ArrayList<>());
        }
        StringBuilder sql = new StringBuilder("GROUP BY ");
        for (int i = 0; i < groupColumns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(groupColumns.get(i));
        }
        return new ClauseResult(sql.toString(), new ArrayList<>());
    }

    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    // ==================== ClauseData 接口实现 ====================
    
    @Override
    public String getClauseType() {
        return "GROUP BY";
    }
    
    // ==================== CommonClause 接口实现 ====================
    
    @Override
    public ClauseData getClauseData() {
        return this;
    }
    
    @Override
    public String getRawString() {
        return getClauseSql();
    }
    
    @Override
    public List<String> getGroupColumns() {
        return new ArrayList<>(groupColumns);
    }
    
    /**
     * 转换为 Map 结构化数据
     * 
     * @return 包含 GROUP BY 子句信息的 Map
     */
    @Override
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("clauseType", "GROUP BY");
        map.put("groupColumns", groupColumns);
        return map;
    }
}
