package com.whz.spring.jmx.standardmbean;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class HelloAgent {

    public static void main(String[] args) throws JMException, Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName helloName = new ObjectName("yunge:name=Hello");
        Hello hello=new Hello();
        server.registerMBean(hello, helloName);
        System.in.read();
        // 通过JConsole可以访问MBean
    }
}