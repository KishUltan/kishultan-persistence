package com.kishultan.persistence.dialect;

/**
 * Sybase ASE数据库方言实现
 */
public class SybaseDialect implements DatabaseDialect {
    private static final char QUOTE_CHAR = '"';

    @Override
    public String getDatabaseType() {
        return "sybase";
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsOffset() {
        return true;
    }

    @Override
    public String limit(int offset, int limit) {
        // Sybase 使用 TOP 子句
        // 注意：Sybase 的分页需要使用 ROW_NUMBER() 窗口函数
        // 这里返回一个标记，实际使用时需要在外层查询中包装
        if (offset > 0) {
            return "TOP " + (offset + limit);
        }
        return "TOP " + limit;
    }

    @Override
    public String limit(int limit) {
        return "TOP " + limit;
    }

    @Override
    public String getParameterPlaceholder() {
        return "?";
    }

    @Override
    public String getTableNameQuoteLeft() {
        return "\"";
    }

    @Override
    public String getTableNameQuoteRight() {
        return "\"";
    }

    @Override
    public String getColumnNameQuoteLeft() {
        return "\"";
    }

    @Override
    public String getColumnNameQuoteRight() {
        return "\"";
    }

    @Override
    public String getCurrentTimeFunction() {
        return "GETDATE()";
    }

    @Override
    public boolean supportsWindowFunctions() {
        return true;  // Sybase 15.0+
    }

    @Override
    public boolean supportsCaseWhen() {
        return true;
    }

    @Override
    public String getConcatFunction(String column1, String column2) {
        // Sybase 使用 + 操作符连接字符串
        return column1 + " + " + column2;
    }

    @Override
    public String getDateDiffFunction(String date1, String date2) {
        // Sybase 支持 DATEDIFF 函数
        return "DATEDIFF(day, " + date2 + ", " + date1 + ")";
    }

    @Override
    public String getDateFormatFunction(String column, String format) {
        // Sybase 使用 CONVERT 函数进行日期格式化
        // 注意：Sybase的CONVERT格式代码与标准不同，这里使用通用格式
        return "CONVERT(VARCHAR, " + column + ", 120)";  // 120 = 'yyyy-MM-dd HH:mm:ss'
    }

    @Override
    public String unquoteIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return identifier;
        }

        int length = identifier.length();

        // 检查长度是否足够包含引号
        if (length < 2) {
            return identifier;
        }

        // 使用字符比较（比字符串比较快）
        if (identifier.charAt(0) == QUOTE_CHAR && 
            identifier.charAt(length - 1) == QUOTE_CHAR) {
            return identifier.substring(1, length - 1);
        }

        return identifier;
    }
}
