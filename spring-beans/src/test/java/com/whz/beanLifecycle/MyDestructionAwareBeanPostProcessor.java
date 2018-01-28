package com.whz.beanLifecycle;/**
 * Created by whz on 2018/1/28.
 */

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;

/**
 * @author whz
 * @version : MyDestructionAwareBeanPostProcessor.java, v 0.1 2018-01-28 13:40 whz Exp $$
 */
public class MyDestructionAwareBeanPostProcessor implements DestructionAwareBeanPostProcessor {
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        System.out.println("执行DestructionAwareBeanPostProcessor#postProcessBeforeDestruction");
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }
}
