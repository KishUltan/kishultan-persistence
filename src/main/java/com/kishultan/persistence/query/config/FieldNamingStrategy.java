package com.kishultan.persistence.query.config;

/**
 * 字段命名转换策略（单一策略）
 * 用于策略链配置
 */
public enum FieldNamingStrategy {
    /**
     * 注解优先：优先使用@Column注解指定名称
     */
    ANNOTATION,
    
    /**
     * 原样匹配：直接使用数据库返回的列名匹配Java属性名
     */
    EXACT_MATCH,
    
    /**
     * 转驼峰：数据库下划线命名(user_name) -> Java驼峰命名(userName)
     */
    CAMEL_CASE,
    
    /**
     * 驼峰转下划线：Java驼峰命名(userName) -> 数据库下划线命名(user_name)
     */
    UNDERLINE,
    
    /**
     * 忽略大小写：匹配时不区分大小写
     */
    CASE_INSENSITIVE,

    /**
     * 转大写：将列名转换为大写后匹配
     */
    UPPER_CASE,

    /**
     * 转小写：将列名转换为小写后匹配
     */
    LOWER_CASE
}
