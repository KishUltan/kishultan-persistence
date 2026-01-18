package com.kishultan.persistence.query.executor;

import com.kishultan.persistence.query.RowMapper;
import com.kishultan.persistence.query.SqlExecutor;
import com.kishultan.persistence.query.clause.SimpleSqlExecutor;

import javax.sql.DataSource;
import java.util.List;

/**
 * SQL 查询执行器
 * 使用现有的 SqlExecutor 实现
 * 
 * @param <T> 实体类型
 */
public class SQLQueryExecutor<T> implements QueryExecutor<T> {
    
    private final SqlExecutor sqlExecutor;
    
    /**
     * 通过 DataSource 创建
     */
    public SQLQueryExecutor(DataSource dataSource) {
        this.sqlExecutor = new SimpleSqlExecutor(dataSource);
    }
    
    /**
     * 通过现有的 SqlExecutor 创建
     */
    public SQLQueryExecutor(SqlExecutor sqlExecutor) {
        this.sqlExecutor = sqlExecutor;
    }
    
    @Override
    public <R> List<R> executeQuery(
        String query,
        List<Object> parameters,
        Class<R> resultType,
        RowMapper<R> rowMapper
    ) {
        return sqlExecutor.executeQuery(query, parameters, resultType, rowMapper);
    }
    
    @Override
    public <R> R executeSingle(
        String query,
        List<Object> parameters,
        Class<R> resultType,
        RowMapper<R> rowMapper
    ) {
        List<R> results = executeQuery(query, parameters, resultType, rowMapper);
        return results.isEmpty() ? null : results.get(0);
    }
    
    @Override
    public long executeCount(String query, List<Object> parameters) {
        return sqlExecutor.executeAsLong(query, parameters);
    }
    
    @Override
    public int executeUpdate(String query, List<Object> parameters) {
        return sqlExecutor.executeUpdate(query, parameters);
    }
    
    @Override
    public String getExecutorType() {
        return "sql";
    }
}
