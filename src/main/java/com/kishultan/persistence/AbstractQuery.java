package com.kishultan.persistence;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.GroupClause;
import com.kishultan.persistence.query.OrderClause;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 查询抽象基类
 * 实现CommonQuery接口的所有公共方法，委托给QueryBuilder执行
 */
public abstract class AbstractQuery<T> implements CommonQuery<T> {
    protected final Criterion<T> criterion;
    protected final Class<T> entityClass;

    protected AbstractQuery(Class<T> entityClass, Criterion<T> criterion) {
        this.entityClass = entityClass;
        this.criterion = criterion;
    }

    /**
     * 获取查询构建器
     */
    protected Criterion<T> getCriterion() {
        return criterion;
    }

    /**
     * 获取实体类
     */
    protected Class<T> getEntityClass() {
        return entityClass;
    }

    // ==================== 执行方法实现 ====================
    @Override
    public List<T> findList() {
        if (criterion == null) {
            throw new UnsupportedOperationException("Criterion is not available");
        }
        return criterion.findList();
    }

    @Override
    public T findOne() {
        if (criterion == null) {
            throw new UnsupportedOperationException("Criterion is not available");
        }
        return criterion.findFirst();
    }

    @Override
    public Optional<T> findOneOptional() {
        T result = findOne();
        return Optional.ofNullable(result);
    }

    @Override
    public long count() {
        if (criterion == null) {
            throw new UnsupportedOperationException("Criterion is not available");
        }
        return criterion.count();
    }

    @Override
    public Stream<T> stream() {
        if (criterion == null) {
            throw new UnsupportedOperationException("Criterion is not available");
        }
        List<T> results = criterion.findList();
        return results.stream();
    }

    // ==================== 排序方法实现 ====================
    @Override
    public CommonQuery<T> orderBy(String property) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接创建 OrderClause，避免清除之前的查询条件
            OrderClause<T> orderClause = criterion.createOrderClause();
            orderClause.asc(property);
        }
        return this;
    }

    @Override
    public CommonQuery<T> orderByAsc(String property) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接创建 OrderClause，避免清除之前的查询条件
            OrderClause<T> orderClause = criterion.createOrderClause();
            orderClause.asc(property);
        }
        return this;
    }

    @Override
    public CommonQuery<T> orderByDesc(String property) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接创建 OrderClause，避免清除之前的查询条件
            OrderClause<T> orderClause = criterion.createOrderClause();
            orderClause.desc(property);
        }
        return this;
    }

    @Override
    public <R> CommonQuery<T> orderBy(Columnable<T, R> property) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接创建 OrderClause，避免清除之前的查询条件
            // 将Columnable转换为列名
            String columnName = ColumnabledLambda.getColumnName(property);
            OrderClause<T> orderClause = criterion.createOrderClause();
            orderClause.asc(columnName);
        }
        return this;
    }

    @Override
    public <R> CommonQuery<T> orderByAsc(Columnable<T, R> property) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接创建 OrderClause，避免清除之前的查询条件
            // 将Columnable转换为列名
            String columnName = ColumnabledLambda.getColumnName(property);
            OrderClause<T> orderClause = criterion.createOrderClause();
            orderClause.asc(columnName);
        }
        return this;
    }

    @Override
    public <R> CommonQuery<T> orderByDesc(Columnable<T, R> property) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接创建 OrderClause，避免清除之前的查询条件
            // 将Columnable转换为列名
            String columnName = ColumnabledLambda.getColumnName(property);
            OrderClause<T> orderClause = criterion.createOrderClause();
            orderClause.desc(columnName);
        }
        return this;
    }

    // ==================== 分页方法实现 ====================
    @Override
    public CommonQuery<T> limit(int offset, int size) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接设置 limit，避免清除之前的查询条件
            criterion.limit(offset, size);
        }
        return this;
    }

    @Override
    public CommonQuery<T> limit(int size) {
        if (criterion != null) {
            // 🔧 修复：不调用 select()，直接设置 limit，避免清除之前的查询条件
            criterion.limit(0, size);
        }
        return this;
    }

    // ==================== 分组方法实现 ====================
    @Override
    public CommonQuery<T> groupBy(String... columns) {
        if (criterion != null && columns != null && columns.length > 0) {
            // 🔧 修复：不调用 select()，直接创建 GroupClause，避免清除之前的查询条件
            GroupClause<T> groupClause =
                criterion.createGroupClause();
            groupClause.column(columns);
        }
        return this;
    }

    @Override
    public <R> CommonQuery<T> groupBy(Columnable<T, R>... columns) {
        if (criterion != null && columns != null && columns.length > 0) {
            // 🔧 修复：不调用 select()，直接创建 GroupClause，避免清除之前的查询条件
            GroupClause<T> groupClause =
                criterion.createGroupClause();
            groupClause.column(columns);
        }
        return this;
    }
}
