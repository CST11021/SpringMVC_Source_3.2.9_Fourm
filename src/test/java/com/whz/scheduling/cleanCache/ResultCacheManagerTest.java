package com.whz.scheduling.cleanCache;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by wb-whz291815 on 2017/7/27.
 */
public class ResultCacheManagerTest {

    private static final Logger logger = LogManager.getLogger("ResultCacheManagerTest");


    public static void main(String[] args) throws FileNotFoundException {
        File config = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "log4j2.xml");
        ConfigurationSource source = new ConfigurationSource(new FileInputStream(config),config);
        Configurator.initialize(null, source);
        ApplicationContext context = new ClassPathXmlApplicationContext("com/whz/scheduling/cleanCache/spring-job.xml");
        String base = (String) ResultCacheManager.get(CacheKeyPrefix.BASE, new ResultCacheManager.ProceedingJoinPoint() {
            @Override
            public Object proceed() {
                //do something ...
                return "";
            }
        });
        System.out.println(base);
    }


    @Before
    public void initLog4j2() throws FileNotFoundException {
        File config = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "log4j2.xml");
        ConfigurationSource source = new ConfigurationSource(new FileInputStream(config),config);
        Configurator.initialize(null, source);
    }

    @Test
    public void get() throws Exception {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/whz/scheduling/cleanCache/spring-job.xml");
        String base = (String) ResultCacheManager.get(CacheKeyPrefix.BASE, new ResultCacheManager.ProceedingJoinPoint() {
            @Override
            public Object proceed() {
                //do something ...
                return "";
            }
        });
        System.out.println(base);
    }




}