package com.whz.beanfactory;

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @author wb-whz291815
 * @version $Id: Test.java, v 0.1 2017-12-17 11:44 wb-whz291815 Exp $$
 */
public class Test {

    @org.junit.Test
    public void testFactoryBean() {
        DefaultListableBeanFactory appFactory = new DefaultListableBeanFactory();
        BeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(appFactory);
        beanDefinitionReader.loadBeanDefinitions(new ClassPathResource("com/whz/beanfactory/spring-factoryBeanTest.xml"));
        System.out.println(appFactory.getBeansOfType(Object.class));
    }

}
