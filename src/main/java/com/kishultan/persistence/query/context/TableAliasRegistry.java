package com.kishultan.persistence.query.context;

import java.util.HashMap;
import java.util.Map;

/**
 * 表别名注册管理器，统一管理所有表的别名
 */
public class TableAliasRegistry {
    private final Map<String, String> tableToAlias = new HashMap<>();
    private final Map<String, String> aliasToTable = new HashMap<>();
    private int aliasCounter = 0;

    /**
     * 注册表别名（自动生成）
     */
    public String registerTable(String tableName) {
        if (tableToAlias.containsKey(tableName)) {
            return tableToAlias.get(tableName);
        }
        // 自动生成别名：表名首字母 + 计数器
        String alias = generateAlias(tableName);
        tableToAlias.put(tableName, alias);
        aliasToTable.put(alias, tableName);
        return alias;
    }

    /**
     * 注册表别名（指定别名）
     */
    public void registerTable(String tableName, String alias) {
        tableToAlias.put(tableName, alias);
        aliasToTable.put(alias, tableName);
    }

    /**
     * 获取表别名
     */
    public String getAlias(String tableName) {
        return tableToAlias.get(tableName);
    }

    /**
     * 获取表名
     */
    public String getTableName(String alias) {
        return aliasToTable.get(alias);
    }

    /**
     * 检查别名是否已注册
     */
    public boolean isAliasRegistered(String alias) {
        return aliasToTable.containsKey(alias);
    }

    /**
     * 自动生成别名
     */
    private String generateAlias(String tableName) {
        String baseAlias = tableName.substring(0, 1).toLowerCase();
        // 如果基础别名已存在，添加计数器
        if (isAliasRegistered(baseAlias)) {
            do {
                aliasCounter++;
                baseAlias = tableName.substring(0, 1).toLowerCase() + aliasCounter;
            } while (isAliasRegistered(baseAlias));
        }
        return baseAlias;
    }

    /**
     * 清空注册表
     */
    public void clear() {
        tableToAlias.clear();
        aliasToTable.clear();
        aliasCounter = 0;
    }
}
