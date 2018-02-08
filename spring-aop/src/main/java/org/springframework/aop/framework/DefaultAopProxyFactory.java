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

import org.springframework.aop.SpringProxy;

/**
 * Default {@link AopProxyFactory} implementation,
 * creating either a CGLIB proxy or a JDK dynamic proxy.
 *
 * <p>Creates a CGLIB proxy if one the following is true
 * for a given {@link AdvisedSupport} instance:
 * <ul>
 * <li>the "optimize" flag is set
 * <li>the "proxyTargetClass" flag is set
 * <li>no proxy interfaces have been specified
 * </ul>
 *
 * <p>Note that the CGLIB library classes have to be present on
 * the class path if an actual CGLIB proxy needs to be created.
 *
 * <p>In general, specify "proxyTargetClass" to enforce a CGLIB proxy,
 * or specify one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
@SuppressWarnings("serial")
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {


	/**
	对于Spring的代理中JDKProxy的实现和CGLIBProxy的实现。Spring是如果选取的呢？从if中的判断条件可以看到3个方面的影响设Spring的判断。
	1、optimize:用来控制通过CGLIB创建的代理是否使用激进的优化策略。除非完全了解AOP代理如果处理优化，否则不推荐用户使用这个设置。目前这个属性仅用于CGLIB代理，对于JDK动态代理（缺省代理）无效。
	2、proxyTargetClass：这个属性为true时，目标类本身被代理而不是目标类的接口。如果这个属性被设置为true，CGLIB代理将被创建，设置方式：<aop:aspectj-autoproxy proxy-target-class="true"/>。
	3、hasNoUserSuppliedProxyInterfaces:是否存在代理接口。

	下面是对JDK与Cglib方式的总结。
	1、如果目标对象实现了接口，默认情况下回采用JDK的动态代理实现AOP。
	2、如果目标对象实现了接口，可以强制使用CGLIB实现AOP。
	3、如果目标对象没有实现接口，必须采用CGLIB库，Spring会自动在JDK动态代理和CGLIB之间转化。

	如何强制使用CGLIB实现AOP？
	1、添加CGLIB库，Spring_HOME/cglib/*.jar
	2、在Spring配置文件加入<aop:aspectj-autoproxy proxy-target-class="true"/>。

	JDK动态代理CGLIB字节码生成的区别？
	1、JDK动态代理只能对实现了接口的类生成代理，而不能针对类。
	2、CGLIB是针对类实现代理，主要是对指定的类生成一个子类，覆盖其中的方法，因为是继承，所以该类或方法最好不要声明成final。

	 */
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		// 如果：optimize、proxyTargetClass或hasNoUserSuppliedProxyInterfaces()为true，将使用CGLIB代理
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			// 如果目标类是接口的话还是会使用JDK代理
			if (targetClass.isInterface()) {
				return new JdkDynamicAopProxy(config);
			}
			return CglibProxyFactory.createCglibProxy(config);
		}
		else {
			// 使用JDK代理
			return new JdkDynamicAopProxy(config);
		}
	}

	// 如果代理的不是接口，或者代理的接口是SpringProxy接口，则返回true
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class[] interfaces = config.getProxiedInterfaces();
		return (interfaces.length == 0 || (interfaces.length == 1 && SpringProxy.class.equals(interfaces[0])));
	}

	// 在实际创建CGLIB代理时，内部工厂类仅用于引入CGLIB依赖项
	private static class CglibProxyFactory {
		public static AopProxy createCglibProxy(AdvisedSupport advisedSupport) {
			return new CglibAopProxy(advisedSupport);
		}
	}

}
