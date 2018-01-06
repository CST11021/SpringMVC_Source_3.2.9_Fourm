package com.whz.spring.cache;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class AccountService2 {

    private final Logger logger = LoggerFactory.getLogger(AccountService2.class);

    // 每次调用后会将返回结果加入缓存，调用前会检查缓存，如果缓存有则直接从缓存取，不执行该方法
    @Cacheable(value="accountCache")
    public Account getAccountByName_Cacheable(String accountName) {
        // 方法内部实现不考虑缓存逻辑，直接实现业务
        Account account = getFromDB(accountName);
        return account;
    }

    // 每次调用后会将返回结果加入缓存，但和 @Cacheable 不同的是，它每次都会触发真实方法的调用
    @CachePut(value="accountCache")
    public Account getAccountByName_CachePut(String accountName) {
        Account account = getFromDB(accountName);
        return account;
    }

    // 清理key对应的缓存
    @CacheEvict(value="accountCache",key="#account.getName()")
    public void updateAccount(Account account) {
        updateDB(account);
    }

    // 清理所有的缓存
    @CacheEvict(value="accountCache",allEntries=true)
    public void reload() {
    }

    private void updateDB(Account account) {
        logger.info("更新DB数据 {}", account.getName());
    }

    private Account getFromDB(String accountName) {
        logger.info("从DB获取 {}", accountName);
        return new Account(accountName);
    }
}