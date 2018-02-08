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

package org.springframework.aop.framework;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.Interceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.adapter.AdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.aop.framework.adapter.UnknownAdviceTypeException;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.OrderComparator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link org.springframework.beans.factory.FactoryBean} implementation that builds an
 * AOP proxy based on beans in Spring {@link org.springframework.beans.factory.BeanFactory}.
 *
 * <p>{@link org.aopalliance.intercept.MethodInterceptor MethodInterceptors} and
 * {@link org.springframework.aop.Advisor Advisors} are identified by a list of bean
 * names in the current bean factory, specified through the "interceptorNames" property.
 * The last entry in the list can be the name of a target bean or a
 * {@link org.springframework.aop.TargetSource}; however, it is normally preferable
 * to use the "targetName"/"target"/"targetSource" properties instead.
 *
 * <p>Global interceptors and advisors can be added at the factory level. The specified
 * ones are expanded in an interceptor list where an "xxx*" entry is included in the
 * list, matching the given prefix with the bean names (e.g. "global*" would match
 * both "globalBean1" and "globalBean2", "*" all defined interceptors). The matching
 * interceptors get applied according to their returned order value, if they implement
 * the {@link org.springframework.core.Ordered} interface.
 *
 * <p>Creates a JDK proxy when proxy interfaces are given, and a CGLIB proxy for the
 * actual target class if not. Note that the latter will only work if the target class
 * does not have final methods, as a dynamic subclass will be created at runtime.
 *
 * <p>It's possible to cast a proxy obtained from this factory to {@link Advised},
 * or to obtain the ProxyFactoryBean reference and programmatically manipulate it.
 * This won't work for existing prototype references, which are independent. However,
 * it will work for prototypes subsequently obtained from the factory. Changes to
 * interception will work immediately on singletons (including existing references).
 * However, to change interfaces or target it's necessary to obtain a new instance
 * from the factory. This means that singleton instances obtained from the factory
 * do not have the same object identity. However, they do have the same interceptors
 * and target, and changing any reference will change all objects.
 *

 此类表示被织入增强后的代理类，在Spring中，我们进行要使用 ProxyFactoryBean 来配置一个，被织入增强后的代理类，如：

 <!--要被织入的增强-->
 <bean id="pmonitor" class="com.whz.aop.advice.ControllablePerformanceMonitor" />
 <!--被织入的目标类-->
 <bean id="forumServiceTarget" class="com.whz.aop.advice.ForumService" />
 <!--被织入增强后的代理类-->
 <bean id="forumService" class="org.springframework.aop.framework.ProxyFactoryBean"
 p:interfaces="com.whz.aop.advice.Monitorable"
 p:target-ref="forumServiceTarget"
 p:interceptorNames="pmonitor"
 p:proxyTargetClass="true" />


 此外，要注意，ProxyFactoryBean 是一个工厂Bean，它实现了 FactoryBean 接口，当我们使用如下代码：
 String configPath = "com/whz/aop/advice/spring-aop.xml";
 ApplicationContext ctx = new ClassPathXmlApplicationContext(configPath);
 ForumService forumService = (ForumService)ctx.getBean("forumService");

 这里获取的 forumService 实例不是一个 ProxyFactoryBean 类型的实例，而是由 ProxyFactoryBean#getObject() 生成的对象实例


 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setInterceptorNames
 * @see #setProxyInterfaces
 * @see org.aopalliance.intercept.MethodInterceptor
 * @see org.springframework.aop.Advisor
 * @see Advised
 */
@SuppressWarnings("serial")
public class ProxyFactoryBean extends ProxyCreatorSupport implements FactoryBean<Object>, BeanClassLoaderAware, BeanFactoryAware {

	protected final Log logger = LogFactory.getLog(getClass());

