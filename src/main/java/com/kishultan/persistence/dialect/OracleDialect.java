package com.kishultan.persistence.dialect;

/**
 * Oracle数据库方言实现
 */
public class OracleDialect implements DatabaseDialect {
    private static final char QUOTE_CHAR = '"';

    @Override
    public String getDatabaseType() {
        return "oracle";
    }

    @Override
    public boolean supportsLimit() {
        return true;
    }

    @Override
    public boolean supportsOffset() {
        return true;  // Oracle 12c+
    }

    @Override
    public String limit(int offset, int limit) {
        StringBuilder sql = new StringBuilder();
        if (offset > 0) {
            sql.append("OFFSET ").append(offset).append(" ROWS ");
        }
        sql.append("FETCH NEXT ").append(limit).append(" ROWS ONLY");
        return sql.toString();
    }

    @Override
    public String limit(int limit) {
        return "FETCH NEXT " + limit + " ROWS ONLY";
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
        return "SYSDATE";
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
        return column1 + " || " + column2;
    }

    @Override
    public String getDateDiffFunction(String date1, String date2) {
        return date1 + " - " + date2;
    }

    @Override
    public String getDateFormatFunction(String column, String format) {
        // Oracle使用TO_CHAR函数
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
