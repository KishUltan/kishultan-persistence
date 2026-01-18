package com.kishultan.persistence.dialect;

/**
 * SQLite数据库方言实现
 */
public class SQLiteDialect implements DatabaseDialect {
    private static final char QUOTE_CHAR = '"';

    @Override
    public String getDatabaseType() {
        return "sqlite";
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
        // SQLite 支持 LIMIT/OFFSET 语法
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
        return "CURRENT_TIMESTAMP";
    }

    @Override
    public boolean supportsWindowFunctions() {
        return true;  // SQLite 3.25.0+
    }

    @Override
    public boolean supportsCaseWhen() {
        return true;
    }

    @Override
    public String getConcatFunction(String column1, String column2) {
        // SQLite 使用 || 操作符连接字符串
        return column1 + " || " + column2;
    }

    @Override
    public String getDateDiffFunction(String date1, String date2) {
        // SQLite 使用 julianday 函数计算日期差
        return "CAST(julianday(" + date1 + ") - julianday(" + date2 + ") AS INTEGER)";
    }

    @Override
    public String getDateFormatFunction(String column, String format) {
        // SQLite 使用 strftime 函数
        return "strftime('" + format + "', " + column + ")";
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
