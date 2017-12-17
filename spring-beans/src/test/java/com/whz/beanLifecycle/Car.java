package com.whz.beanLifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class Car implements BeanFactoryAware, BeanNameAware, InitializingBean, DisposableBean {
   private String brand;
   private String color;
   private int maxSpeed;
   private String name;
   private BeanFactory beanFactory;
   private String beanName;

   public Car() {
      System.out.println("调用Car()构造函数");
   }

   public String getBrand() {
      return brand;
   }
   public void setBrand(String brand) {
      System.out.println("---调用setBrand()设置属性。");
      this.brand = brand;
   }
   public String getColor() {
      return color;
   }
   public void setColor(String color) {
      System.out.println("---调用setColor()设置属性。");
      this.color = color;
   }
   public int getMaxSpeed() {
      return maxSpeed;
   }
   public void setMaxSpeed(int maxSpeed) {
      System.out.println("---调用setMaxSpeed()设置属性。");
      this.maxSpeed = maxSpeed;
   }
   // BeanNameAware接口方法
   @Override
   public void setBeanName(String beanName) {
      System.out.println("---BeanNameAware.setBeanName()。");
      this.beanName = beanName;
   }
   // BeanFactoryAware接口方法
   @Override
   public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      System.out.println("---BeanFactoryAware.setBeanFactory()。");
      this.beanFactory = beanFactory;
   }

   @PostConstruct
   public void testPostConstruct() {
      System.out.println("测试@PostConstruct注解");
   }
   public void myInit() {
      System.out.println("6、调用<bean>配置中init-method");
      System.out.println("---执行Car.myInit()，将maxSpeed设置为240。");
      this.maxSpeed = 240;
   }

   public void introduce(){

      System.out.println("--------使用Car.introduce:"+this.toString());
      System.out.println();
   }
   public String toString() {
      return "brand:" + brand + "/color:" + color + "/maxSpeed:"+ maxSpeed;
   }

   // InitializingBean接口方法
   @Override
   public void afterPropertiesSet() throws Exception {
      System.out.println("5、InitializingBean.afterPropertiesSet：在设置了所有的bean属性之后，由BeanFactory调用");
      System.out.println("---执行Car.afterPropertiesSet");
   }
   // DisposableBean接口方法
   @Override
   public void destroy() throws Exception {
      System.out.println("7、DisposableBean.destroy：在bean被销毁的时候调用");
      System.out.println("---执行Car.destory()");
   }

   public void myDestory() {
      System.out.println("调用myDestroy()。");
   }
   @PreDestroy
   public void testPreDestory() {
      System.out.println("测试@PreDestroy注解");
   }

}