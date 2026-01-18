package com.kishultan.persistence.dialect;

/**
 * SQL Server数据库方言实现
 */
public class SQLServerDialect implements DatabaseDialect {
    private static final char LEFT_QUOTE_CHAR = '[';
    private static final char RIGHT_QUOTE_CHAR = ']';

    @Override
    public String getDatabaseType() {
        return "sqlserver";
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsOffset() {
        return true;  // SQL Server 2012+
    }

    @Override
    public String limit(int offset, int limit) {
        return "OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
    }

    @Override
    public String limit(int limit) {
        return "OFFSET 0 ROWS FETCH NEXT " + limit + " ROWS ONLY";
    }

    @Override
    public String getParameterPlaceholder() {
        return "?";
    }

    @Override
    public String getTableNameQuoteLeft() {
        return "[";
    }

    @Override
    public String getTableNameQuoteRight() {
        return "]";
    }

    @Override
    public String getColumnNameQuoteLeft() {
        return "[";
    }

    @Override
    public String getColumnNameQuoteRight() {
        return "]";
    }

    @Override
    public String getCurrentTimeFunction() {
        return "GETDATE()";
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
        return column1 + " + " + column2;
    }

    @Override
    public String getDateDiffFunction(String date1, String date2) {
        return "DATEDIFF(day, " + date2 + ", " + date1 + ")";
    }

    @Override
    public String getDateFormatFunction(String column, String format) {
        // SQL Server使用FORMAT函数
        return "FORMAT(" + column + ", '" + format + "')";
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

        // 字符比较（左右引号不同）
        if (identifier.charAt(0) == LEFT_QUOTE_CHAR && 
            identifier.charAt(length - 1) == RIGHT_QUOTE_CHAR) {
            return identifier.substring(1, length - 1);
        }

        return identifier;
    }
}
