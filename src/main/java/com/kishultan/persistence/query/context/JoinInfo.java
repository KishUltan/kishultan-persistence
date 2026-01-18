package com.kishultan.persistence.query.context;

/**
 * JOIN信息，存储JOIN的详细信息
 */
public class JoinInfo {
    private final String joinType;      // JOIN类型：INNER, LEFT, RIGHT, FULL
    private final String tableName;     // 表名
    private final String tableAlias;    // 表别名
    private final String onCondition;   // ON条件

    public JoinInfo(String joinType, String tableName, String tableAlias, String onCondition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.onCondition = onCondition;
    }

    // getter方法
    public String getJoinType() {
        return joinType;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getOnCondition() {
        return onCondition;
    }
}
