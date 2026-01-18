package com.kishultan.persistence.query;

import com.kishultan.persistence.query.Criterion;
import com.kishultan.persistence.query.expression.Functions;
import com.kishultan.persistence.query.FromClause;
import com.kishultan.persistence.query.SelectClause;
import com.kishultan.persistence.query.clause.StandardCriterion;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import javax.sql.DataSource;

/**
 * 聚合函数测试类
 * 使用新的 column() 方法API
 */
public class AggregateFunctionTest {
    
    private DataSource dataSource;
    private Criterion<TestEntity> queryBuilder;
    
    @Before
    public void setUp() {
        // 为了测试，创建一个模拟的Criterion
        queryBuilder = new StandardCriterion<>(TestEntity.class, null);
    }
    
    @Test
    public void testAggregateFunctionBasic() {
        // 测试基本的聚合函数调用 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        // 验证方法存在且可以调用
        assertNotNull(selectClause);
        
        // 测试计数函数
        SelectClause<TestEntity> countResult = selectClause.column(Functions.count(TestEntity::getId));
        assertNotNull(countResult);
        
        // 测试带别名的计数函数
        SelectClause<TestEntity> countWithAlias = selectClause.column(Functions.count(TestEntity::getId), "total_count");
        assertNotNull(countWithAlias);
        
        // 测试求和函数
        SelectClause<TestEntity> sumResult = selectClause.column(Functions.sum(TestEntity::getAmount));
        assertNotNull(sumResult);
        
        // 测试平均值函数
        SelectClause<TestEntity> avgResult = selectClause.column(Functions.avg(TestEntity::getAmount));
        assertNotNull(avgResult);
        
        // 测试最大值函数
        SelectClause<TestEntity> maxResult = selectClause.column(Functions.max(TestEntity::getAmount));
        assertNotNull(maxResult);
        
        // 测试最小值函数
        SelectClause<TestEntity> minResult = selectClause.column(Functions.min(TestEntity::getAmount));
        assertNotNull(minResult);
    }
    
    @Test
    public void testAggregateFunctionChain() {
        // 测试聚合函数链式调用 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        SelectClause<TestEntity> result = selectClause
            .column(Functions.count(TestEntity::getId), "total_count")
            .column(Functions.sum(TestEntity::getAmount), "total_amount")
            .column(Functions.avg(TestEntity::getAmount), "avg_amount")
            .column(Functions.max(TestEntity::getAmount), "max_amount")
            .column(Functions.min(TestEntity::getAmount), "min_amount");
        
        assertNotNull(result);
    }
    
    @Test
    public void testAggregateWithFrom() {
        // 测试聚合函数与FROM子句的组合 - 使用新的 column() 方法
        SelectClause<TestEntity> selectClause = queryBuilder.select();
        
        FromClause<TestEntity> fromClause = selectClause
            .column(Functions.count(TestEntity::getId), "total_count")
            .column(Functions.sum(TestEntity::getAmount), "total_amount")
            .from("test_entity", "te");
        
        assertNotNull(fromClause);
    }
    
    // 测试实体类
    public static class TestEntity {
        private Long id;
        private String name;
        private Double amount;
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
    }
}
