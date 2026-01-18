package com.kishultan.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 带可变参数的 SQL 函数接口
 * <p>
 * 定义在数据库连接上执行带参数的 SQL 操作的函数接口。
 * 这是对 SansORM SqlVarArgsFunction 的抽象，避免业务层直接依赖 SansORM 库。
 *
 * @param <T> 函数的返回类型
 */
@FunctionalInterface
public interface SqlVarArgsFunction<T> {
    /**
     * 在指定的数据库连接上执行带参数的 SQL 操作
     *
     * @param connection 数据库连接
     * @param args       可变参数
     * @return 操作结果
     * @throws SQLException SQL 异常
     */
    T execute(Connection connection, Object... args) throws SQLException;
}
