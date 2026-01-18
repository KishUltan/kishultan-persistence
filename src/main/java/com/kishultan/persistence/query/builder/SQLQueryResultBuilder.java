package com.kishultan.persistence.query.builder;

import com.kishultan.persistence.dialect.DatabaseDialect;
import com.kishultan.persistence.query.ClauseBuilder;
import com.kishultan.persistence.query.JoinClause;
import com.kishultan.persistence.query.context.ClauseResult;
import com.kishultan.persistence.query.context.QueryBuildContext;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 查询结果构建器
 * 将 QueryBuildContext 转换为 SQL 语句，并收集查询参数
 * 支持数据库方言，能够根据不同数据库生成相应的 LIMIT 子句
 */
public class SQLQueryResultBuilder implements QueryResultBuilder {
    
    /**
     * 构建查询并收集参数
     */
    public QueryResultWithParams buildQueryWithParams(QueryBuildContext<?> context) {
        StringBuilder sql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        // SELECT 子句
        if (context.getSelectClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getSelectClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // FROM 子句
        if (context.getFromClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getFromClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // JOIN 子句
        if (!context.getJoinClauseList().isEmpty()) {
            for (JoinClause<?> joinClause : context.getJoinClauseList()) {
                ClauseResult result = ((ClauseBuilder<?>) joinClause).buildClause();
                if (!result.getSql().isEmpty()) {
                    sql.append(result.getSql()).append(" ");
                    if (result.getParameters() != null) {
                        parameters.addAll(result.getParameters());
                    }
                }
            }
        } else if (context.getJoinClause() != null) {
            // 兼容旧的单一 JOIN 子句
            ClauseResult result = ((ClauseBuilder<?>) context.getJoinClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // WHERE 子句
        if (context.getWhereClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getWhereClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // GROUP BY 子句
        if (context.getGroupByClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getGroupByClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // HAVING 子句
        if (context.getHavingClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getHavingClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // ORDER BY 子句
        if (context.getOrderByClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getOrderByClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // LIMIT 子句 - 使用数据库方言生成
        if (context.hasLimit()) {
            String limitSql = buildLimitClause(context);
            if (!limitSql.isEmpty()) {
                sql.append(limitSql).append(" ");
            }
        }
        
        return new QueryResultWithParams(sql.toString().trim(), parameters);
    }
    
    /**
     * 构建计数查询并收集参数
     */
    public QueryResultWithParams buildCountQueryWithParams(QueryBuildContext<?> context) {
        StringBuilder countSql = new StringBuilder();
        List<Object> parameters = new ArrayList<>();
        
        countSql.append("SELECT COUNT(*) ");
        
        // FROM 子句
        if (context.getFromClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getFromClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // JOIN 子句
        if (!context.getJoinClauseList().isEmpty()) {
            for (JoinClause<?> joinClause : context.getJoinClauseList()) {
                ClauseResult result = ((ClauseBuilder<?>) joinClause).buildClause();
                if (!result.getSql().isEmpty()) {
                    countSql.append(result.getSql()).append(" ");
                    if (result.getParameters() != null) {
                        parameters.addAll(result.getParameters());
                    }
                }
            }
        } else if (context.getJoinClause() != null) {
            // 兼容旧的单一 JOIN 子句
            ClauseResult result = ((ClauseBuilder<?>) context.getJoinClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // WHERE 子句
        if (context.getWhereClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getWhereClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // GROUP BY 子句（计数查询通常不需要 GROUP BY，但保留以支持分组计数）
        if (context.getGroupByClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getGroupByClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        // HAVING 子句（计数查询通常不需要 HAVING，但保留以支持分组过滤）
        if (context.getHavingClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getHavingClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
                if (result.getParameters() != null) {
                    parameters.addAll(result.getParameters());
                }
            }
        }
        
        return new QueryResultWithParams(countSql.toString().trim(), parameters);
    }
    
    /**
     * 查询结果和参数
     */
    public static class QueryResultWithParams {
        private final String sql;
        private final List<Object> parameters;
        
        public QueryResultWithParams(String sql, List<Object> parameters) {
            this.sql = sql;
            this.parameters = parameters;
        }
        
        public String getSql() {
            return sql;
        }
        
        public List<Object> getParameters() {
            return parameters;
        }
    }
    
    @Override
    public String buildQuery(QueryBuildContext<?> context) {
        StringBuilder sql = new StringBuilder();
        
        // SELECT 子句
        if (context.getSelectClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getSelectClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
            }
        }
        
        // FROM 子句
        if (context.getFromClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getFromClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
            }
        }
        
        // JOIN 子句
        if (!context.getJoinClauseList().isEmpty()) {
            for (JoinClause<?> joinClause : context.getJoinClauseList()) {
                ClauseResult result = ((ClauseBuilder<?>) joinClause).buildClause();
                if (!result.getSql().isEmpty()) {
                    sql.append(result.getSql()).append(" ");
                }
            }
        } else if (context.getJoinClause() != null) {
            // 兼容旧的单一 JOIN 子句
            ClauseResult result = ((ClauseBuilder<?>) context.getJoinClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
            }
        }
        
        // WHERE 子句
        if (context.getWhereClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getWhereClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
            }
        }
        
        // GROUP BY 子句
        if (context.getGroupByClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getGroupByClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
            }
        }
        
        // HAVING 子句
        if (context.getHavingClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getHavingClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
            }
        }
        
        // ORDER BY 子句
        if (context.getOrderByClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getOrderByClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                sql.append(result.getSql()).append(" ");
            }
        }
        
        // LIMIT 子句 - 使用数据库方言生成
        if (context.hasLimit()) {
            String limitSql = buildLimitClause(context);
            if (!limitSql.isEmpty()) {
                sql.append(limitSql).append(" ");
            }
        }
        
        return sql.toString().trim();
    }
    
    @Override
    public String buildCountQuery(QueryBuildContext<?> context) {
        StringBuilder countSql = new StringBuilder();
        
        countSql.append("SELECT COUNT(*) ");
        
        // FROM 子句
        if (context.getFromClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getFromClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
            }
        }
        
        // JOIN 子句
        if (!context.getJoinClauseList().isEmpty()) {
            for (JoinClause<?> joinClause : context.getJoinClauseList()) {
                ClauseResult result = ((ClauseBuilder<?>) joinClause).buildClause();
                if (!result.getSql().isEmpty()) {
                    countSql.append(result.getSql()).append(" ");
                }
            }
        } else if (context.getJoinClause() != null) {
            // 兼容旧的单一 JOIN 子句
            ClauseResult result = ((ClauseBuilder<?>) context.getJoinClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
            }
        }
        
        // WHERE 子句
        if (context.getWhereClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getWhereClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
            }
        }
        
        // GROUP BY 子句
        if (context.getGroupByClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getGroupByClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
            }
        }
        
        // HAVING 子句
        if (context.getHavingClause() != null) {
            ClauseResult result = ((ClauseBuilder<?>) context.getHavingClause()).buildClause();
            if (!result.getSql().isEmpty()) {
                countSql.append(result.getSql()).append(" ");
            }
        }
        
        return countSql.toString().trim();
    }
    
    @Override
    public String getBuilderType() {
        return "sql";
    }
    
    /**
     * 构建 LIMIT 子句，使用数据库方言
     * 
     * @param context SQL 构建上下文
     * @return LIMIT 子句 SQL
     */
    private String buildLimitClause(QueryBuildContext<?> context) {
        int offset = context.getOffsetValue();
        int limit = context.getLimitValue();
        DatabaseDialect dialect = context.getDialect();
        
        if (dialect == null) {
            // 默认使用标准 LIMIT/OFFSET 语法
            return "LIMIT " + limit + (offset > 0 ? " OFFSET " + offset : "");
        }
        
        // 使用数据库方言生成 LIMIT 子句
        if (offset > 0) {
            return dialect.limit(offset, limit);
        } else {
            return dialect.limit(limit);
        }
    }
}
