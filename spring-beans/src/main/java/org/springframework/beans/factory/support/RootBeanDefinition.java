/*
 * Copyright 2002-2013 the original author or authors.
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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.Assert;

// 一个RootBeanDefinition定义表明它是一个可合并的bean definition：即在spring beanFactory运行期间，可以返回一个特定的bean。RootBeanDefinition可以作为一个重要的通用的bean definition 视图。
// RootBeanDefinition用来在配置阶段进行注册bean definition。 从spring 2.5后，编写注册bean definition有了更好的的方法：GenericBeanDefinition。GenericBeanDefinition支持动态定义父类依赖，而非硬编码作为root bean definition。
// 涉及到的类：BeanDefinitionHolder，根据名称或者别名持有beanDefinition。可以为一个内部bean 注册为placeholder。
// BeanDefinitionHolder也可以编写一个内部bean definition的注册，如果你不关注BeanNameAware等，完全可以使用RootBeanDefinition或者ChildBeanDefinition来替代。
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

	boolean allowCaching = true;
	private BeanDefinitionHolder decoratedDefinition;
	private volatile Class<?> targetType;
	boolean isFactoryMethodUnique = false;
	final Object constructorArgumentLock = new Object();

	// 表示bean实例创建时要使用的构造器
	Object resolvedConstructorOrFactoryMethod;

	// 标识是否已经知道使用哪个构造器来进行实例化了
	boolean constructorArgumentsResolved = false;

	// 表示bean实例化时需要的构造器参数
	Object[] resolvedConstructorArguments;

	// 表示缓存中的构造器参数，它与resolvedConstructorArguments的区别是，resolvedConstructorArguments是最终实例化时要用的参数，
	// 如给定方法的构造函数A(int ,int)则通过此方法后就会把配置中的("1","1")转化为(1,1)，缓存中的值可能是原始值也可能是最终值
	Object[] preparedConstructorArguments;

	final Object postProcessingLock = new Object();

	// 用来标记是否已经对这个应用了MergedBeanDefinitionPostProcessor处理器
	boolean postProcessed = false;

	/** Package-visible field that indicates a before-instantiation post-processor having kicked in */
	// 用于标识这个bean是否由后处理创建的，bean实例化前会执行一些后处理器方法，如果bean在这些处理器方法中就被实例化了，则beforeInstantiationResolved为true
	volatile Boolean beforeInstantiationResolved;

	private Set<Member> externallyManagedConfigMembers;
	private Set<String> externallyManagedInitMethods;
	private Set<String> externallyManagedDestroyMethods;


	public RootBeanDefinition() {
		super();
	}
	public RootBeanDefinition(Class<?> beanClass) {
		super();
		setBeanClass(beanClass);
	}
	@Deprecated
	public RootBeanDefinition(Class beanClass, boolean singleton) {
		super();
		setBeanClass(beanClass);
		setSingleton(singleton);
	}
	@Deprecated
	public RootBeanDefinition(Class beanClass, int autowireMode) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
	}
	public RootBeanDefinition(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		}
	}
	@Deprecated
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs) {
		super(null, pvs);
		setBeanClass(beanClass);
	}
	@Deprecated
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs, boolean singleton) {
		super(null, pvs);
		setBeanClass(beanClass);
		setSingleton(singleton);
	}
	public RootBeanDefinition(Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClass(beanClass);
	}
	public RootBeanDefinition(String beanClassName) {
		setBeanClassName(beanClassName);
	}
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}
	public RootBeanDefinition(RootBeanDefinition original) {
		super((BeanDefinition) original);
		this.allowCaching = original.allowCaching;
		this.decoratedDefinition = original.decoratedDefinition;
		this.targetType = original.targetType;
		this.isFactoryMethodUnique = original.isFactoryMethodUnique;
	}
	RootBeanDefinition(BeanDefinition original) {
		super(original);
	}


	public String getParentName() {
		return null;
	}

	public void setParentName(String parentName) {
		if (parentName != null) {
			throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
		}
	}

	/**
	 * Register a target definition that is being decorated by this bean definition.
	 */
	public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
		this.decoratedDefinition = decoratedDefinition;
	}

	/**
	 * Return the target definition that is being decorated by this bean definition, if any.
	 */
	public BeanDefinitionHolder getDecoratedDefinition() {
		return this.decoratedDefinition;
	}

	/**
	 * Specify the target type of this bean definition, if known in advance.
	 */
	public void setTargetType(Class<?> targetType) {
		this.targetType = targetType;
	}

	/**
	 * Return the target type of this bean definition, if known
	 * (either specified in advance or resolved on first instantiation).
	 */
	public Class<?> getTargetType() {
		return this.targetType;
	}

	/**
	 * Specify a factory method name that refers to a non-overloaded method.
	 */
	public void setUniqueFactoryMethodName(String name) {
		Assert.hasText(name, "Factory method name must not be empty");
		setFactoryMethodName(name);
		this.isFactoryMethodUnique = true;
	}

	// 检查 candidate 是否可以作为工厂方法
	public boolean isFactoryMethod(Method candidate) {
		// 判断依据是该方法名称是否是对应 factory-method 配置的名称
		return (candidate != null && candidate.getName().equals(getFactoryMethodName()));
	}

	/**
	 * Return the resolved factory method as a Java Method object, if available.
	 * @return the factory method, or {@code null} if not found or not resolved yet
	 */
	public Method getResolvedFactoryMethod() {
		synchronized (this.constructorArgumentLock) {
			Object candidate = this.resolvedConstructorOrFactoryMethod;
			return (candidate instanceof Method ? (Method) candidate : null);
		}
	}

	// 将配置的属性（比如通过@Autowire注入的属性）添加到 this.externallyManagedConfigMembers
	public void registerExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedConfigMembers == null) {
				this.externallyManagedConfigMembers = new HashSet<Member>(1);
			}
			this.externallyManagedConfigMembers.add(configMember);
		}
	}
	// 判断 this.externallyManagedConfigMembers 是否包含指定的配置属性
	public boolean isExternallyManagedConfigMember(Member configMember) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedConfigMembers != null &&
					this.externallyManagedConfigMembers.contains(configMember));
		}
	}

	public void registerExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedInitMethods == null) {
				this.externallyManagedInitMethods = new HashSet<String>(1);
			}
			this.externallyManagedInitMethods.add(initMethod);
		}
	}

	public boolean isExternallyManagedInitMethod(String initMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedInitMethods != null &&
					this.externallyManagedInitMethods.contains(initMethod));
		}
	}

	public void registerExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			if (this.externallyManagedDestroyMethods == null) {
				this.externallyManagedDestroyMethods = new HashSet<String>(1);
			}
			this.externallyManagedDestroyMethods.add(destroyMethod);
		}
	}

	public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
		synchronized (this.postProcessingLock) {
			return (this.externallyManagedDestroyMethods != null &&
					this.externallyManagedDestroyMethods.contains(destroyMethod));
		}
	}


	@Override
	public RootBeanDefinition cloneBeanDefinition() {
		return new RootBeanDefinition(this);
	}
	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
	}
	@Override
	public String toString() {
		return "Root bean: " + super.toString();
	}

}
