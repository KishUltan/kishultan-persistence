package com.kishultan.persistence.query;

import com.kishultan.persistence.query.context.ClauseResult;

/**
 * 子句构建器接口，定义所有子句的构建契约
 */
public interface ClauseBuilder<T> {
    /**
     * 构建当前子句，返回SQL片段和参数
     */
    ClauseResult buildClause();

    /**
     * 获取当前子句的SQL片段（用于调试）
     */
    String getClauseSql();
}
