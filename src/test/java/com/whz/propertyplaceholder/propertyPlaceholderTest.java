package com.whz.propertyplaceholder;

import org.junit.Test;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by wb-whz291815 on 2017/7/14.
 */
public class propertyPlaceholderTest {

    @Test
    public void test() {
        DefaultListableBeanFactory daoFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(daoFactory)
                .loadBeanDefinitions(new ClassPathResource("com/whz/propertyplaceholder/spring-propertyPlaceholderTest.xml"));
        PropertyPlaceholderConfigurer ppc = (PropertyPlaceholderConfigurer) daoFactory.getBean("org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#0");
        TestBean testBean = (TestBean) daoFactory.getBean("testBean");
        System.out.println(testBean.getTestStr());// ${testStr}


        ApplicationContext app = new ClassPathXmlApplicationContext("com/whz/propertyplaceholder/spring-propertyPlaceholderTest.xml");
        TestBean testBean2 = (TestBean) app.getBean("testBean");
        System.out.println(testBean2.getTestStr());// just a test !!!
    }

}
