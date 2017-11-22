
package org.springframework.beans.factory;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.springframework.beans.BeansException;

// 该接口定义了访问容器中Bean的基本信息的若干方法，如查看Bean的个数、获取某一类型Bean的配置名、查看容器中是否包含某一Bean等方法
public interface ListableBeanFactory extends BeanFactory {

	// 是否包含某bean
	boolean containsBeanDefinition(String beanName);
	// 返回IOC容器bean的个数
	int getBeanDefinitionCount();
	// 获取容器中所有Bean名称（这里是指的是当前容器的Bean）
	String[] getBeanDefinitionNames();

	// 根据指定类型查找bean
	String[] getBeanNamesForType(Class<?> type);
	String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit);
	<T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException;
	<T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException;

	// 查找注解类型的bean
	Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException;
	// 判断这个bean是否带有annotationType类型的注解，如果有，则返回配置的注解信息；否则返回null
	<A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType);

}
