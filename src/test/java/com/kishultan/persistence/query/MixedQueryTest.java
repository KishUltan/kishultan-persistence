package com.kishultan.persistence.query;

import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;

/**
 * 混合查询结果映射测试
 */
public class MixedQueryTest {
    
    @Mock
    private ResultSet resultSet;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testCustomRowMapper() throws Exception {
        // 模拟ResultSet数据
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("张三", "李四");
        when(resultSet.getLong("count")).thenReturn(10L, 20L);
        when(resultSet.getString("upperName")).thenReturn("ZHANGSAN", "LISI");
        when(resultSet.getString("ageGroup")).thenReturn("adult", "minor");
        
        // 创建自定义RowMapper
        RowMapper<UserStatistics> rowMapper = new UserStatisticsRowMapper();
        
        // 测试映射
        UserStatistics result1 = rowMapper.mapRow(resultSet, UserStatistics.class);
        UserStatistics result2 = rowMapper.mapRow(resultSet, UserStatistics.class);
        
        // 验证结果
        assertNotNull(result1);
        assertEquals("张三", result1.getName());
        assertEquals(Long.valueOf(10L), result1.getCount());
        assertEquals("ZHANGSAN", result1.getUpperName());
        assertEquals("adult", result1.getAgeGroup());
        
        assertNotNull(result2);
        assertEquals("李四", result2.getName());
        assertEquals(Long.valueOf(20L), result2.getCount());
        assertEquals("LISI", result2.getUpperName());
        assertEquals("minor", result2.getAgeGroup());
    }
    
    @Test
    public void testLambdaRowMapper() throws Exception {
        // 模拟ResultSet数据
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("王五");
        when(resultSet.getLong("count")).thenReturn(30L);
        
        // 使用Lambda表达式创建RowMapper
        RowMapper<UserStatistics> rowMapper = new RowMapper<UserStatistics>() {
            @Override
            public UserStatistics mapRow(ResultSet rs, Class<UserStatistics> resultType) throws Exception {
                return new UserStatistics(
                    rs.getString("name"),
                    rs.getLong("count"),
                    null,
                    null
                );
            }
        };
        
        // 测试映射
        UserStatistics result = rowMapper.mapRow(resultSet, UserStatistics.class);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("王五", result.getName());
        assertEquals(Long.valueOf(30L), result.getCount());
    }
    
    @Test
    public void testMapRowMapper() throws Exception {
        // 模拟ResultSet数据
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("赵六");
        when(resultSet.getLong("count")).thenReturn(40L);
        when(resultSet.getString("upperName")).thenReturn("ZHAOLIU");
        
        // 创建Map类型的RowMapper
        RowMapper<Map<String, Object>> rowMapper = new RowMapper<Map<String, Object>>() {
            @Override
            public Map<String, Object> mapRow(ResultSet rs, Class<Map<String, Object>> resultType) throws Exception {
                Map<String, Object> map = new HashMap<>();
                map.put("name", rs.getString("name"));
                map.put("count", rs.getLong("count"));
                map.put("upperName", rs.getString("upperName"));
                return map;
            }
        };
        
        // 测试映射
        @SuppressWarnings("unchecked")
        Map<String, Object> result = rowMapper.mapRow(resultSet, (Class<Map<String, Object>>) (Class<?>) Map.class);
        
        // 验证结果
        assertNotNull(result);
        assertEquals("赵六", result.get("name"));
        assertEquals(40L, result.get("count"));
        assertEquals("ZHAOLIU", result.get("upperName"));
    }
    
    
    /**
     * 测试用的用户统计类
     */
    public static class UserStatistics {
        private String name;
        private Long count;
        private String upperName;
        private String ageGroup;
        
        public UserStatistics() {}
        
        public UserStatistics(String name, Long count, String upperName, String ageGroup) {
            this.name = name;
            this.count = count;
            this.upperName = upperName;
            this.ageGroup = ageGroup;
        }
        
        // getters/setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
        public String getUpperName() { return upperName; }
        public void setUpperName(String upperName) { this.upperName = upperName; }
        public String getAgeGroup() { return ageGroup; }
        public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    }
    
    /**
     * 用户统计RowMapper实现
     */
    public static class UserStatisticsRowMapper implements RowMapper<UserStatistics> {
        @Override
        public UserStatistics mapRow(ResultSet rs, Class<UserStatistics> resultType) throws Exception {
            return new UserStatistics(
                rs.getString("name"),
                rs.getLong("count"),
                rs.getString("upperName"),
                rs.getString("ageGroup")
            );
        }
    }
}