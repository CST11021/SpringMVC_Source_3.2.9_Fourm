package com.whz.spring.jmx.notification;

/**
 * 遵循规范，接口为HelloMBean，则实现类必须为Hello
 */
public class Hello implements HelloMBean {
         
    private String name;

    public void printHello() {
        System.out.println("Hello, " + name);
    }
    public void printHello(String theName) {
        System.out.println("Hello, " + theName);
    }

    public String getName() {    
        return name;    
    }
    public void setName(String name) {
        this.name = name;
    }

}