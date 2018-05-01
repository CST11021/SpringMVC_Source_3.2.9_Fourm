package com.whz.spring.jmx.mbeanserver;

import java.io.IOException;

import javax.management.MalformedObjectNameException;


import mx4j.tools.adaptor.http.HttpAdaptor;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.support.ObjectNameManager;

public class JmxTestStart {
    public static void main(String[] args) throws IOException, MalformedObjectNameException, Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{"/com/whz/spring/jmx/mbeanserver/beanRefJMXServer.xml",});

        HttpAdaptor httpAdaptor = (HttpAdaptor) ctx.getBean("httpAdaptor");
        httpAdaptor.start();

        //动态注册一个MBean的例子  
        MBeanExporter exporter = (MBeanExporter) ctx.getBean("exporter");
        exporter.registerManagedResource(new TestMbean(), ObjectNameManager.getInstance("ZtManager:name=mbean"));
    }
}  