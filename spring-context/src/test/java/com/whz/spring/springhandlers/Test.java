package com.whz.spring.springhandlers;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wb-whz291815
 * @version $Id: Test.java, v 0.1 2018-02-01 10:21 wb-whz291815 Exp $$
 */
public class Test {

    @org.junit.Test
    public void test() {
        String res = "com/whz/spring/springhandlers/spring-customNameSpaceHandler.xml";
        ApplicationContext ctx = new ClassPathXmlApplicationContext(res);
        People people = (People) ctx.getBean("people");
        System.out.println(people);
    }

}
