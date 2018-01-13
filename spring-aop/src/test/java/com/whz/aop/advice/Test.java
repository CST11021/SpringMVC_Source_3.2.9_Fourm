package com.whz.aop.advice;

import com.whz.aop.advice.introinterceptor.ForumService;
import com.whz.aop.advice.introinterceptor.Monitorable;
import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {

    // 测试使用编程的形式,织入前置增强
    @org.junit.Test
    public void TestBeforeAdvice() {
        // 首先声明一个要被代理的目标对象和增强对象
        WaiterImpl target = new WaiterImpl();
        BeforeAdvice advice = new GreetingBeforeAdvice();

        // 声明一个Spring提供的代理工厂实例，并设置代理的接口，代理的目标类和增强逻辑
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(target.getClass().getInterfaces());
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(advice);

        //生成代理实例
        Waiter proxy = (Waiter) proxyFactory.getProxy();

        proxy.greetTo("John");
        proxy.serveTo("John");

    }

    // 使用spring配置的形式，测试前置、后置和环绕增强
    @org.junit.Test
    public void TestAdvice1() {
        String configPath = "com/whz/aop/advice/spring-aop.xml";
        ApplicationContext ctx = new ClassPathXmlApplicationContext(configPath);
        Waiter waiter = (Waiter)ctx.getBean("waiter");

        waiter.greetTo("John");
        System.out.println();
        waiter.serveTo("John");
    }

}