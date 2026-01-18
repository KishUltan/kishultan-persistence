package com.kishultan.persistence.dialect;

/**
 * MariaDB数据库方言实现
 * MariaDB 与 MySQL 兼容，但有一些差异和优化
 */
public class MariaDBDialect implements DatabaseDialect {
    private static final char QUOTE_CHAR = '`';

    @Override
    public String getDatabaseType() {
        return "mariadb";
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
        // MariaDB 支持 LIMIT/OFFSET 语法
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
        return "`";
    }

    @Override
    public String getTableNameQuoteRight() {
        return "`";
    }

    @Override
    public String getColumnNameQuoteLeft() {
        return "`";
    }

    @Override
    public String getColumnNameQuoteRight() {
        return "`";
    }

    @Override
    public String getCurrentTimeFunction() {
        return "NOW()";
    }

    @Override
    public boolean supportsWindowFunctions() {
        return true;  // MariaDB 10.2+
    }

    @Override
    public boolean supportsCaseWhen() {
        return true;
    }

    @Override
    public String getConcatFunction(String column1, String column2) {
        // MariaDB 支持 CONCAT 函数
        return "CONCAT(" + column1 + ", " + column2 + ")";
    }

    @Override
    public String getDateDiffFunction(String date1, String date2) {
        // MariaDB 支持 DATEDIFF 函数
        return "DATEDIFF(" + date1 + ", " + date2 + ")";
    }

    @Override
    public String getDateFormatFunction(String column, String format) {
        // MariaDB 兼容 MySQL，使用 DATE_FORMAT 函数
        return "DATE_FORMAT(" + column + ", '" + format + "')";
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
