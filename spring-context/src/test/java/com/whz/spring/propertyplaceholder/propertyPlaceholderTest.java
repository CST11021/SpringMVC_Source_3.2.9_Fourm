package com.whz.spring.propertyplaceholder;

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

    private static final String RES = "com/whz/spring/propertyplaceholder/spring-propertyPlaceholderTest.xml";

    @Test
    public void test() {
        DefaultListableBeanFactory daoFactory = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(daoFactory).loadBeanDefinitions(new ClassPathResource(RES));
        PropertyPlaceholderConfigurer ppc = (PropertyPlaceholderConfigurer) daoFactory.getBean(
                "org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#0");
        TestBean testBean = (TestBean) daoFactory.getBean("testBean");
        System.out.println(testBean.getTestStr());// ${testStr}


        ApplicationContext app = new ClassPathXmlApplicationContext(RES);
        TestBean testBean2 = (TestBean) app.getBean("testBean");
        System.out.println(testBean2.getTestStr());// just a test !!!
    }

}
