package com.whz.spring.scheduling.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 基于xml配置的定时器
public class MyTaskXml {

    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);

    public void show(){
        LOGGER.info("XMl：show方法1秒执行一次");
    }  
      
    public void print(){
        LOGGER.info("XMl：print方法3秒执行一次");
    }
}