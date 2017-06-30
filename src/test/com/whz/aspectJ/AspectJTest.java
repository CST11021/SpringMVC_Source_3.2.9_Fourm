package com.whz.aspectJ;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.junit.Test;
import org.springframework.beans.annotation.AnnotationBeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;

@Aspect
public class AspectJTest {

    @Pointcut("execution(* *.test(..))")
    public void test() {

    }

    @Before("test()")
    public void beforeTest() {
        System.out.println("beforeTest");
    }

    @After("test()")
    public void afterTest() {
        System.out.println("afterTest");
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

//        ApplicationContext context = new ClassPathXmlApplicationContext("com/whz/aspectJ/aspectJConfig.xml");
//        TestBean bean = (TestBean) context.getBean("test");
//        bean.test();



        Annotation annotation2 = new RequestMapping() {
            public Class<? extends Annotation> annotationType() {
                return RequestMapping.class;
            }

            public String[] value() {
                return new String[]{"value1","value2"};
            }

            public String[] params() {
                return new String[]{"param1","param1"};
            }

            public RequestMethod[] method() {
                return new RequestMethod[0];
            }

            public String[] headers() {
                return new String[0];
            }

            public String[] consumes() {
                return new String[0];
            }

            public String[] produces() {
                return new String[0];
            }
        };

        TestBean testBean = new TestBean();
        AnnotationBeanUtils.copyPropertiesToBean(annotation2, testBean, "method", "headers", "consumes", "produces");
        System.out.print("");

    }

}