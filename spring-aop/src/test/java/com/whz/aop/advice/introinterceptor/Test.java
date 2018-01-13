package com.whz.aop.advice.introinterceptor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {

    // 测试引介增强
    @org.junit.Test
    public void TestInterceptorAdvice() {
        String configPath = "com/whz/aop/advice/introinterceptor/spring-aop.xml";
        ApplicationContext ctx = new ClassPathXmlApplicationContext(configPath);
        ForumService forumService = (ForumService)ctx.getBean("forumServiceProxy");
        forumService.removeForum(10);
        forumService.removeTopic(1022);

        System.out.println();

        Monitorable moniterable = (Monitorable) forumService;
        moniterable.setMonitorActive(true);
        forumService.removeForum(10);
        forumService.removeTopic(1022);
    }


}