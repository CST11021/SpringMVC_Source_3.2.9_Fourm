package com.whz.beanLifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class MyBeanPostProcessor implements BeanPostProcessor{

   // Bean 调用构造函数，实例化之前执行该方法
   public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {       
      if(beanName.equals("car")){
         Car car = (Car)bean;
         if(car.getColor() == null){
            System.out.println("4、BeanPostProcessor.postProcessBeforeInitialization：Bean 实例化之前执行该方法");
            System.out.println("---调用MyBeanPostProcessor.postProcessBeforeInitialization()，color为空，设置为默认黑色");
            car.setColor("黑色");
         }
      }
      return bean;
   }

   // Bean 调用构造函数，实例化之后执行该方法
   public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {    
      if(beanName.equals("car")){
         Car car = (Car)bean;
         if(car.getMaxSpeed() >= 200){
            System.out.println("4、BeanPostProcessor.postProcessAfterInitialization：Bean 实例化之后执行该方法");
            System.out.println("---调用MyBeanPostProcessor.postProcessAfterInitialization()，将maxSpeed调整为200");
            car.setMaxSpeed(200);
            System.out.println();
         }
      }
      return bean;
   }
}