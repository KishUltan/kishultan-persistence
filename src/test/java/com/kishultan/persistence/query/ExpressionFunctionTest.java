package com.kishultan.persistence.query;

import com.kishultan.persistence.Columnable;
import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.expression.Functions;
import com.kishultan.persistence.query.FromClause;
import com.kishultan.persistence.query.SelectClause;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;
import com.kishultan.persistence.query.clause.StandardCriterion;

/**
 * 表达式函数测试类
 * 使用新的 column() 方法API
 */
public class ExpressionFunctionTest {
    
    private DataSource dataSource;
    private Criterion<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 为了测试，创建一个模拟的Criterion
        queryBuilder = new StandardCriterion<>(TestEntity.class, null);
    }
    
    @Test
    public void testStringFunctions() {
        // 测试字符串函数 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        // 测试大小写转换
        Columnable<TestEntity, String> nameField = TestEntity::getName;
        SelectClause<TestEntity> upperResult = selectClause.column(Functions.upper(nameField), "upper_name");
        assertNotNull(upperResult);
        
        SelectClause<TestEntity> lowerResult = selectClause.column(Functions.lower(nameField), "lower_name");
        assertNotNull(lowerResult);
        
        // 测试长度函数
        SelectClause<TestEntity> lengthResult = selectClause.column(Functions.length(nameField), "name_length");
        assertNotNull(lengthResult);
    }
    
    @Test
    public void testMathFunctions() {
        // 测试数学函数 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        // 测试基本运算
        Columnable<TestEntity, Number> amountField = TestEntity::getAmount;
        Columnable<TestEntity, Number> scoreField = TestEntity::getScore;
        SelectClause<TestEntity> absResult = selectClause.column(Functions.abs(amountField), "abs_amount");
        assertNotNull(absResult);
        
        // 测试四舍五入
        SelectClause<TestEntity> roundResult = selectClause.column(Functions.round(scoreField), "rounded_score");
        assertNotNull(roundResult);
    }
    
    @Test
    public void testExpressionWithFrom() {
        // 测试表达式函数与FROM子句的组合 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        FromClause<TestEntity> fromClause = selectClause
            .column(Functions.upper(TestEntity::getName), "upper_name")
            .column(Functions.lower(TestEntity::getEmail), "lower_email")
            .column(Functions.length(TestEntity::getName), "name_length")
            .column(Functions.round(TestEntity::getScore), "rounded_score")
            .from("test_entity", "te");
        
        assertNotNull(fromClause);
    }
    
    @Test
    public void testExpressionChain() {
        // 测试表达式函数链式调用 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        SelectClause<TestEntity> result = selectClause
            .column(Functions.upper(TestEntity::getName), "upper_name")
            .column(Functions.lower(TestEntity::getEmail), "lower_email")
            .column(Functions.length(TestEntity::getName), "name_length")
            .column(Functions.round(TestEntity::getScore), "rounded_score");
        
        assertNotNull(result);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private String email;
        private Double amount;
        private Double score;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }
}
