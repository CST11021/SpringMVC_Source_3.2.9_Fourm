package com.whz.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHolder implements ApplicationContextAware {
    private static final Logger log = LoggerFactory.getLogger(ApplicationContextHolder.class);

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext != null) {
            throw new IllegalStateException("ApplicationContextHolder already holded 'applicationContext'.");
        }
        applicationContext = context;
        log.info("holded applicationContext,displayName:" + applicationContext.getDisplayName());
    }

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null)
            throw new IllegalStateException("'applicationContext' property is null,ApplicationContextHolder not yet init.");
        return applicationContext;
    }

    //可使用该方法获取容器中的Bean
    public static <T> T getBean(String beanName) {
        return (T)getApplicationContext().getBean(beanName);
    }


}