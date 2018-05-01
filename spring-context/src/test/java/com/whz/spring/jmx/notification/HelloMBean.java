package com.whz.spring.jmx.notification;

/**
 * 命名必须遵循规范，例如我们的MBean为Hello，则接口必须为HelloMBean。
 */
public interface HelloMBean {
         
    String getName();
    void setName(String name);
    
    void printHello();
    void printHello(String theName);
}