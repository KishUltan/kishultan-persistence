package com.kishultan.persistence.query.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kishultan.persistence.dialect.DatabaseDialect;
import com.kishultan.persistence.query.FromClause;
import com.kishultan.persistence.query.GroupClause;
import com.kishultan.persistence.query.HavingClause;
import com.kishultan.persistence.query.JoinClause;
import com.kishultan.persistence.query.OrderClause;
import com.kishultan.persistence.query.SelectClause;
import com.kishultan.persistence.query.WhereClause;

/**
 * 查询构建上下文，存储所有子句的信息
 * 
 * 支持统一持久化抽象层设计，提供 SQL 和 NoSQL 两种访问方式：
 * - SQL 方式：通过子句对象的 buildClause() 方法获取 SQL 片段和参数
 * - NoSQL 方式：直接使用子句对象，操作结构化数据
 * - 结构化数据：通过子句对象的 getClauseData().toMap() 方法获取 Map 形式的结构化数据
 * 
 * @param <T> 实体类型
 */
public class QueryBuildContext<T> {
    // 直接使用子句对象
    private SelectClause<T> selectClause;
    private FromClause<T> fromClause;
    private List<JoinClause<T>> joinClauseList = new ArrayList<>();
    private WhereClause<T> whereClause;
    private GroupClause<T> groupClause;
    private HavingClause<T> havingClause;
    private OrderClause<T> orderClause;
    
    // LIMIT 相关字段（不是子句对象，而是简单字段）
    private int offsetValue = 0;
    private int limitValue = 0;
    
    // 数据库方言
    private DatabaseDialect dialect;
    
    // 兼容旧版本的字段（保留用于向后兼容）
    private List<JoinInfo> joinClauses = new ArrayList<>();
    
    // ==================== 子句 Getter 方法（返回对象） ====================
    
    /**
     * 获取 SELECT 子句对象
     * @return SelectClause 对象，如果不存在则返回 null
     */
    public SelectClause<T> getSelectClause() {
        return selectClause;
    }
    
    /**
     * 获取 FROM 子句对象
     * @return FromClause 对象，如果不存在则返回 null
     */
    public FromClause<T> getFromClause() {
        return fromClause;
    }
    
    /**
     * 获取 JOIN 子句对象列表
     * @return JoinClause 对象列表
     */
    public List<JoinClause<T>> getJoinClauseList() {
        return joinClauseList;
    }

    /**
     * 获取单个 JOIN 子句对象（为了兼容性，返回列表中的最后一个）
     * @return JoinClause 对象，如果列表为空则返回 null
     */
    public JoinClause<T> getJoinClause() {
        return joinClauseList.isEmpty() ? null : joinClauseList.get(joinClauseList.size() - 1);
    }
    
    /**
     * 获取 WHERE 子句对象
     * @return WhereClause 对象，如果不存在则返回 null
     */
    public WhereClause<T> getWhereClause() {
        return whereClause;
    }
    
    /**
     * 获取 GROUP BY 子句对象
     * @return GroupClause 对象，如果不存在则返回 null
     */
    public GroupClause<T> getGroupByClause() {
        return groupClause;
    }
    
    /**
     * 获取 HAVING 子句对象
     * @return HavingClause 对象，如果不存在则返回 null
     */
    public HavingClause<T> getHavingClause() {
        return havingClause;
    }
    
    /**
     * 获取 ORDER BY 子句对象
     * @return OrderClause 对象，如果不存在则返回 null
     */
    public OrderClause<T> getOrderByClause() {
        return orderClause;
    }
    
    /**
     * 获取 offset 值
     * @return offset 值
     */
    public int getOffsetValue() {
        return offsetValue;
    }
    
    /**
     * 获取 limit 值
     * @return limit 值
     */
    public int getLimitValue() {
        return limitValue;
    }
    
    /**
     * 判断是否有 LIMIT
     * @return 如果有 LIMIT 返回 true，否则返回 false
     */
    public boolean hasLimit() {
        return limitValue > 0;
    }
    
    // ==================== 结构化数据 Getter 方法（返回 Map） ====================
    
