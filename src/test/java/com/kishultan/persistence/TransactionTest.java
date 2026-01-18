package com.kishultan.persistence;

import com.kishultan.persistence.EntityManager;
import com.kishultan.persistence.PersistenceManager;
import com.kishultan.persistence.datasource.DataSourceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 持久层事务测试类
 */
public class TransactionTest {
    
    private EntityManager entityManager;
    
    @Before
    public void setUp() throws Exception {
        // 初始化数据源
        DataSourceManager.setUseJNDI(false);
        
        // 创建H2数据源（使用 H2 的 JdbcDataSource，不依赖外部连接池）
        org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        
        DataSourceManager.addLocalDataSource("default", dataSource);
        
        // 创建测试表
        try (java.sql.Connection conn = DataSourceManager.getDataSource("default").getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            
            // 创建账户表
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "account_number VARCHAR(50), " +
                "balance DECIMAL(10,2), " +
                "status VARCHAR(20), " +
                "created_time TIMESTAMP)");
            
            // 创建交易记录表
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "from_account_id INT, " +
                "to_account_id INT, " +
                "amount DECIMAL(10,2), " +
                "transaction_type VARCHAR(20), " +
                "status VARCHAR(20), " +
                "created_time TIMESTAMP)");
            
            // 插入初始测试数据
            stmt.execute("INSERT INTO accounts (account_number, balance, status, created_time) VALUES " +
                "('ACC001', 1000.00, 'ACTIVE', NOW()), " +
                "('ACC002', 500.00, 'ACTIVE', NOW())");
        }
        
        entityManager = PersistenceManager.getDefaultManager();
    }
    
    @After
    public void tearDown() throws Exception {
        // 清理事务状态
        if (entityManager != null && entityManager.isTransactionActive()) {
            try {
                entityManager.closeTransaction();
            } catch (Exception e) {
                // 忽略清理错误
            }
        }
        
        
        // 清理数据源
        DataSourceManager.removeLocalDataSource("default");
        DataSourceManager.setUseJNDI(true);
    }
    
    @Test
    public void testBasicTransaction() throws Exception {
        // 开始事务
        entityManager.beginTransaction();
        
        // 创建测试账户
        Account account = new Account();
        account.setAccountNumber("测试账户");
        account.setBalance(new BigDecimal("1000.00"));
        
        // 保存账户
        entityManager.save(account);
        
        // 提交事务
        entityManager.commitTransaction();
        
        // 验证账户已保存
        Account savedAccount = entityManager.findById(Account.class, account.getId());
        Assert.assertNotNull("账户应该被保存", savedAccount);
        Assert.assertEquals("测试账户", savedAccount.getAccountNumber());
        Assert.assertEquals(new BigDecimal("1000.00"), savedAccount.getBalance());
    }
    
    @Test
    public void testTransactionRollback() throws Exception {
        // 开始事务
        entityManager.beginTransaction();
        
        // 创建测试账户
        Account account = new Account();
        account.setAccountNumber("回滚测试账户");
        account.setBalance(new BigDecimal("2000.00"));
        
        // 保存账户
        entityManager.save(account);
        
        // 回滚事务
        entityManager.rollbackTransaction();
        
        // 验证账户未被保存
        Account savedAccount = entityManager.findById(Account.class, account.getId());
        Assert.assertNull("账户应该被回滚", savedAccount);
    }
    
    @Test
    public void testTransactionAutoClose() throws Exception {
        // 开始事务
        entityManager.beginTransaction();
        
        // 创建测试账户
        Account account = new Account();
        account.setAccountNumber("自动关闭测试账户");
        account.setBalance(new BigDecimal("3000.00"));
        
        // 保存账户
        entityManager.save(account);
        
        // 自动关闭事务（应该回滚）
        entityManager.closeTransaction();
        
        // 验证账户未被保存
        Account savedAccount = entityManager.findById(Account.class, account.getId());
        Assert.assertNull("账户应该被回滚", savedAccount);
    }
    
    @Test
    public void testTransactionIsolation() throws Exception {
        // 第一个事务
        entityManager.beginTransaction();
        
        Account account1 = new Account();
        account1.setAccountNumber("隔离测试账户1");
        account1.setBalance(new BigDecimal("4000.00"));
        entityManager.save(account1);
        
        // 第一个事务提交
        entityManager.commitTransaction();
        
        // 第二个EntityManager（不同的事务）
        EntityManager entityManager2 = PersistenceManager.getDefaultManager();
        entityManager2.beginTransaction();
        
        Account account2 = new Account();
        account2.setAccountNumber("隔离测试账户2");
        account2.setBalance(new BigDecimal("5000.00"));
        entityManager2.save(account2);
        
        // 第二个事务回滚
        entityManager2.rollbackTransaction();
        
        // 验证结果
        Account savedAccount1 = entityManager.findById(Account.class, account1.getId());
        Account savedAccount2 = entityManager.findById(Account.class, account2.getId());
        
        Assert.assertNotNull("第一个账户应该被保存", savedAccount1);
        Assert.assertNull("第二个账户应该被回滚", savedAccount2);
    }
    
    @Test
    public void testTransactionState() throws Exception {
        // 初始状态
        Assert.assertFalse("初始状态不应该有活动事务", entityManager.isTransactionActive());
        
        // 开始事务
        entityManager.beginTransaction();
        Assert.assertTrue("开始事务后应该有活动事务", entityManager.isTransactionActive());
        
        // 提交事务
        entityManager.commitTransaction();
        Assert.assertFalse("提交事务后不应该有活动事务", entityManager.isTransactionActive());
    }
    
    @Test
    public void testTransactionStateAfterRollback() throws Exception {
        // 开始事务
        entityManager.beginTransaction();
        Assert.assertTrue("开始事务后应该有活动事务", entityManager.isTransactionActive());
        
        // 回滚事务
        entityManager.rollbackTransaction();
        Assert.assertFalse("回滚事务后不应该有活动事务", entityManager.isTransactionActive());
    }
    
    @Test
    public void testTransactionStateAfterClose() throws Exception {
        // 开始事务
        entityManager.beginTransaction();
        Assert.assertTrue("开始事务后应该有活动事务", entityManager.isTransactionActive());
        
        // 关闭事务
        entityManager.closeTransaction();
        Assert.assertFalse("关闭事务后不应该有活动事务", entityManager.isTransactionActive());
    }
    
    @Test
    public void testNestedTransactionHandling() throws Exception {
        // 开始第一个事务
        entityManager.beginTransaction();
        
        Account account = new Account();
        account.setAccountNumber("嵌套事务测试账户");
        account.setBalance(new BigDecimal("6000.00"));
        entityManager.save(account);
        
        // 尝试开始第二个事务（应该抛出异常）
        try {
            entityManager.beginTransaction();
            Assert.fail("应该抛出异常，因为已经有活动事务");
        } catch (RuntimeException e) {
            // 预期的异常
            Assert.assertTrue("异常消息应该包含事务相关信息", 
                e.getMessage().contains("Transaction is already active"));
        }
        
        // 清理
        entityManager.rollbackTransaction();
    }
    
    @Test
    public void testCommitInactiveTransaction() throws Exception {
        // 尝试提交非活动事务
        entityManager.commitTransaction(); // 应该不会抛出异常，只是记录警告
        
        // 验证没有异常抛出
        Assert.assertFalse("不应该有活动事务", entityManager.isTransactionActive());
    }
    
    @Test
    public void testRollbackInactiveTransaction() throws Exception {
        // 尝试回滚非活动事务
        entityManager.rollbackTransaction(); // 应该不会抛出异常，只是记录警告
        
        // 验证没有异常抛出
        Assert.assertFalse("不应该有活动事务", entityManager.isTransactionActive());
    }
    
    // 测试实体类
    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "accounts")
    public static class Account {
        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
        private Integer id;
        
        @jakarta.persistence.Column(name = "account_number")
        private String accountNumber;
        
        @jakarta.persistence.Column(name = "balance")
        private BigDecimal balance;
        
        @jakarta.persistence.Column(name = "status")
        private String status;
        
        @jakarta.persistence.Column(name = "created_time")
        private Date createdTime;
        
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
        
        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Date getCreatedTime() { return createdTime; }
        public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }
    }
    
    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "transactions")
    public static class Transaction {
        @jakarta.persistence.Id
        @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
        private Integer id;
        
        @jakarta.persistence.Column(name = "from_account_id")
        private Integer fromAccountId;
        
        @jakarta.persistence.Column(name = "to_account_id")
        private Integer toAccountId;
        
        @jakarta.persistence.Column(name = "amount")
        private BigDecimal amount;
        
        @jakarta.persistence.Column(name = "transaction_type")
        private String transactionType;
        
        @jakarta.persistence.Column(name = "status")
        private String status;
        
        @jakarta.persistence.Column(name = "created_time")
        private Date createdTime;
        
        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        
        public Integer getFromAccountId() { return fromAccountId; }
        public void setFromAccountId(Integer fromAccountId) { this.fromAccountId = fromAccountId; }
        
        public Integer getToAccountId() { return toAccountId; }
        public void setToAccountId(Integer toAccountId) { this.toAccountId = toAccountId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Date getCreatedTime() { return createdTime; }
        public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }
    }
} 