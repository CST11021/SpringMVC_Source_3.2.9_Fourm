
package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * ConfigurableBeanFactory：是一个重要的接口，增强IOC容器的可定制性，它定义了设置类装载器、属性编辑器、容器初始化后置处理器等方法
 * AutowireCapableBeanFactory：定义了将容器的Bean按某种规则（如按名字匹配、按类型匹配等）进行自动装配的方法
 * SingletonBeanRegistry:定义了允许在运行期间向容器注册单实例bean的方法
 * BeanDefinitionRegistry：Spring配置文件中每一个<bean>节点元素在Spring容器里都通过一个BeanDefinition对象表示，他描述了Bean的配置信息。而BeanDefinitionRegistry接口提供了向容器手工注册BeanDefinition对象的方法。
 */
public interface BeanFactory {

	// 用户使用容器时，可以使用转义符“&”来得到FactoryBean本身，用来区分通过容器来获取FactoryBean产生的对象和获取FactoryBean本身。
	// 举例来说，如果 myJndiObject 是一个FactoryBean，那么使用 &myJndiObject 得到的是 FactoryBean，而不是 myJndiObject 这个 FactoryBean 产生的出来的对象。
	String FACTORY_BEAN_PREFIX = "&";

	// 获取Bean
	Object getBean(String name) throws BeansException;
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;
	<T> T getBean(Class<T> requiredType) throws BeansException;
	// 按名字和显示声明的参数来创建实例，注意显示声明的参数是用来创建原型的
	Object getBean(String name, Object... args) throws BeansException;

	// 判断是否包含指定id的bean
	boolean containsBean(String name);
	// 判断是否为单例bean
	boolean isSingleton(String name) throws NoSuchBeanDefinitionException;
	// 判断是否为原型bean，原型bean是无状态的
	boolean isPrototype(String name) throws NoSuchBeanDefinitionException;
	// 判断该bean是否匹配指定的类型
	boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException;
	// 获取该bean的类类型
	Class<?> getType(String name) throws NoSuchBeanDefinitionException;
	// 获取bean的别名
	String[] getAliases(String name);

}
