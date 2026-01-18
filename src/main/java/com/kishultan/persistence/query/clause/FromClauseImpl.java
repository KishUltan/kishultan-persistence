package com.kishultan.persistence.query.clause;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.*;
import com.kishultan.persistence.query.context.ClauseResult;
import com.kishultan.persistence.query.context.ClauseData;
import com.kishultan.persistence.query.utils.EntityUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * FromClause 实现类
 */
public class FromClauseImpl<T> extends AbstractClause<T> implements FromClause<T>, ClauseBuilder<T>, CommonClause<T>, ClauseData {
    private final String tableName;
    private final String tableAlias;
    private final Class<?> entityClass; // 添加实体类字段

    public FromClauseImpl(Criterion<T> criterion, String tableName, String tableAlias) {
        super(criterion);
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.entityClass = null; // 字符串构造器不设置实体类
    }

    public FromClauseImpl(Criterion<T> criterion) {
        super(criterion);
        this.tableName = null;
        this.tableAlias = null;
        this.entityClass = null;
    }

    // 添加带实体类的构造器
    public FromClauseImpl(Criterion<T> criterion, Class<?> entityClass, String tableName, String tableAlias) {
        super(criterion);
        this.tableName = tableName;
        this.tableAlias = tableAlias;
        this.entityClass = entityClass;
    }

