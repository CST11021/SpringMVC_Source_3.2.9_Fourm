
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

// 集大成者,提供解析,修改bean定义,并与初始化单例
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, AutowireCapableBeanFactory, ConfigurableBeanFactory {

	// 忽略自动装配依赖类型，比如String
	void ignoreDependencyType(Class<?> type);
	// 忽略了自动装配依赖接口
	void ignoreDependencyInterface(Class<?> ifc);

	// 注册一个与依赖类型相对应的自动绑定值
	void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue);

	// 判断指定的bean是否具有自动注入的功能，并将其注入到其他bean中，这些bean声明了匹配类型的依赖关系。
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException;

	// 根据beanName返回一个BeanDefinition
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	// 冻结所有的bean定义，意味着已注册的bean定义将不再被修改或后处理。
	void freezeConfiguration();

	// 返回该工厂的bean定义是否被冻结(也就是说，不应该被修改或后处理。)
	boolean isConfigurationFrozen();

	// 实例化所有的单例bean（配置了lazy-init="true"的bean除外），用于解决循环依赖问题
	void preInstantiateSingletons() throws BeansException;

}
