package com.whz.autowire;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.CollectionUtils;

public class Test {

    @org.junit.Test
    public void test1() throws IOException {
        Resource[] configResources = new PathMatchingResourcePatternResolver().getResources(
            "classpath*:com/whz/autowire/spring-autowire.xml");
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(configResources[0]);

        AutowiredAnnotationBeanPostProcessor postProcessor = new AutowiredAnnotationBeanPostProcessor();
        postProcessor.setBeanFactory(xmlBeanFactory);
        xmlBeanFactory.addBeanPostProcessor(postProcessor);

        Person person = (Person)xmlBeanFactory.getBean("person");
        System.out.println(person);
    }

    @org.junit.Test
    public void test() throws IOException {

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();

        AutowiredAnnotationBeanPostProcessor postProcessor = new AutowiredAnnotationBeanPostProcessor();
        postProcessor.setBeanFactory(factory);
        factory.addBeanPostProcessor(postProcessor);

        RequiredAnnotationBeanPostProcessor postProcessor1 = new RequiredAnnotationBeanPostProcessor();
        postProcessor1.setBeanFactory(factory);
        factory.addBeanPostProcessor(postProcessor1);


        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] configResources = resourcePatternResolver.getResources(
                "classpath*:com/whz/autowire/spring-autowire.xml");

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions(configResources);

        List<String> beanNames = CollectionUtils.arrayToList(factory.getBeanDefinitionNames());
        System.out.println(beanNames);

        com.whz.autowire.Person person = (com.whz.autowire.Person) factory.getBean("person");
        System.out.println(person);
    }

}
