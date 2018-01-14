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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * JDK-based {@link AopProxy} implementation for the Spring AOP framework,
 * based on JDK {@link java.lang.reflect.Proxy dynamic proxies}.
 *
 * <p>Creates a dynamic proxy, implementing the interfaces exposed by
 * the AopProxy. Dynamic proxies <i>cannot</i> be used to proxy methods
 * defined in classes, rather than interfaces.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by an {@link AdvisedSupport} class. This class is internal
 * to Spring's AOP framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class will be thread-safe if the
 * underlying (target) class is thread-safe.
 *
 * <p>Proxies are serializable so long as all Advisors (including Advices
 * and Pointcuts) and the TargetSource are serializable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @see java.lang.reflect.Proxy
 * @see AdvisedSupport
 * @see ProxyFactory
 */
/**
  使用JDK代理示例：

	  public class PerformaceHandler implements InvocationHandler {
		private Object target;
		public PerformaceHandler(Object target){
			this.target = target;
		}
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			PerformanceMonitor.begin(target.getClass().getName()+"."+ method.getName());
			Object obj = method.invoke(target, args);
			PerformanceMonitor.end();
			return obj;
		}
	}

	测试类：
	public class TestForumService {
		public static void main(String[] args) {
			// 业务类正常编码的测试
			 ForumService forumService = new ForumServiceImpl();
			 forumService.removeForum(10);
			 forumService.removeTopic(1012);

			// 使用JDK动态代理
			ForumService target = new ForumServiceImpl();
			PerformaceHandler handler = new PerformaceHandler(target);
			ForumService proxy = (ForumService) Proxy.newProxyInstance(
					target.getClass().getClassLoader(),
					target.getClass().getInterfaces(),
					handler);
			proxy.removeForum(10);
			proxy.removeTopic(1012);
		}
	}



 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

	private static Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);
	private static final long serialVersionUID = 5531744639992436476L;

	// AdvisedSupport 封装了代理的相关配置信息
	private final AdvisedSupport advised;
	// 判断被代理的接口中是否有equals()方法
	private boolean equalsDefined;
	// 判断被代理的接口中是否有hashCode()方法
	private boolean hashCodeDefined;


	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
	}

	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}
	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating JDK dynamic proxy: target source is " + this.advised.getTargetSource());
		}
		// 首先从配置中获取所有要代理的接口
		Class[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);
		// 标记状态：判断被代理的接口中是否有equals()和hashCode()方法
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
		// 我们可以看到，底层使用的是JDK代理的方式，这里传入了一个this参数，它实现了 InvocationHandler 接口，所以，当执
		// 行代理类方法时，如果这个方法是被代理的接口方法，就会自动来调用 this.invoke()这个方法，这个方法将调用目标类的
		// 原始方法
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}
	// 判断被代理的接口中是否有equals()和hashCode()方法
	private void findDefinedEqualsAndHashCodeMethods(Class[] proxiedInterfaces) {
		for (Class proxiedInterface : proxiedInterfaces) {
			Method[] methods = proxiedInterface.getDeclaredMethods();
			for (Method method : methods) {
				if (AopUtils.isEqualsMethod(method)) {
					this.equalsDefined = true;
				}
				if (AopUtils.isHashCodeMethod(method)) {
					this.hashCodeDefined = true;
				}
				if (this.equalsDefined && this.hashCodeDefined) {
					return;
				}
			}
		}
	}
	// 实现JDK动态代理的InvocationHandler接口
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation;
		Object oldProxy = null;
		boolean setProxyContext = false;

		// 获取要带被代理的目标类对象
		TargetSource targetSource = this.advised.targetSource;
		Class targetClass = null;
		Object target = null;

		try {
			// 1、如果代理的接口没有定义equals方法，而当前执行又是equals方法
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// The target does not implement the equals(Object) method itself.
				return equals(args[0]);
			}
			///2、如果代理的接口没有定义hash方法，而当前执行又是hash方法
			if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// The target does not implement the hashCode() method itself.
				return hashCode();
			}

			///3、如果调用的是Advised接口的方法（比如织入的是引介增强）
			// Class类的isAssignableFrom(Class cls)方法：
			// 如果调用这个方法的Class或接口与参数cls 表示的类或接口相同，或者是参数cls表示的类或接口的父类，则返回true。
			// 形象地：自身类.class.isAssignableFrom(自身类或子类.class)返回true
			// 例如：
			// System.out.println(ArrayList.class.isAssignableFrom(Object.class));// false
			// System.out.println(Object.class.isAssignableFrom(ArrayList.class));// true
			if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// Service invocations on ProxyConfig with the proxy config...
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal;
			// 有时候目标对象内部的自我调用将无法实施切面中的增强则需要通过此属性暴露代理
			if (this.advised.exposeProxy) {
				// 将当前的代理对象记录在当前线程的AopContext中
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// May be null. Get as late as possible to minimize the time we "own" the target, in case it comes from a pool.
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}

			// 获取当前方法的拦截器，方法拦截器用户过滤增强是否只可以织入目标方法
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
			if (chain.isEmpty()) {
				// 如果没有发现任何拦截器那么直接调用切点方法
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, args);
			}
			else {
				// 如果当前方法有拦截器，则将拦截器封装为ReflectiveMethodInvocation，以便于调用proceed方法执行拦截器
				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// 执行代理方法：该方法调用完后就相当于执行的代理后的方法，即执行了增强代码也执行了目标方法的代码
				retVal = invocation.proceed();
			}

			// Massage return value if necessary.
			Class<?> returnType = method.getReturnType();
			if (retVal != null && retVal == target && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// Special case: it returned "this" and the return type of the method
				// is type-compatible. Note that we can't help if the target sets
				// a reference to itself in another returned object.
				retVal = proxy;
			} else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException("Null return value from advice does not match primitive return type for: " + method);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource.
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// Restore old proxy.
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}



	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		JdkDynamicAopProxy otherProxy;
		if (other instanceof JdkDynamicAopProxy) {
			otherProxy = (JdkDynamicAopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy)) {
				return false;
			}
			otherProxy = (JdkDynamicAopProxy) ih;
		}
		else {
			// Not a valid comparison...
			return false;
		}

		// If we get here, otherProxy is the other AopProxy.
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}

}
