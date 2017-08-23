package com.whz.designPattern.decoratorPattern;

public abstract class ICoffee {
    String description = "Unknown Coffee";

    public String getDescription() {
        return description;
    }

    public abstract double cost();
}


