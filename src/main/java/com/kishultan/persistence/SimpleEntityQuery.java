package com.kishultan.persistence;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.WhereClause;

/**
 * 简单的EntityQuery实现类
 * 直接委托给QueryBuilder执行，不重新实现查询逻辑
 */
public class SimpleEntityQuery<T> extends AbstractQuery<T> implements EntityQuery<T> {
    private WhereClause<T> whereClause;

    public SimpleEntityQuery(Class<T> entityClass, Criterion<T> criterion) {
        super(entityClass, criterion);
    }

    /**
     * 获取实体类（供内部类使用）
     */
    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * 获取查询构建器（供内部类使用）
     */
    public Criterion<T> getCriterion() {
        return criterion;
    }

    // ==================== 基础查询条件 ====================
    @Override
    public QueryCondition<T> where() {
        // 确保每次都返回同一个WhereClause实例
        if (whereClause == null) {
            if (criterion != null) {
                whereClause = criterion.select().from(entityClass).where();
            }
        }
        return new SimpleQueryCondition(this);
    }

    @Override
    public EntityQuery<T> where(java.util.function.Consumer<QueryCondition<T>> whereBuilder) {
        if (whereBuilder != null) {
            // 确保 whereClause 已初始化
            if (whereClause == null) {
                if (criterion != null) {
                    whereClause = criterion.select().from(entityClass).where();
                }
            }
            // 使用 Consumer 构建 where 条件
            whereBuilder.accept(new SimpleQueryCondition(this));
        }
        return this;
    }

    /*@Override
    public QueryCondition or() {
        // 确保whereClause已初始化，然后设置OR操作符
        if (whereClause == null && criterion != null) {
            whereClause = criterion.select().from(entityClass).where();
        }
        if (whereClause != null) {
            whereClause.or();
        }
        return new SimpleQueryCondition(this);
    }
    @Override
    public EntityQuery<T> or(java.util.function.Consumer<QueryCondition> orBuilder) {
        // 暂时简单实现
        return this;
    }*/
    // ==================== 字段选择 ====================
    @Override
    public EntityQuery<T> select(String... columns) {
        // 直接委托给QueryBuilder
        if (criterion != null && columns != null) {
            criterion.select(columns);
        }
        return this;
    }

    @Override
    public EntityQuery<T> select(Columnable<T, ?>... columns) {
        // 直接委托给QueryBuilder
        if (criterion != null && columns != null) {
            criterion.select(columns);
        }
        return this;
    }

    @Override
    public EntityQuery<T> selectAll() {
        // 直接委托给QueryBuilder
        if (criterion != null) {
            criterion.selectAll();
        }
        return this;
    }

    // @Override
    // public EntityQuery<T> distinct() {
    //     // TODO: SelectClause中没有distinct方法，暂时不实现
    //     return this;
    // }
    // ==================== 排序 ====================
    // 排序方法已在AbstractQuery中实现
    // ==================== 分页 ====================
    // @Override
    // public EntityQuery<T> setFirstRow(int firstRow) {
    //     // TODO: 需要实现分页逻辑，暂时不实现
    //     return this;
    // }
    // @Override
    // public EntityQuery<T> setMaxRows(int maxRows) {
    //     // TODO: 需要实现分页逻辑，暂时不实现
    //     return this;
    // }
    // ==================== 分组和聚合 ====================
    // 分组方法已在AbstractQuery中实现
    @Override
    public QueryCondition having() {
        return new SimpleQueryCondition(this);
    }
    // ==================== 查询执行 ====================
    // 执行方法已在AbstractQuery中实现
    // @Override
    // public Stream<T> streamWithPagination(int pageSize) {
    //     // TODO: 需要实现分页流逻辑，暂时不实现
    //     return Stream.empty();
    // }
    // ==================== 分页控制 ====================
    // 分页方法已在AbstractQuery中实现
    // 注意：如果需要更复杂的查询功能，请直接使用 Criterion
    // EntityQuery 设计为简单查询的完整接口，不暴露底层实现
    // ==================== 内部类 ====================

    /**
     * 简单的QueryCondition实现
     * 直接操作QueryBuilder中的WhereClause实例
     * 继承AbstractQuery以复用执行方法的实现
     */
    private class SimpleQueryCondition extends AbstractQuery<T> implements QueryCondition<T> {
        private final SimpleEntityQuery<T> parent;

        public SimpleQueryCondition(SimpleEntityQuery<T> parent) {
            super(parent.entityClass, parent.criterion);
            this.parent = parent;
        }

