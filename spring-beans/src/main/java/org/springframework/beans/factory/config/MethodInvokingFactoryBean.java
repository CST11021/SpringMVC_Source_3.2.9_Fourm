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

import java.lang.reflect.InvocationTargetException;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.ClassUtils;


/**
 通过MethodInvokingFactoryBean工厂Bean，可将目标方法的返回值注入为Bean的属性值。这个工厂Bean用来获取指定方法的返回值，该
 方法既可以是静态方法，也可以是实例方法；这个值既可以被注入到指定Bean实例的指定属性，也可以直接定义成Bean实例。看例子：
 <bean id="valueGenerator" class="com.abc.util.ValueGenerator" />
 <bean id="son1" class="com.abc.service.Son">
	 <property name="age">
		 <!-- 获取方法返回值：调用valueGenerator的getValue方法 -->
		 <bean class="org.springframework.beans.factory.config.MethodInvokingFactoryBean">
			 <property name="targetObject" ref="valueGenerator" />
			 <property name="targetMethod" value="getValue" />
		 </bean>
	 </property>
 </bean>
 */
public class MethodInvokingFactoryBean extends ArgumentConvertingMethodInvoker
		implements FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware, InitializingBean {

	private boolean singleton = true;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private ConfigurableBeanFactory beanFactory;

	private boolean initialized = false;

	/** Method call result in the singleton case */
	private Object singletonObject;


	/**
	 * Set if a singleton should be created, or a new object on each
	 * request else. Default is "true".
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public boolean isSingleton() {
		return this.singleton;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	@Override
	protected Class resolveClassName(String className) throws ClassNotFoundException {
		return ClassUtils.forName(className, this.beanClassLoader);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableBeanFactory) {
			this.beanFactory = (ConfigurableBeanFactory) beanFactory;
		}
	}

	/**
	 * Obtain the TypeConverter from the BeanFactory that this bean runs in,
	 * if possible.
	 * @see ConfigurableBeanFactory#getTypeConverter()
	 */
	@Override
	protected TypeConverter getDefaultTypeConverter() {
		if (this.beanFactory != null) {
			return this.beanFactory.getTypeConverter();
		}
		else {
			return super.getDefaultTypeConverter();
		}
	}


	public void afterPropertiesSet() throws Exception {
		prepare();
		if (this.singleton) {
			this.initialized = true;
			this.singletonObject = doInvoke();
		}
	}

	/**
	 * Perform the invocation and convert InvocationTargetException
	 * into the underlying target exception.
	 */
	private Object doInvoke() throws Exception {
		try {
			return invoke();
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof Exception) {
				throw (Exception) ex.getTargetException();
			}
			if (ex.getTargetException() instanceof Error) {
				throw (Error) ex.getTargetException();
			}
			throw ex;
		}
	}

	/**
	 * Returns the same value each time if the singleton property is set
	 * to "true", otherwise returns the value returned from invoking the
	 * specified method on the fly.
	 */
	public Object getObject() throws Exception {
		if (this.singleton) {
			if (!this.initialized) {
				throw new FactoryBeanNotInitializedException();
			}
			// Singleton: return shared object.
			return this.singletonObject;
		}
		else {
			// Prototype: new object on each call.
			return doInvoke();
		}
	}

	/**
	 * Return the type of object that this FactoryBean creates,
	 * or {@code null} if not known in advance.
	 */
	public Class<?> getObjectType() {
		if (!isPrepared()) {
			// Not fully initialized yet -> return null to indicate "not known yet".
			return null;
		}
		return getPreparedMethod().getReturnType();
	}

}
