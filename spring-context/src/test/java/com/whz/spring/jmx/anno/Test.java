package com.whz.spring.jmx.anno;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author whz
 * @version : Test.java, v 0.1 2018-04-29 10:05 whz Exp $$
 */
public class Test {

    @org.junit.Test
    public void test() throws IOException {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"/com/whz/spring/jmx/anno/spring-jmx.xml",});

        System.in.read();
    }

}
