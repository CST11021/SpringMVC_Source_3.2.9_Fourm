package com.whz.spring.scheduling.xml;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wb-whz291815
 * @version $Id: Test.java, v 0.1 2018-02-06 14:18 wb-whz291815 Exp $$
 */
public class Test {

    @org.junit.Test
    public void test() throws IOException {
        String res = "com/whz/spring/scheduling/xml/spring-scheduling.xml";
        new ClassPathXmlApplicationContext(res);
        System.in.read();
    }

}
