package com.whz.spring.scheduling.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;  
  
// 基于注解的定时器
@Component  
public class MyTaskAnnotation {

    private static final Logger LOGGER = LoggerFactory.getLogger(Test.class);
      
    // 定时计算。每天凌晨 01:00 执行一次
    @Scheduled(cron = "0 0 1 * * *")   
    public void show(){
        LOGGER.info("Annotation：show run");
    }
      
    // 心跳更新。启动时执行一次，之后每隔2秒执行一次
    @Scheduled(fixedRate = 1000*2)   
    public void print(){
        LOGGER.info("Annotation：print run");
    }
} 