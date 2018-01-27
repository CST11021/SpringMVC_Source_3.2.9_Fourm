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


public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 说明：
	 	在调用bean构造函数实例化前被调用，IOC层在调用Bean构造器实例化之前会先执行该处理器方法，如果该处理器方法返回一个非
	 	空对象，则IOC容器会中断后续初始化流程，即后续的属性注入也就不再执行了，直接返回该非空对象作为Bean的实例。

	 相关的应用：
	 	1、AbstractAutoProxyCreator
	 	Spring中的自动代理机制中就是通过该处理器方法来实现的，它通过扩展该处理器方法，在IOC层调用Bean构造器实例化之前会先
	 	执行该处理器方法，并遍历所有的Bean判断这个Bean是否可以被代理（该实现机制是通过配置一个目标Bean与增强匹配的表达式
	 	来现实的，如RegexpMethodPointcutAdvisor，并通过该表达式判断每个Bean是否存有对应的增强器，如果存在说明该bean可以被
	 	自动代理），然后在 InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation 处理器方法中织入增强，并返
	 	回代理后的代理类，返回代理类后IOC就直接返回该Bean实例了，后续的属性注入则无法再执行了。
	 */
	Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;
	// Spring调用构造器实例化Bean，然后将Bean包装为一个BeanWrapper后，并在所有的配置属性注入到Bean前该处理器方法被调用，
	// 该处理器方法的返回值是一个Boolean值，它可以用来控制是否继续注入Bean属性

	/**
	 说明：
		 Spring调用构造器实例化Bean，然后将Bean包装为一个BeanWrapper后，并在所有的配置属性注入到Bean前该处理器方法被调用，
	 	该处理器方法的返回值是一个Boolean值，它可以用来控制是否继续注入Bean属性，在Spring源码中属性注入方法populateBean()
	 	的执行步骤如下：
		 1、执行InstantiationAwareBeanPostProcessor处理器的postProcessAfterInstantiation方法，该函数可以控制程序是否继续
	 		进行属性填充；
		 2、根据注入类型(byName/byType)，提取依赖的bean，并统一存入PropertyValues中；
		 3、执行InstantiationAwareBeanPostProcessor#postProcessPropertyValues方法，属性获取完毕后，并在将PropertyValues注
	 		入到Bean前对属性的再次处理，典型应用是requiredAnnotationBeanPostProcessor 类中对属性的验证；
		 4、将所有PropertyValues中的属性填充至BeanWrapper中。

	 相关的应用：
	 	。。。
	 */
	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;

	/**
	 说明：
	 	该处理器方法是在BeanWrapper给Bean注入属性之前被调用的

	 相关的应用：
		 1、AutowiredAnnotationBeanPostProcessor
		 BeanWrapper在将给定的属性值注入到目标Bean之前，Spring会调用AutowiredAnnotationBeanPostProcessor#postProcessPropertyValues
	 	 处理器方法，将所有@Autowired注解修饰的依赖Bean注入到目标Bean，也就说由@Autowired注解修饰的Bean属性最先被注入到Bean中

		 2、RequiredAnnotationBeanPostProcessor
		 如果一个bean某些字段必须含有，则可以使用@Required注释，RequiredAnnotationBeanPostProcessor#postProcessPropertyValues
	 	 在所有属性注入到Bean前，回去检查所有被@Required注解修饰的方法（@Required只能修饰方法），判断是否有对应的属性注入。
	 	 如果任何带有@Required的属性未设置的话 将会抛出BeanInitializationException异常。

		 3、CommonAnnotationBeanPostProcessor
		 CommonAnnotationBeanPostProcessor通过扩展该处理器方法，将那些被@Resource注解修饰的属性注入到Bean

		 4、PersistenceAnnotationBeanPostProcessor执行@ PersistenceContext等JPA注解的注入
	 */
	PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException;

}
