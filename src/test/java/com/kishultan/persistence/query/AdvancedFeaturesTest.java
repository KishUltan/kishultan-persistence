package com.kishultan.persistence.query;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.SelectClause;
import com.kishultan.persistence.query.FromClause;
import com.kishultan.persistence.query.expression.Functions;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;
import com.kishultan.persistence.query.clause.StandardCriterion;

import static com.kishultan.persistence.query.expression.Functions.*;

/**
 * 高级功能测试类
 * 测试聚合函数、窗口函数、表达式函数等新功能
 * 使用新的 column() 方法API
 */
public class AdvancedFeaturesTest {
    
    private DataSource dataSource;
    private Criterion<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 为了测试，创建一个模拟的Criterion
        queryBuilder = new StandardCriterion<>(TestEntity.class, null);
    }
    
    @Test
    public void testAggregateFunctions() {
        // 测试聚合函数 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        // 验证方法存在且可以调用
        assertNotNull(selectClause);
        
        // 测试计数函数
        SelectClause<TestEntity> countResult = selectClause.column(Functions.count(TestEntity::getId), "total_count");
        assertNotNull(countResult);
        
        // 测试求和函数
        SelectClause<TestEntity> sumResult = selectClause.column(Functions.sum(TestEntity::getAmount), "total_amount");
        assertNotNull(sumResult);
        
        // 测试平均值函数
        SelectClause<TestEntity> avgResult = selectClause.column(Functions.avg(TestEntity::getAmount), "avg_amount");
        assertNotNull(avgResult);
        
        // 测试最大值函数
        SelectClause<TestEntity> maxResult = selectClause.column(Functions.max(TestEntity::getAmount), "max_amount");
        assertNotNull(maxResult);
        
        // 测试最小值函数
        SelectClause<TestEntity> minResult = selectClause.column(Functions.min(TestEntity::getAmount), "min_amount");
        assertNotNull(minResult);
    }
    
    @Test
    public void testWindowFunctions() {
        // 测试窗口函数 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        // 验证方法存在且可以调用
        assertNotNull(selectClause);
        
        // 测试行号函数
        SelectClause<TestEntity> rowNumberResult = selectClause.column(rowNumber().over()).as("row_num");
        assertNotNull(rowNumberResult);
        
        // 测试排名函数
        SelectClause<TestEntity> rankResult = selectClause.column(rank().over()).as("rank_num");
        assertNotNull(rankResult);
        
        // 测试密集排名函数
        SelectClause<TestEntity> denseRankResult = selectClause.column(denseRank().over()).as("dense_rank_num");
        assertNotNull(denseRankResult);
        
        // 测试滞后值函数
        SelectClause<TestEntity> lagResult = selectClause.column(lag(TestEntity::getAmount, 1, "te").over()).as("prev_amount");
        assertNotNull(lagResult);
        
        // 测试领先值函数
        SelectClause<TestEntity> leadResult = selectClause.column(lead(TestEntity::getAmount, 1, "te").over()).as("next_amount");
        assertNotNull(leadResult);
    }
    
    @Test
    public void testExpressionFunctions() {
        // 测试表达式函数 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        // 验证方法存在且可以调用
        assertNotNull(selectClause);
        
        // 测试字符串函数
        SelectClause<TestEntity> upperResult = selectClause.column(Functions.upper(TestEntity::getName), "upper_name");
        assertNotNull(upperResult);
        
        SelectClause<TestEntity> lowerResult = selectClause.column(Functions.lower(TestEntity::getName), "lower_name");
        assertNotNull(lowerResult);
        
        SelectClause<TestEntity> lengthResult = selectClause.column(Functions.length(TestEntity::getName), "name_length");
        assertNotNull(lengthResult);
        
        // 测试数学函数
        SelectClause<TestEntity> absResult = selectClause.column(Functions.abs(TestEntity::getAmount), "abs_amount");
        assertNotNull(absResult);
        
        SelectClause<TestEntity> roundResult = selectClause.column(Functions.round(TestEntity::getAmount), "round_amount");
        assertNotNull(roundResult);
    }
    
    @Test
    public void testFunctionChaining() {
        // 测试函数链式调用 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        SelectClause<TestEntity> aggregateResult = selectClause
            .column(Functions.count(TestEntity::getId), "total_count")
            .column(Functions.sum(TestEntity::getAmount), "total_amount")
            .column(Functions.avg(TestEntity::getAmount), "avg_amount")
            .column(Functions.max(TestEntity::getAmount), "max_amount")
            .column(Functions.min(TestEntity::getAmount), "min_amount");
        
        assertNotNull(aggregateResult);
        
        // 测试与FROM子句的组合
        FromClause<TestEntity> fromClause = aggregateResult.from("test_entity", "te");
        assertNotNull(fromClause);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private Double amount;
        private java.util.Date createTime;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public java.util.Date getCreateTime() { return createTime; }
        public void setCreateTime(java.util.Date createTime) { this.createTime = createTime; }
    }
}
