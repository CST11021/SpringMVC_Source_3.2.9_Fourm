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

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;

// 这里将大部分方法设计为 protected 为了让子类继承
public abstract class FactoryBeanRegistrySupport extends DefaultSingletonBeanRegistry {

	// 缓存被工厂bean创建的单例对象，Map：FactoryBean name --> object
	private final Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>(16);

	// 判断这个工厂bean创建的bean的类型
	protected Class<?> getTypeForFactoryBean(final FactoryBean<?> factoryBean) {
		try {
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(new PrivilegedAction<Class<?>>() {
					public Class<?> run() {
						return factoryBean.getObjectType();
					}
				}, getAccessControlContext());
			}
			else {
				return factoryBean.getObjectType();
			}
		}
		catch (Throwable ex) {
			// Thrown from the FactoryBean's getObjectType implementation.
			logger.warn("FactoryBean threw exception from getObjectType, despite the contract saying " + "that it should return null if the type of its object cannot be determined yet", ex);
			return null;
		}
	}

	// 从this.factoryBeanObjectCache 中获取这个beanName对应的工厂bean
	protected Object getCachedObjectForFactoryBean(String beanName) {
		Object object = this.factoryBeanObjectCache.get(beanName);
		return (object != NULL_OBJECT ? object : null);
	}

	// 根据beanName从给定的工厂bean中获取一个bean
	protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
		if (factory.isSingleton() && containsSingleton(beanName)) {
			synchronized (getSingletonMutex()) {
				Object object = this.factoryBeanObjectCache.get(beanName);
				if (object == null) {
					object = doGetObjectFromFactoryBean(factory, beanName, shouldPostProcess);
					this.factoryBeanObjectCache.put(beanName, (object != null ? object : NULL_OBJECT));
				}
				return (object != NULL_OBJECT ? object : null);
			}
		}
		else {
			return doGetObjectFromFactoryBean(factory, beanName, shouldPostProcess);
		}
	}
	// shouldPostProcess为true时，则给则个bean应用 applyBeanPostProcessorsAfterInitialization（bean实例化后的后处理器）
	private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName, final boolean shouldPostProcess) throws BeanCreationException {

		Object object;
		try {
			if (System.getSecurityManager() != null) {
				AccessControlContext acc = getAccessControlContext();
				try {
					object = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
								return factory.getObject();
							}
						}, acc);
				}
				catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			}
			else {
				object = factory.getObject();
			}
		}
		catch (FactoryBeanNotInitializedException ex) {
			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
		}


		// Do not accept a null value for a FactoryBean that's not fully
		// initialized yet: Many FactoryBeans just return null then.
		if (object == null && isSingletonCurrentlyInCreation(beanName)) {
			throw new BeanCurrentlyInCreationException(
					beanName, "FactoryBean which is currently in creation returned null from getObject");
		}

		if (object != null && shouldPostProcess) {
			try {
				object = postProcessObjectFromFactoryBean(object, beanName);
			}
			catch (Throwable ex) {
				throw new BeanCreationException(beanName, "Post-processing of the FactoryBean's object failed", ex);
			}
		}

		return object;
	}
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) throws BeansException {
		return object;
	}

	// 判断这个beanInstance 是不是一个工厂bean，如果是做强转
	protected FactoryBean<?> getFactoryBean(String beanName, Object beanInstance) throws BeansException {
		if (!(beanInstance instanceof FactoryBean)) {
			throw new BeanCreationException(beanName, "Bean instance of type [" + beanInstance.getClass() + "] is not a FactoryBean");
		}
		return (FactoryBean<?>) beanInstance;
	}

	// this.factoryBeanObjectCache.remove(beanName)
	@Override
	protected void removeSingleton(String beanName) {
		super.removeSingleton(beanName);
		this.factoryBeanObjectCache.remove(beanName);
	}

	// 返回此bean工厂的安全上下文. 如果有设置安全管理器，那么与用户交互的代码将使用此方法返回的安全上下文权限来执行
	protected AccessControlContext getAccessControlContext() {
		return AccessController.getContext();
	}

}
