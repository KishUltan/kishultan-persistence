package com.kishultan.persistence.query;

import java.util.List;

import com.kishultan.persistence.query.context.ClauseData;

/**
 * 所有 Clause 的公共方法接口
 * 定义了 orderBy、limit、end 等公共方法
 * 以及执行方法，让子句不必返回到builder就能调用
 */
public interface CommonClause<T> {
    /**
     * 获取子句数据（结构化数据）
     * 由 QueryResultBuilder 负责将这些数据转换为特定格式的查询（SQL、NoSQL等）
     * 
     * @return 子句数据对象
     */
    ClauseData getClauseData();
    /**
     * ORDER BY 子句
     */
    OrderClause<T> orderBy();

    /**
     * 设置 LIMIT
     */
    Criterion<T> limit(int offset, int size);

    /**
     * 结束当前查询构建
     */
    Criterion<T> end();
    // ==================== 执行方法 ====================
    // 执行方法定义到CommonClause中，可以让多个子句不必返回到builder就能调用

    /**
     * 执行查询并返回结果列表
     */
    List<T> findList();

    /**
     * 执行查询并返回单个结果
     */
    T findOne();

    /**
     * 执行查询并返回结果数量
     */
    long count();
}
