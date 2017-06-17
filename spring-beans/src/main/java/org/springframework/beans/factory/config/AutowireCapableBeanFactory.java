
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
	/**
	 * Instantiate a new bean instance of the given class with the specified autowire
	 * strategy. All constants defined in this interface are supported here.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * before-instantiation callbacks (e.g. for annotation-driven injection).
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the construction of the instance.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance (not applicable to autowiring a constructor,
	 * thus ignored there)
	 * @return the new bean instance
	 * @throws BeansException if instantiation or wiring failed
	 * @see #AUTOWIRE_NO
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_CONSTRUCTOR
	 * @see #AUTOWIRE_AUTODETECT
	 * @see #initializeBean
	 * @see #applyBeanPostProcessorsBeforeInitialization
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException;
	/**
	 * Autowire the bean properties of the given bean instance by name or type.
	 * Can also be invoked with {@code AUTOWIRE_NO} in order to just apply
	 * after-instantiation callbacks (e.g. for annotation-driven injection).
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * @param existingBean the existing bean instance
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for object
	 * references in the bean instance
	 * @throws BeansException if wiring failed
	 * @see #AUTOWIRE_BY_NAME
	 * @see #AUTOWIRE_BY_TYPE
	 * @see #AUTOWIRE_NO
	 */
	void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException;
	/**
	 * Apply the property values of the bean definition with the given name to
	 * the given bean instance. The bean definition can either define a fully
	 * self-contained bean, reusing its property values, or just property values
	 * meant to be used for existing bean instances.
	 * <p>This method does <i>not</i> autowire bean properties; it just applies
	 * explicitly defined property values. Use the {@link #autowireBeanProperties}
	 * method to autowire an existing bean instance.
	 * <b>Note: This method requires a bean definition for the given name!</b>
	 * <p>Does <i>not</i> apply standard {@link BeanPostProcessor BeanPostProcessors}
	 * callbacks or perform any further initialization of the bean. This interface
	 * offers distinct, fine-grained operations for those purposes, for example
	 * {@link #initializeBean}. However, {@link InstantiationAwareBeanPostProcessor}
	 * callbacks are applied, if applicable to the configuration of the instance.
	 * @param existingBean the existing bean instance
	 * @param beanName the name of the bean definition in the bean factory
	 * (a bean definition of that name has to be available)
	 * @throws org.springframework.beans.factory.NoSuchBeanDefinitionException
	 * if there is no bean definition with the given name
	 * @throws BeansException if applying the property values failed
	 * @see #autowireBeanProperties
	 */
	void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException;

	// 初始化一个Bean...
	Object initializeBean(Object existingBean, String beanName) throws BeansException;
	// 初始化之前执行BeanPostProcessors
	Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException;
	// 初始化之后执行BeanPostProcessors
	Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException;

	/**
	 * Resolve the specified dependency against the beans defined in this factory.
	 * @param descriptor the descriptor for the dependency
	 * @param beanName the name of the bean which declares the present dependency
	 * @param autowiredBeanNames a Set that all names of autowired beans (used for
	 * resolving the present dependency) are supposed to be added to
	 * @param typeConverter the TypeConverter to use for populating arrays and
	 * collections
	 * @return the resolved object, or {@code null} if none found
	 * @throws BeansException in dependency resolution failed
	 */
	Object resolveDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException;

}
