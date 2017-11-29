package com.whz.beanLifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import java.beans.PropertyDescriptor;

public class MyInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter{

   // 在调用bean构造函数实例化前被调用
   public Object postProcessBeforeInstantiation(Class beanClass, String beanName) throws BeansException {
      if("car".equals(beanName)){
         System.out.println("2、InstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation：在调用bean构造函数实例化前被调用");
         System.out.println("---执行MyInstantiationAwareBeanPostProcessor.postProcessBeforeInstantiation");
      }
      return null;
   }
   // 调用bean的构造函数实例化后被调用
   public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
      if("car".equals(beanName)){
         System.out.println("2、InstantiationAwareBeanPostProcessor.postProcessAfterInstantiation：在调用bean构造函数实例后前被调用");
         System.out.println("---执行MyInstantiationAwareBeanPostProcessor.postProcessAfterInstantiation");
         System.out.println();
      }
      return true;
   }

   // 给bean的属性赋值之前调用
   public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
      if("car".equals(beanName)){
         System.out.println("3、InstantiationAwareBeanPostProcessor.postProcessPropertyValues：给bean的属性赋值之前调用");
         System.out.println("---执行MyInstantiationAwareBeanPostProcessor.postProcessPropertyValues");
      }
      return pvs;
   }

   // Bean 实例化之前执行该方法
   public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
      if("car".equals(beanName)){
         System.out.println();
         System.out.println("4、BeanPostProcessor.postProcessBeforeInitialization：Bean 实例化之前执行该方法");
         System.out.println("---执行MyInstantiationAwareBeanPostProcessor.postProcessBeforeInitialization");
      }
      return bean;
   }
   // Bean 实例化之后执行该方法
   public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
      if("car".equals(beanName)){
         System.out.println("4、BeanPostProcessor.postProcessAfterInitialization：Bean 实例化之后执行该方法");
         System.out.println("---执行MyInstantiationAwareBeanPostProcessor.postProcessAfterInitialization");
      }
      return bean;
   }



}