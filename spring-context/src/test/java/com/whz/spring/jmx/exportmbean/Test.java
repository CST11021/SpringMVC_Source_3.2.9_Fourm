package com.whz.spring.jmx.exportmbean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author whz
 * @version : Test.java, v 0.1 2018-05-01 10:41 whz Exp $$
 */
public class Test {

    @org.junit.Test
    public void test() throws IOException {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{"com/whz/spring/jmx/exportmbean/spring-jmx.xml"});
        System.in.read();
    }

}
