package com.whz.beanLifecycle;/**
 * Created by whz on 2018/1/28.
 */

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author whz
 * @version : MyMergedBeanDefinitionPostProcessor.java, v 0.1 2018-01-28 14:09 whz Exp $$
 */
public class MyMergedBeanDefinitionPostProcessor implements MergedBeanDefinitionPostProcessor {
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        System.out.println("执行MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition");
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
