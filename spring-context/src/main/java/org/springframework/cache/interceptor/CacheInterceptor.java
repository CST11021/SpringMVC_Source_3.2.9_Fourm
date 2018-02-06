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

package org.springframework.cache.interceptor;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * AOP Alliance MethodInterceptor for declarative cache
 * management using the common Spring caching infrastructure
 * ({@link org.springframework.cache.Cache}).
 *
 * <p>Derives from the {@link CacheAspectSupport} class which
 * contains the integration with Spring's underlying caching API.
 * CacheInterceptor simply calls the relevant superclass methods
 * in the correct order.
 *
 * <p>CacheInterceptors are thread-safe.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
// 当一个方法上配置了Cacheable之类的注解后，这个方法被调用时，就会被一个叫CacheInterceptor的拦截器拦截，进入该类的invoke()
// 方法中，如果当前context已经初始化完成，该方法紧接着会调用execute()。execute()方法中会读取原来被调用业务方法上的注解信
// 息，通过这些信息进行相应的缓存操作，再跟据操作的结果决定是否调用原方法中的业务逻辑。这就是spring通过注解操作缓存的总
// 体流程。
@SuppressWarnings("serial")
public class CacheInterceptor extends CacheAspectSupport implements MethodInterceptor, Serializable {

	public Object invoke(final MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();

		Invoker aopAllianceInvoker = new Invoker() {
			public Object invoke() {
				try {
					return invocation.proceed();
				} catch (Throwable ex) {
					throw new ThrowableWrapper(ex);
				}
			}
		};

		try {
			//  CacheInterceptor在执行execute()的过程中会调用事先注入的CacheResolver实例的resolveCaches()方法解析业务方
			// 法中需要操作的缓存Cache列表（resolveCaches()方法内部实现是通过调用此CacheResolver实例中的cacheManager属性
			// 的getCache()方法获取Cache）。获取到需要操作的Cache列表后，遍历这个列表，然后都过调用doGet(Cache cache)或
			// doPut(Cachecache)方法进行缓存操作。
			// 总体上说，CacheInterceptor的execute()中对缓存的操作就是通过事先注一个CacheResolver和CacheManager实例，然
			// 后通过调用这个CacheResolver实例的resolveCaches()获得需要操作的Cache列表，再遍历列表，将每个Cache实例作为
			// 参数传入doGet()或doPut()来实现缓存读取。当然，还需要一些Key之类的参数，这个是由keyGenerator自动生成的。
			return execute(aopAllianceInvoker, invocation.getThis(), method, invocation.getArguments());
		} catch (ThrowableWrapper th) {
			throw th.original;
		}
	}

	private static class ThrowableWrapper extends RuntimeException {
		private final Throwable original;

		ThrowableWrapper(Throwable original) {
			this.original = original;
		}
	}
}
