package com.kishultan.persistence.query;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.SelectClause;
import com.kishultan.persistence.query.clause.StandardCriterion;

import static com.kishultan.persistence.query.expression.Functions.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;

/**
 * 窗口函数测试类
 * 
 * 使用新的Expression方式，通过column()方法使用窗口函数表达式
 */
public class WindowFunctionTest {
    
    private DataSource dataSource;
    private Criterion<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 为了测试，创建一个模拟的Criterion
        queryBuilder = new StandardCriterion<>(TestEntity.class, null);
    }
    
    @Test
    public void testBasicWindowFunctions() {
        // 测试基本窗口函数：ROW_NUMBER, RANK, DENSE_RANK
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(rowNumber().over()).as("row_num")
            .column(rank().over()).as("rank_num")
            .column(denseRank().over()).as("dense_rank_num")
            .column(percentRank().over()).as("percent_rank")
            .column(cumeDist().over()).as("cume_dist")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testOffsetWindowFunctions() {
        // 测试偏移窗口函数：LAG, LEAD
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(lag(TestEntity::getAmount, 1, "te").over()).as("prev_amount")
            .column(lead(TestEntity::getAmount, 1, "te").over()).as("next_amount")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testNtileFunction() {
        // 测试NTILE函数
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(ntile(4).over()).as("quartile")
            .column(ntile(10).over()).as("decile")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testValueWindowFunctions() {
        // 测试值窗口函数：FIRST_VALUE, LAST_VALUE, NTH_VALUE
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(firstValue(TestEntity::getName, "te").over()).as("first_name")
            .column(lastValue(TestEntity::getName, "te").over()).as("last_name")
            .column(nthValue(TestEntity::getName, 3, "te").over()).as("third_name")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testWindowWithPartitionAndOrder() {
        // 测试带PARTITION BY和ORDER BY的窗口函数
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(rowNumber()
                .over(TestEntity::getCategory, TestEntity::getCreateTime)).as("row_num_by_category")
            .column(rank()
                .over(TestEntity::getCategory, TestEntity::getScore)).as("rank_by_category")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testWindowWithFrom() {
        // 测试窗口函数与FROM子句结合
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(rowNumber().over()).as("row_num")
            .column(rank().over()).as("rank_num")
            .column(denseRank().over()).as("dense_rank_num")
            .column(lag(TestEntity::getAmount, 1, "te").over()).as("prev_amount")
            .column(lead(TestEntity::getAmount, 1, "te").over()).as("next_amount")
            .column(ntile(4).over()).as("quartile")
            .column(firstValue(TestEntity::getName, "te").over()).as("first_name")
            .column(lastValue(TestEntity::getName, "te").over()).as("last_name")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testWindowChain() {
        // 测试多个窗口函数链式调用
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(rowNumber().over()).as("row_num")
            .column(rank().over()).as("rank_num")
            .column(denseRank().over()).as("dense_rank_num")
            .column(percentRank().over()).as("percent_rank")
            .column(cumeDist().over()).as("cume_dist")
            .column(lag(TestEntity::getAmount, 1, "te").over()).as("prev_amount")
            .column(lead(TestEntity::getAmount, 1, "te").over()).as("next_amount")
            .column(ntile(4).over()).as("quartile")
            .column(firstValue(TestEntity::getName, "te").over()).as("first_name")
            .column(lastValue(TestEntity::getName, "te").over()).as("last_name")
            .column(nthValue(TestEntity::getName, 3, "te").over()).as("third_name")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    @Test
    public void testComplexWindowFunctions() {
        // 测试复杂窗口函数组合
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        selectClause
            .column(TestEntity::getId)
            .column(TestEntity::getCategory)
            .column(rowNumber()
                .over(TestEntity::getCategory, TestEntity::getCreateTime)).as("row_num_multi_partition")
            .column(rank()
                .over(TestEntity::getCategory, TestEntity::getScore)).as("rank_multi_order")
            .from("test_entity", "te");
        
        assertNotNull(selectClause);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private String category;
        private String status;
        private Double amount;
        private Double score;
        private java.util.Date createTime;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        
        public java.util.Date getCreateTime() { return createTime; }
        public void setCreateTime(java.util.Date createTime) { this.createTime = createTime; }
    }
}
