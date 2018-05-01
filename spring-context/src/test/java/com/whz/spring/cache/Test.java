package com.whz.spring.cache;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/com/whz/spring/cache/applicationContext.xml"})
public class Test {

    @Autowired
    private AccountService1 accountService1;

    // AccountService2中使用了Spring缓存注解，该注入的Bean是一个被代理后的Bean，它通过Spring自动代理机制，以及Bean初始化
    // 后的后置处理器方法实现的
    @Autowired
    private AccountService2 accountService2;

    private final Logger logger = LoggerFactory.getLogger(Test.class);

    // 测试不使用Spring缓存注解
    @org.junit.Test
    public void testGetAccountByName_WithoutSpringCache() throws Exception {
        accountService1.getAccountByName("accountName");
        accountService1.getAccountByName("accountName");

        accountService1.reload();
        logger.info("after reload ....");

        accountService1.getAccountByName("accountName");
        accountService1.getAccountByName("accountName");
    }

    // 测试使用Spring缓存注解
    @org.junit.Test
    public void testGetAccountByName_Cacheable() throws Exception {
        logger.info("第一次查询：");
        accountService2.getAccountByName_Cacheable("accountName");

        logger.info("第二次查询：");
        accountService2.getAccountByName_Cacheable("accountName");
    }

    @org.junit.Test
    public void testGetAccountByName_CachePut() throws Exception {
        logger.info("第一次查询：");
        accountService2.getAccountByName_CachePut("accountName");

        logger.info("第二次查询：");
        accountService2.getAccountByName_CachePut("accountName");
    }

    @org.junit.Test
    public void testUpdateAccountByCacheEvict() throws Exception {
        Account account1 = accountService2.getAccountByName_Cacheable("accountName1");
        Account account2 = accountService2.getAccountByName_Cacheable("accountName2");

        account2.setId(121212);
        accountService2.updateAccount(account2);

        // account1会走缓存
        account1 = accountService2.getAccountByName_Cacheable("accountName1");
        // account2会查询db
        account2 = accountService2.getAccountByName_Cacheable("accountName2");

    }

    @org.junit.Test
    public void testReload() throws Exception {
        // 查询数据库
        accountService2.getAccountByName_Cacheable("somebody1");
        // 走缓存
        accountService2.getAccountByName_Cacheable("somebody1");

        accountService2.reload();
        // 查询数据库
        accountService2.getAccountByName_Cacheable("somebody1");
        // 走缓存
        accountService2.getAccountByName_Cacheable("somebody1");

    }

}