package com.whz.designPattern.prototypePattern;

public class Shape_Rectangle extends Shape {

    public Shape_Rectangle() {
        type = "Shape_Rectangle";
    }

    @Override
    public void draw() {
        System.out.println("Inside Shape_Rectangle::draw() method.");
    }
}