	// 拦截器列表中的这个后缀表示要扩展全局
	public static final String GLOBAL_SUFFIX = "*";
	// 表示被织入的增强名，比如一个目标类可以被织入多个增强，这些增强需事先一些增强接口，
	// 如：MethodBeforeAdvice、AfterReturningAdvice和MethodInterceptor等，而这个interceptorNames表示的是实现这些增强接口
	// 的bean的BeanName，该属性也称为拦截器链
	private String[] interceptorNames;
	// 表示目标bean的beanName
	private String targetName;
	// 设置是否在没有指定的情况下自动检测代理接口，置为false后，如果没有指定代理接口，将为目标类创建一个CGLIB代理
	private boolean autodetectInterfaces = true;
	// 标识这个代理工厂返回的代理bean是否总是单例bean
	private boolean singleton = true;
	// AdvisorAdapter用于将advice包装为advisor对象，和从advisor中获取方法拦截器
	private AdvisorAdapterRegistry advisorAdapterRegistry = GlobalAdvisorAdapterRegistry.getInstance();
	// 表示是否冻结代理的相关配置
	private boolean freezeProxy = false;
	// 表示创建代理时使用的类加载器
	private transient ClassLoader proxyClassLoader = ClassUtils.getDefaultClassLoader();
	// 判断是否已配置proxyClassLoader（代理类的类加载器）
	private transient boolean classLoaderConfigured = false;
	private transient BeanFactory beanFactory;
	// 标识该增强链是否已经被初始化
	private boolean advisorChainInitialized = false;
	// 如果这个代理工厂返回的代理bean总是单例bean，则缓存这个代理实例
	private Object singletonInstance;



	// ProxyFactoryBean 将调用该方法生成一个代理对象返回给用户（因为返回的Object对象是由JDK 动态代理生成的，所以在代码调
	// 试的时候无法查看其对象信息）
	public Object getObject() throws BeansException {
		initializeAdvisorChain();
		if (isSingleton()) {
			return getSingletonInstance();
		}
		else {
			if (this.targetName == null) {
				logger.warn("Using non-singleton proxies with singleton targets is often undesirable. " +
						"Enable prototype proxies by setting the 'targetName' property.");
			}
			return newPrototypeInstance();
		}
	}
	// 将配置中增强器添加到拦截器链中，如：p:interceptorNames="greetingBefore,greetingAfter,greetAround" 这样的配置
	private synchronized void initializeAdvisorChain() throws AopConfigException, BeansException {
		if (this.advisorChainInitialized) {
			// 如果增强链已经被初始化了，则直接返回
			return;
		}

		// 首先要判断是否有配置增强逻辑
		if (!ObjectUtils.isEmpty(this.interceptorNames)) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("No BeanFactory available anymore (probably due to serialization) " +
						"- cannot resolve interceptor names " + Arrays.asList(this.interceptorNames));
			}

			// Globals can't be last unless we specified a targetSource using the property...
			if (this.interceptorNames[this.interceptorNames.length - 1].endsWith(GLOBAL_SUFFIX) &&
					this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
				throw new AopConfigException("Target required after globals");
			}

