package com.whz.spring.cache;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

public class AccountService1 {

    private final Logger logger = LoggerFactory.getLogger(AccountService1.class);

    @Resource
    private CacheContext<Account> accountCacheContext;

    public Account getAccountByName(String accountName) {
        Account result = accountCacheContext.get(accountName);
        if (result != null) {
            logger.info("get from cache... {}", accountName);
            return result;
        }

        Account account = getFromDB(accountName);
        accountCacheContext.addOrUpdateCache(accountName, account);
        return account;
    }

    public void reload() {
        accountCacheContext.evictCache();
    }

    private Account getFromDB(String accountName) {
        logger.info("real querying db... {}", accountName);
        //Todo query data from database
        return new Account(accountName);
    }

}