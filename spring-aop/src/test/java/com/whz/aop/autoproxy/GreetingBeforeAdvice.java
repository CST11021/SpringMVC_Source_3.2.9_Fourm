package com.whz.aop.autoproxy;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

//定义一个前置增强，它实现了MethodBeforeAdvice接口
public class GreetingBeforeAdvice implements MethodBeforeAdvice {

   // 在目标对象方法被调用前会先执行before()方法
   @Override
   public void before(Method method, Object[] args, Object obj) throws Throwable {
      String clientName = (String)args[0];
      System.out.println("GreetingBeforeAdvice：How are you！Mr."+clientName+".");
   }

}