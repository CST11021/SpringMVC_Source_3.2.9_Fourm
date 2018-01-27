package com.whz.circulreference;/**
 * Created by whz on 2018/1/28.
 */

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * @author whz
 * @version : Test.java, v 0.1 2018-01-28 0:15 whz Exp $$
 */
public class Test {

    // 使用构造器依赖注入会报错
    @org.junit.Test
    public void testConstructorCircul() {
        Resource res = new ClassPathResource("com/whz/circulreference/spring-constructor-circul.xml");
        BeanFactory beanFactory = new XmlBeanFactory(res);
        System.out.println(beanFactory.getBean("a"));
    }

    // 使用注入依赖注入不会报错
    @org.junit.Test
    public void testPropertyCircul() {
        Resource res = new ClassPathResource("com/whz/circulreference/spring-property-circul.xml");
        BeanFactory beanFactory = new XmlBeanFactory(res);
        StudentA studentA = (StudentA) beanFactory.getBean("a");
        System.out.println(studentA);
    }

}
