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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Simple object instantiation strategy for use in a BeanFactory.
 *
 * <p>Does not support Method Injection, although it provides hooks for subclasses
 * to override to add Method Injection support, for example by overriding methods.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
// 在BeanFactory中使用的简单对象实例化策略。
//SimpleInstantiationStrategy是spring用来生成bean对象的默认类，他提供了两种实例java对象的方法。
//一种是通过BeanUtils，他使用JDK的反射功能，一种是通过cglib来生成的
public class SimpleInstantiationStrategy implements InstantiationStrategy {

	private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<Method>();


	// 返回一个当前被调用的工厂方法对象
	public static Method getCurrentlyInvokedFactoryMethod() {
		return currentlyInvokedFactoryMethod.get();
	}
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {
		// Don't override the class with CGLIB if no overrides.
		// 如果没有方法覆盖 lookup-override 和 replaced-method
		// 如果有需要覆盖或者动态替换的方法则当然需要使用cglib进行动态代理，因为可以在创建代理的同事将动态方法织入类中，但如果没有需要动态改变的方法，为了方便直接反射就可以了
		if (beanDefinition.getMethodOverrides().isEmpty()) {
			Constructor<?> constructorToUse;
			synchronized (beanDefinition.constructorArgumentLock) {
				//这里取指定的构造器或生产对象的工厂方法来对bean进行实例化
				constructorToUse = (Constructor<?>) beanDefinition.resolvedConstructorOrFactoryMethod;
				if (constructorToUse == null) {
					final Class<?> clazz = beanDefinition.getBeanClass();
					if (clazz.isInterface()) {
						throw new BeanInstantiationException(clazz, "Specified class is an interface");
					}
					try {
						if (System.getSecurityManager() != null) {
							constructorToUse = AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor>() {
								public Constructor<?> run() throws Exception {
									return clazz.getDeclaredConstructor((Class[]) null);
								}
							});
						}
						else {
							constructorToUse =	clazz.getDeclaredConstructor((Class[]) null);
						}
						beanDefinition.resolvedConstructorOrFactoryMethod = constructorToUse;
					}
					catch (Exception ex) {
						throw new BeanInstantiationException(clazz, "No default constructor found", ex);
					}
				}
			}
			// 通过BeanUtils进行实例化，这个BeanUtils的实例化通过Constructor来实例化bean，
			// 在beanUtils中可以到具体的调用ctor.newInstance(args)
			return BeanUtils.instantiateClass(constructorToUse);
		}
		else {
			// 使用cglib实例化对象
			return instantiateWithMethodInjection(beanDefinition, beanName, owner);
		}
	}
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, final Constructor<?> ctor, Object[] args) {

		if (beanDefinition.getMethodOverrides().isEmpty()) {
			if (System.getSecurityManager() != null) {
				// use own privileged to change accessibility (when security is on)
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						ReflectionUtils.makeAccessible(ctor);
						return null;
					}
				});
			}
			return BeanUtils.instantiateClass(ctor, args);
		}
		else {
			return instantiateWithMethodInjection(beanDefinition, beanName, owner, ctor, args);
		}
	}
	public Object instantiate(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Object factoryBean, final Method factoryMethod, Object[] args) {

		try {
			if (System.getSecurityManager() != null) {
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						ReflectionUtils.makeAccessible(factoryMethod);
						return null;
					}
				});
			}
			else {
				ReflectionUtils.makeAccessible(factoryMethod);
			}

			Method priorInvokedFactoryMethod = currentlyInvokedFactoryMethod.get();
			try {
				currentlyInvokedFactoryMethod.set(factoryMethod);
				return factoryMethod.invoke(factoryBean, args);
			}
			finally {
				if (priorInvokedFactoryMethod != null) {
					currentlyInvokedFactoryMethod.set(priorInvokedFactoryMethod);
				}
				else {
					currentlyInvokedFactoryMethod.remove();
				}
			}
		}
		catch (IllegalArgumentException ex) {
			throw new BeanDefinitionStoreException("Illegal arguments to factory method [" + factoryMethod + "]; " + "args: " + StringUtils.arrayToCommaDelimitedString(args));
		}
		catch (IllegalAccessException ex) {
			throw new BeanDefinitionStoreException("Cannot access factory method [" + factoryMethod + "]; is it public?");
		}
		catch (InvocationTargetException ex) {
			throw new BeanDefinitionStoreException("Factory method [" + factoryMethod + "] threw exception", ex.getTargetException());
		}
	}

	// 使用无参构造器来实例化，使用给定的RootBeanDefinition中指定的方法注入一个实例化对象，默认抛出一个异常，子类可以覆盖这个方法。
	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}
	// 使用给定的RootBeanDefinition中指定的方法注入一个实例化对象，默认抛出一个异常，子类可以覆盖这个方法。
	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Constructor<?> ctor, Object[] args) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}



}
