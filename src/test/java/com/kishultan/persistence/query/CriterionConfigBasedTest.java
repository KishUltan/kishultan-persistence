package com.kishultan.persistence.query;

import com.kishultan.persistence.query.clause.StandardCriterion;
import com.kishultan.persistence.query.config.CriterionConfigManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * QueryBuilder基于配置的性能监控和缓存测试
 * 测试QueryBuilder通过配置管理器自动启用性能监控和缓存功能
 * 
 * @author Portal Team
 */
public class CriterionConfigBasedTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    private StandardCriterion<User> queryBuilder;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 设置Mock对象
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // 默认空结果集（findList查询）
        
        // 创建QueryBuilder
        queryBuilder = new StandardCriterion<>(User.class, dataSource);
        
        // 重置配置管理器
        CriterionConfigManager.reset();
    }
    
    @After
    public void tearDown() {
        // 清理系统属性
        System.clearProperty("querybuilder.performance.monitor.enabled");
        System.clearProperty("querybuilder.cache.enabled");
    }
    
    @Test
    public void testPerformanceMonitoringEnabled() {
        // 启用性能监控
        System.setProperty("querybuilder.performance.monitor.enabled", "true");
        
        // 执行查询
        List<User> users = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .findList();
        
        // 验证性能监控
        assertNotNull("性能监控器不应为null", queryBuilder.getPerformanceMonitor());
        assertNotNull("性能指标不应为null", queryBuilder.getPerformanceMetrics());
        
        // 验证性能指标
        assertTrue("应该有查询执行", queryBuilder.getPerformanceMetrics().getTotalQueryCount() > 0);
    }
    
    @Test
    public void testCacheEnabled() {
        // 启用缓存
        System.setProperty("querybuilder.cache.enabled", "true");
        
        // 执行查询
        List<User> users1 = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .findList();
        
        // 再次执行相同查询（应该从缓存获取）
        List<User> users2 = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .findList();
        
        // 验证缓存
        assertNotNull("缓存不应为null", queryBuilder.getQueryCache());
        assertTrue("缓存应启用", queryBuilder.getQueryCache().isEnabled());
    }
    
    @Test
    public void testBothEnabled() {
        // 同时启用性能监控和缓存
        System.setProperty("querybuilder.performance.monitor.enabled", "true");
        System.setProperty("querybuilder.cache.enabled", "true");
        
        // 执行查询
        List<User> users = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .findList();
        
        // 验证性能监控
        assertNotNull("性能监控器不应为null", queryBuilder.getPerformanceMonitor());
        assertNotNull("性能指标不应为null", queryBuilder.getPerformanceMetrics());
        
        // 验证缓存
        assertNotNull("缓存不应为null", queryBuilder.getQueryCache());
        assertTrue("缓存应启用", queryBuilder.getQueryCache().isEnabled());
    }
    
    @Test
    public void testBothDisabled() {
        // 禁用性能监控和缓存
        System.setProperty("querybuilder.performance.monitor.enabled", "false");
        System.setProperty("querybuilder.cache.enabled", "false");
        
        // 执行查询
        List<User> users = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .findList();
        
        // 验证性能监控未启用
        assertNull("性能监控器应为null", queryBuilder.getPerformanceMonitor());
        assertNull("性能指标应为null", queryBuilder.getPerformanceMetrics());
        
        // 验证缓存未启用
        assertNull("缓存应为null", queryBuilder.getQueryCache());
    }
    
    @Test
    public void testCountWithCache() throws Exception {
        // 启用缓存
        System.setProperty("querybuilder.cache.enabled", "true");
        
        // 为count查询设置Mock：ResultSet返回一行数据，第一列为Long类型的count值
        when(resultSet.next()).thenReturn(true, false); // 第一次返回true（有数据），之后返回false
        when(resultSet.getObject(1)).thenReturn(5L); // count查询返回5
        
        // 执行count查询
        long count1 = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .count();
        
        // 重置Mock以准备第二次查询（但应该从缓存获取，不会执行）
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getObject(1)).thenReturn(5L);
        
        // 再次执行相同count查询（应该从缓存获取）
        long count2 = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"))
            .count();
        
        // 验证结果
        assertEquals("第一次查询应该返回5", 5L, count1);
        assertEquals("第二次查询应该从缓存返回5", 5L, count2);
        
        // 验证缓存
        assertNotNull("缓存不应为null", queryBuilder.getQueryCache());
        assertTrue("缓存应启用", queryBuilder.getQueryCache().isEnabled());
    }
    
    // 测试用的User类
    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "user")
    public static class User {
        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String email;
        
        public User() {}
        
        public User(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}



