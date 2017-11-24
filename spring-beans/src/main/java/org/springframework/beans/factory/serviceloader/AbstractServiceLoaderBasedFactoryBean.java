/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.serviceloader;

import java.util.ServiceLoader;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for FactoryBeans operating on the
 * JDK 1.6 {@link java.util.ServiceLoader} facility.
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see java.util.ServiceLoader
 */

/**
 AbstractServiceLoaderBasedFactoryBean：FactoryBean的抽象基类，它是操作JDK1.6 ServiceLoader的基础工具。
 ServiceFactoryBean：暴露指定配置的服务类的基础服务的FactoryBean，通过JDK1.6 serviceLoader基础类来获取这些服务。
 ServiceListFactoryBean：暴露配置的服务类的所有基础服务的FactoryBea，表现为一组服务对象，可以通过JDK1.6 serviceLoader基础类来获取这些服务。
 ServiceLoaderFactoryBean：暴露指定配置服务类的JDK1.6 serviceLoader的FactoryBean。
 */
public abstract class AbstractServiceLoaderBasedFactoryBean extends AbstractFactoryBean
		implements BeanClassLoaderAware {

	private Class serviceType;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();


	/**
	 * Specify the desired service type (typically the service's public API).
	 */
	public void setServiceType(Class serviceType) {
		this.serviceType = serviceType;
	}

	/**
	 * Return the desired service type.
	 */
	public Class getServiceType() {
		return this.serviceType;
	}

	@Override
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}


	/**
	 * Delegates to {@link #getObjectToExpose(java.util.ServiceLoader)}.
	 * @return the object to expose
	 */
	@Override
	protected Object createInstance() {
		Assert.notNull(getServiceType(), "Property 'serviceType' is required");
		return getObjectToExpose(ServiceLoader.load(getServiceType(), this.beanClassLoader));
	}

	/**
	 * Determine the actual object to expose for the given ServiceLoader.
	 * <p>Left to concrete subclasses.
	 * @param serviceLoader the ServiceLoader for the configured service class
	 * @return the object to expose
	 */
	protected abstract Object getObjectToExpose(ServiceLoader serviceLoader);

}
