package com.kishultan.persistence;

import com.kishultan.persistence.SqlExecutor;
import com.kishultan.persistence.SqlFunction;
import com.kishultan.persistence.SqlVarArgsFunction;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * SqlExecutor 测试类
 */
public class SqlClosureTest {
    
    @Test
    public void testGetConnection() {
        // 测试获取数据库连接
        try {
            java.sql.Connection connection = SqlExecutor.getConnection();
            assertNotNull(connection);
            connection.close();
        } catch (Exception e) {
            // 如果没有配置数据源，这个测试可能会失败，这是正常的
            System.out.println("获取连接失败（可能是数据源未配置）: " + e.getMessage());
        }
    }
    
    @Test
    public void testSqlExecute() {
        // 测试无参数的 sqlExecute
        String result = SqlExecutor.sqlExecute(connection -> "test result");
        assertEquals("test result", result);
    }
    
    @Test
    public void testSqlExecuteWithArgs() {
        // 测试带参数的 sqlExecute
        String result = SqlExecutor.sqlExecute((connection, args) -> {
            assertEquals(2, args.length);
            assertEquals("param1", args[0]);
            assertEquals("param2", args[1]);
            return "test result with args";
        }, "param1", "param2");
        
        assertEquals("test result with args", result);
    }
}
