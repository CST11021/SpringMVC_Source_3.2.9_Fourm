package com.whz.aop.autoproxy;


import com.whz.aop.advice.Waiter;

//定义一个目标类
public class WaiterImpl implements Waiter {

   // 被自动代理后，该属性无法再被注入了，InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation方法返回代理
   // 类后，IOC就直接返回Bean实例了，后续的属性注入则无法再被注入了
   private String testString;

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

   public String getTestString() {
      return testString;
   }

   public void setTestString(String testString) {
      this.testString = testString;
   }
}