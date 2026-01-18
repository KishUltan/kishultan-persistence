package com.kishultan.persistence.query.expression;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.dialect.DatabaseDialect;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.clause.StandardCriterion;

/**
 * 日期格式化表达式
 * 根据数据库方言自动选择正确的日期格式化函数
 * 
 * MySQL: DATE_FORMAT(column, 'format')
 * PostgreSQL/Oracle: TO_CHAR(column, 'format')
 * SQL Server: FORMAT(column, 'format')
 * H2: FORMATDATETIME(column, 'format')
 */
public class DateFormatExpression extends AbstractSelectExpression {
    private final Columnable<?, ?> field;
    private final String format;
    private final String tableAlias;
    private final DatabaseDialect dialect;
    private final Criterion<?> criterion; // 用于延迟获取方言
    
    public <T, R> DateFormatExpression(Columnable<T, R> field, String format, String tableAlias, DatabaseDialect dialect) {
        this(field, format, tableAlias, dialect, null);
    }
    
    public <T, R> DateFormatExpression(Columnable<T, R> field, String format, String tableAlias, DatabaseDialect dialect, Criterion<?> criterion) {
        this.field = field;
        this.format = format;
        this.tableAlias = tableAlias;
        this.dialect = dialect;
        this.criterion = criterion;
    }
    
    @Override
    protected String getSqlWithoutAlias() {
        String fieldName = ColumnabledLambda.getColumnName(field);
        String qualifiedField = tableAlias != null ? tableAlias + "." + fieldName : fieldName;
        
        // 优先使用传入的dialect，否则从queryBuilder获取
        DatabaseDialect actualDialect = dialect;
        if (actualDialect == null && criterion instanceof StandardCriterion) {
            actualDialect = ((StandardCriterion<?>) criterion).getDialect();
        }
        
        if (actualDialect != null) {
            return actualDialect.getDateFormatFunction(qualifiedField, format);
        }
        
        // 默认使用MySQL格式（向后兼容）
        return "DATE_FORMAT(" + qualifiedField + ", '" + format + "')";
    }
    
    public Columnable<?, ?> getField() {
        return field;
    }
    
    public String getFormat() {
        return format;
    }
    
    public String getTableAlias() {
        return tableAlias;
    }
    
    public DatabaseDialect getDialect() {
        return dialect;
    }
    
    public Criterion<?> getCriterion() {
        return criterion;
    }
}
