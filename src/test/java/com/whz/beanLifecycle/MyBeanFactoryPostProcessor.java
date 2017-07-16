package com.whz.beanLifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor{

   // 该接口是在spring容器解析完配置文件（注册了BeanDefinition）之后，在bean实例化之前被调用的
   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) throws BeansException {
      BeanDefinition bd = bf.getBeanDefinition("car");
      bd.getPropertyValues().addPropertyValue("brand", "奇瑞QQ");
      System.out.println("1、BeanFactoryPostProcessor.postProcessBeanFactory 方法：该接口是在spring容器解析完配置文件（注册了BeanDefinition）之后，在bean实例化之前被调用的");
      System.out.println("---执行MyBeanFactoryPostProcessor.postProcessBeanFactory()：设置brand=奇瑞QQ");
      System.out.println();
   }

}