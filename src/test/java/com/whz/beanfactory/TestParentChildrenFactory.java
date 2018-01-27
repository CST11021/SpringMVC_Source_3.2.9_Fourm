package com.whz.beanfactory;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.CollectionUtils;

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

    @Test
    public void defaultListableBeanFactoryTest() throws IOException {

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] configResources = resourcePatternResolver.getResources("classpath*:applicationContext.xml");

        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
        reader.loadBeanDefinitions(configResources);

        List<String> beanNames = CollectionUtils.arrayToList(factory.getBeanDefinitionNames());
        System.out.println(beanNames);

        // 该方法可以实例化所有的单例bean
        //factory.preInstantiateSingletons();
    }

}
