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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.util.ObjectUtils;

/**
 * Programmatic means of constructing
 * {@link org.springframework.beans.factory.config.BeanDefinition BeanDefinitions}
 * using the builder pattern. Intended primarily for use when implementing Spring 2.0
 * {@link org.springframework.beans.factory.xml.NamespaceHandler NamespaceHandlers}.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanDefinitionBuilder {



	// 创建一个GenericBeanDefinition实例
	public static BeanDefinitionBuilder genericBeanDefinition() {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new GenericBeanDefinition();
		return builder;
	}
	public static BeanDefinitionBuilder genericBeanDefinition(Class beanClass) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new GenericBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		return builder;
	}
	public static BeanDefinitionBuilder genericBeanDefinition(String beanClassName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new GenericBeanDefinition();
		builder.beanDefinition.setBeanClassName(beanClassName);
		return builder;
	}
	// 创建一个RootBeanDefinition实例
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass) {
		return rootBeanDefinition(beanClass, null);
	}
	public static BeanDefinitionBuilder rootBeanDefinition(Class beanClass, String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClass(beanClass);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName) {
		return rootBeanDefinition(beanClassName, null);
	}
	public static BeanDefinitionBuilder rootBeanDefinition(String beanClassName, String factoryMethodName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new RootBeanDefinition();
		builder.beanDefinition.setBeanClassName(beanClassName);
		builder.beanDefinition.setFactoryMethodName(factoryMethodName);
		return builder;
	}
	// 创建一个ChildBeanDefinition实例
	public static BeanDefinitionBuilder childBeanDefinition(String parentName) {
		BeanDefinitionBuilder builder = new BeanDefinitionBuilder();
		builder.beanDefinition = new ChildBeanDefinition(parentName);
		return builder;
	}

	private AbstractBeanDefinition beanDefinition;
	// 表示Bean构造器当前的参数索引
	private int constructorArgIndex;

	private BeanDefinitionBuilder() {
	}

	public AbstractBeanDefinition getRawBeanDefinition() {
		return this.beanDefinition;
	}
	public AbstractBeanDefinition getBeanDefinition() {
		this.beanDefinition.validate();
		return this.beanDefinition;
	}
	// 设置BeanDefinition的父类
	public BeanDefinitionBuilder setParentName(String parentName) {
		this.beanDefinition.setParentName(parentName);
		return this;
	}
	// 设置BeanDefinition的FactoryMethod
	public BeanDefinitionBuilder setFactoryMethod(String factoryMethod) {
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}
	@Deprecated
	public BeanDefinitionBuilder setFactoryBean(String factoryBean, String factoryMethod) {
		this.beanDefinition.setFactoryBeanName(factoryBean);
		this.beanDefinition.setFactoryMethodName(factoryMethod);
		return this;
	}
	@Deprecated
	public BeanDefinitionBuilder addConstructorArg(Object value) {
		return addConstructorArgValue(value);
	}
	public BeanDefinitionBuilder addConstructorArgValue(Object value) {
		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(this.constructorArgIndex++, value);
		return this;
	}
	public BeanDefinitionBuilder addConstructorArgReference(String beanName) {
		this.beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(
				this.constructorArgIndex++, new RuntimeBeanReference(beanName));
		return this;
	}
	public BeanDefinitionBuilder addPropertyValue(String name, Object value) {
		this.beanDefinition.getPropertyValues().add(name, value);
		return this;
	}
	public BeanDefinitionBuilder addPropertyReference(String name, String beanName) {
		this.beanDefinition.getPropertyValues().add(name, new RuntimeBeanReference(beanName));
		return this;
	}
	public BeanDefinitionBuilder setInitMethodName(String methodName) {
		this.beanDefinition.setInitMethodName(methodName);
		return this;
	}
	public BeanDefinitionBuilder setDestroyMethodName(String methodName) {
		this.beanDefinition.setDestroyMethodName(methodName);
		return this;
	}
	public BeanDefinitionBuilder setScope(String scope) {
		this.beanDefinition.setScope(scope);
		return this;
	}
	@Deprecated
	public BeanDefinitionBuilder setSingleton(boolean singleton) {
		this.beanDefinition.setSingleton(singleton);
		return this;
	}
	public BeanDefinitionBuilder setAbstract(boolean flag) {
		this.beanDefinition.setAbstract(flag);
		return this;
	}
	public BeanDefinitionBuilder setLazyInit(boolean lazy) {
		this.beanDefinition.setLazyInit(lazy);
		return this;
	}
	public BeanDefinitionBuilder setAutowireMode(int autowireMode) {
		beanDefinition.setAutowireMode(autowireMode);
		return this;
	}
	public BeanDefinitionBuilder setDependencyCheck(int dependencyCheck) {
		beanDefinition.setDependencyCheck(dependencyCheck);
		return this;
	}
	public BeanDefinitionBuilder addDependsOn(String beanName) {
		if (this.beanDefinition.getDependsOn() == null) {
			this.beanDefinition.setDependsOn(new String[] {beanName});
		}
		else {
			String[] added = ObjectUtils.addObjectToArray(this.beanDefinition.getDependsOn(), beanName);
			this.beanDefinition.setDependsOn(added);
		}
		return this;
	}
	public BeanDefinitionBuilder setRole(int role) {
		this.beanDefinition.setRole(role);
		return this;
	}
	@Deprecated
	public BeanDefinitionBuilder setSource(Object source) {
		this.beanDefinition.setSource(source);
		return this;
	}
	@Deprecated
	public BeanDefinitionBuilder setResourceDescription(String resourceDescription) {
		this.beanDefinition.setResourceDescription(resourceDescription);
		return this;
	}

}
