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

package org.springframework.cache.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * Advisor driven by a {@link CacheOperationSource}, used to include a
 * cache advice bean for methods that are cacheable.
 *
 * @author Costin Leau
 * @since 3.1
 */
@SuppressWarnings("serial")
public class BeanFactoryCacheOperationSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

	private CacheOperationSource cacheOperationSource;

	// 带有缓存注解的方法都是增强切入点，Spring缓存机制就是通过 CacheOperationSourcePointcut 来判断目标Bean是否需要被代理
	// 的，Spring的自动代理通过 BeanPostProcessor#postProcessAfterInitialization 处理器方法，判断当前Bean是否需要被代理
	// 而当前Bean是否需要被代理取决于 CacheOperationSourcePointcut#matches方法，该方法判断这个当前Bean的指定方法是否有被
	// Spring的缓存注解修改，如果存在一个被缓存注解修改的方法，则说明该Bean需要被代理。
	private final CacheOperationSourcePointcut pointcut = new CacheOperationSourcePointcut() {
		@Override
		protected CacheOperationSource getCacheOperationSource() {
			return cacheOperationSource;
		}
	};
	public void setCacheOperationSource(CacheOperationSource cacheOperationSource) {
		this.cacheOperationSource = cacheOperationSource;
	}
	public void setClassFilter(ClassFilter classFilter) {
		this.pointcut.setClassFilter(classFilter);
	}
	public Pointcut getPointcut() {
		return this.pointcut;
	}

}
