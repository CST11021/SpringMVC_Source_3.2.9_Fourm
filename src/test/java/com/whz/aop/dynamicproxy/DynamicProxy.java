package com.whz.aop.dynamicproxy;

import org.junit.Test;

import java.lang.reflect.Proxy;

// 通过反射，使用JDK动态代理实现动态代理
public class DynamicProxy {

    @Test
    public void testDynamicProxy() {
        Calculator calculator = new CalculatorImpl();
        LogHandler lh = new LogHandler(calculator);
        Calculator proxy = (Calculator) Proxy.newProxyInstance(
                calculator.getClass().getClassLoader(),
                calculator.getClass().getInterfaces(),
                lh);
        proxy.add(1, 1);
    }

}