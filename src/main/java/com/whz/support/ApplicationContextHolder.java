package com.whz.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 注意：这里我们通过实现ApplicationContextAware接口，在Spring容器启动的时候，将Spring容器注入进来，然而这里的Spring容器
 * 并不维护SpringMVC相关的Bean，所以如果我们需要查看WEB-INF下相关的Bean，可以通过如下方法来查看：
 * XmlWebApplicationContext wac = (XmlWebApplicationContext) request.getAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE);
 * wac.getBean("xxx");
 *
 * 另外，需要注意，使用这种方式注入Spring容器时，记得要将 ApplicationContextHolder 声明为Bean，这样Spring才能调用Bean的后处理方法进行注入。
 */
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