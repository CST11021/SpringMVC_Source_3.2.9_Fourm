/*
 * Copyright 2002-2014 the original author or authors.
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
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;

/**
 * Default object instantiation strategy for use in BeanFactories.
 * Uses CGLIB to generate subclasses dynamically if methods need to be overridden by the container, to implement Method Injection.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
// 使用CGLIB动态生成子类如果方法需要由容器覆盖，实现方法注入
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {


	// 在CGLIB回调数组中对passthrough行为进行索引，在这种情况下，子类不会覆盖原来的类。
	private static final int PASSTHROUGH = 0;

	// 在CGLIB回调数组中的索引，该数组应该被覆盖以提供方法查找。
	private static final int LOOKUP_OVERRIDE = 1;

	// CGLIB回调数组中的索引，该方法应该使用通用的方法-dreplacer功能来覆盖。
	private static final int METHOD_REPLACER = 2;


	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {
		// 必须生成CGLIB子类。
		return new CglibSubclassCreator(beanDefinition, owner).instantiate(null, null);
	}
	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition beanDefinition, String beanName, BeanFactory owner, Constructor<?> ctor, Object[] args) {
		return new CglibSubclassCreator(beanDefinition, owner).instantiate(ctor, args);
	}


	// 出于历史原因而创建的内部类，以避免在Spring版本中出现的外部CGLIB依赖于3.2。
	// 如果用户没有使用replace或者lookup的配置方法，那么直接使用反射的方式，简单快捷，
	// 但是如果使用了这两个特性，在直接使用反射的方式创建实例就不妥了，因为需要将这两个配置提供的功能切入进去，
	// 所以就必须要使用动态代理的方式将包括两个特性所对应的逻辑的拦截增强器设置进去，这样才可以保证在调用方法的时候会被相应的拦截器增强，返回值为包含拦截器的代理实例。
	private static class CglibSubclassCreator {
		private static final Log logger = LogFactory.getLog(CglibSubclassCreator.class);
		private final RootBeanDefinition beanDefinition;
		private final BeanFactory owner;

		public CglibSubclassCreator(RootBeanDefinition beanDefinition, BeanFactory owner) {
			this.beanDefinition = beanDefinition;
			this.owner = owner;
		}

		/**
		 * Create a new instance of a dynamically generated subclasses implementing the
		 * required lookups.
		 * @param ctor constructor to use. If this is {@code null}, use the
		 * no-arg constructor (no parameterization, or Setter Injection)
		 * @param args arguments to use for the constructor.
		 * Ignored if the ctor parameter is {@code null}.
		 * @return new instance of the dynamically generated class
		 */
		public Object instantiate(Constructor<?> ctor, Object[] args) {
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(this.beanDefinition.getBeanClass());
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setCallbackFilter(new CallbackFilterImpl());
			enhancer.setCallbacks(new Callback[] {
					NoOp.INSTANCE,
					new LookupOverrideMethodInterceptor(),
					new ReplaceOverrideMethodInterceptor()
			});

			return (ctor != null ? enhancer.create(ctor.getParameterTypes(), args) : enhancer.create());
		}
		/**
		 * Class providing hashCode and equals methods required by CGLIB to ensure that CGLIB doesn't generate a distinct class per bean.
		 * Identity is based on class and bean definition.
		 */
		private class CglibIdentitySupport {

			/**
			 * Exposed for equals method to allow access to enclosing class field
			 */
			protected RootBeanDefinition getBeanDefinition() {
				return beanDefinition;
			}

			@Override
			public boolean equals(Object other) {
				return (other.getClass().equals(getClass()) &&
						((CglibIdentitySupport) other).getBeanDefinition().equals(beanDefinition));
			}

			@Override
			public int hashCode() {
				return beanDefinition.hashCode();
			}
		}
		/**
		 * CGLIB MethodInterceptor to override methods, replacing them with an implementation that returns a bean looked up in the container.
		 */
		private class LookupOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

			public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
				// Cast is safe, as CallbackFilter filters are used selectively.
				LookupOverride lo = (LookupOverride) beanDefinition.getMethodOverrides().getOverride(method);
				return owner.getBean(lo.getBeanName());
			}
		}
		/**
		 * CGLIB MethodInterceptor to override methods, replacing them with a call to a generic MethodReplacer.
		 */
		private class ReplaceOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

			public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
				ReplaceOverride ro = (ReplaceOverride) beanDefinition.getMethodOverrides().getOverride(method);
				// TODO could cache if a singleton for minor performance optimization
				MethodReplacer mr = (MethodReplacer) owner.getBean(ro.getMethodReplacerBeanName());
				return mr.reimplement(obj, method, args);
			}
		}
		/**
		 * CGLIB object to filter method interception behavior.
		 */
		private class CallbackFilterImpl extends CglibIdentitySupport implements CallbackFilter {

			public int accept(Method method) {
				MethodOverride methodOverride = beanDefinition.getMethodOverrides().getOverride(method);
				if (logger.isTraceEnabled()) {
					logger.trace("Override for '" + method.getName() + "' is [" + methodOverride + "]");
				}
				if (methodOverride == null) {
					return PASSTHROUGH;
				}
				else if (methodOverride instanceof LookupOverride) {
					return LOOKUP_OVERRIDE;
				}
				else if (methodOverride instanceof ReplaceOverride) {
					return METHOD_REPLACER;
				}
				throw new UnsupportedOperationException(
						"Unexpected MethodOverride subclass: " + methodOverride.getClass().getName());
			}
		}
	}

}
