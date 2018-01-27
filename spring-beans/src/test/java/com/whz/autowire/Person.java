package com.whz.autowire;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Person {

    private String name;
    @Autowired
    private Address address;

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
    @Qualifier
    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public String toString() {
        return "Person{" + "name='" + name + '\'' + ", address=" + address + ", car=" + car + '}';
    }
}