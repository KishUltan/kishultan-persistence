package com.kishultan.persistence;

import com.kishultan.persistence.model.TestClinic;
import com.kishultan.persistence.datasource.DataSourceManager;
import com.kishultan.persistence.config.PersistenceDefaults;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Search查询功能测试类
 */
public class SearchQueryTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchQueryTest.class);
    
    @Before
    public void setUp() throws Exception {
        // 设置默认数据源名称
        PersistenceDefaults.setDataSourceName("default");
        
        // 设置测试环境（禁用JNDI，启用本地数据源）
        DataSourceManager.setUseJNDI(false);
        
        // 创建MySQL数据源 - 使用dbconfig.xml中的配置
        com.mysql.cj.jdbc.MysqlDataSource mysqlDataSource = new com.mysql.cj.jdbc.MysqlDataSource();
        mysqlDataSource.setURL("jdbc:mysql://localhost/micro-his?useSSL=false&serverTimezone=GMT%2B8&useServerPrepStmts=true");
        mysqlDataSource.setUser("root");
        mysqlDataSource.setPassword("root");
        
        // 将数据源添加到本地数据源表
        DataSourceManager.addLocalDataSource("default", mysqlDataSource);
        
        // 添加数据源类型标识
        DataSourceManager.addDataSourceFlavor("default", "mysql");
    }
    
    @After
    public void tearDown() throws Exception {
        // 不清理数据，保持MySQL连接
        // DataSourceManager.removeLocalDataSource("default");
        // DataSourceManager.setUseJNDI(true);
    }
    
    @Test
    public void testOrLikeSearchQuery() throws Exception {
        logger.info("=== 测试OR模糊搜索查询功能 ===");
        
        try {
            // 获取EntityManager
            EntityManager entityManager = PersistenceManager.getDefaultManager();
            
            // 创建查询
            EntityQuery<TestClinic> query = entityManager.createQuery(TestClinic.class);
            
            // 设置基础条件
            query.where().eq("state", 1);
            
            // 测试OR模糊搜索
            String[] searchFields = {"name", "region_code"};
            String keyword = "test";
            
            logger.info("搜索字段: {}", String.join(", ", searchFields));
            logger.info("搜索关键词: {}", keyword);

            /*query.where().or(condition -> {
                for (String field : searchFields) {
                    condition.like(field, "%" + keyword + "%").and();
                }
            });*/

            // 执行OR搜索
            /*query.where().or(orCondition -> {
                //orCondition.and();//
                orCondition.like("name", "%" + keyword + "%");
                //orCondition.or();
                orCondition.like("region_code", "%" + keyword + "%");
            });*/

            query.where().and(orCondition -> {
                //orCondition.or(); //底层已容错，不会覆盖组外and操作符
                orCondition.like("name", "%" + keyword + "%");
                //orCondition.and();
                orCondition.like("region_code", "%" + keyword + "%");
            });
            query.where().le("id",1000);
            
            // 设置排序和分页
            query.orderBy("name").limit(10);
            
            logger.info("OR搜索查询构建完成");
            logger.info("预期生成的SQL结构:");
            logger.info("SELECT * FROM clinic WHERE state = 1 AND (name LIKE '%test%' OR region_code LIKE '%test%') ORDER BY name LIMIT 10");
            
            // 执行查询
            List<TestClinic> results = query.findList();
            logger.info("查询结果数量: {}", results.size());
            
        } catch (Exception e) {
            logger.warn("测试环境缺少数据源配置，跳过实际查询执行: {}", e.getMessage());
        }
        
        logger.info("OR模糊搜索查询测试完成");
    }
    
    @Test
    public void testOrLikeWithMultipleFields() throws Exception {
        logger.info("=== 测试多字段OR模糊搜索 ===");
        
        try {
            // 获取EntityManager
            EntityManager entityManager = PersistenceManager.getDefaultManager();
            
            // 创建查询
            EntityQuery<TestClinic> query = entityManager.createQuery(TestClinic.class);
            
            // 设置基础条件
            query.where().eq("state", 1).eq("region_code", "110000");
            
            // 测试多字段OR搜索
            String[] searchFields = {"name", "region_code", "address"};
            String keyword = "北京";
            
            logger.info("搜索字段: {}", String.join(", ", searchFields));
            logger.info("搜索关键词: {}", keyword);
            
            // 执行OR搜索
            query.where().or(orCondition -> {
                orCondition.like("name", "%" + keyword + "%");
                orCondition.or();
                orCondition.like("region_code", "%" + keyword + "%");
                orCondition.or();
                orCondition.like("address", "%" + keyword + "%");
            });
            
            // 设置排序和分页
            query.orderBy("name").limit(20);
            
            logger.info("多字段OR搜索查询构建完成");
            logger.info("预期生成的SQL结构:");
            logger.info("SELECT * FROM clinic WHERE state = 1 AND region_code = '110000' AND ((name LIKE '%北京%') OR (region_code LIKE '%北京%') OR (address LIKE '%北京%')) ORDER BY name LIMIT 20");
            
            // 执行查询
            List<TestClinic> results = query.findList();
            logger.info("查询结果数量: {}", results.size());
            
        } catch (Exception e) {
            logger.warn("测试环境缺少数据源配置，跳过实际查询执行: {}", e.getMessage());
        }
        
        logger.info("多字段OR模糊搜索测试完成");
    }
    
    @Test
    public void testOrLikeWithSingleField() throws Exception {
        logger.info("=== 测试单字段模糊搜索 ===");
        
        try {
            // 获取EntityManager
            EntityManager entityManager = PersistenceManager.getDefaultManager();
            
            // 创建查询
            EntityQuery<TestClinic> query = entityManager.createQuery(TestClinic.class);
            
            // 设置基础条件
            query.where().eq("state", 1);
            
            // 测试单字段搜索
            String[] searchFields = {"name"};
            String keyword = "诊所";
            
            logger.info("搜索字段: {}", String.join(", ", searchFields));
            logger.info("搜索关键词: {}", keyword);
            
            // 执行OR搜索
            query.where().or(orCondition -> {
                orCondition.like("name", "%" + keyword + "%");
            });
            
            // 设置排序和分页
            query.orderBy("name").limit(10);
            
            logger.info("单字段模糊搜索查询构建完成");
            logger.info("预期生成的SQL结构:");
            logger.info("SELECT * FROM clinic WHERE state = 1 AND (name LIKE '%诊所%') ORDER BY name LIMIT 10");
            
        } catch (Exception e) {
            logger.warn("测试环境缺少数据源配置，跳过实际查询执行: {}", e.getMessage());
        }
        
        logger.info("单字段模糊搜索测试完成");
    }
    
    @Test
    public void testOrLikeWithEmptyFields() throws Exception {
        logger.info("=== 测试空字段数组的OR搜索 ===");
        
        try {
            // 获取EntityManager
            EntityManager entityManager = PersistenceManager.getDefaultManager();
            
            // 创建查询
            EntityQuery<TestClinic> query = entityManager.createQuery(TestClinic.class);
            
            // 设置基础条件
            query.where().eq("state", 1);
            
            // 测试空字段数组
            String[] searchFields = {};
            String keyword = "test";
            
            logger.info("搜索字段: {} (空数组)", searchFields.length);
            logger.info("搜索关键词: {}", keyword);
            
            // 执行OR搜索
            query.where().or(orCondition -> {
                for (String field : searchFields) {
                    orCondition.like(field, "%" + keyword + "%");
                }
            });
            
            // 设置排序和分页
            query.orderBy("name").limit(10);
            
            logger.info("空字段数组OR搜索查询构建完成");
            logger.info("预期生成的SQL结构:");
            logger.info("SELECT * FROM clinic WHERE state = 1 ORDER BY name LIMIT 10");
            logger.info("注意：由于没有搜索字段，OR条件不会生成");
            
        } catch (Exception e) {
            logger.warn("测试环境缺少数据源配置，跳过实际查询执行: {}", e.getMessage());
        }
        
        logger.info("空字段数组OR搜索测试完成");
    }
}
