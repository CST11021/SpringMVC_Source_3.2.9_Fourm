
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

	/**
	 * Determine whether the specified bean qualifies as an autowire candidate,
	 * to be injected into other beans which declare a dependency of matching type.
	 * <p>This method checks ancestor factories as well.
	 * @param beanName the name of the bean to check
	 * @param descriptor the descriptor of the dependency to resolve
	 * @return whether the bean should be considered as autowire candidate
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 */
	boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException;

	/**
	 * Return the registered BeanDefinition for the specified bean, allowing access
	 * to its property values and constructor argument value (which can be
	 * modified during bean factory post-processing).
	 * <p>A returned BeanDefinition object should not be a copy but the original
	 * definition object as registered in the factory. This means that it should
	 * be castable to a more specific implementation type, if necessary.
	 * <p><b>NOTE:</b> This method does <i>not</i> consider ancestor factories.
	 * It is only meant for accessing local bean definitions of this factory.
	 * @param beanName the name of the bean
	 * @return the registered BeanDefinition
	 * @throws NoSuchBeanDefinitionException if there is no bean with the given name
	 * defined in this factory
	 */
	BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;

	/**
	 * Freeze all bean definitions, signalling that the registered bean definitions
	 * will not be modified or post-processed any further.
	 * <p>This allows the factory to aggressively cache bean definition metadata.
	 */
	void freezeConfiguration();

	/**
	 * Return whether this factory's bean definitions are frozen,
	 * i.e. are not supposed to be modified or post-processed any further.
	 * @return {@code true} if the factory's configuration is considered frozen
	 */
	boolean isConfigurationFrozen();

	// 预加载不是懒加载的单例.用于解决循环依赖问题
	void preInstantiateSingletons() throws BeansException;

}
