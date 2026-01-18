package com.kishultan.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 查询公共接口
 * 定义所有查询对象共有的方法，包括执行方法和链式调用方法
 */
public interface CommonQuery<T> {
    // ==================== 执行方法 ====================

    /**
     * 执行查询并返回列表
     *
     * @return 实体列表
     */
    List<T> findList();

    /**
     * 执行查询并返回单个结果
     *
     * @return 实体对象，如果不存在则返回null
     */
    T findOne();

    /**
     * 执行查询并返回Optional包装的结果
     *
     * @return Optional包装的实体对象
     */
    Optional<T> findOneOptional();

    /**
     * 执行查询并返回结果数量
     *
     * @return 结果数量
     */
    long count();

    /**
     * 执行查询并返回流
     *
     * @return 实体流
     */
    Stream<T> stream();
    // ==================== 排序方法 ====================

    /**
     * 设置排序（升序）
     *
     * @param property 排序属性
     * @return 查询对象
     */
    CommonQuery<T> orderBy(String property);

    /**
     * 设置排序（升序）- 明确指定ASC
     *
     * @param property 排序属性
     * @return 查询对象
     */
    CommonQuery<T> orderByAsc(String property);

    /**
     * 设置排序（降序）- 明确指定DESC
     *
     * @param property 排序属性
     * @return 查询对象
     */
    CommonQuery<T> orderByDesc(String property);

    /**
     * 使用Lambda表达式设置排序（升序）
     *
     * @param property Lambda表达式
     * @return 查询对象
     */
    <R> CommonQuery<T> orderBy(Columnable<T, R> property);

    /**
     * 使用Lambda表达式设置排序（升序）- 明确指定ASC
     *
     * @param property Lambda表达式
     * @return 查询对象
     */
    <R> CommonQuery<T> orderByAsc(Columnable<T, R> property);

    /**
     * 使用Lambda表达式设置排序（降序）- 明确指定DESC
     *
     * @param property Lambda表达式
     * @return 查询对象
     */
    <R> CommonQuery<T> orderByDesc(Columnable<T, R> property);
    // ==================== 分页方法 ====================

    /**
     * 设置分页限制
     *
     * @param offset 偏移量
     * @param size   页面大小
     * @return 查询对象
     */
    CommonQuery<T> limit(int offset, int size);

    /**
     * 设置分页限制（从第一条开始）
     *
     * @param size 页面大小
     * @return 查询对象
     */
    CommonQuery<T> limit(int size);
    // ==================== 分组方法 ====================

    /**
     * 设置分组
     *
     * @param columns 分组字段数组
     * @return 查询对象
     */
    CommonQuery<T> groupBy(String... columns);

    /**
     * 设置分组（Lambda表达式）
     *
     * @param columns Lambda表达式数组
     * @return 查询对象
     */
    <R> CommonQuery<T> groupBy(Columnable<T, R>... columns);
}
