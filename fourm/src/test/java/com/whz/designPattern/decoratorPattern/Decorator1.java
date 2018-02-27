package com.whz.designPattern.decoratorPattern;

public class Decorator1 extends CoffeeDecorator {
    ICoffee iCoffee;

    public Decorator1(ICoffee iCoffee) {
        this.iCoffee = iCoffee;
    }

    @Override
    public String getDescription() {
        return iCoffee.getDescription() + ", 调味料1";
    }

    @Override
    public double cost() {
        return 1 + iCoffee.cost();
    }
}