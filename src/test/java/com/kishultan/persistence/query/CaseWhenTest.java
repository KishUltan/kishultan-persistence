package com.kishultan.persistence.query;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.SelectClause;
import com.kishultan.persistence.query.clause.StandardCriterion;
import com.kishultan.persistence.query.expression.Functions;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;

/**
 * CASE WHEN表达式测试类
 * 
 * 使用新的Expression方式，通过column()方法使用CaseWhen表达式
 */
public class CaseWhenTest {
    
    private DataSource dataSource;
    private Criterion<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 为了测试，创建一个模拟的Criterion
        queryBuilder = new StandardCriterion<>(TestEntity.class, null);
    }
    
    @Test
    public void testSimpleCaseWhen() {
        // 测试简单CASE表达式
        // CASE status WHEN 'A' THEN 'Active' WHEN 'I' THEN 'Inactive' ELSE 'Unknown' END
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(Functions.caseWhen(TestEntity::getStatus, "te")
                .when("active").then("启用")
                .when("inactive").then("禁用")
                .elseResult("未知")
                .end()).as("status_text")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testSearchCaseWhen() {
        // 测试搜索CASE表达式
        // CASE WHEN age < 18 THEN 'Minor' WHEN age >= 65 THEN 'Senior' ELSE 'Adult' END
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(Functions.caseWhen()
                .when("age >= 18").then("成年人")
                .when("age >= 13").then("青少年")
                .elseResult("儿童")
                .end()).as("age_group")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testComplexCaseWhen() {
        // 测试复杂CASE表达式
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(Functions.caseWhen()
                .when("score >= 90").then("优秀")
                .when("score >= 80").then("良好")
                .when("score >= 70").then("中等")
                .when("score >= 60").then("及格")
                .elseResult("不及格")
                .end()).as("grade")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testFieldValueCaseWhen() {
        // 测试字段值CASE表达式
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(Functions.caseWhen(TestEntity::getType, "te")
                .when("A").then(TestEntity::getName)
                .when("B").then(TestEntity::getCode)
                .elseResult("未知类型")
                .end()).as("display_name")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testCaseWhenWithFrom() {
        // 测试CASE表达式与FROM子句结合
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(Functions.caseWhen(TestEntity::getStatus, "te")
                .when("active").then("启用")
                .when("inactive").then("禁用")
                .elseResult("未知")
                .end()).as("status_text")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testCaseWhenErrorHandling() {
        // 测试错误处理 - 没有WHEN条件应该抛出异常
        try {
            Functions.caseWhen().end();
            fail("应该抛出异常：CASE表达式必须至少有一个WHEN条件");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("WHEN") || e.getMessage().contains("when"));
        }
        
        // 测试错误处理 - THEN之前必须有WHEN
        try {
            Functions.caseWhen().then("value").end();
            fail("应该抛出异常：THEN之前必须有WHEN条件");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("THEN") || e.getMessage().contains("then"));
        }
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private String status;
        private String type;
        private String code;
        private Integer age;
        private Integer score;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }
}
