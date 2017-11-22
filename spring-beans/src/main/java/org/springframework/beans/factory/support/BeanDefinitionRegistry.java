
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.AliasRegistry;

// spring通过BeanDefinition将配置文件中Bean转化为容器的内部表示，并将这些BeanDefinition注册到BeanDefinitionRegistry中
// spring容器的BeanDefinitionRegistry就像是Spring配置信息的内存数据库，主要是以map的形式保存，后续操作直接从BeanDefinitionRegistry中读取配置信息
public interface BeanDefinitionRegistry extends AliasRegistry {

	// 注册beanDefinition
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException;
	// 移除beanDefinition
	void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	// 获取beanDefinition
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	// 判断是否包含beanDefinition
	boolean containsBeanDefinition(String beanName);
	// 获取所有已经注册的beanName
	String[] getBeanDefinitionNames();
	// 获取已经注册的beanDefinition的个数
	int getBeanDefinitionCount();
	// 确定给定bean名称是否已在该注册表中使用，即是否有本地bean或别名在该名称下注册
	boolean isBeanNameInUse(String beanName);

}
