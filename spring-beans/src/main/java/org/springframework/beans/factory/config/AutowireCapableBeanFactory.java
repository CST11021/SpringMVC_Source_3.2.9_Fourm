
package org.springframework.beans.factory.config;

import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;

// AutowireCapableBeanFactory：定义了将容器的Bean按某种规则（如按名字匹配、按类型匹配等）进行自动装配的方法
public interface AutowireCapableBeanFactory extends BeanFactory {

	//不使用自动装配
	int AUTOWIRE_NO = 0;
	//通过名称自动装配
	int AUTOWIRE_BY_NAME = 1;
	//通过类型自动装配
	int AUTOWIRE_BY_TYPE = 2;
	//构造器装配
	int AUTOWIRE_CONSTRUCTOR = 3;
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;


	//------ 创建外部Bean实例的典型方法---------------------------
	// 根据指定Class创建一个全新的Bean实例
	<T> T createBean(Class<T> beanClass) throws BeansException;
	// 给定对象，根据注释、后处理器等，进行自动装配
	void autowireBean(Object existingBean) throws BeansException;
	// 根据Bean名的BeanDefinition装配这个未加工的Object，执行回调和各种后处理器
	Object configureBean(Object existingBean, String beanName) throws BeansException;
	// 分解Bean在工厂中定义的这个指定的依赖descriptor
	Object resolveDependency(DependencyDescriptor descriptor, String beanName) throws BeansException;


	//-----------Bean实例生命周期相关方法-------------------
	// 根据给定的类型和指定的装配策略，创建一个新的Bean实例
	Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;
	// // 根据指定autowireMode实例化策略，初始化这个bean，bean的初始化有多种方式，比如利用反射机制调用构造器实例化，或
	// 通过动态代理的方式实例化，或通过工厂模式进行创建等
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;
	// 给这个已经实例化的bean进行属性注入
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException;
	// 给这个已经实例化的bean进行属性注入
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	// 初始化一个Bean...
	Object initializeBean(Object existingBean, String beanName) throws BeansException;
	// 初始化之前执行BeanPostProcessors
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException;
	// 初始化之后执行BeanPostProcessors
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException;
	// 根据工厂中定义的bean来解决指定的依赖项，descriptor用来描述被依赖的Bean对象，比如：Bean A中通过xml配置或通过@Autowire
	// 注解配置依赖对象B，Spring会先实例化B，初始化B时会通过这个方法来进行实例化，descriptor用来描述B，beanName表示A的beanName
	// autowiredBeanNames 表示被注入的Bean B
	Object resolveDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException;

}