			// 遍历这些增强链
			for (String name : this.interceptorNames) {
				if (logger.isTraceEnabled()) {
					logger.trace("Configuring advisor or advice '" + name + "'");
				}

				if (name.endsWith(GLOBAL_SUFFIX)) {
					// 判断是否以“*”结尾
					if (!(this.beanFactory instanceof ListableBeanFactory)) {
						throw new AopConfigException("Can only use global advisors or interceptors with a ListableBeanFactory");
					}
					addGlobalAdvisor((ListableBeanFactory) this.beanFactory, name.substring(0, name.length() - GLOBAL_SUFFIX.length()));
				}
				else {
					Object advice;
					if (this.singleton || this.beanFactory.isSingleton(name)) {
						// 如果ProxyFactoryBean总是返回单实例的代理对象，或者配置的这个增强是一个单实例
						advice = this.beanFactory.getBean(name);
					}
					else {
						// 如果是一个原型增强，则用原型替换
						advice = new PrototypePlaceholderAdvisor(name);
					}
					// 将增强添加到增强链中
					addAdvisorOnChainCreation(advice, name);
				}
			}
		}

		this.advisorChainInitialized = true;
	}
	// Return the singleton instance of this class's proxy object, lazily creating it if it hasn't been created already.
	private synchronized Object getSingletonInstance() {
		if (this.singletonInstance == null) {
			this.targetSource = freshTargetSource();

			if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
				// 依赖于AOP的配置来告诉我们要代理的接口
				Class targetClass = getTargetClass();
				if (targetClass == null) {
					throw new FactoryBeanNotInitializedException("Cannot determine target class for proxy");
				}
				setInterfaces(ClassUtils.getAllInterfacesForClass(targetClass, this.proxyClassLoader));
			}
			// Initialize the shared singleton instance.
			super.setFrozen(this.freezeProxy);
			this.singletonInstance = getProxy(createAopProxy());
		}
		return this.singletonInstance;
	}

	// 设置代理类要实现的接口
	public void setProxyInterfaces(Class[] proxyInterfaces) throws ClassNotFoundException {
		setInterfaces(proxyInterfaces);
	}

	// 使用JDK代理这些指定的接口
	protected Class createCompositeInterface(Class[] interfaces) {
		return ClassUtils.createCompositeInterface(interfaces, this.proxyClassLoader);
	}

	// 创建一个原型的代理bean
	private synchronized Object newPrototypeInstance() {
		if (logger.isTraceEnabled()) {
			logger.trace("Creating copy of prototype ProxyFactoryBean config: " + this);
		}

		// 1、复制代理配置对象
		ProxyCreatorSupport copy = new ProxyCreatorSupport(getAopProxyFactory());
		// 从beanFactory中获取目标类对应的Bean，并包装为一个TargetSource对象
		TargetSource targetSource = freshTargetSource();
		copy.copyConfigurationFrom(this, targetSource, freshAdvisorChain());
		if (this.autodetectInterfaces && getProxiedInterfaces().length == 0 && !isProxyTargetClass()) {
			// 返回目标类实现的所有接口
			copy.setInterfaces(
					ClassUtils.getAllInterfacesForClass(targetSource.getTargetClass(), this.proxyClassLoader));
		}
		copy.setFrozen(this.freezeProxy);

		if (logger.isTraceEnabled()) {
			logger.trace("Using ProxyCreatorSupport copy: " + copy);
		}
		// 2、根据代理配置创建代理对象
		return getProxy(copy.createAopProxy());
	}

	// 使用相应的代理方式，返回一个代理对象
	protected Object getProxy(AopProxy aopProxy) {
		return aopProxy.getProxy(this.proxyClassLoader);
	}

	// 检查interceptorNames列表的最后一个元素可能是目标类Bean，如果是，则从列表移除
	private void checkInterceptorNames() {
		if (!ObjectUtils.isEmpty(this.interceptorNames)) {
			String finalName = this.interceptorNames[this.interceptorNames.length - 1];
			if (this.targetName == null && this.targetSource == EMPTY_TARGET_SOURCE) {
				// The last name in the chain may be an Advisor/Advice or a target/TargetSource.
				// Unfortunately we don't know; we must look at type of the bean.
				if (!finalName.endsWith(GLOBAL_SUFFIX) && !isNamedBeanAnAdvisorOrAdvice(finalName)) {
					// The target isn't an interceptor.
					this.targetName = finalName;
					if (logger.isDebugEnabled()) {
						logger.debug("Bean with name '" + finalName + "' concluding interceptor chain " +
								"is not an advisor class: treating it as a target or TargetSource");
					}
					String[] newNames = new String[this.interceptorNames.length - 1];
					// 将 interceptorNames 复制到 newNames
					System.arraycopy(this.interceptorNames, 0, newNames, 0, newNames.length);
					this.interceptorNames = newNames;
				}
			}
		}
	}

	// 判断指定beanName对应的bean是否是Advice或Advisor对象
	private boolean isNamedBeanAnAdvisorOrAdvice(String beanName) {
		Class namedBeanClass = this.beanFactory.getType(beanName);
		if (namedBeanClass != null) {
			return (Advisor.class.isAssignableFrom(namedBeanClass) || Advice.class.isAssignableFrom(namedBeanClass));
		}
		// Treat it as an target bean if we can't tell.
		if (logger.isDebugEnabled()) {
			logger.debug("Could not determine type of bean with name '" + beanName +
					"' - assuming it is neither an Advisor nor an Advice");
		}
		return false;
	}

	// 将所有的增强转为可被织入的Advisor对象，配置的原型bean
	private List<Advisor> freshAdvisorChain() {
		// 获取增强链，所有配置的增强Advice都会被包装成 Advisor
		Advisor[] advisors = getAdvisors();
		List<Advisor> freshAdvisors = new ArrayList<Advisor>(advisors.length);

		// 遍历所有的增强，则将所有增强转为一个可被织入的Advisor对象（配置的Advice如果是原型bean，一开始会被封装为一个
		// PrototypePlaceholderAdvisor对象）
		for (Advisor advisor : advisors) {
			if (advisor instanceof PrototypePlaceholderAdvisor) {
				PrototypePlaceholderAdvisor pa = (PrototypePlaceholderAdvisor) advisor;
				if (logger.isDebugEnabled()) {
					logger.debug("Refreshing bean named '" + pa.getBeanName() + "'");
				}
				// Replace the placeholder with a fresh prototype instance resulting from a getBean() lookup
				if (this.beanFactory == null) {
					throw new IllegalStateException("No BeanFactory available anymore (probably due to serialization) " +
							"- cannot resolve prototype advisor '" + pa.getBeanName() + "'");
				}
				// 从beanFactory中获取增强bean
				Object bean = this.beanFactory.getBean(pa.getBeanName());
				// 将Advice包装为一个Advisor
				Advisor refreshedAdvisor = namedBeanToAdvisor(bean);

				freshAdvisors.add(refreshedAdvisor);
			}
			else {
				// Add the shared instance.
				freshAdvisors.add(advisor);
			}
		}
		return freshAdvisors;
	}

	// Add all global interceptors and pointcuts.
	private void addGlobalAdvisor(ListableBeanFactory beanFactory, String prefix) {
		// 返回这个beanFactory中，所有增强bean
		String[] globalAdvisorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Advisor.class);
		String[] globalInterceptorNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(beanFactory, Interceptor.class);

		List<Object> beans = new ArrayList<Object>(globalAdvisorNames.length + globalInterceptorNames.length);
		Map<Object, String> names = new HashMap<Object, String>(beans.size());
		for (String name : globalAdvisorNames) {
			Object bean = beanFactory.getBean(name);
			beans.add(bean);
			names.put(bean, name);
		}
		for (String name : globalInterceptorNames) {
			Object bean = beanFactory.getBean(name);
			beans.add(bean);
			names.put(bean, name);
		}

		OrderComparator.sort(beans);

		for (Object bean : beans) {
			String name = names.get(bean);
			if (name.startsWith(prefix)) {
				addAdvisorOnChainCreation(bean, name);
			}
		}
	}

	// 添加 next 对应的增强到增强链中
	private void addAdvisorOnChainCreation(Object next, String name) {
		// We need to convert to an Advisor if necessary so that our source reference matches what we find from superclass interceptors.
		// 将Advice包装为一个Advisor
		Advisor advisor = namedBeanToAdvisor(next);
		if (logger.isTraceEnabled()) {
			logger.trace("Adding advisor with name '" + name + "'");
		}
		addAdvisor(advisor);
	}

	// 从beanFactory中获取目标类对应的Bean，并包装为一个TargetSource对象
	private TargetSource freshTargetSource() {
		if (this.targetName == null) {
			if (logger.isTraceEnabled()) {
				logger.trace("Not refreshing target: Bean name not specified in 'interceptorNames'.");
			}
			return this.targetSource;
		}
		else {
			if (this.beanFactory == null) {
				throw new IllegalStateException("No BeanFactory available anymore (probably due to serialization) " +
						"- cannot resolve target with name '" + this.targetName + "'");
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Refreshing target with name '" + this.targetName + "'");
			}
			Object target = this.beanFactory.getBean(this.targetName);
			return (target instanceof TargetSource ? (TargetSource) target : new SingletonTargetSource(target));
		}
	}

	// 将Advice包装为一个Advisor
	private Advisor namedBeanToAdvisor(Object next) {
		try {
			return this.advisorAdapterRegistry.wrap(next);
		}
		catch (UnknownAdviceTypeException ex) {
			// We expected this to be an Advisor or Advice, but it wasn't. This is a configuration error.
			throw new AopConfigException("Unknown advisor type " + next.getClass() +
					"; Can only include Advisor or Advice type beans in interceptorNames chain except for last entry," +
					"which may also be target or TargetSource", ex);
		}
	}

	// Blow away and recache singleton on an advice change.
	@Override
	protected void adviceChanged() {
		super.adviceChanged();
		if (this.singleton) {
			logger.debug("Advice has changed; recaching singleton instance");
			synchronized (this) {
				this.singletonInstance = null;
			}
		}
	}

	// Serialization support
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization; just initialize state after deserialization.
		ois.defaultReadObject();

		// Initialize transient fields.
		this.proxyClassLoader = ClassUtils.getDefaultClassLoader();
	}

	// 如果配置的增强bean是一个原型bean，则会将这个增强包装为一个PrototypePlaceholderAdvisor，并放到增强链中
	private static class PrototypePlaceholderAdvisor implements Advisor, Serializable {

		private final String beanName;
		private final String message;

		public PrototypePlaceholderAdvisor(String beanName) {
			this.beanName = beanName;
			this.message = "Placeholder for prototype Advisor/Advice with bean name '" + beanName + "'";
		}

		public String getBeanName() {
			return beanName;
		}
		public Advice getAdvice() {
			throw new UnsupportedOperationException("Cannot invoke methods: " + this.message);
		}
		public boolean isPerInstance() {
			throw new UnsupportedOperationException("Cannot invoke methods: " + this.message);
		}

		@Override
		public String toString() {
			return this.message;
		}
	}









	// -------------- getter and setter ... ----------------------------------------------------

	// 设置拦截器的beanName，拦截器用来将增强逻辑织入到目标中
	public void setInterceptorNames(String[] interceptorNames) {
		this.interceptorNames = interceptorNames;
	}
	/**
	 * Set the name of the target bean. This is an alternative to specifying
	 * the target name at the end of the "interceptorNames" array.
	 * <p>You can also specify a target object or a TargetSource object
	 * directly, via the "target"/"targetSource" property, respectively.
	 * @see #setInterceptorNames(String[])
	 * @see #setTarget(Object)
	 * @see #setTargetSource(org.springframework.aop.TargetSource)
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public void setAutodetectInterfaces(boolean autodetectInterfaces) {
		this.autodetectInterfaces = autodetectInterfaces;
	}
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}
	public boolean isSingleton() {
		return this.singleton;
	}
	public Class<?> getObjectType() {
		synchronized (this) {
			if (this.singletonInstance != null) {
				return this.singletonInstance.getClass();
			}
		}
		Class[] ifcs = getProxiedInterfaces();
		if (ifcs.length == 1) {
			return ifcs[0];
		}
		else if (ifcs.length > 1) {
			return createCompositeInterface(ifcs);
		}
		else if (this.targetName != null && this.beanFactory != null) {
			return this.beanFactory.getType(this.targetName);
		}
		else {
			return getTargetClass();
		}
	}
	@Override
	public void setFrozen(boolean frozen) {
		this.freezeProxy = frozen;
	}
	/**
	 * Specify the AdvisorAdapterRegistry to use.
	 * Default is the global AdvisorAdapterRegistry.
	 * @see org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry
	 */
	public void setAdvisorAdapterRegistry(AdvisorAdapterRegistry advisorAdapterRegistry) {
		this.advisorAdapterRegistry = advisorAdapterRegistry;
	}
	/**
	 * Set the ClassLoader to generate the proxy class in.
	 * <p>Default is the bean ClassLoader, i.e. the ClassLoader used by the
	 * containing BeanFactory for loading all bean classes. This can be
	 * overridden here for specific proxies.
	 */
	public void setProxyClassLoader(ClassLoader classLoader) {
		this.proxyClassLoader = classLoader;
		this.classLoaderConfigured = (classLoader != null);
	}
	public void setBeanClassLoader(ClassLoader classLoader) {
		if (!this.classLoaderConfigured) {
			this.proxyClassLoader = classLoader;
		}
	}
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		checkInterceptorNames();
	}





}
