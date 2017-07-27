package com.whz.aop.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


import java.lang.reflect.Method;


//定义一个环绕增强
public class GreetingInterceptor implements MethodInterceptor {

   public Object invoke(MethodInvocation invocation) throws Throwable {
      Object[] args = invocation.getArguments();
      String clientName = (String)args[0];
      
      System.out.println("2.How are you！Mr."+clientName+".");
      Object obj = invocation.proceed();
      System.out.println("2.Please enjoy yourself!");
      
      return obj;
   }

}