package com.whz.jmx;

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