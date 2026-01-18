package com.kishultan.persistence.dialect;

/**
 * GaussDB 数据库方言实现（华为）
 * GaussDB 高度兼容 PostgreSQL
 */
public class GaussDBDialect implements DatabaseDialect {
    private static final char QUOTE_CHAR = '"';

    @Override
    public String getDatabaseType() {
        return "gaussdb";
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
        // GaussDB 兼容 PostgreSQL 的 LIMIT/OFFSET 语法
        return "LIMIT " + limit + (offset > 0 ? " OFFSET " + offset : "");
    }

    @Override
    public String limit(int limit) {
        return "LIMIT " + limit;
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
        return "NOW()";
    }

    @Override
    public boolean supportsWindowFunctions() {
        return true;  // GaussDB 2.0+
    }

    @Override
    public boolean supportsCaseWhen() {
        return true;
    }

    @Override
    public String getConcatFunction(String column1, String column2) {
        // GaussDB 使用 || 操作符连接字符串
        return column1 + " || " + column2;
    }

    @Override
    public String getDateDiffFunction(String date1, String date2) {
        // GaussDB 兼容 PostgreSQL 的日期减法
        return "DATE_PART('day', " + date1 + " - " + date2 + ")";
    }

    @Override
    public String getDateFormatFunction(String column, String format) {
        // GaussDB 兼容 PostgreSQL，使用 TO_CHAR 函数
        return "TO_CHAR(" + column + ", '" + format + "')";
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
