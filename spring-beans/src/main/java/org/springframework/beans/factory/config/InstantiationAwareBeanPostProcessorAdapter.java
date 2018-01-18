/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.config;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

/**
 * Adapter that implements all methods on {@link SmartInstantiationAwareBeanPostProcessor}
 * as no-ops, which will not change normal processing of each bean instantiated
 * by the container. Subclasses may override merely those methods that they are
 * actually interested in.
 *
 * <p>Note that this base class is only recommendable if you actually require
 * {@link InstantiationAwareBeanPostProcessor} functionality. If all you need
 * is plain {@link BeanPostProcessor} functionality, prefer a straight
 * implementation of that (simpler) interface.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class InstantiationAwareBeanPostProcessorAdapter implements SmartInstantiationAwareBeanPostProcessor {

	// SmartInstantiationAwareBeanPostProcessor 接口
	public Class<?> predictBeanType(Class<?> beanClass, String beanName) {
		return null;
	}
	// 解析这个bean实例化时要使用的构造器
	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}
	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}





	// InstantiationAwareBeanPostProcessor 接口：

	// bean实例化前被调用
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}
	// bean实例化后被调用
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}
	// 在工厂将给定的属性值应用到给定的bean之前。允许检查是否已满足所有依赖项，例如基于bean属性设置器上的“Required”注解。
	// 还允许替换属性值，通常通过创建一个基于原始属性值的MutablePropertyValues实例，添加或删除特定的值
	// 该方法完成其他定制的一些依赖注入，如：
	// AutowiredAnnotationBeanPostProcessor执行@Autowired注解注入
	// CommonAnnotationBeanPostProcessor执行@Resource等注解的注入
	// PersistenceAnnotationBeanPostProcessor执行@ PersistenceContext等JPA注解的注入
	// RequiredAnnotationBeanPostProcessor执行@Required注解的检查等等
	// checkDependencies：依赖检查
	// applyPropertyValues：应用明确的setter属性注入
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}




	// BeanPostProcessor 接口：

	// Bean 调用构造函数，实例化之前执行该方法
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	// Bean 调用构造函数，实例化之后执行该方法
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
