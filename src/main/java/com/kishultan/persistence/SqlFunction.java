package com.kishultan.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL 函数接口
 * <p>
 * 定义在数据库连接上执行 SQL 操作的函数接口。
 * 这是对 SansORM SqlFunction 的抽象，避免业务层直接依赖 SansORM 库。
 *
 * @param <T> 函数的返回类型
 */
@FunctionalInterface
public interface SqlFunction<T> {
    /**
     * 在指定的数据库连接上执行 SQL 操作
     *
     * @param connection 数据库连接
     * @return 操作结果
     * @throws SQLException SQL 异常
     */
    T execute(Connection connection) throws SQLException;
}
