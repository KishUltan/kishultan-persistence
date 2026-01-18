package com.kishultan.persistence.query.context;

/**
 * 分组条件标记
 * 用于在SQL生成时添加括号，不继承ConditionInfo
 */
public class GroupCondition {
    private final Type groupType;
    private final String groupOperator; // AND 或 OR

    /**
     * 构造函数
     *
     * @param groupOperator 分组操作符 (AND/OR)
     * @param groupType     分组类型 (START/END/NONE)
     */
    public GroupCondition(String groupOperator, Type groupType) {
        this.groupOperator = groupOperator;
        this.groupType = groupType;
    }

    /**
     * 默认构造函数 - 类型为NONE（普通条件）
     */
    public GroupCondition() {
        this.groupOperator = "AND";
        this.groupType = Type.NONE;
    }

    public Type getGroupType() {
        return groupType;
    }

    public String getGroupOperator() {
        return groupOperator;
    }

    /**
     * 判断是否为分组开始
     */
    public boolean isGroupStart() {
        return groupType == Type.START;
    }

    /**
     * 判断是否为分组结束
     */
    public boolean isGroupEnd() {
        return groupType == Type.END;
    }

    /**
     * 判断是否为普通条件
     */
    public boolean isNormalCondition() {
        return groupType == Type.NONE;
    }

    // 分组类型枚举
    public enum Type {
        START,  // 分组开始
        END,    // 分组结束
        NONE    // 普通条件（无分组）
    }
}
