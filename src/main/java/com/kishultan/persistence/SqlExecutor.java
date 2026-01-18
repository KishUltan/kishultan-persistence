package com.kishultan.persistence;

import com.kishultan.persistence.datasource.DataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.function.Function;

/**
 * SQL 执行器工具类
 * <p>
 * 提供完整的 SQL 执行能力，完全不依赖第三方库。
 * 充分利用现有的 DataSourceManager 进行数据源管理和连接获取。
 */
public final class SqlExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SqlExecutor.class);

    private SqlExecutor() {
        // 工具类，不允许实例化
    }

    /**
     * 执行 SQL 函数闭包
     *
     * @param functional SQL 函数
     * @param <V>        返回类型
     * @return 函数执行结果
     */
    public static <V> V sqlExecute(SqlFunction<V> functional) {
        logger.debug("执行 SQL 函数闭包");
        Connection connection = null;
        try {
            connection = DataSourceManager.getConnection();
            return functional.execute(connection);
        } catch (Exception e) {
            logger.error("执行 SQL 函数时发生异常", e);
            throw new RuntimeException("执行 SQL 函数时发生异常", e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * 执行带可变参数的 SQL 函数闭包
     *
     * @param functional SQL 函数
     * @param args       参数
     * @param <V>        返回类型
     * @return 函数执行结果
     */
    public static <V> V sqlExecute(SqlVarArgsFunction<V> functional, Object... args) {
        logger.debug("执行带参数的 SQL 函数闭包，参数: {}", args);
        Connection connection = null;
        try {
            connection = DataSourceManager.getConnection();
            return functional.execute(connection, args);
        } catch (Exception e) {
            logger.error("执行带参数的 SQL 函数时发生异常", e);
            throw new RuntimeException("执行带参数的 SQL 函数时发生异常", e);
        } finally {
            closeConnection(connection);
        }
    }

    /**
     * 执行更新 SQL（INSERT、UPDATE、DELETE）
     *
     * @param sql        SQL 语句
     * @param parameters 参数
     * @return 影响的行数
     */
    public static int executeUpdate(String sql, Object... parameters) {
        logger.debug("执行更新 SQL: {}", sql);
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DataSourceManager.getConnection();
            statement = connection.prepareStatement(sql);
            setParameters(statement, parameters);
            int result = statement.executeUpdate();
            logger.debug("SQL 更新执行完成，影响行数: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("执行更新 SQL 时发生异常: {}", sql, e);
            throw new RuntimeException("执行更新 SQL 时发生异常: " + sql, e);
        } finally {
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    /**
     * 执行查询 SQL
     *
     * @param sql        SQL 语句
     * @param parameters 参数
     * @return 查询结果列表
     */
    public static List<Object[]> executeQuery(String sql, Object... parameters) {
        logger.debug("执行查询 SQL: {}", sql);
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = DataSourceManager.getConnection();
            statement = connection.prepareStatement(sql);
            setParameters(statement, parameters);
            resultSet = statement.executeQuery();
            return extractResultSet(resultSet);
        } catch (Exception e) {
            logger.error("执行查询 SQL 时发生异常: {}", sql, e);
            throw new RuntimeException("执行查询 SQL 时发生异常: " + sql, e);
        } finally {
            closeResultSet(resultSet);
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    /**
     * 执行查询 SQL 并转换结果
     *
     * @param sql        SQL 语句
     * @param rowMapper  行映射器
     * @param parameters 参数
     * @param <T>        返回类型
     * @return 转换后的结果列表
     */
    public static <T> List<T> executeQuery(String sql, Function<Object[], T> rowMapper, Object... parameters) {
        logger.debug("执行查询 SQL 并转换结果: {}", sql);
        List<Object[]> rawResults = executeQuery(sql, parameters);
        List<T> results = new ArrayList<>();
        for (Object[] row : rawResults) {
            results.add(rowMapper.apply(row));
        }
        return results;
    }

    /**
     * 执行批量更新
     *
     * @param sql             SQL 语句
     * @param batchParameters 批量参数
     * @return 每批影响的行数数组
     */
    public static int[] executeBatch(String sql, List<Object[]> batchParameters) {
        logger.debug("执行批量更新 SQL: {}，批次数量: {}", sql, batchParameters.size());
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DataSourceManager.getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            for (Object[] parameters : batchParameters) {
                setParameters(statement, parameters);
                statement.addBatch();
            }
            int[] results = statement.executeBatch();
            connection.commit();
            logger.debug("批量更新执行完成，批次结果: {}", results);
            return results;
        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                logger.error("回滚事务时发生异常", rollbackEx);
            }
            logger.error("执行批量更新 SQL 时发生异常: {}", sql, e);
            throw new RuntimeException("执行批量更新 SQL 时发生异常: " + sql, e);
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error("恢复自动提交时发生异常", e);
            }
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    /**
     * 在事务中执行 SQL 函数
     *
     * @param functional SQL 函数
     * @param <V>        返回类型
     * @return 函数执行结果
     */
    public static <V> V executeInTransaction(SqlFunction<V> functional) {
        logger.debug("在事务中执行 SQL 函数");
        Connection connection = null;
        try {
            connection = DataSourceManager.getConnection();
            connection.setAutoCommit(false);
            V result = functional.execute(connection);
            connection.commit();
            logger.debug("事务执行成功");
            return result;
        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                logger.error("回滚事务时发生异常", rollbackEx);
            }
            logger.error("在事务中执行 SQL 函数时发生异常", e);
            throw new RuntimeException("在事务中执行 SQL 函数时发生异常", e);
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error("恢复自动提交时发生异常", e);
            }
            closeConnection(connection);
        }
    }

    /**
     * 在事务中执行带参数的 SQL 函数
     *
     * @param functional SQL 函数
     * @param args       参数
     * @param <V>        返回类型
     * @return 函数执行结果
     */
    public static <V> V executeInTransaction(SqlVarArgsFunction<V> functional, Object... args) {
        logger.debug("在事务中执行带参数的 SQL 函数，参数: {}", args);
        Connection connection = null;
        try {
            connection = DataSourceManager.getConnection();
            connection.setAutoCommit(false);
            V result = functional.execute(connection, args);
            connection.commit();
            logger.debug("事务执行成功");
            return result;
        } catch (Exception e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException rollbackEx) {
                logger.error("回滚事务时发生异常", rollbackEx);
            }
            logger.error("在事务中执行带参数的 SQL 函数时发生异常", e);
            throw new RuntimeException("在事务中执行带参数的 SQL 函数时发生异常", e);
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.error("恢复自动提交时发生异常", e);
            }
            closeConnection(connection);
        }
    }

    /**
     * 获取指定数据源的连接
     *
     * @param dataSourceName 数据源名称
     * @return 数据库连接
     */
    public static Connection getConnection(String dataSourceName) {
        try {
            return DataSourceManager.getConnection(dataSourceName);
        } catch (Exception e) {
            logger.error("获取数据源连接时发生异常: {}", dataSourceName, e);
            throw new RuntimeException("获取数据源连接时发生异常: " + dataSourceName, e);
        }
    }

    /**
     * 获取默认数据源的连接
     *
     * @return 数据库连接
     */
    public static Connection getConnection() {
        try {
            return DataSourceManager.getConnection();
        } catch (Exception e) {
            logger.error("获取默认数据源连接时发生异常", e);
            throw new RuntimeException("获取默认数据源连接时发生异常", e);
        }
    }


    /**
     * @param dsName
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static boolean execute(
            String dsName,
            String sql,
            Object[] params)
            throws SQLException, NamingException {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = DataSourceManager.getConnection(dsName);
            st = conn.prepareStatement(sql);
            if (params != null) {
                int numParams = params.length;
                for (int i = 0; i < numParams; i++) {
                    st.setObject(i + 1, params[i]);
                }
            }
            boolean result = st.execute();
            return result;
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeStatement(st);
            closeConnection(conn);
        }
    }

    /**
     * @param ds
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static boolean execute(
            DataSource ds,
            String sql,
            Object[] params)
            throws SQLException {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = ds.getConnection();
            st = conn.prepareStatement(sql);
            if (params != null) {
                int numParams = params.length;
                for (int i = 0; i < numParams; i++) {
                    st.setObject(i + 1, params[i]);
                }
            }
            boolean result = st.execute();
            return result;
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeStatement(st);
            closeConnection(conn);
        }
    }

    /**
     * @param dsName
     * @param sql
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static boolean execute(String dsName, String sql)
            throws SQLException, NamingException {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = DataSourceManager.getConnection(dsName);
            st = conn.prepareStatement(sql);
            boolean result = st.execute();
            return result;
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeStatement(st);
            closeConnection(conn);
        }
    }

    /**
     * @param ds
     * @param sql
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static boolean execute(DataSource ds, String sql)
            throws SQLException {
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = ds.getConnection();
            st = conn.prepareStatement(sql);
            boolean result = st.execute();
            return result;
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeStatement(st);
            closeConnection(conn);
        }
    }


    /**
     * @param dsName
     * @param sql
     * @param sqlType
     * @param format
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static String getField(
            String dsName,
            String sql,
            int sqlType,
            String format)
            throws SQLException, NamingException {
        return getField(dsName, sql, sqlType, format, null);
    }

    /**
     * @param ds
     * @param sql
     * @param sqlType
     * @param format
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static String getField(
            DataSource ds,
            String sql,
            int sqlType,
            String format)
            throws SQLException {
        return getField(ds, sql, sqlType, format, null);
    }

    /**
     * Date and Timestamp are formatted based on Locale provided (optional)
     *
     * @param dsName
     * @param sql
     * @param sqlType
     * @param format
     * @param locale
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static String getField(
            String dsName,
            String sql,
            int sqlType,
            String format,
            Locale locale)
            throws SQLException, NamingException {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet results = null;
        try {
            conn = DataSourceManager.getConnection(dsName);
            st = conn.prepareStatement(sql);
            results = st.executeQuery();
            if (!results.next()) {
                return null;
            }
            return getFormattedValue(results, 1, sqlType, format, locale);
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeResultSet(results);
            closeStatement(st);
            closeConnection(conn);
        }
    }

    /**
     * Date and Timestamp are formatted based on Locale provided (optional)
     *
     * @param ds
     * @param sql
     * @param sqlType
     * @param format
     * @param locale
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static String getField(
            DataSource ds,
            String sql,
            int sqlType,
            String format,
            Locale locale)
            throws SQLException {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet results = null;
        try {
            conn = ds.getConnection();
            st = conn.prepareStatement(sql);
            results = st.executeQuery();
            if (!results.next()) {
                return null;
            }
            return getFormattedValue(results, 1, sqlType, format, locale);
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeResultSet(results);
            closeStatement(st);
            closeConnection(conn);
        }
    }

    public static java.sql.Date getDateField(
            String dsName,
            String sql)
            throws SQLException, NamingException {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet results = null;
        try {
            conn = DataSourceManager.getConnection(dsName);
            st = conn.prepareStatement(sql);
            results = st.executeQuery();
            if (!results.next()) {
                return null;
            }
            return results.getDate(1);
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeResultSet(results);
            closeStatement(st);
            closeConnection(conn);
        }
    }

    public static java.sql.Date getDateField(
            DataSource ds,
            String sql)
            throws SQLException {
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet results = null;
        try {
            conn = ds.getConnection();
            st = conn.prepareStatement(sql);
            results = st.executeQuery();
            if (!results.next()) {
                return null;
            }
            return results.getDate(1);
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeResultSet(results);
            closeStatement(st);
            closeConnection(conn);
        }
    }

    /**
     * @param dsName
     * @param sql
     * @param sqlType
     * @param format
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static Vector getFields(
            String dsName,
            String sql,
            int sqlType,
            String format)
            throws SQLException, NamingException {
        Connection conn = null;
        try {
            conn = DataSourceManager.getConnection(dsName);
            return getFields(conn, sql, sqlType, format);
        } finally {
            closeConnection(conn);
        }
    }

    /**
     * @param ds
     * @param sql
     * @param sqlType
     * @param format
     * @return
     * @throws SQLException
     * @throws NamingException
     */
    public static Vector getFields(
            DataSource ds,
            String sql,
            int sqlType,
            String format)
            throws SQLException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            return getFields(conn, sql, sqlType, format);
        } finally {
            closeConnection(conn);
        }
    }

    @SuppressWarnings("unchecked")
    private static Vector getFields(Connection conn, String sql,
                                    int sqlType, String format) throws SQLException {
        PreparedStatement st = null;
        ResultSet results = null;
        try {
            st = conn.prepareStatement(sql);
            results = st.executeQuery();
            Vector fieldVec = new Vector();
            while (results.next()) {
                fieldVec.addElement(
                        getFormattedValue(results, 1, sqlType, format));
            }
            return fieldVec;
        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            throw ex;
        } finally {
            closeResultSet(results);
            closeStatement(st);
        }
    }

    /**
     * @param rs
     * @param colIndex
     * @param colType
     * @param format
     * @return
     * @throws SQLException
     */
    public static String getFormattedValue(
            ResultSet rs,
            int colIndex,
            int colType,
            String format)
            throws SQLException {
        switch (colType) {
            case java.sql.Types.INTEGER:
            case java.sql.Types.DECIMAL:
                if (format == null)
                    return Integer.toString(rs.getInt(colIndex));
                else
                    return new DecimalFormat(format).format(
                            rs.getInt(colIndex));
            case java.sql.Types.FLOAT:
                if (format == null)
                    return Float.toString(rs.getFloat(colIndex));
                else
                    return new DecimalFormat(format).format(
                            rs.getFloat(colIndex));
            case java.sql.Types.DOUBLE:
                if (format == null)
                    return Double.toString(rs.getDouble(colIndex));
                else
                    return new DecimalFormat(format).format(
                            rs.getDouble(colIndex));
            case java.sql.Types.TIMESTAMP:
                java.sql.Timestamp ts = rs.getTimestamp(colIndex);
                if (ts == null) return "N/A";
                if (format == null)
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts);
                else
                    return new SimpleDateFormat(format).format(ts);
            case java.sql.Types.DATE:
                java.sql.Date date = rs.getDate(colIndex);
                if (date == null)
                    return "N/A";
                if (format == null)
                    return new SimpleDateFormat("yyyy-MM-dd").format(date);
                else
                    return new SimpleDateFormat(format).format(date);
            case java.sql.Types.BOOLEAN:
                return Boolean.toString(rs.getBoolean(colIndex));
            default:
                return rs.getString(colIndex);
        }
    }

    public static String getFormattedValue(
            ResultSet rs,
            int colIndex,
            int colType,
            String format,
            Locale locale)
            throws SQLException {
        switch (colType) {
            case java.sql.Types.INTEGER:
            case java.sql.Types.DECIMAL:
                if (format == null)
                    return Integer.toString(rs.getInt(colIndex));
                else
                    return new DecimalFormat(format).format(
                            rs.getInt(colIndex));
            case java.sql.Types.FLOAT:
                if (format == null)
                    return Float.toString(rs.getFloat(colIndex));
                else
                    return new DecimalFormat(format).format(
                            rs.getFloat(colIndex));
            case java.sql.Types.DOUBLE:
                if (format == null)
                    return Double.toString(rs.getDouble(colIndex));
                else
                    return new DecimalFormat(format).format(
                            rs.getDouble(colIndex));
            case java.sql.Types.TIMESTAMP:
                if (format == null)
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale != null ? locale : Locale.getDefault()).format(
                            rs.getTimestamp(colIndex));
                else if (locale != null)
                    return new SimpleDateFormat(format, locale).format(
                            rs.getTimestamp(colIndex));
                else
                    return new SimpleDateFormat(format).format(
                            rs.getTimestamp(colIndex));
            case java.sql.Types.DATE:
                java.sql.Date date = rs.getDate(colIndex);
                if (date == null)
                    return "N/A";
                if (format == null)
                    return new SimpleDateFormat("yyyy-MM-dd", locale != null ? locale : Locale.getDefault()).format(date);
                else if (locale != null)
                    return new SimpleDateFormat(format, locale).format(date);
                else
                    return new SimpleDateFormat(format).format(date);
            case java.sql.Types.BOOLEAN:
                return Boolean.toString(rs.getBoolean(colIndex));
            default:
                return rs.getString(colIndex);
        }
    }

    // 私有辅助方法


    /**
     * 设置 PreparedStatement 参数
     */
    private static void setParameters(PreparedStatement statement, Object... parameters) throws SQLException {
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
        }
    }

    /**
     * 提取 ResultSet 结果
     */
    private static List<Object[]> extractResultSet(ResultSet resultSet) throws SQLException {
        List<Object[]> results = new ArrayList<>();
        int columnCount = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = resultSet.getObject(i + 1);
            }
            results.add(row);
        }
        return results;
    }

    /**
     * 关闭连接
     */
    private static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("关闭数据库连接时发生异常", e);
            }
        }
    }

    /**
     * 关闭语句
     */
    private static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("关闭语句时发生异常", e);
            }
        }
    }

    /**
     * 关闭结果集
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("关闭结果集时发生异常", e);
            }
        }
    }
}