    @Override
    public JoinClause<T> innerJoin(Class<?> entityClass, String alias) {
        if (criterion instanceof StandardCriterion) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "INNER JOIN", entityClass, alias);
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }

    @Override
    public JoinClause<T> join(Class<?> entityClass) {
        String alias = generateAlias(entityClass);
        return innerJoin(entityClass, alias);
    }

    @Override
    public JoinClause<T> innerJoin(Class<?> entityClass) {
        String alias = generateAlias(entityClass);
        return innerJoin(entityClass, alias);
    }

    @Override
    public JoinClause<T> innerJoin(String tableName, String alias) {
        if (criterion instanceof StandardCriterion) {
            // 通过表名推断实体类，这里需要根据业务逻辑处理
            // 暂时抛出异常，提示需要使用带entityClass的方法
            throw new UnsupportedOperationException(
                    "innerJoin(String tableName, String alias) 方法已废弃，" +
                            "请使用 innerJoin(Class<?> entityClass, String alias) 方法，" +
                            "或者提供表名对应的实体类信息");
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }

    @Override
    public JoinClause<T> leftJoin(Class<?> entityClass, String alias) {
        if (criterion instanceof StandardCriterion) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "LEFT JOIN", entityClass, alias);
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }

    @Override
    public JoinClause<T> leftJoin(Class<?> entityClass) {
        String alias = generateAlias(entityClass);
        return leftJoin(entityClass, alias);
    }

    @Override
    public JoinClause<T> leftJoin(String tableName, String alias) {
        if (criterion instanceof StandardCriterion) {
            // 通过表名推断实体类，这里需要根据业务逻辑处理
            // 暂时抛出异常，提示需要使用带entityClass的方法
            throw new UnsupportedOperationException(
                    "leftJoin(String tableName, String alias) 方法已废弃，" +
                            "请使用 leftJoin(Class<?> entityClass, String alias) 方法，" +
                            "或者提供表名对应的实体类信息");
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }

    @Override
    public JoinClause<T> rightJoin(Class<?> entityClass, String alias) {
        if (criterion instanceof StandardCriterion) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "RIGHT JOIN", entityClass, alias);
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }

    @Override
    public JoinClause<T> fullJoin(Class<?> entityClass, String alias) {
        if (criterion instanceof StandardCriterion) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "FULL JOIN", entityClass, alias);
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }

    @Override
    public JoinClause<T> crossJoin(Class<?> entityClass, String alias) {
        if (criterion instanceof StandardCriterion) {
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "CROSS JOIN", entityClass, alias);
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }
    
    // ==================== JOIN子查询方法重载 ====================
    
    @Override
    public JoinClause<T> innerJoin(Criterion<?> subquery, String alias) {
        if (criterion instanceof StandardCriterion) {
            String subquerySql = subquery.getGeneratedSql();
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "INNER JOIN", "(" + subquerySql + ")", alias);
            // 保存子查询引用以便后续参数收集
            if (subquery instanceof StandardCriterion) {
                ((StandardCriterion<T>) criterion).setSubquery((StandardCriterion<?>) subquery);
            }
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }
    
    @Override
    public JoinClause<T> leftJoin(Criterion<?> subquery, String alias) {
        if (criterion instanceof StandardCriterion) {
            String subquerySql = subquery.getGeneratedSql();
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "LEFT JOIN", "(" + subquerySql + ")", alias);
            // 保存子查询引用以便后续参数收集
            if (subquery instanceof StandardCriterion) {
                ((StandardCriterion<T>) criterion).setSubquery((StandardCriterion<?>) subquery);
            }
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }
    
    @Override
    public JoinClause<T> rightJoin(Criterion<?> subquery, String alias) {
        if (criterion instanceof StandardCriterion) {
            String subquerySql = subquery.getGeneratedSql();
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "RIGHT JOIN", "(" + subquerySql + ")", alias);
            // 保存子查询引用以便后续参数收集
            if (subquery instanceof StandardCriterion) {
                ((StandardCriterion<T>) criterion).setSubquery((StandardCriterion<?>) subquery);
            }
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }
    
    @Override
    public JoinClause<T> fullJoin(Criterion<?> subquery, String alias) {
        if (criterion instanceof StandardCriterion) {
            String subquerySql = subquery.getGeneratedSql();
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "FULL JOIN", "(" + subquerySql + ")", alias);
            // 保存子查询引用以便后续参数收集
            if (subquery instanceof StandardCriterion) {
                ((StandardCriterion<T>) criterion).setSubquery((StandardCriterion<?>) subquery);
            }
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }
    
    @Override
    public JoinClause<T> crossJoin(Criterion<?> subquery, String alias) {
        if (criterion instanceof StandardCriterion) {
            String subquerySql = subquery.getGeneratedSql();
            JoinClause<T> joinClause = new JoinClauseImpl<>((StandardCriterion<T>) criterion, "CROSS JOIN", "(" + subquerySql + ")", alias);
            // 保存子查询引用以便后续参数收集
            if (subquery instanceof StandardCriterion) {
                ((StandardCriterion<T>) criterion).setSubquery((StandardCriterion<?>) subquery);
            }
            ((StandardCriterion<T>) criterion).addJoinClause(joinClause);
            return joinClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for join operations");
        }
    }

    @Override
    public WhereClause<T> where() {
        if (criterion instanceof StandardCriterion) {
            WhereClause<T> whereClause = new WhereClauseImpl<>((StandardCriterion<T>) criterion);
            ((StandardCriterion<T>) criterion).setWhereClause(whereClause);
            return whereClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for where operations");
        }
    }

    @Override
    public WhereClause<T> where(Consumer<WhereClause<T>> whereBuilder) {
        if (criterion instanceof StandardCriterion) {
            WhereClause<T> whereClause = new WhereClauseImpl<>((StandardCriterion<T>) criterion);
            ((StandardCriterion<T>) criterion).setWhereClause(whereClause);
            // 应用Consumer中定义的条件
            if (whereBuilder != null) {
                whereBuilder.accept(whereClause);
            }
            return whereClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for where operations");
        }
    }

    @Override
    public GroupClause<T> groupBy() {
        if (criterion instanceof StandardCriterion) {
            GroupClause<T> groupClause = new GroupClauseImpl<>((StandardCriterion<T>) criterion);
            ((StandardCriterion<T>) criterion).setGroupClause(groupClause);
            return groupClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for groupBy operations");
        }
    }

    @Override
    public GroupClause<T> groupBy(String... columns) {
        if (criterion instanceof StandardCriterion) {
            GroupClause<T> groupClause = new GroupClauseImpl<>((StandardCriterion<T>) criterion);
            if (columns != null) {
                for (String column : columns) {
                    ((GroupClauseImpl<T>) groupClause).addColumn(column);
                }
            }
            ((StandardCriterion<T>) criterion).setGroupClause(groupClause);
            return groupClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for groupBy operations");
        }
    }

    @Override
    public <R> GroupClause<T> groupBy(Columnable<T, R>... columns) {
        if (criterion instanceof StandardCriterion) {
            GroupClause<T> groupClause = new GroupClauseImpl<>((StandardCriterion<T>) criterion);
            if (columns != null) {
                for (Columnable<T, R> column : columns) {
                    String fieldName = ColumnabledLambda.getFieldName(column);
                    ((GroupClauseImpl<T>) groupClause).addColumn(fieldName);
                }
            }
            ((StandardCriterion<T>) criterion).setGroupClause(groupClause);
            return groupClause;
        } else {
            throw new UnsupportedOperationException("Criterion must be StandardCriterion for groupBy operations");
        }
    }
    // end方法实现已移除，FromClause现在直接继承CommonClause，可以直接调用执行方法

    /**
     * 生成表别名
     */
    private String generateAlias(Class<?> entityClass) {
        String tableName = EntityUtils.getTableName(entityClass);
        return tableName; // 别名和表名保持一致
    }

    // ==================== 新架构方法 ====================
    @Override
    public ClauseResult buildClause() {
        if (tableName == null) {
            return new ClauseResult("", new ArrayList<>());
        }
        StringBuilder sql = new StringBuilder("FROM ").append(tableName);
        if (tableAlias != null) {
            sql.append(" AS ").append(tableAlias);
        }
        // 自动注册别名到QueryBuilder
        if (criterion instanceof StandardCriterion && entityClass != null) {
            ((StandardCriterion<T>) criterion).registerTable(entityClass, tableName, tableAlias != null ? tableAlias : tableName);
        }
        return new ClauseResult(sql.toString(), new ArrayList<>());
    }

    @Override
    public String getClauseSql() {
        return buildClause().getSql();
    }
    
    // ==================== CommonClause 接口实现 ====================
    
    @Override
    public ClauseData getClauseData() {
        return this;
    }
    
    // ==================== ClauseData 接口实现 ====================
    
    @Override
    public String getClauseType() {
        return "FROM";
    }
    
    @Override
    public String getRawString() {
        return getClauseSql();
    }
    
    /**
     * 转换为 Map 结构化数据
     * 
     * @return 包含 FROM 子句信息的 Map
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("clauseType", "FROM");
        map.put("tableName", tableName);
        map.put("tableAlias", tableAlias);
        map.put("entityClass", entityClass != null ? entityClass.getName() : null);
        return map;
    }
}
