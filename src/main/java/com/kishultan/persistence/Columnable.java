package com.kishultan.persistence;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.FunctionQuery;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 可序列化的函数接口
 * 支持lambda表达式的序列化和反序列化
 * <p>
 * 从 query 包移到此包，以打破 query ↔ query.utils 的循环依赖
 * 虽然静态工厂方法依赖 query 包，但这是单向依赖，可以接受
 */
@FunctionalInterface
public interface Columnable<T, R> extends Function<T, R>, Serializable {
    /**
     * 获取列名（工具方法）
     */
    static <T, R> String getColumnName(Columnable<T, R> columnable) {
        ColumnabledLambda.FieldInfo info = ColumnabledLambda.getFieldInfo(columnable);
        return info != null ? info.getColumnName() : "unknown_column";
    }

    /**
     * 函数列
     */
    static <T> Columnable<T, Object> of(FunctionQuery function) {
        return new ColumnLiteral<>(function.toFunctionSql());
    }

    static <T> Columnable<T, Object> of(FunctionQuery function, String alias) {
        return new ColumnLiteral<>(function.toFunctionSql() + " AS " + alias);
    }

    /**
     * 标量子查询
     */
    static <T> Columnable<T, Object> of(Criterion<?> subquery) {
        return new ColumnLiteral<>("(" + subquery.getGeneratedSql() + ")");
    }

    static <T> Columnable<T, Object> of(Criterion<?> subquery, String alias) {
        return new ColumnLiteral<>("(" + subquery.getGeneratedSql() + ") AS " + alias);
    }

    /**
     * 字符串列名
     */
    static <T> Columnable<T, Object> of(String column) {
        return new ColumnLiteral<>(column);
    }

    // ==================== 静态工厂方法 ====================
    static <T> Columnable<T, Object> of(String column, String alias) {
        return new ColumnLiteral<>(column + " AS " + alias);
    }

    default boolean isField() {
        // Lambda表达式返回true，其他类型（ColumnLiteral）返回false
        return !(this instanceof ColumnLiteral);
    }

    /**
     * 获取字段名
     * 用于从lambda表达式中提取字段名
     */
    default String getFieldName() {
        ColumnabledLambda.FieldInfo info = ColumnabledLambda.getFieldInfo(this);
        return info != null ? info.getFieldName() : "unknown_field";
    }

    /**
     * 获取字段类型
     */
    default Class<?> getFieldType() {
        ColumnabledLambda.FieldInfo info = ColumnabledLambda.getFieldInfo(this);
        return info != null ? info.getFieldType() : Object.class;
    }

    /**
     * 如果是 Lambda 字段引用，返回列名
     * 普通 SQL 片段可以直接返回 expression()
     */
    default String columnName() {
        return toSql();
    }

    /**
     * 默认实现，返回列名
     * Lambda 字段引用会自动解析
     */
    default String toSql() {
        return ColumnabledLambda.getColumnName(this);
    }

    // ==================== 内部类：普通列/函数列/子查询列实现 ====================
    class ColumnLiteral<T> implements Columnable<T, Object> {
        private final String sql;

        public ColumnLiteral(String sql) {
            this.sql = sql;
        }

        @Override
        public Object apply(T t) {
            return null; // Lambda 不执行
        }

        @Override
        public String toSql() {
            return sql;
        }
    }
}
