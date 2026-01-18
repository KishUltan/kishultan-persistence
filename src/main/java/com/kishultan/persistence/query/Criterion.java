package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.query.config.FieldNamingStrategyChain;
import com.kishultan.persistence.query.config.CriterionConfig;
import com.kishultan.persistence.query.cache.QueryCache;
import com.kishultan.persistence.query.monitor.QueryMetrics;
import com.kishultan.persistence.query.monitor.QueryPerformanceMonitor;

import java.util.List;
import java.util.function.Consumer;

/**
 * 查询构建器基础接口
 * 提供查询构建和执行的核心功能
 * 支持Lambda表达式和类型安全查询
 */
public interface Criterion<T> {
    // 子查询构建
    //Criterion<T> subquery();

    // 主查询构建
    SelectClause<T> select();

    SelectClause<T> select(String... columns);

    SelectClause<T> select(Columnable<T, ?>... fields);

    SelectClause<T> selectAll();

    // ========== 配置支持 ==========
    
    /**
     * 获取当前查询构建器的配置
     */
    default CriterionConfig getConfig() {
        return null;
    }
    
    /**
     * 设置当前查询构建器的配置
     */
    default Criterion<T> setConfig(CriterionConfig config) {
        return this;
    }
    
    /**
     * 设置字段命名策略链
     */
    default Criterion<T> setFieldNamingStrategy(FieldNamingStrategyChain chain) {
        return this;
    }
    
    /**
     * 设置是否在找不到字段时输出警告日志
     */
    default Criterion<T> setWarnOnMissingField(Boolean warn) {
        return this;
    }
    
    /**
     * 设置严格模式
     */
    default Criterion<T> setStrictMode(Boolean strict) {
        return this;
    }

    // 执行方法
    List<T> findList();

    T findFirst();

    long count();

    String getGeneratedSql();

    // 子查询相关
    boolean isSubquery();

    String getSubquerySql();

    // 分页支持
    Criterion<T> limit(int offset, int size);

    // 当前查询字段引用
    String selfField(Columnable<T, ?> fieldSelector);

    // 子查询字段引用
    String subqueryField(Columnable<T, ?> fieldSelector);

    // 条件构建器模式 - 支持 Consumer 的 where 方法
    Criterion<T> where(Consumer<WhereClause<T>> whereBuilder);

    // 性能监控支持
    QueryMetrics getPerformanceMetrics();

    QueryPerformanceMonitor getPerformanceMonitor();

    // 缓存支持
    QueryCache getQueryCache();

    RowMapper<?> getRowMapper();

    // 自定义映射器支持
    Criterion setRowMapper(RowMapper rowMapper);
    // 子句创建方法（用于解耦query包和query.impl包）

    /**
     * 创建OrderClause实例
     *
     * @return OrderClause实例
     */
    OrderClause<T> createOrderClause();

    /**
     * 创建GroupClause实例
     *
     * @return GroupClause实例
     */
    GroupClause<T> createGroupClause();
}
