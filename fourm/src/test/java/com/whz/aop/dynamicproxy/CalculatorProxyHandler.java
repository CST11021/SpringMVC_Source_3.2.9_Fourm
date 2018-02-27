package com.whz.aop.dynamicproxy;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class CalculatorProxyHandler implements InvocationHandler, Serializable {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(method.toString() + "方法被调用");
        if (Object.class.equals(method.getDeclaringClass())) {
            // 我们知道，所有的类都继承自Object ，当JDK动态代理为目标类 proxy 创建代理对象时，
            // 会首先调用一次Object#toString方法，所以这里需要进行判断
            return method.invoke(this, args);
        } else {
            // 这里可以判断调用的是哪个接口的那个方法，然后进行动态执行
            if ("add".equals(method.getName())) {
                return (Integer)args[0] + (Integer)args[1];
            } else if ("sub".equals(method.getName())) {
                return (Integer)args[0] - (Integer)args[1];
            }
            return null;
        }
    }

}
