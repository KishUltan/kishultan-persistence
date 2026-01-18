package com.kishultan.persistence.query.expression;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.Criterion;

import java.util.Arrays;
import java.util.List;

/**
 * 函数工厂类
 * 提供静态方法创建各种函数表达式
 */
public class Functions {
    
    // ==================== 聚合函数 ====================
    
    /**
     * COUNT函数
     */
    public static <T, R> AggregateExpression count(Columnable<T, R> field) {
        return new AggregateExpression("COUNT", field);
    }
    
    /**
     * SUM函数
     */
    public static <T, R> AggregateExpression sum(Columnable<T, R> field) {
        return new AggregateExpression("SUM", field);
    }
    
    /**
     * AVG函数
     */
    public static <T, R> AggregateExpression avg(Columnable<T, R> field) {
        return new AggregateExpression("AVG", field);
    }
    
    /**
     * MAX函数
     */
    public static <T, R> AggregateExpression max(Columnable<T, R> field) {
        return new AggregateExpression("MAX", field);
    }
    
    /**
     * MIN函数
     */
    public static <T, R> AggregateExpression min(Columnable<T, R> field) {
        return new AggregateExpression("MIN", field);
    }
    
    // ==================== 字符串函数 ====================
    
    /**
     * UPPER函数
     */
    public static <T> FunctionExpression upper(Columnable<T, String> field) {
        return new FunctionExpression("UPPER", field, null);
    }
    
    /**
     * LOWER函数
     */
    public static <T> FunctionExpression lower(Columnable<T, String> field) {
        return new FunctionExpression("LOWER", field, null);
    }
    
    /**
     * LENGTH函数
     */
    public static <T> FunctionExpression length(Columnable<T, String> field) {
        return new FunctionExpression("LENGTH", field, null);
    }
    
    // ==================== 数学函数 ====================
    
    /**
     * ABS函数
     */
    public static <T> FunctionExpression abs(Columnable<T, Number> field) {
        return new FunctionExpression("ABS", field, null);
    }
    
    /**
     * ROUND函数
     */
    public static <T> FunctionExpression round(Columnable<T, Number> field) {
        return new FunctionExpression("ROUND", field, null);
    }
    
    /**
     * ROUND函数（指定小数位）
     */
    public static <T> FunctionExpression round(Columnable<T, Number> field, int decimals) {
        String fieldName = ColumnabledLambda.getColumnName(field);
        return new FunctionExpression("ROUND", fieldName + ", " + decimals);
    }
    
    // ==================== 日期函数 ====================
    
    /**
     * 日期格式化函数
     * 根据数据库方言自动选择正确的日期格式化函数
     * 
     * <p>注意：方言信息会在SelectClause.column()方法中自动从QueryBuilder获取，
     * 无需手动传递QueryBuilder。</p>
     * 
     * @param field 日期字段
     * @param format 格式化字符串
     *               MySQL格式示例: '%Y-%m-%d', '%Y-%m-%d %H:%i:%s'
     *               PostgreSQL/Oracle格式示例: 'YYYY-MM-DD', 'YYYY-MM-DD HH24:MI:SS'
     *               SQL Server格式示例: 'yyyy-MM-dd', 'yyyy-MM-dd HH:mm:ss'
     * @return 日期格式化表达式
     */
    public static <T, R> DateFormatExpression dateFormat(Columnable<T, R> field, String format) {
        return new DateFormatExpression(field, format, null, null);
    }
    
    /**
     * 日期格式化函数（带表别名）
     * 
     * <p>注意：方言信息会在SelectClause.column()方法中自动从QueryBuilder获取，
     * 无需手动传递QueryBuilder。</p>
     * 
     * @param field 日期字段
     * @param format 格式化字符串
     * @param tableAlias 表别名
     * @return 日期格式化表达式
     */
    public static <T, R> DateFormatExpression dateFormat(Columnable<T, R> field, String format, String tableAlias) {
        return new DateFormatExpression(field, format, tableAlias, null);
    }
    
    // ==================== 标量子查询 ====================
    
    /**
     * 标量子查询
     */
    public static ScalarSubqueryExpression scalarSubquery(Criterion<?> subquery) {
        return new ScalarSubqueryExpression(subquery);
    }
    
    // ==================== CASE WHEN表达式 ====================
    
    /**
     * 开始简单CASE表达式
     * CASE field WHEN value THEN result ...
     * 
     * @param field 要比较的字段
     * @param tableAlias 表别名（可选）
     * @return CaseWhenBuilder构建器
     */
    public static <T, R> CaseWhenBuilder caseWhen(Columnable<T, R> field, String tableAlias) {
        return CaseWhenBuilder.simpleCase(field, tableAlias);
    }
    
    /**
     * 开始简单CASE表达式（无表别名）
     * 
     * @param field 要比较的字段
     * @return CaseWhenBuilder构建器
     */
    public static <T, R> CaseWhenBuilder caseWhen(Columnable<T, R> field) {
        return CaseWhenBuilder.simpleCase(field, null);
    }
    
