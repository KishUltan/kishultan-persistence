package com.kishultan.persistence.query;

import com.kishultan.persistence.query.clause.StandardCriterion;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * FromClause where(Consumer)方法简单测试
 * 只测试方法调用和返回值，不执行实际查询
 * 
 * @author Portal Team
 */
public class FromClauseWhereConsumerSimpleTest {
    
    @Mock
    private DataSource dataSource;
    
    private StandardCriterion<User> queryBuilder;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // 创建QueryBuilder
        queryBuilder = new StandardCriterion<>(User.class, dataSource);
    }
    
    @Test
    public void testWhereConsumerReturnsWhereClause() {
        // 测试新的where(Consumer)方法返回WhereClause
        WhereClause<User> whereClause = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"));
        
        // 验证返回的是WhereClause实例
        assertNotNull("WhereClause不应为null", whereClause);
        assertTrue("应返回WhereClause实例", whereClause instanceof WhereClause);
    }
    
    @Test
    public void testWhereConsumerWithMultipleConditions() {
        // 测试多个条件
        WhereClause<User> whereClause = queryBuilder
            .select()
            .from()
            .where(w -> {
                w.eq(User::getName, "John");
                w.like(User::getEmail, "@gmail.com");
                w.gt(User::getId, 100L);
            });
        
        // 验证返回的是WhereClause实例
        assertNotNull("WhereClause不应为null", whereClause);
        assertTrue("应返回WhereClause实例", whereClause instanceof WhereClause);
    }
    
    @Test
    public void testWhereConsumerWithNull() {
        // 测试传入null Consumer
        WhereClause<User> whereClause = queryBuilder
            .select()
            .from()
            .where(null);
        
        // 验证返回的是WhereClause实例
        assertNotNull("WhereClause不应为null", whereClause);
        assertTrue("应返回WhereClause实例", whereClause instanceof WhereClause);
    }
    
    @Test
    public void testWhereConsumerChaining() {
        // 测试链式调用
        WhereClause<User> whereClause = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"));
        
        // 验证可以继续链式调用
        WhereClause<User> chainedWhere = whereClause.and(w -> w.like(User::getEmail, "@gmail.com"));
        
        assertNotNull("链式调用结果不应为null", chainedWhere);
        assertTrue("应返回WhereClause实例", chainedWhere instanceof WhereClause);
    }
    
    @Test
    public void testWhereConsumerVsWhereMethod() {
        // 比较两种where方法的返回值类型
        WhereClause<User> where1 = queryBuilder
            .select()
            .from()
            .where();
        
        WhereClause<User> where2 = queryBuilder
            .select()
            .from()
            .where(w -> w.eq(User::getName, "John"));
        
        // 验证两种方法都返回WhereClause
        assertNotNull("where()方法结果不应为null", where1);
        assertNotNull("where(Consumer)方法结果不应为null", where2);
        assertTrue("where()应返回WhereClause实例", where1 instanceof WhereClause);
        assertTrue("where(Consumer)应返回WhereClause实例", where2 instanceof WhereClause);
    }
    
    // 测试用户类
    public static class User {
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



