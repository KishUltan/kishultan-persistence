package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.ColumnabledLambda;
import com.kishultan.persistence.query.utils.EntityUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 所有 Clause 的抽象基类，提供公共方法
 */
public abstract class AbstractClause<T> implements CommonClause<T> {
    // Lambda 表别名缓存，避免重复的反射操作
    private static final Map<Class<?>, String> LAMBDA_TABLE_ALIAS_CACHE = new ConcurrentHashMap<>();
    protected final Criterion<T> criterion;

    protected AbstractClause(Criterion<T> criterion) {
        this.criterion = criterion;
    }

    /**
     * 获取查询构建器
     */
    protected Criterion<T> getCriterion() {
        return criterion;
    }

    /**
     * 构建最终的 SQL 查询
     */
    public String getGeneratedSql() {
        return criterion.getGeneratedSql();
    }

    /**
     * 执行查询并返回结果
     */
    public T findFirst() {
        return criterion.findFirst();
    }

    /**
     * 执行查询并返回单个结果（CommonClause接口要求）
     */
    public T findOne() {
        return criterion.findFirst();
    }

    /**
     * 执行查询并返回结果列表
     */
    public java.util.List<T> findList() {
        return criterion.findList();
    }

    /**
     * 执行查询并返回总数
     */
    public long count() {
        return criterion.count();
    }

    /**
     * ORDER BY 子句
     * 通过QueryBuilder接口创建OrderClause，避免直接依赖impl包
     */
    public OrderClause<T> orderBy() {
        return criterion.createOrderClause();
    }

    /**
     * 设置 LIMIT，返回 Criterion 表示完成当前子句
     */
    public Criterion<T> limit(int offset, int size) {
        criterion.limit(offset, size);
        return criterion;
    }

    /**
     * 结束当前查询构建，返回 Criterion
     */
    public Criterion<T> end() {
        return criterion;
    }
    // ==================== 表别名获取方法 ====================

    /**
     * 根据 Lambda 表达式获取正确的表别名
     * 这个方法应该被所有子句使用，确保表别名的一致性
     * 使用统一的反射工具类，避免重复的反射操作
     * <p>
     * 注意：简化实现，直接使用表名作为别名，避免依赖impl包
     */
    protected <E, R> String getTableAlias(Columnable<E, R> fieldSelector) {
        // 使用统一的反射工具类获取实体类
        ColumnabledLambda.FieldInfo fieldInfo = ColumnabledLambda.getFieldInfo(fieldSelector);
        if (fieldInfo == null) {
            throw new RuntimeException("无法从 Lambda 表达式中获取表别名: " + fieldSelector);
        }
        Class<?> targetClass = fieldInfo.getEntityClass();
        // 直接使用表名作为别名（简化实现，避免依赖impl包）
        return EntityUtils.getTableName(targetClass);
    }

    /**
     * 获取主表别名（作为后备方案，不推荐使用）
     *
     * @deprecated 请使用 getTableAlias 方法
     */
    @Deprecated
    protected String getMainTableAlias() {
        // 简化实现，返回null（避免依赖impl包）
        return null;
    }
}
