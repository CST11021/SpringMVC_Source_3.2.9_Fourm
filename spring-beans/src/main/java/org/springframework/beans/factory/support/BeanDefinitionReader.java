
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

//spring提供了有两种方式的BeanDefinition解析器：PropertiesBeanDefinitionReader和XmLBeanDefinitionReader即属性文件格式的BeanDefinition解析器和xml文件格式的BeanDefinition解析器。
public interface BeanDefinitionReader {

	// 获取一个BeanDefinitionRegistry，用于存放从BeanDefinition
	BeanDefinitionRegistry getRegistry();
	// 获取Spring配置文件信息
	ResourceLoader getResourceLoader();
	// 获取bean的装载器
	ClassLoader getBeanClassLoader();
	// 获取一个生成beanname的策略接口
	BeanNameGenerator getBeanNameGenerator();


	// 从资源文件中将配置的bean信息转化为BeanDefinition，并将其注册到一个BeanDefinitionRegistry
	int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException;
	int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException;
	int loadBeanDefinitions(String location) throws BeanDefinitionStoreException;
	int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException;

}
