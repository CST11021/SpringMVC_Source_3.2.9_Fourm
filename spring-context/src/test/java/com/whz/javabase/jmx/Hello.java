package com.whz.javabase.jmx;

/*
 * 该类名称必须与实现的接口的前缀保持一致（即MBean前面的名称）
 */
public class Hello implements HelloMBean {

    private int id;

    private String name;

    public void sayHello() {
        System.out.println("hello " + name);
    }

    public void sayHello(String name) {
        System.out.println("hello " + name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Hello{" +
            "id=" + id +
            ", name='" + name + '\'' +
            '}';
    }
}