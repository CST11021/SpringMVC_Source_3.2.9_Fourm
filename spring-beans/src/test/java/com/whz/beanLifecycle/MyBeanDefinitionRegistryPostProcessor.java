package com.whz.beanLifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @author wb-whz291815
 * @version $Id: MyBeanDefinitionRegistryPostProcessor.java, v 0.1 2018-01-27 10:29 wb-whz291815 Exp $$
 */
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 该方法在BeanFactory层不会被执行，只有在ApplicationContext容器中才会被调用
        System.out.println("MyBeanDefinitionRegistryPostProcessor#postProcessBeanDefinitionRegistry");
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
