package com.whz.spring.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService1 {

    private final Logger logger = LoggerFactory.getLogger(AccountService1.class);

    @Autowired
    private CacheContext<Account> accountCacheContext;

    public Account getAccountByName(String accountName) {
        Account result = accountCacheContext.get(accountName);
        if (result != null) {
            logger.info("从缓存获取： {}", accountName);
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
        logger.info("从DB获取： {}", accountName);
        return new Account(accountName);
    }

}