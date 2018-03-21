package com.whz.spring.rmi;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wb-whz291815
 * @version $Id: ServiceTest.java, v 0.1 2018-03-21 15:31 wb-whz291815 Exp $$
 */
public class ServiceTest {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("com/whz/spring/rmi/spring-rmi-service.xml");
    }

}
