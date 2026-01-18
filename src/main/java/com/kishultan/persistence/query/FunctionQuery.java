package com.kishultan.persistence.query;

/**
 * 函数查询接口
 * 用于生成函数表达式SQL
 */
public interface FunctionQuery {
    /**
     * 生成函数表达式SQL
     *
     * @return 函数表达式SQL字符串
     */
    String toFunctionSql();
}