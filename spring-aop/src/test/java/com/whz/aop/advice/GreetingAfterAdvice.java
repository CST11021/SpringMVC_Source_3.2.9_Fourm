package com.whz.aop.advice;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

// 定义个后置增强
public class GreetingAfterAdvice implements AfterReturningAdvice {

   @Override
   public void afterReturning(Object returnObj, Method method, Object[] args, Object obj) throws Throwable {
      System.out.println("GreetingAfterAdvice：Please enjoy yourself!");
   }
}