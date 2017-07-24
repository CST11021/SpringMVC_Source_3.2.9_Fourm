package com.whz;

import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlListableBeanFactoryTests;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by wb-whz291815 on 2017/7/14.
 */
public class TestParentChildrenFactory {

    @Test
    public void beanFactoryTest() {
        DefaultListableBeanFactory daoFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(daoFactory)
                .loadBeanDefinitions(new ClassPathResource("baobaotao-dao.xml"));

        DefaultListableBeanFactory appFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(appFactory)
                .loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));

        System.out.print("");
    }

    @Test
    public void applicationContextTest() {

        ApplicationContext app = new ClassPathXmlApplicationContext("applicationContext.xml");
        System.out.print("");
    }

}
