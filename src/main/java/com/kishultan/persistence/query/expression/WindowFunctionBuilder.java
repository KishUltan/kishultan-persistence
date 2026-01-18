package com.kishultan.persistence.query.expression;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;

import java.util.ArrayList;
import java.util.List;

/**
 * 窗口函数构建器
 * 用于构建窗口函数表达式
 */
public class WindowFunctionBuilder {
    private final String functionName;
    private final StringBuilder sql = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();
    private String tableAlias;
    
    /**
     * 构造函数
     * 
     * @param functionName 函数名
     * @param tableAlias 表别名（可选）
     */
    private WindowFunctionBuilder(String functionName, String tableAlias) {
        this.functionName = functionName;
        this.tableAlias = tableAlias;
    }
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * ROW_NUMBER函数
     */
    public static WindowFunctionBuilder rowNumber() {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("ROW_NUMBER", null);
        builder.sql.append("ROW_NUMBER()");
        return builder;
    }
    
    /**
     * RANK函数
     */
    public static WindowFunctionBuilder rank() {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("RANK", null);
        builder.sql.append("RANK()");
        return builder;
    }
    
    /**
     * DENSE_RANK函数
     */
    public static WindowFunctionBuilder denseRank() {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("DENSE_RANK", null);
        builder.sql.append("DENSE_RANK()");
        return builder;
    }
    
    /**
     * LAG函数
     */
    public static <T, R> WindowFunctionBuilder lag(Columnable<T, R> field, int offset, String tableAlias) {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("LAG", tableAlias);
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        builder.sql.append("LAG(").append(qualifiedField).append(", ").append(offset).append(")");
        return builder;
    }
    
    /**
     * LEAD函数
     */
    public static <T, R> WindowFunctionBuilder lead(Columnable<T, R> field, int offset, String tableAlias) {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("LEAD", tableAlias);
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        builder.sql.append("LEAD(").append(qualifiedField).append(", ").append(offset).append(")");
        return builder;
    }
    
    /**
     * NTILE函数
     */
    public static WindowFunctionBuilder ntile(int buckets) {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("NTILE", null);
        builder.sql.append("NTILE(").append(buckets).append(")");
        return builder;
    }
    
    /**
     * PERCENT_RANK函数
     */
    public static WindowFunctionBuilder percentRank() {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("PERCENT_RANK", null);
        builder.sql.append("PERCENT_RANK()");
        return builder;
    }
    
    /**
     * CUME_DIST函数
     */
    public static WindowFunctionBuilder cumeDist() {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("CUME_DIST", null);
        builder.sql.append("CUME_DIST()");
        return builder;
    }
    
    /**
     * FIRST_VALUE函数
     */
    public static <T, R> WindowFunctionBuilder firstValue(Columnable<T, R> field, String tableAlias) {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("FIRST_VALUE", tableAlias);
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        builder.sql.append("FIRST_VALUE(").append(qualifiedField).append(")");
        return builder;
    }
    
    /**
     * LAST_VALUE函数
     */
    public static <T, R> WindowFunctionBuilder lastValue(Columnable<T, R> field, String tableAlias) {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("LAST_VALUE", tableAlias);
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        builder.sql.append("LAST_VALUE(").append(qualifiedField).append(")");
        return builder;
    }
    
    /**
     * NTH_VALUE函数
     */
    public static <T, R> WindowFunctionBuilder nthValue(Columnable<T, R> field, int n, String tableAlias) {
        WindowFunctionBuilder builder = new WindowFunctionBuilder("NTH_VALUE", tableAlias);
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        builder.sql.append("NTH_VALUE(").append(qualifiedField).append(", ").append(n).append(")");
        return builder;
    }
    
    // ==================== OVER子句 ====================
    
    /**
     * 添加OVER子句（无参数）
     */
    public WindowExpression over() {
        sql.append(" OVER ()");
        return new WindowExpression(sql.toString());
    }
    
    /**
     * 添加OVER子句（PARTITION BY和ORDER BY都是单个方法引用）
     * 支持 .over(TestEntity::getCategory, TestEntity::getAmount) 的形式
     */
    @SuppressWarnings("unchecked")
    public <T1, T2> WindowExpression over(Columnable<T1, ?> partitionBy, Columnable<T2, ?> orderBy) {
        List<Columnable<?, ?>> partList = new ArrayList<>();
        partList.add((Columnable<?, ?>) partitionBy);
        List<Columnable<?, ?>> orderList = new ArrayList<>();
        orderList.add((Columnable<?, ?>) orderBy);
        return overList(partList, orderList);
    }
    
    /**
     * 添加OVER子句（带PARTITION BY和ORDER BY）
     * 支持直接使用Arrays.asList(TestEntity::getCategory)，无需显式类型参数
     * 
     * 使用泛型类型参数T来支持Arrays.asList的类型推断
     * Arrays.asList(TestEntity::getCategory) 返回 List<Columnable<TestEntity, String>>
     * 可以直接传递给 List<Columnable<T, ?>>
     */
    public <T> WindowExpression over(List<Columnable<T, ?>> partitionBy, List<Columnable<T, ?>> orderBy) {
        return overList(partitionBy, orderBy);
    }
    
    /**
     * 添加OVER子句（带PARTITION BY和ORDER BY）的内部实现
     */
    @SuppressWarnings("unchecked")
    private WindowExpression overList(List<? extends Columnable<?, ?>> partitionBy, List<? extends Columnable<?, ?>> orderBy) {
        sql.append(" OVER (");
        boolean hasPart = false;
        
        // PARTITION BY
        if (partitionBy != null && !partitionBy.isEmpty()) {
            sql.append("PARTITION BY ");
            for (int i = 0; i < partitionBy.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                String fieldName = ColumnabledLambda.getColumnName(partitionBy.get(i));
                String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
                sql.append(qualifiedField);
            }
            hasPart = true;
        }
        
        // ORDER BY
        if (orderBy != null && !orderBy.isEmpty()) {
            if (hasPart) {
                sql.append(" ");
            }
            sql.append("ORDER BY ");
            for (int i = 0; i < orderBy.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                String fieldName = ColumnabledLambda.getColumnName(orderBy.get(i));
                String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
                sql.append(qualifiedField);
            }
        }
        
        sql.append(")");
        return new WindowExpression(sql.toString());
    }
    
}