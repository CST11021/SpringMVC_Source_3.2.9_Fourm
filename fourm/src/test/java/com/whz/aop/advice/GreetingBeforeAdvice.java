package com.whz.aop.advice;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

//定义一个前置增强
public class GreetingBeforeAdvice implements MethodBeforeAdvice {

   @Override
   public void before(Method method, Object[] args, Object obj) throws Throwable {
      String clientName = (String)args[0];
      System.out.println("1.How are you！Mr."+clientName+".");
   }

}