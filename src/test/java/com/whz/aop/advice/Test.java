package com.whz.aop.advice;

import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {

    // 测试使用编程的形式织入前置增强
    @org.junit.Test
    public void TestBeforeAdvice() {
        Waiter target = new NaiveWaiter();
        BeforeAdvice advice = new GreetingBeforeAdvice();

        //spring提供的代理工厂
        ProxyFactory pf = new ProxyFactory();
        //指定对接口进行代理
        pf.setInterfaces(target.getClass().getInterfaces());
        //启用优化
        pf.setOptimize(true);
        //设置代理目标
        pf.setTarget(target);
        //织入增强
        pf.addAdvice(advice);
        //生成代理实例
        Waiter proxy = (Waiter) pf.getProxy();

        proxy.greetTo("John");
    }

    // 使用spring配置的形式，测试前置、后置和环绕增强
    @org.junit.Test
    public void TestAdvice() {
        String configPath = "com/whz/aop/advice/spring-aop.xml";
        ApplicationContext ctx = new ClassPathXmlApplicationContext(configPath);
        Waiter waiter = (Waiter)ctx.getBean("waiter");
        waiter.greetTo("John");
    }

    // 测试引介增强
    @org.junit.Test
    public void t() {
        String configPath = "com/whz/aop/advice/spring-aop.xml";
        ApplicationContext ctx = new ClassPathXmlApplicationContext(configPath);
        ForumService forumService = (ForumService)ctx.getBean("forumService");
//        forumService.removeForum(10);
//        forumService.removeTopic(1022);

        Monitorable moniterable = (Monitorable)forumService;
        moniterable.setMonitorActive(true);
        forumService.removeForum(10);
        forumService.removeTopic(1022);
    }



}