    /**
     * 获取 WHERE 子句的结构化数据
     * @return WHERE 子句的结构化数据 Map，如果子句不存在则返回空 Map
     */
    public Map<String, Object> getWhereData() {
        if (whereClause != null && whereClause.getClauseData() != null) {
            return whereClause.getClauseData().toMap();
        }
        return Collections.emptyMap();
    }
    
    /**
     * 获取 ORDER BY 子句的结构化数据
     * @return ORDER BY 子句的结构化数据 Map，如果子句不存在则返回空 Map
     */
    public Map<String, Object> getOrderData() {
        if (orderClause != null && orderClause.getClauseData() != null) {
            return orderClause.getClauseData().toMap();
        }
        return Collections.emptyMap();
    }
    
    /**
     * 获取 LIMIT 的结构化数据
     * @return LIMIT 的结构化数据 Map，如果没有 LIMIT 则返回空 Map
     */
    public Map<String, Object> getLimitData() {
        if (!hasLimit()) {
            return Collections.emptyMap();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("offset", offsetValue);
        map.put("limit", limitValue);
        return map;
    }
    
    // ==================== Setter 方法 ====================
    
    public void setSelectClause(SelectClause<T> selectClause) {
        this.selectClause = selectClause;
    }
    
    public void setFromClause(FromClause<T> fromClause) {
        this.fromClause = fromClause;
    }
    
    public void addJoinClause(JoinClause<T> joinClause) {
        this.joinClauseList.add(joinClause);
    }

    public void setJoinClause(JoinClause<T> joinClause) {
        this.joinClauseList.clear();
        if (joinClause != null) {
            this.joinClauseList.add(joinClause);
        }
    }
    
    public void setWhereClause(WhereClause<T> whereClause) {
        this.whereClause = whereClause;
    }
    
    public void setGroupClause(GroupClause<T> groupClause) {
        this.groupClause = groupClause;
    }
    
    public void setHavingClause(HavingClause<T> havingClause) {
        this.havingClause = havingClause;
    }
    
    public void setOrderClause(OrderClause<T> orderClause) {
        this.orderClause = orderClause;
    }
    
    /**
     * 设置 offset 值
     * @param offsetValue offset 值
     */
    public void setOffsetValue(int offsetValue) {
        this.offsetValue = offsetValue;
    }
    
    /**
     * 设置 limit 值
     * @param limitValue limit 值
     */
    public void setLimitValue(int limitValue) {
        this.limitValue = limitValue;
    }
    
    // ==================== 兼容旧版本的 Getter/Setter ====================
    
    public List<JoinInfo> getJoinClauses() {
        return joinClauses;
    }

    public void setJoinClauses(List<JoinInfo> joinClauses) {
        this.joinClauses = joinClauses;
    }
    
    // ==================== 方言 Getter/Setter ====================
    
    /**
     * 获取数据库方言
     * @return 数据库方言对象
     */
    public DatabaseDialect getDialect() {
        return dialect;
    }
    
    /**
     * 设置数据库方言
     * @param dialect 数据库方言对象
     */
    public void setDialect(DatabaseDialect dialect) {
        this.dialect = dialect;
    }
    
    /**
     * 清空构建上下文
     */
    public void clear() {
        selectClause = null;
        fromClause = null;
        joinClauseList.clear();
        whereClause = null;
        groupClause = null;
        havingClause = null;
        orderClause = null;
        offsetValue = 0;
        limitValue = 0;
        joinClauses.clear();
    }
    
    /**
     * 内部类：JoinInfo
     * 用于存储 JOIN 信息
     */
    public static class JoinInfo {
        private String joinType;
        private String tableName;
        private String alias;
        private String onCondition;
        
        public JoinInfo(String joinType, String tableName, String alias, String onCondition) {
            this.joinType = joinType;
            this.tableName = tableName;
            this.alias = alias;
            this.onCondition = onCondition;
        }
        
        public String getJoinType() {
            return joinType;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public String getAlias() {
            return alias;
        }
        
        public String getOnCondition() {
            return onCondition;
        }
    }
}

