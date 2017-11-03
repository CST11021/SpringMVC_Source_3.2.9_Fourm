package com.whz.aop.dynamicproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created by wb-whz291815 on 2017/8/2.
 */
public class Test {

    // 测试静态代理
    @org.junit.Test
    public void testStaticProxy() {
        CglibProxy proxy = new CglibProxy();
        Calculator calculator = (Calculator)proxy.getProxy(CalculatorImpl.class);
        calculator.add(1,1);
    }

    // 测试CGLib动态代理
    @org.junit.Test
    public void testCGLib_DynamicProxy() {
        CglibProxy proxy = new CglibProxy();
        Calculator calculator = (Calculator) proxy.getProxy(CalculatorImpl.class);
        calculator.add(1,1);
    }

    // 1、测试JDK动态代理（为实现类创建代理对象）
    @org.junit.Test
    public void testJDK_DynamicProxy1() {
        Calculator calculator = new CalculatorImpl();
        InvocationHandler logHandler = new CalculatorImplProxyHandler(calculator);
        Calculator proxy = (Calculator) Proxy.newProxyInstance(
                calculator.getClass().getClassLoader(),
                calculator.getClass().getInterfaces(),// 获取被代理的接口
                logHandler);
        proxy.add(1, 1);
        proxy.sub(1, 1);
    }

    ///2、测试JDK动态代理（为接口创建代理对象）
    @org.junit.Test
    public void testJDK_DynamicProxy2() {
        Class<Calculator> targetInterface = Calculator.class;
        InvocationHandler logHandler = new CalculatorProxyHandler();
        Calculator proxy = (Calculator) Proxy.newProxyInstance(
                targetInterface.getClassLoader(),
                new Class[]{targetInterface},// 获取被代理的接口
                logHandler);
        System.out.println(proxy.add(1, 1));
        System.out.println(proxy.sub(1, 1));
    }

    // 补充：java动态代理是利用反射机制生成一个实现代理接口的匿名类，在调用具体方法前调用InvokeHandler来处理，
    // 该匿名类中使用了装饰器的设计思想为目标类织入增强逻辑；
    //
    // 而cglib动态代理是利用asm开源包，对代理对象类的class文件加载进来，通过修改其字节码生成子类来处理。

}
