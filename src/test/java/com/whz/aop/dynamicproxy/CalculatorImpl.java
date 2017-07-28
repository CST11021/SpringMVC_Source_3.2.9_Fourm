package com.whz.aop.dynamicproxy;

public class CalculatorImpl implements Calculator {
    public int add(int a, int b) {
        System.out.println("执行add方法");
        return a + b;
    }
}