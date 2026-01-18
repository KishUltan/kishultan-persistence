package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.AbstractClause;
import com.kishultan.persistence.query.ClauseBuilder;
import com.kishultan.persistence.query.OrderClause;
import com.kishultan.persistence.query.context.ClauseData;
import com.kishultan.persistence.query.context.ClauseResult;
import com.kishultan.persistence.query.context.OrderInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ORDER BY子句实现类
 * 使用新的架构：存储排序信息，通过 buildClause() 方法生成SQL
 */
public class OrderClauseImpl<T> extends AbstractClause<T> implements OrderClause<T>, ClauseBuilder<T>, ClauseData {
    private final List<OrderInfo> orderInfos = new ArrayList<>();

    public OrderClauseImpl(StandardCriterion<T> queryBuilder) {
        super(queryBuilder);
    }

    // ==================== 排序方法 ====================
    @Override
    public OrderClause<T> asc(String column) {
        orderInfos.add(new OrderInfo(column, "ASC"));
        return this;
    }

    @Override
    public OrderClause<T> desc(String column) {
        orderInfos.add(new OrderInfo(column, "DESC"));
        return this;
    }

    @Override
    public OrderClause<T> asc(String tableAlias, String column) {
        orderInfos.add(new OrderInfo(tableAlias + "." + column, "ASC"));
        return this;
    }

    @Override
    public OrderClause<T> desc(String tableAlias, String column) {
        orderInfos.add(new OrderInfo(tableAlias + "." + column, "DESC"));
        return this;
    }

    @Override
    public <R> OrderClause<T> asc(Columnable<T, R> fieldSelector) {
        String columnName = ColumnabledLambda.getColumnName(fieldSelector);
        // 根据 Lambda 表达式获取正确的表别名
        String tableAlias = getTableAlias(fieldSelector);
        String qualifiedFieldName = tableAlias + "." + columnName;
        orderInfos.add(new OrderInfo(qualifiedFieldName, "ASC"));
        return this;
    }

    @Override
    public <R> OrderClause<T> desc(Columnable<T, R> fieldSelector) {
        String columnName = ColumnabledLambda.getColumnName(fieldSelector);
        // 根据 Lambda 表达式获取正确的表别名
        String tableAlias = getTableAlias(fieldSelector);
        String qualifiedFieldName = tableAlias + "." + columnName;
        // 🔧 添加调试信息
//        System.out.println("=== Lambda 表别名调试 ===");
//        System.out.println("columnName: " + columnName);
//        System.out.println("tableAlias: " + tableAlias);
//        System.out.println("qualifiedFieldName: " + qualifiedFieldName);
//        System.out.println("=================================");
        orderInfos.add(new OrderInfo(qualifiedFieldName, "DESC"));
        return this;
    }

    @Override
    public <R> OrderClause<T> asc(String tableAlias, Columnable<T, R> fieldSelector) {
        String columnName = ColumnabledLambda.getColumnName(fieldSelector);
        orderInfos.add(new OrderInfo(tableAlias + "." + columnName, "ASC"));
        return this;
    }

    @Override
    public <R> OrderClause<T> desc(String tableAlias, Columnable<T, R> fieldSelector) {
        String columnName = ColumnabledLambda.getColumnName(fieldSelector);
        orderInfos.add(new OrderInfo(tableAlias + "." + columnName, "DESC"));
        return this;
    }

    // ==================== 新架构方法 ====================
    @Override
    public ClauseResult buildClause() {
        if (orderInfos.isEmpty()) {
            return new ClauseResult("", new ArrayList<>());
        }
        StringBuilder sql = new StringBuilder("ORDER BY ");
        for (int i = 0; i < orderInfos.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            OrderInfo orderInfo = orderInfos.get(i);
            sql.append(orderInfo.getColumn()).append(" ").append(orderInfo.getDirection());
        }
        // 🔧 添加调试信息
//        System.out.println("=== ORDER BY 调试信息 ===");
//        System.out.println("orderInfos数量: " + orderInfos.size());
//        for (OrderInfo info : orderInfos) {
//            System.out.println("字段: " + info.getColumn() + ", 方向: " + info.getDirection());
//        }
//        System.out.println("生成的ORDER BY SQL: " + sql.toString());
//        System.out.println("================================");
        return new ClauseResult(sql.toString(), new ArrayList<>());
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
        return "ORDER BY";
    }
    
    @Override
    public String getRawString() {
        return getClauseSql();
    }
    
    @Override
    public List<OrderInfo> getOrders() {
        return Collections.unmodifiableList(orderInfos);
    }
    
    /**
     * 转换为 Map 结构化数据
     * 
     * @return 包含 ORDER BY 子句信息的 Map
     */
    @Override
    public java.util.Map<String, Object> toMap() {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("clauseType", "ORDER BY");
        map.put("orderInfos", orderInfos);
        return map;
    }
    
    // ==================== 内部方法 ====================
    // 注意：getTableAlias 方法现在在父类 AbstractClause 中定义
}
