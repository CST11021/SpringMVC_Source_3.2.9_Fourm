
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

// 为bean定义生成bean名称的策略接口
public interface BeanNameGenerator {

	// 对于给定的BeanDefinition生成一个name
	String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);

}
