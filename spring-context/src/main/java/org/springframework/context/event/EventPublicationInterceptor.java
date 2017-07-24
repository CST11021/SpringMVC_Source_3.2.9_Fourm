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

package org.springframework.context.event;

import java.lang.reflect.Constructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

// 事件发布拦截器
public class EventPublicationInterceptor implements MethodInterceptor, ApplicationEventPublisherAware, InitializingBean {

	private Constructor applicationEventClassConstructor;

	private ApplicationEventPublisher applicationEventPublisher;


	/**
	 * Set the application event class to publish.
	 * <p>The event class <b>must</b> have a constructor with a single
	 * {@code Object} argument for the event source. The interceptor
	 * will pass in the invoked object.
	 * @throws IllegalArgumentException if the supplied {@code Class} is
	 * {@code null} or if it is not an {@code ApplicationEvent} subclass or
	 * if it does not expose a constructor that takes a single {@code Object} argument
	 */
	public void setApplicationEventClass(Class applicationEventClass) {
		if (ApplicationEvent.class.equals(applicationEventClass) ||
			!ApplicationEvent.class.isAssignableFrom(applicationEventClass)) {
			throw new IllegalArgumentException("applicationEventClass needs to extend ApplicationEvent");
		}
		try {
			this.applicationEventClassConstructor =
					applicationEventClass.getConstructor(new Class[] {Object.class});
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalArgumentException("applicationEventClass [" +
					applicationEventClass.getName() + "] does not have the required Object constructor: " + ex);
		}
	}

	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void afterPropertiesSet() throws Exception {
		if (this.applicationEventClassConstructor == null) {
			throw new IllegalArgumentException("applicationEventClass is required");
		}
	}

	// MethodInterceptor接口的方法
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Object retVal = invocation.proceed();

		ApplicationEvent event = (ApplicationEvent)
				this.applicationEventClassConstructor.newInstance(new Object[] {invocation.getThis()});
		this.applicationEventPublisher.publishEvent(event);

		return retVal;
	}

}
