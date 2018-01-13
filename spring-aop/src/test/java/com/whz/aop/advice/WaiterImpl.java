package com.whz.aop.advice;



//定义一个目标类
public class WaiterImpl implements Waiter {

   public void greetTo(String name) {
      System.out.println("greet to "+name+"...");
   }
   
   public void serveTo(String name){
      System.out.println("serving "+name+"...");
   }

   // 非接口方法无法被代理
   public void sayHello(String name) {
      System.out.println("hello "+name+"...");
   }
}