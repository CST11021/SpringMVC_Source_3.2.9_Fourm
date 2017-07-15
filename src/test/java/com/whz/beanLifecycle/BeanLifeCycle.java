package com.whz.beanLifecycle;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class BeanLifeCycle {
    private static void LifeCycleInBeanFactory(){
        Resource res = new ClassPathResource("com/whz/beanLifecycle/bean.xml");
        BeanFactory bf = new XmlBeanFactory(res);

        MyBeanFactoryPostProcessor beanFactoryPostProcessor = new MyBeanFactoryPostProcessor();
        beanFactoryPostProcessor.postProcessBeanFactory((XmlBeanFactory) bf);

        MyInstantiationAwareBeanPostProcessor instantiationAwareBeanPostProcessor = new MyInstantiationAwareBeanPostProcessor();
        ((ConfigurableBeanFactory)bf).addBeanPostProcessor(instantiationAwareBeanPostProcessor);

        MyBeanPostProcessor beanPostProcessor = new MyBeanPostProcessor();
        ((ConfigurableBeanFactory)bf).addBeanPostProcessor(beanPostProcessor);

        Car car1 = (Car)bf.getBean("car");
        car1.introduce();
        car1.setColor("红色");
        Car car2 = bf.getBean("car", Car.class);
        car2.introduce();
        ((XmlBeanFactory)bf).destroySingletons();
    }

    public static void main(String[] args) {
        LifeCycleInBeanFactory();
    }
}