package com.whz.aop.aspectJ;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@Aspect
public class AspectJTest {

    @Pointcut("execution(* *.test(..))")
    public void test() {

    }

    @Before("test()")
    public void beforeTest() {
        System.out.println("beforeTest2");
    }

    @After("test()")
    public void afterTest() {
        System.out.println("afterTest2");
    }

    @Around("test()")
    public Object arountTest(ProceedingJoinPoint p) {

        System.out.println("before1");
        Object o = null;
        try {
            o = p.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("after1");
        return o;
    }


    @Test
    public void t() {

        ApplicationContext context = new ClassPathXmlApplicationContext("com/whz/aop/aspectJ/aspectJConfig.xml");
        TestBean bean = (TestBean) context.getBean("test");
        bean.test();

    }

}