        @Override
        public QueryCondition eq(String column, Object value) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.eq(column, value);
            }
            return this;
        }

        @Override
        public QueryCondition ne(String column, Object value) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.ne(column, value);
            }
            return this;
        }

        @Override
        public QueryCondition gt(String column, Object value) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.gt(column, value);
            }
            return this;
        }

        @Override
        public QueryCondition ge(String column, Object value) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.ge(column, value);
            }
            return this;
        }

        @Override
        public QueryCondition lt(String column, Object value) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.lt(column, value);
            }
            return this;
        }

        @Override
        public QueryCondition le(String column, Object value) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.le(column, value);
            }
            return this;
        }

        @Override
        public QueryCondition like(String column, String value) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.like(column, value);
            }
            return this;
        }

        @Override
        public QueryCondition in(String column, Object... values) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.in(column, values);
            }
            return this;
        }

        @Override
        public QueryCondition in(String column, java.util.Collection<?> values) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.in(column, values);
            }
            return this;
        }

        @Override
        public QueryCondition notIn(String column, Object... values) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.notIn(column, values);
            }
            return this;
        }

        @Override
        public QueryCondition notIn(String column, java.util.Collection<?> values) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.notIn(column, values);
            }
            return this;
        }

        @Override
        public QueryCondition isNull(String column) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.isNull(column);
            }
            return this;
        }

        @Override
        public QueryCondition isNotNull(String column) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.isNotNull(column);
            }
            return this;
        }

        @Override
        public QueryCondition between(String column, Object start, Object end) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.between(column, start, end);
            }
            return this;
        }

        // Lambda表达式方法
        @Override
        public <E, R> QueryCondition<T> eq(Columnable<E, R> fieldSelector, R value) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return eq(fieldName, value);
        }

        @Override
        public <E, R> QueryCondition<T> ne(Columnable<E, R> fieldSelector, R value) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return ne(fieldName, value);
        }

        @Override
        public <E, R> QueryCondition<T> gt(Columnable<E, R> fieldSelector, R value) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return gt(fieldName, value);
        }

        @Override
        public <E, R> QueryCondition<T> ge(Columnable<E, R> fieldSelector, R value) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return ge(fieldName, value);
        }

        @Override
        public <E, R> QueryCondition<T> lt(Columnable<E, R> fieldSelector, R value) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return lt(fieldName, value);
        }

        @Override
        public <E, R> QueryCondition<T> le(Columnable<E, R> fieldSelector, R value) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return le(fieldName, value);
        }

        @Override
        public <E, R> QueryCondition<T> like(Columnable<E, R> fieldSelector, String value) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return like(fieldName, value);
        }

        @Override
        public <E, R> QueryCondition<T> in(Columnable<E, R> fieldSelector, Object... values) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return in(fieldName, values);
        }

        @Override
        public <E, R> QueryCondition<T> in(Columnable<E, R> fieldSelector, java.util.Collection<?> values) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return in(fieldName, values);
        }

        @Override
        public <E, R> QueryCondition<T> notIn(Columnable<E, R> fieldSelector, Object... values) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return notIn(fieldName, values);
        }

        @Override
        public <E, R> QueryCondition<T> notIn(Columnable<E, R> fieldSelector, java.util.Collection<?> values) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return notIn(fieldName, values);
        }

        @Override
        public <E, R> QueryCondition<T> isNull(Columnable<E, R> fieldSelector) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return isNull(fieldName);
        }

        @Override
        public <E, R> QueryCondition<T> isNotNull(Columnable<E, R> fieldSelector) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return isNotNull(fieldName);
        }

        @Override
        public <E, R> QueryCondition<T> between(Columnable<E, R> fieldSelector, R start, R end) {
            String fieldName = ColumnabledLambda.getColumnName(fieldSelector);
            return between(fieldName, start, end);
        }

        // 逻辑操作符
        @Override
        public QueryCondition and() {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.and();
            }
            return this;
        }

        @Override
        public QueryCondition<T> and(java.util.function.Consumer<QueryCondition<T>> andBuilder) {
            if (parent.whereClause != null && andBuilder != null) {
                // 创建适配器，将QueryCondition的Consumer转换为WhereClause的Consumer
                parent.whereClause.and(whereClauseBuilder -> andBuilder.accept(this));
            }
            return this;
        }

        @Override
        public QueryCondition or() {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.or();
            }
            return this;
        }

        @Override
        public QueryCondition<T> or(java.util.function.Consumer<QueryCondition<T>> orBuilder) {
            if (parent.whereClause != null && orBuilder != null) {
                // 创建适配器，将QueryCondition的Consumer转换为WhereClause的Consumer
                parent.whereClause.or(whereClauseBuilder -> orBuilder.accept(this));
            }
            return this;
        }

        // @Override
        // public QueryCondition leftParen() {
        //     // TODO: WhereClause中没有leftParen方法，暂时不实现
        //     return this;
        // }
        // @Override
        // public QueryCondition rightParen() {
        //     // TODO: WhereClause中没有rightParen方法，暂时不实现
        //     return this;
        // }
        // 子查询条件
        @Override
        public QueryCondition exists(String subQuery) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.exists(subQuery);
            }
            return this;
        }

        @Override
        public QueryCondition notExists(String subQuery) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.notExists(subQuery);
            }
            return this;
        }

        @Override
        public QueryCondition in(String column, String subQuery) {
            // 直接操作QueryBuilder中的WhereClause实例
            if (parent.whereClause != null) {
                parent.whereClause.in(column, subQuery);
            }
            return this;
        }
    }
}
