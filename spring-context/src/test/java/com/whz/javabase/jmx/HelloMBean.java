package com.whz.javabase.jmx;

public interface HelloMBean {

    void sayHello();

    void sayHello(String name);

    String getName();

    void setName(String name);

}