package com.whz.designPattern.decoratorPattern;

public class Decorator2 extends CoffeeDecorator {
    ICoffee iCoffee;

    public Decorator2(ICoffee iCoffee) {
        this.iCoffee = iCoffee;
    }

    @Override
    public String getDescription() {
        return iCoffee.getDescription() + ", 调味料2";
    }

    @Override
    public double cost() {
        return 2 + iCoffee.cost();
    }
}