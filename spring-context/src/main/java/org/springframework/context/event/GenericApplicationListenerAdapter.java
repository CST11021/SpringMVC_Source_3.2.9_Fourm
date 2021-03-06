/*
 * Copyright 2002-2013 the original author or authors.
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

import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

/**
 * {@link SmartApplicationListener} adapter that determines supported event types
 * through introspecting the generically declared type of the target listener.
 * SmartApplicationListener适配器通过内省目标监听器的通用声明类型来确定受支持的事件类型。
 * @author Juergen Hoeller
 * @since 3.0
 * @see org.springframework.context.ApplicationListener#onApplicationEvent
 */
// 适配器类，通过继承该类，我们可以不用去实现所有接口方法，只需要重写我们需要的方法即可。
public class GenericApplicationListenerAdapter implements SmartApplicationListener {

	private final ApplicationListener delegate;

	public GenericApplicationListenerAdapter(ApplicationListener delegate) {
		Assert.notNull(delegate, "Delegate listener must not be null");
		this.delegate = delegate;
	}

	// 监听器的处理方法
	@SuppressWarnings("unchecked")
	public void onApplicationEvent(ApplicationEvent event) {
		this.delegate.onApplicationEvent(event);
	}

	// 判断指定的事件类型是否可被监听
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		Class<?> typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), ApplicationListener.class);
		if (typeArg == null || typeArg.equals(ApplicationEvent.class)) {
			Class<?> targetClass = AopUtils.getTargetClass(this.delegate);
			if (targetClass != this.delegate.getClass()) {
				typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, ApplicationListener.class);
			}
		}
		return (typeArg == null || typeArg.isAssignableFrom(eventType));
	}
	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	// 返回对应事件的监听器的调用顺序
	public int getOrder() {
		return (this.delegate instanceof Ordered ? ((Ordered) this.delegate).getOrder() : Ordered.LOWEST_PRECEDENCE);
	}

}
