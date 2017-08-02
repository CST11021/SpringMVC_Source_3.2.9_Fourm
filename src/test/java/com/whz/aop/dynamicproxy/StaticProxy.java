package com.whz.aop.dynamicproxy;

import org.junit.Test;

// 静态代理，代理类需依赖被代理的对象
public class StaticProxy implements Calculator {
    private Calculator calculator;

    public StaticProxy(Calculator calculator) {
        this.calculator = calculator;
    }

    public int add(int a, int b) {
        System.out.println("do before");
        int result = calculator.add(a, b);
        System.out.println("do after");
        return result;
    }

}