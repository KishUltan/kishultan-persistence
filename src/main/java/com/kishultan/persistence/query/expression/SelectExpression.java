package com.kishultan.persistence.query.expression;

import java.util.List;

/**
 * SELECT表达式接口
 * 用于表示SELECT子句中的表达式（函数、子查询等）
 */
public interface SelectExpression {
    /**
     * 转换为SQL字符串
     * 
     * @return SQL字符串
     */
    String toSql();
    
    /**
     * 获取表达式参数
     * 
     * @return 参数列表
     */
    List<Object> getParameters();
    
    /**
     * 设置别名
     * 
     * @param alias 别名
     * @return 带别名的表达式
     */
    SelectExpression as(String alias);
    
    /**
     * 获取别名
     * 
     * @return 别名，如果没有则返回null
     */
    String getAlias();
}
