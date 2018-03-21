package com.whz.spring.rmi;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wb-whz291815
 * @version $Id: ClientTest.java, v 0.1 2018-03-21 15:35 wb-whz291815 Exp $$
 */
public class ClientTest {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/whz/spring/rmi/spring-rmi-client.xml");
        HelloRMIService helloRMIService = context.getBean("myClient", HelloRMIService.class);
        System.out.println(helloRMIService.getAdd(1,2));
    }

}
