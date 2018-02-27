package com.whz.designPattern.decoratorPattern;

public class Coffee_B extends ICoffee {
    public Coffee_B() {
        description = "Coffee_B";
    }

    @Override
    public double cost() {
        return 20;
    }
}