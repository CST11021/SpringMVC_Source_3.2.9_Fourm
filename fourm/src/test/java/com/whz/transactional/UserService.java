package com.whz.transactional;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public interface UserService {

    // 该方法没有配置事务相关
    public void saveWithoutTransaction(User user) throws Exception;

    // 使用早期的事务配置
    public void saveByConfg1(User user) throws Exception;

    // 使用tx命名空间方式的配置
    public void saveByConfg2(User user) throws Exception;

    // 使用注解的方式配置事务
    @Transactional(propagation= Propagation.REQUIRED)
    public void saveByAnno(User user) throws Exception;

}