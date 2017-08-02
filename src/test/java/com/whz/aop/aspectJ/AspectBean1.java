package com.whz.aop.aspectJ;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

// 测试一般类型的增强，引介增强比较特殊，在 AspectBean2 中测试
@Aspect
public class AspectBean1 {

    // 定义切入点（以所有方法名为 printStr 作为切入点）
    @Pointcut("execution(* *.printStr(..))")
    public void customPointcutMethod() {}

    // 前置增强
    @Before("customPointcutMethod()")
    public void beforeTest() {
        System.out.println("@Before");
    }
    // 后置增强
    @AfterReturning("customPointcutMethod()")
    public void afterReturningTest() {
        System.out.println("@AfterReturning");
    }
    // 环绕增强
    @Around("customPointcutMethod()")
    public Object arountTest(ProceedingJoinPoint p) {
        System.out.println("@Around(Before)");
        Object o = null;
        try {
            o = p.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("@Around(After)");
        return o;
    }
    // 异常增强
    @AfterThrowing("customPointcutMethod()")
    public void afterThrowingTest() {
        System.out.println("@AfterThrowing");
    }
    // Final增强
    @After("customPointcutMethod()")
    public void afterTest() {
        System.out.println("@After");
    }

}