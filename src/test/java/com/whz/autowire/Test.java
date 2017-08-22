package com.whz.autowire;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by wb-whz291815 on 2017/8/22.
 */
public class Test {

    @org.junit.Test
    public void test() {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("com/whz/autowire/spring-autowire.xml");
        Person person = (Person) ctx.getBean("person");
        System.out.println(person);

    }

}
