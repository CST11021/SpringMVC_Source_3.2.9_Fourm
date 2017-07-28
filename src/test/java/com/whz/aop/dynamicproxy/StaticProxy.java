package com.whz.aop.dynamicproxy;

// 静态代理，代理类需依赖被代理的对象
public class StaticProxy implements Calculator {
    private Calculator calculator;

    StaticProxy(Calculator calculator) {
        this.calculator = calculator;
    }

    public int add(int a, int b) {
        //具体执行前可以做的工作
        int result = calculator.add(a, b);
        //具体执行后可以做的工作
        return result;
    }
}