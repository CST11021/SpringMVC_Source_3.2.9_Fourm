/*
 * Copyright 2002-2012 the original author or authors.
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

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

// 该接口可以在Bean生命周期的另外两个时期提供扩展的回调接口，
// 即实例化Bean之前（调用postProcessBeforeInstantiation方法）和实例化Bean之后（调用postProcessAfterInstantiation方法）
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	// Bean 调用构造函数实例化之前执行该方法，如果该方法一个非空的实例，则BeanFactory直接返回该Bean实例对象，后续的属性
	// 注入也就不再执行了
	Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;
	// Spring调用构造器实例化Bean，然后将Bean包装为一个BeanWrapper后，并在所有的配置属性注入到Bean前该处理器方法被调用，
	// 该处理器方法的返回值是一个Boolean值，它可以用来控制是否继续注入Bean属性
	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;


	// 在工厂将给定的属性值应用到给定的bean之前。允许检查是否已满足所有依赖项，例如基于bean属性设置器上的“Required”注解。
	// 还允许替换属性值，通常通过创建一个基于原始属性值的MutablePropertyValues实例，添加或删除特定的值
	// 该方法完成其他定制的一些依赖注入，如：
	// AutowiredAnnotationBeanPostProcessor执行@Autowired注解注入
	// CommonAnnotationBeanPostProcessor执行@Resource等注解的注入
	// PersistenceAnnotationBeanPostProcessor执行@ PersistenceContext等JPA注解的注入
	// RequiredAnnotationBeanPostProcessor执行@Required注解的检查等等
	// checkDependencies：依赖检查
	// applyPropertyValues：应用明确的setter属性注入
	PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException;

}
