package com.whz.designPattern.prototypePattern;

public class Shape_Circle extends Shape {

    public Shape_Circle() {
        type = "Shape_Circle";
    }

    @Override
    public void draw() {
        System.out.println("Inside Shape_Circle::draw() method.");
    }
}