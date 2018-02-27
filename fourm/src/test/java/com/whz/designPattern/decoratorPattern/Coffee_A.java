package com.whz.designPattern.decoratorPattern;

public class Coffee_A extends ICoffee {
    public Coffee_A() {
        description = "Coffee_A";
    }

    @Override
    public double cost() {
        return 10;
    }
}