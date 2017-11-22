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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Simple template superclass for {@link FactoryBean} implementations that
 * creates a singleton or a prototype object, depending on a flag.
 *
 * <p>If the "singleton" flag is {@code true} (the default),
 * this class will create the object that it creates exactly once
 * on initialization and subsequently return said singleton instance
 * on all calls to the {@link #getObject()} method.
 *
 * <p>Else, this class will create a new instance every time the
 * {@link #getObject()} method is invoked. Subclasses are responsible
 * for implementing the abstract {@link #createInstance()} template
 * method to actually create the object(s) to expose.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @since 1.0.2
 * @see #setSingleton
 * @see #createInstance()
 */
// 工厂bean的接口抽象基类
public abstract class AbstractFactoryBean<T>
		implements FactoryBean<T>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	// 表示该工厂创建的是否为单例对象
	private boolean singleton = true;
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
	// 表示该工厂bean所在的IOC容器
	private BeanFactory beanFactory;
	// 表示要创建的实例对象是否已经被创建
	private boolean initialized = false;
	// 缓存工厂创建的单例（如果工厂创建的是单例对象，则缓存该对象，第二次创建的时候直接返回该对象）
	private T singletonInstance;
	private T earlySingletonInstance;


	// 设置该工厂是否生成单例bean
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	public boolean isSingleton() {
		return this.singleton;
	}
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	protected BeanFactory getBeanFactory() {
		return this.beanFactory;
	}
	protected TypeConverter getBeanTypeConverter() {
		BeanFactory beanFactory = getBeanFactory();
		if (beanFactory instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) beanFactory).getTypeConverter();
		}
		else {
			return new SimpleTypeConverter();
		}
	}
	public void afterPropertiesSet() throws Exception {
		if (isSingleton()) {
			this.initialized = true;
			this.singletonInstance = createInstance();
			this.earlySingletonInstance = null;
		}
	}
	public final T getObject() throws Exception {
		if (isSingleton()) {
			return (this.initialized ? this.singletonInstance : getEarlySingletonInstance());
		}
		else {
			return createInstance();
		}
	}
	// 销毁所有的单例bean
	public void destroy() throws Exception {
		if (isSingleton()) {
			destroyInstance(this.singletonInstance);
		}
	}
	// 获取该工厂创建的bean的类型
	public abstract Class<?> getObjectType();
	// 创建bean实例的逻辑留给子类实现
	protected abstract T createInstance() throws Exception;
	// 返回该工厂要创建的实例，是否为一个接口的实例，如果是则返回接口，否则返回null
	protected Class[] getEarlySingletonInterfaces() {
		Class type = getObjectType();
		return (type != null && type.isInterface() ? new Class[] {type} : null);
	}
	// 销毁实例
	protected void destroyInstance(T instance) throws Exception {
	}
	// 使用JDK 动态代理创建一个实例
	@SuppressWarnings("unchecked")
	private T getEarlySingletonInstance() throws Exception {
		Class[] ifcs = getEarlySingletonInterfaces();
		if (ifcs == null) {
			throw new FactoryBeanNotInitializedException(getClass().getName() + " does not support circular references");
		}
		if (this.earlySingletonInstance == null) {
			// JDK 动态代理
			this.earlySingletonInstance = (T) Proxy.newProxyInstance(
				this.beanClassLoader, ifcs, new EarlySingletonInvocationHandler());
		}
		return this.earlySingletonInstance;
	}
	// 如果该工厂的单例对象已经创建好了则返回，否则抛异常
	private T getSingletonInstance() throws IllegalStateException {
		if (!this.initialized) {
			throw new IllegalStateException("Singleton instance not initialized yet");
		}
		return this.singletonInstance;
	}



	// JDK 动态代理的一个调用处理器实现
	private class EarlySingletonInvocationHandler implements InvocationHandler {
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (ReflectionUtils.isEqualsMethod(method)) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			}
			else if (ReflectionUtils.isHashCodeMethod(method)) {
				// Use hashCode of reference proxy.
				return System.identityHashCode(proxy);
			}
			else if (!initialized && ReflectionUtils.isToStringMethod(method)) {
				return "Early singleton proxy for interfaces " +
						ObjectUtils.nullSafeToString(getEarlySingletonInterfaces());
			}
			try {
				return method.invoke(getSingletonInstance(), args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
