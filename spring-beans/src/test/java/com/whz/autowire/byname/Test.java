package com.whz.autowire.byname;

import java.io.IOException;

import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class Test {

    @org.junit.Test
    public void test1() throws IOException {
        Resource[] configResources = new PathMatchingResourcePatternResolver().getResources(
            "classpath*:com/whz/autowire/byname/spring-autowireByName.xml");
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(configResources[0]);

        AutowiredAnnotationBeanPostProcessor postProcessor = new AutowiredAnnotationBeanPostProcessor();
        postProcessor.setBeanFactory(xmlBeanFactory);
        xmlBeanFactory.addBeanPostProcessor(postProcessor);


        Person person = (Person)xmlBeanFactory.getBean("person");
        System.out.println(person);
    }

}
