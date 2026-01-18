package com.kishultan.persistence.query.context;

/**
 * 条件信息，用于存储WHERE条件的信息
 */
public class ConditionInfo {
    private final String tableName;     // 表名或别名
    private final String column;        // 字段名
    private final String operator;      // 操作符
    private final Object value;         // 值
    private final boolean isAlias;      // 是否为别名
    private final String logicalOperator; // 逻辑操作符 (AND/OR)
    /**
     * 构造函数：通过表名引用
     */
    /*public ConditionInfo(String tableName, String column, String operator, Object value) {
        this.tableName = tableName;
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.isAlias = false;
        this.logicalOperator = "AND"; // 默认使用AND
    }*/

    /**
     * 构造函数：通过别名引用
     */
    public ConditionInfo(String alias, String column, String operator, Object value, boolean isAlias, String logicalOperator) {
        this.tableName = alias;
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.isAlias = isAlias;
        this.logicalOperator = logicalOperator; // 默认使用AND
    }
    /**
     * 构造函数：指定逻辑操作符
     */
    /*public ConditionInfo(String tableName, String column, String operator, Object value, boolean isAlias, String logicalOperator) {
        this.tableName = tableName;
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.isAlias = isAlias;
        this.logicalOperator = logicalOperator != null ? logicalOperator : "AND";
    }*/

    /**
     * 构造函数：通过表名引用，指定逻辑操作符
     */
    public ConditionInfo(String tableName, String column, String operator, Object value, String logicalOperator) {
        this.tableName = tableName;
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.isAlias = false;
        this.logicalOperator = logicalOperator;
    }

    /**
     * 获取完整的字段引用（需要传入别名注册表）
     */
    public String getFullColumnReference(TableAliasRegistry registry) {
        if (tableName == null) {
            return column; // 没有表名，直接返回字段
        }
        if (isAlias) {
            return tableName + "." + column; // 使用别名
        } else {
            // 是表名，获取对应的别名
            String alias = registry.getAlias(tableName);
            return alias != null ? alias + "." + column : column;
        }
    }

    // getter方法
    public String getTableName() {
        return tableName;
    }

    public String getColumn() {
        return column;
    }

    public String getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    public boolean isAlias() {
        return isAlias;
    }

    public String getLogicalOperator() {
        return logicalOperator;
    }
}
