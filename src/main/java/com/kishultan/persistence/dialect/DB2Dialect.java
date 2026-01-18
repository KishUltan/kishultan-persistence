package com.kishultan.persistence.dialect;

/**
 * IBM DB2数据库方言实现
 */
public class DB2Dialect implements DatabaseDialect {
    private static final char QUOTE_CHAR = '"';

    @Override
    public String getDatabaseType() {
        return "db2";
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsOffset() {
        return true;  // DB2 9.7+
    }

    @Override
    public String limit(int offset, int limit) {
        // DB2 使用 FETCH FIRST n ROWS ONLY 和 OFFSET
        StringBuilder sql = new StringBuilder();
        if (offset > 0) {
            sql.append("OFFSET ").append(offset).append(" ROWS ");
        }
        sql.append("FETCH FIRST ").append(limit).append(" ROWS ONLY");
        return sql.toString();
    }

    @Override
    public String limit(int limit) {
        return "FETCH FIRST " + limit + " ROWS ONLY";
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
        return "CURRENT TIMESTAMP";
    }

    @Override
    public boolean supportsWindowFunctions() {
        return true;
    }

    @Override
    public boolean supportsCaseWhen() {
        return true;
    }

    @Override
    public String getConcatFunction(String column1, String column2) {
        // DB2 使用 || 操作符或 CONCAT 函数
        return column1 + " || " + column2;
    }

    @Override
    public String getDateDiffFunction(String date1, String date2) {
        // DB2 使用 DAYS 函数计算日期差
        return "DAYS(" + date1 + ") - DAYS(" + date2 + ")";
    }

    @Override
    public String getDateFormatFunction(String column, String format) {
        // DB2 使用 VARCHAR_FORMAT 函数进行日期格式化
        return "VARCHAR_FORMAT(" + column + ", '" + format + "')";
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
