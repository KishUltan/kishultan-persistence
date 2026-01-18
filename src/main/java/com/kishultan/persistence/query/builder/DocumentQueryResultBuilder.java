package com.kishultan.persistence.query.builder;

import com.kishultan.persistence.query.WhereClause;
import com.kishultan.persistence.query.OrderClause;
import com.kishultan.persistence.query.SelectClause;
import com.kishultan.persistence.query.context.QueryBuildContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档查询结果构建器（支持 MongoDB 等文档数据库）
 * 
 * 注意：此实现不依赖具体的 NoSQL 驱动，实际使用时需要：
 * 1. 在项目中添加对应的 NoSQL 驱动依赖（如 MongoDB、CouchDB 等）
 * 2. 创建子类实现具体的查询构建逻辑
 * 
 * 当前实现返回通用的 JSON 格式查询，便于后续转换为具体数据库的查询语法
 */
public class DocumentQueryResultBuilder implements QueryResultBuilder {
    
    @Override
    public String buildQuery(QueryBuildContext<?> context) {
        // 构建文档查询（如 MongoDB 的 JSON 查询）
        Map<String, Object> query = new HashMap<>();
        
        // 从 whereClause 提取条件
        WhereClause<?> whereClause = context.getWhereClause();
        if (whereClause != null && whereClause.getClauseData() != null) {
            Map<String, Object> whereData = whereClause.getClauseData().toMap();
            if (!whereData.isEmpty()) {
                query.put("filter", whereData);
            }
        }
        
        // 处理排序
        OrderClause<?> orderClause = context.getOrderByClause();
        if (orderClause != null && orderClause.getClauseData() != null) {
            Map<String, Object> orderData = orderClause.getClauseData().toMap();
            if (!orderData.isEmpty()) {
                query.put("sort", orderData);
            }
        }
        
        // 处理限制
        if (context.hasLimit()) {
            Map<String, Object> limitData = context.getLimitData();
            if (!limitData.isEmpty()) {
                query.put("limit", limitData);
            }
        }
        
        // 处理投影
        SelectClause<?> selectClause = context.getSelectClause();
        if (selectClause != null && selectClause.getClauseData() != null) {
            Map<String, Object> selectData = selectClause.getClauseData().toMap();
            if (!selectData.isEmpty()) {
                query.put("projection", selectData);
            }
        }
        
        // 返回 JSON 字符串
        return mapToJson(query);
    }
    
    @Override
    public String buildCountQuery(QueryBuildContext<?> context) {
        // 计数查询使用相同的 filter
        Map<String, Object> countQuery = new HashMap<>();
        
        WhereClause<?> whereClause = context.getWhereClause();
        if (whereClause != null && whereClause.getClauseData() != null) {
            Map<String, Object> whereData = whereClause.getClauseData().toMap();
            if (!whereData.isEmpty()) {
                countQuery.put("filter", whereData);
            }
        }
        
        return mapToJson(countQuery);
    }
    
    @Override
    public String getBuilderType() {
        return "document";
    }
    
    /**
     * 将 Map 转换为 JSON 字符串
     * 
     * @param map Map 对象
     * @return JSON 字符串
     */
    protected String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value));
            } else if (value instanceof java.util.List) {
                json.append(listToJson((java.util.List<Object>) value));
            } else {
                json.append(value);
            }
            
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 将 List 转换为 JSON 字符串
     * 
     * @param list List 对象
     * @return JSON 字符串
     */
    protected String listToJson(java.util.List<Object> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (Object value : list) {
            if (!first) {
                json.append(",");
            }
            
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Map) {
                json.append(mapToJson((Map<String, Object>) value));
            } else if (value instanceof java.util.List) {
                json.append(listToJson((java.util.List<Object>) value));
            } else {
                json.append(value);
            }
            
            first = false;
        }
        
        json.append("]");
        return json.toString();
    }
}
