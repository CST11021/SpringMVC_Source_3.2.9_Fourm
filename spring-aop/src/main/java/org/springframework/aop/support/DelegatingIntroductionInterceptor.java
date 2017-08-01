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

package org.springframework.aop.support;

import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.DynamicIntroductionAdvice;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.util.Assert;

/**
 * Convenient implementation of the
 * {@link org.springframework.aop.IntroductionInterceptor} interface.
 *
 * <p>Subclasses merely need to extend this class and implement the interfaces
 * to be introduced themselves. In this case the delegate is the subclass
 * instance itself. Alternatively a separate delegate may implement the
 * interface, and be set via the delegate bean property.
 *
 * <p>Delegates or subclasses may implement any number of interfaces.
 * All interfaces except IntroductionInterceptor are picked up from
 * the subclass or delegate by default.
 *
 * <p>The {@code suppressInterface} method can be used to suppress interfaces
 * implemented by the delegate but which should not be introduced to the owning
 * AOP proxy.
 *
 * <p>An instance of this class is serializable if the delegate is.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.11.2003
 * @see #suppressInterface
 * @see DelegatePerTargetObjectIntroductionInterceptor
 */
@SuppressWarnings("serial")
public class DelegatingIntroductionInterceptor extends IntroductionInfoSupport implements IntroductionInterceptor {

	// 实际实现接口的对象，如果子类实现引介接口，则该对象可能就是 this
	private Object delegate;

	public DelegatingIntroductionInterceptor(Object delegate) {
		init(delegate);
	}
	protected DelegatingIntroductionInterceptor() {
		init(this);
	}


	private void init(Object delegate) {
		Assert.notNull(delegate, "Delegate must not be null");
		this.delegate = delegate;
		implementInterfacesOnObject(delegate);

		// 我们不想暴露 DelegatingIntroductionInterceptor 子类自身实现的IntroductionInterceptor和DynamicIntroductionAdvice接口
		suppressInterface(IntroductionInterceptor.class);
		suppressInterface(DynamicIntroductionAdvice.class);
	}


	/**
	 * Subclasses may need to override this if they want to perform custom behaviour in around advice.
	 * 如果子类想要在增强中执行定制的行为，子类可能需要重写它。
	 * However, subclasses should invoke this method, which handles introduced interfaces and forwarding to the target.
	 * 但是，子类应该调用这个方法，该方法处理引入的接口并将其转发到目标类
	 */
	// 子类可以覆盖该方法进行环绕增强的织入
	public Object invoke(MethodInvocation mi) throws Throwable {
		if (isMethodOnIntroducedInterface(mi)) {
			// 调用 delegate 对象指定的方法
			Object retVal = AopUtils.invokeJoinpointUsingReflection(this.delegate, mi.getMethod(), mi.getArguments());

			// Massage return value if possible: if the delegate returned itself, we really want to return the proxy.
			if (retVal == this.delegate && mi instanceof ProxyMethodInvocation) {
				Object proxy = ((ProxyMethodInvocation) mi).getProxy();
				if (mi.getMethod().getReturnType().isInstance(proxy)) {
					retVal = proxy;
				}
			}
			return retVal;
		}

		return doProceed(mi);
	}

	protected Object doProceed(MethodInvocation mi) throws Throwable {
		// If we get here, just pass the invocation on.
		return mi.proceed();
	}

}
