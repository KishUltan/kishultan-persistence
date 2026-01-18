package com.kishultan.persistence.dialect;

/**
 * 数据库方言接口
 * 用于处理不同数据库的SQL语法差异
 * 
 * 设计原则：方言只提供数据库特性，不提供 SQL 构建逻辑
 */
public interface DatabaseDialect {
    /**
     * 获取数据库类型标识
     */
    String getDatabaseType();

    /**
     * 是否支持 LIMIT 子句
     */
    boolean supportsLimit();

    /**
     * 是否支持 OFFSET 子句
     */
    boolean supportsOffset();

    /**
     * 生成分页子句（带 offset）
     * @param offset 偏移量（从 0 开始）
     * @param limit 每页记录数
     * @return 分页子句 SQL，例如 "LIMIT 10 OFFSET 20" 或 "OFFSET 20 ROWS FETCH NEXT 10 ROWS ONLY"
     */
    String limit(int offset, int limit);

    /**
     * 生成分页子句（无 offset）
     * @param limit 每页记录数
     * @return 分页子句 SQL，例如 "LIMIT 10" 或 "FETCH NEXT 10 ROWS ONLY"
     */
    String limit(int limit);

    /**
     * 获取参数占位符
     * 大多数数据库是 "?"
     */
    String getParameterPlaceholder();

    /**
     * 获取表名引号（左）
     * MySQL: "`", PostgreSQL: "\"", SQL Server: "["
     */
    String getTableNameQuoteLeft();

    /**
     * 获取表名引号（右）
     * MySQL: "`", PostgreSQL: "\"", SQL Server: "]"
     */
    String getTableNameQuoteRight();

    /**
     * 获取列名引号（左）
     * MySQL: "`", PostgreSQL: "\"", SQL Server: "["
     */
    String getColumnNameQuoteLeft();

    /**
     * 获取列名引号（右）
     * MySQL: "`", PostgreSQL: "\"", SQL Server: "]"
     */
    String getColumnNameQuoteRight();

    /**
     * 获取当前时间函数
     * MySQL: "NOW()", PostgreSQL: "NOW()", Oracle: "SYSDATE", SQL Server: "GETDATE()"
     */
    String getCurrentTimeFunction();

    /**
     * 是否支持窗口函数（ROW_NUMBER, RANK 等）
     */
    boolean supportsWindowFunctions();

    /**
     * 是否支持 CASE WHEN 表达式
     */
    boolean supportsCaseWhen();

    /**
     * 获取字符串连接函数
     * MySQL: "CONCAT(?, ?)", PostgreSQL: "? || ?"
     */
    String getConcatFunction(String column1, String column2);

    /**
     * 获取日期差函数
     * MySQL: "DATEDIFF(?, ?)", PostgreSQL: "? - ?"
     */
    String getDateDiffFunction(String date1, String date2);

    /**
     * 获取日期格式化函数
     * MySQL: "DATE_FORMAT(column, 'format')"
     * PostgreSQL/Oracle: "TO_CHAR(column, 'format')"
     * SQL Server: "FORMAT(column, 'format')"
     * 
     * @param column 日期列名或表达式
     * @param format 格式化字符串，如 'YYYY-MM-DD', '%Y-%m-%d' 等
     * @return 格式化后的SQL表达式
     */
    String getDateFormatFunction(String column, String format);

    /**
     * 去掉标识符引号（表名、列名）
     * 
     * @param identifier 带引号的标识符
     * @return 去掉引号的标识符
     */
    String unquoteIdentifier(String identifier);
}
