package com.whz.designPattern.prototypePattern;

public class Shape_Square extends Shape {

    public Shape_Square() {
        type = "Shape_Square";
    }

    @Override
    public void draw() {
        System.out.println("Inside Shape_Square::draw() method.");
    }
}