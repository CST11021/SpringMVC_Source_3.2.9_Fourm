package com.whz.aop.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

// 使用JDK动态代理，将增强代码织入目标类
public class LogHandler implements InvocationHandler {
    // 表示将要被代理的目标类
    Object obj;

    LogHandler(Object obj) {
        this.obj = obj;
    }

    public Object invoke(Object obj1, Method method, Object[] args) throws Throwable {
        this.doBefore();
        Object o = method.invoke(obj, args);
        this.doAfter();
        return o;
    }

    public void doBefore() {
        System.out.println("do before");
    }

    public void doAfter() {
        System.out.println("do after");
    }

}