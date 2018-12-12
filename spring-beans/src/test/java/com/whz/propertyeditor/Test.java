package com.whz.propertyeditor;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

/**
 * @author wb-whz291815
 * @version $Id: Test.java, v 0.1 2018-01-10 16:33 wb-whz291815 Exp $$
 */
public class Test {

    @org.junit.Test
    public void test() throws IOException {
        Resource[] configResources = new PathMatchingResourcePatternResolver().getResources(
                "classpath*:com/whz/propertyeditor/spring-propertyEditorConfig.xml");
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(configResources[0]);

        // 通过BeanFactoryPostProcessor后置处理器注册自定义属性编辑器（BeanFactory需要通过硬编码的形式手动注入编辑器，
        // ApplicationContext容器则不需要手动注入，该注入操作交由容器自动注入）
        CustomEditorConfigurer configurer = (CustomEditorConfigurer) xmlBeanFactory.getBean("customEditorConfigurer");
        configurer.postProcessBeanFactory((ConfigurableListableBeanFactory) xmlBeanFactory);

        Person person = (Person) xmlBeanFactory.getBean("person");
        System.out.println(person);
    }

}
