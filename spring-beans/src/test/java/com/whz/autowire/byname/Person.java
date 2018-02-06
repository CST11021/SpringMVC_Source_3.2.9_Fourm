package com.whz.autowire.byname;

import org.springframework.beans.factory.annotation.Autowired;

public class Person {

    private String name;

    private Address address;

    // 如果不适用xml配置的方法自动注入，也可以使用@Autowired注解实现自动注入，需要注意的是在BeanFactory中需要，通过设置
    // AutowiredAnnotationBeanPostProcessor后处理器，才能使该注解生效。@Autowired自动装配是使用ByType方式的，也就是说，
    // 如果候选Bean中存在多个Car类型的Bean，则会报错。
    @Autowired
    private Car car;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public String toString() {
        return "Person{" + "name='" + name + '\'' + ", address=" + address + ", car=" + car + '}';
    }
}