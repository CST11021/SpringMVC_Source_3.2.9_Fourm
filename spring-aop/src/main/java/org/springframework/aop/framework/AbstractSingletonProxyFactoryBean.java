/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.aop.framework;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;

/**
 * Convenient superclass for {@link FactoryBean} types that produce singleton-scoped
 * proxy objects.
 *
 * <p>Manages pre- and post-interceptors (references, rather than
 * interceptor names, as in {@link ProxyFactoryBean}) and provides
 * consistent interface management.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractSingletonProxyFactoryBean extends ProxyConfig
		implements FactoryBean<Object>, BeanClassLoaderAware, InitializingBean {

	// 要被代理的目标对象
	private Object target;
	// 要代理的接口
	private Class<?>[] proxyInterfaces;
	//
	private Object[] preInterceptors;

	private Object[] postInterceptors;

	// Default is global AdvisorAdapterRegistry
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
	// 表示创建代理对象使用的类加载器
	private transient ClassLoader proxyClassLoader;
	// 表示代理后的对象
	private Object proxy;


	// 设置目标类、增强和代理接口等信息，生成的代理对象也是在该方法中创建的
	public void afterPropertiesSet() {
		// 代理工厂要代理的目标可能不能为空
		if (this.target == null) {
			throw new IllegalArgumentException("Property 'target' is required");
		}
		// 要代理的目标类，必须指向一个Bean（也就是目标类必须声明为一个Bean）
		if (this.target instanceof String) {
			throw new IllegalArgumentException("'target' needs to be a bean reference, not a bean name as value");
		}
		// 如果没有设置类加载器，则使用默认的类加载器
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
		}

		ProxyFactory proxyFactory = new ProxyFactory();

		// 给代理工厂设置增强信息，将拦截器包装为Advisor
		if (this.preInterceptors != null) {
			for (Object interceptor : this.preInterceptors) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
			}
		}

		// Add the main interceptor (typically an Advisor).
		// 给代理工厂添加主要的拦截器（比较典型的一种增强）
		proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(createMainInterceptor()));

		// 给代理工厂设置增强信息，将拦截器包装为Advisor
		if (this.postInterceptors != null) {
			for (Object interceptor : this.postInterceptors) {
				proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(interceptor));
			}
		}

		// 复制代理的配置信息（从this中拷贝代理信息到代理工厂，注意：AbstractSingletonProxyFactoryBean extends ProxyConfig）
		proxyFactory.copyFrom(this);

		TargetSource targetSource = createTargetSource(this.target);
		proxyFactory.setTargetSource(targetSource);

		if (this.proxyInterfaces != null) {
			proxyFactory.setInterfaces(this.proxyInterfaces);
		}
		else if (!isProxyTargetClass()) {
			// Rely on AOP infrastructure to tell us what interfaces to proxy.
			proxyFactory.setInterfaces(
					ClassUtils.getAllInterfacesForClass(targetSource.getTargetClass(), this.proxyClassLoader));
		}

		this.proxy = proxyFactory.getProxy(this.proxyClassLoader);
	}
	public Object getObject() {
		// 这里的 this.proxy 在Bean的后置处理器方法afterPropertiesSet()中被创建了
		if (this.proxy == null) {
			throw new FactoryBeanNotInitializedException();
		}
		return this.proxy;
	}
	public Class<?> getObjectType() {
		if (this.proxy != null) {
			return this.proxy.getClass();
		}
		if (this.proxyInterfaces != null && this.proxyInterfaces.length == 1) {
			return this.proxyInterfaces[0];
		}
		if (this.target instanceof TargetSource) {
			return ((TargetSource) this.target).getTargetClass();
		}
		if (this.target != null) {
			return this.target.getClass();
		}
		return null;
	}
	public final boolean isSingleton() {
		return true;
	}


	// 将目标对象包装为一个 TargetSource 为后续代理做准备
	protected TargetSource createTargetSource(Object target) {
		if (target instanceof TargetSource) {
			return (TargetSource) target;
		}
		else {
			return new SingletonTargetSource(target);
		}
	}
	// Create the "main" interceptor for this proxy factory bean.
	// Typically an Advisor, but can also be any type of Advice.
	// Pre-interceptors will be applied before, post-interceptors will be applied after this interceptor.
	protected abstract Object createMainInterceptor();


	// setter ...
	public void setTarget(Object target) {
		this.target = target;
	}
	public void setProxyInterfaces(Class<?>[] proxyInterfaces) {
		this.proxyInterfaces = proxyInterfaces;
	}
	public void setPreInterceptors(Object[] preInterceptors) {
		this.preInterceptors = preInterceptors;
	}
	public void setPostInterceptors(Object[] postInterceptors) {
		this.postInterceptors = postInterceptors;
	}
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}
	public void setProxyClassLoader(ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
	}
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (this.proxyClassLoader == null) {
			this.proxyClassLoader = classLoader;
		}
	}

}
