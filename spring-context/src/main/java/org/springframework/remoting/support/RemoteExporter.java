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

package org.springframework.remoting.support;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.util.ClassUtils;

/**
 * Abstract base class for classes that export a remote service.
 * Provides "service" and "serviceInterface" bean properties.
 *
 * <p>Note that the service interface being used will show some signs of
 * remotability, like the granularity of method calls that it offers.
 * Furthermore, it has to have serializable arguments etc.
 *
 * @author Juergen Hoeller
 * @since 26.12.2003
 */
public abstract class RemoteExporter extends RemotingSupport {

	/** 表示服务接口的实现对象，服务启动时，该对象必须不能为空 */
	private Object service;
	/** 要暴露的服务接口类型 */
	private Class serviceInterface;
    /** 用于标识生成服务接口实例的代理时，是否织入日志跟踪拦截器 */
	private Boolean registerTraceInterceptor;
	/** 表示生成代理对象时，要织入的增强 */
	private Object[] interceptors;


	/**
     * 为这个要暴露的服务创建一个代理对象，该代理主要是用于织入日志跟踪拦截器
	 * Get a proxy for the given service object, implementing the specified service interface.
	 * <p>Used to export a proxy that does not expose any internals but just
	 * a specific interface intended for remote access. Furthermore, a
	 * {@link RemoteInvocationTraceInterceptor} will be registered (by default).
	 * @return the proxy
	 * @see #setServiceInterface
	 * @see #setRegisterTraceInterceptor
	 * @see RemoteInvocationTraceInterceptor
	 */
	protected Object getProxyForService() {
		checkService();
		checkServiceInterface();
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.addInterface(getServiceInterface());
		if (this.registerTraceInterceptor != null ?
				this.registerTraceInterceptor.booleanValue() : this.interceptors == null) {
			proxyFactory.addAdvice(new RemoteInvocationTraceInterceptor(getExporterName()));
		}
		if (this.interceptors != null) {
			AdvisorAdapterRegistry adapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
			for (int i = 0; i < this.interceptors.length; i++) {
				proxyFactory.addAdvisor(adapterRegistry.wrap(this.interceptors[i]));
			}
		}
		proxyFactory.setTarget(getService());
        // 设置生成的代理对象可以强制转型为Advised
		proxyFactory.setOpaque(true);
		return proxyFactory.getProxy(getBeanClassLoader());
	}
	/**
	 * 检查暴露的服务接口是有对应的实现类，并且是否有没有声明为Bean
	 * @see #setService
	 */
	protected void checkService() throws IllegalArgumentException {
		if (getService() == null) {
			throw new IllegalArgumentException("Property 'service' is required");
		}
	}
	/**
	 * Check whether a service reference has been set,
	 * and whether it matches the specified service.
	 * @see #setServiceInterface
	 * @see #setService
	 */
	protected void checkServiceInterface() throws IllegalArgumentException {
		Class serviceInterface = getServiceInterface();
		Object service = getService();
		if (serviceInterface == null) {
			throw new IllegalArgumentException("Property 'serviceInterface' is required");
		}
		if (service instanceof String) {
			throw new IllegalArgumentException("Service [" + service + "] is a String " +
					"rather than an actual service reference: Have you accidentally specified " +
					"the service bean name as value instead of as reference?");
		}
		if (!serviceInterface.isInstance(service)) {
			throw new IllegalArgumentException("Service interface [" + serviceInterface.getName() +
					"] needs to be implemented by service [" + service + "] of class [" +
					service.getClass().getName() + "]");
		}
	}
	/**
	 * Return a short name for this exporter.
	 * Used for tracing of remote invocations.
	 * <p>Default is the unqualified class name (without package).
	 * Can be overridden in subclasses.
	 * @see #getProxyForService
	 * @see RemoteInvocationTraceInterceptor
	 * @see org.springframework.util.ClassUtils#getShortName
	 */
	protected String getExporterName() {
		return ClassUtils.getShortName(getClass());
	}


    public void setService(Object service) {
        this.service = service;
    }
    public Object getService() {
        return this.service;
    }
    public void setServiceInterface(Class serviceInterface) {
        if (serviceInterface != null && !serviceInterface.isInterface()) {
            throw new IllegalArgumentException("'serviceInterface' must be an interface");
        }
        this.serviceInterface = serviceInterface;
    }
    public Class getServiceInterface() {
        return this.serviceInterface;
    }
    public void setRegisterTraceInterceptor(boolean registerTraceInterceptor) {
        this.registerTraceInterceptor = Boolean.valueOf(registerTraceInterceptor);
    }
    public void setInterceptors(Object[] interceptors) {
        this.interceptors = interceptors;
    }

}
