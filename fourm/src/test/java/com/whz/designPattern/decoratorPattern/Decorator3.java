package com.whz.designPattern.decoratorPattern;

public class Decorator3 extends CoffeeDecorator {
    ICoffee iCoffee;

    public Decorator3(ICoffee iCoffee) {
        this.iCoffee = iCoffee;
    }

    @Override
    public String getDescription() {
        return iCoffee.getDescription() + ", 调味料3";
    }

    @Override
    public double cost() {
        return 3 + iCoffee.cost();
    }
}