    /**
     * 开始搜索CASE表达式
     * CASE WHEN condition THEN result ...
     * 
     * @return CaseWhenBuilder构建器
     */
    public static CaseWhenBuilder caseWhen() {
        return CaseWhenBuilder.searchCase();
    }
    
    // ==================== 窗口函数 ====================
    
    /**
     * ROW_NUMBER窗口函数
     * 
     * @return WindowFunctionBuilder构建器
     */
    public static WindowFunctionBuilder rowNumber() {
        return WindowFunctionBuilder.rowNumber();
    }
    
    /**
     * RANK窗口函数
     * 
     * @return WindowFunctionBuilder构建器
     */
    public static WindowFunctionBuilder rank() {
        return WindowFunctionBuilder.rank();
    }
    
    /**
     * DENSE_RANK窗口函数
     * 
     * @return WindowFunctionBuilder构建器
     */
    public static WindowFunctionBuilder denseRank() {
        return WindowFunctionBuilder.denseRank();
    }
    
    /**
     * LAG窗口函数
     * 
     * @param field 字段
     * @param offset 偏移量
     * @param tableAlias 表别名（可选）
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder lag(Columnable<T, R> field, int offset, String tableAlias) {
        return WindowFunctionBuilder.lag(field, offset, tableAlias);
    }
    
    /**
     * LAG窗口函数（无表别名）
     * 
     * @param field 字段
     * @param offset 偏移量
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder lag(Columnable<T, R> field, int offset) {
        return WindowFunctionBuilder.lag(field, offset, null);
    }
    
    /**
     * LEAD窗口函数
     * 
     * @param field 字段
     * @param offset 偏移量
     * @param tableAlias 表别名（可选）
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder lead(Columnable<T, R> field, int offset, String tableAlias) {
        return WindowFunctionBuilder.lead(field, offset, tableAlias);
    }
    
    /**
     * LEAD窗口函数（无表别名）
     * 
     * @param field 字段
     * @param offset 偏移量
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder lead(Columnable<T, R> field, int offset) {
        return WindowFunctionBuilder.lead(field, offset, null);
    }
    
    /**
     * NTILE窗口函数
     * 
     * @param buckets 桶数
     * @return WindowFunctionBuilder构建器
     */
    public static WindowFunctionBuilder ntile(int buckets) {
        return WindowFunctionBuilder.ntile(buckets);
    }
    
    /**
     * PERCENT_RANK窗口函数
     * 
     * @return WindowFunctionBuilder构建器
     */
    public static WindowFunctionBuilder percentRank() {
        return WindowFunctionBuilder.percentRank();
    }
    
    /**
     * CUME_DIST窗口函数
     * 
     * @return WindowFunctionBuilder构建器
     */
    public static WindowFunctionBuilder cumeDist() {
        return WindowFunctionBuilder.cumeDist();
    }
    
    /**
     * FIRST_VALUE窗口函数
     * 
     * @param field 字段
     * @param tableAlias 表别名（可选）
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder firstValue(Columnable<T, R> field, String tableAlias) {
        return WindowFunctionBuilder.firstValue(field, tableAlias);
    }
    
    /**
     * FIRST_VALUE窗口函数（无表别名）
     * 
     * @param field 字段
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder firstValue(Columnable<T, R> field) {
        return WindowFunctionBuilder.firstValue(field, null);
    }
    
    /**
     * LAST_VALUE窗口函数
     * 
     * @param field 字段
     * @param tableAlias 表别名（可选）
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder lastValue(Columnable<T, R> field, String tableAlias) {
        return WindowFunctionBuilder.lastValue(field, tableAlias);
    }
    
    /**
     * LAST_VALUE窗口函数（无表别名）
     * 
     * @param field 字段
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder lastValue(Columnable<T, R> field) {
        return WindowFunctionBuilder.lastValue(field, null);
    }
    
    /**
     * NTH_VALUE窗口函数
     * 
     * @param field 字段
     * @param n 第N个值
     * @param tableAlias 表别名（可选）
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder nthValue(Columnable<T, R> field, int n, String tableAlias) {
        return WindowFunctionBuilder.nthValue(field, n, tableAlias);
    }
    
    /**
     * NTH_VALUE窗口函数（无表别名）
     * 
     * @param field 字段
     * @param n 第N个值
     * @return WindowFunctionBuilder构建器
     */
    public static <T, R> WindowFunctionBuilder nthValue(Columnable<T, R> field, int n) {
        return WindowFunctionBuilder.nthValue(field, n, null);
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 创建Columnable列表的辅助方法
     * 用于解决Arrays.asList在处理函数引用时的类型推断问题
     * 
     * <p>使用示例：</p>
     * <pre>
     * // 替代 Arrays.asList(TestEntity::getCategory)
     * columns(TestEntity::getCategory)
     * 
     * // 多个字段
     * columns(TestEntity::getCategory, TestEntity::getStatus)
     * </pre>
     * 
     * @param cols Columnable字段（可变参数）
     * @return Columnable列表
     */
    public static <T> List<Columnable<T, ?>> columns(Columnable<T, ?>... cols) {
        return Arrays.asList(cols);
    }
}
