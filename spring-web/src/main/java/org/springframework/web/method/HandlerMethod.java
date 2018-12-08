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

package org.springframework.web.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * 表示控制器（即Controller）对应的方法
 *
 * Encapsulates information about a handler method consisting of a {@linkplain #getMethod() method}
 * and a {@linkplain #getBean() bean}. Provides convenient access to method parameters,
 * method return value, method annotations.
 *
 * <p>The class may be created with a bean instance or with a bean name (e.g. lazy-init bean,
 * prototype bean). Use {@link #createWithResolvedBean()} to obtain a {@link HandlerMethod}
 * instance with a bean instance resolved through the associated {@link BeanFactory}.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class HandlerMethod {

	protected final Log logger = LogFactory.getLog(HandlerMethod.class);

	private final BeanFactory beanFactory;

	// 表示该请求对应处理方法的所在控制器Bean
	private final Object bean;
	// 表示该请求对应的处理方法
	private final Method method;
	// 表示该请求对应的处理方法(用于处理泛型方法)
	private final Method bridgedMethod;
	// 表示该请求对应处理方法的入参
	private final MethodParameter[] parameters;


	public HandlerMethod(Object bean, Method method) {
		Assert.notNull(bean, "Bean is required");
		Assert.notNull(method, "Method is required");
		this.bean = bean;
		this.beanFactory = null;
		this.method = method;
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
		this.parameters = initMethodParameters();
	}
	public HandlerMethod(Object bean, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		Assert.notNull(bean, "Bean is required");
		Assert.notNull(methodName, "Method name is required");
		this.bean = bean;
		this.beanFactory = null;
		this.method = bean.getClass().getMethod(methodName, parameterTypes);
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(this.method);
		this.parameters = initMethodParameters();
	}
	public HandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
		Assert.hasText(beanName, "Bean name is required");
		Assert.notNull(beanFactory, "BeanFactory is required");
		Assert.notNull(method, "Method is required");
		Assert.isTrue(beanFactory.containsBean(beanName),
				"BeanFactory [" + beanFactory + "] does not contain bean [" + beanName + "]");
		this.bean = beanName;
		this.beanFactory = beanFactory;
		this.method = method;
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
		this.parameters = initMethodParameters();
	}
	protected HandlerMethod(HandlerMethod handlerMethod) {
		Assert.notNull(handlerMethod, "HandlerMethod is required");
		this.bean = handlerMethod.bean;
		this.beanFactory = handlerMethod.beanFactory;
		this.method = handlerMethod.method;
		this.bridgedMethod = handlerMethod.bridgedMethod;
		this.parameters = handlerMethod.parameters;
	}
	private HandlerMethod(HandlerMethod handlerMethod, Object handler) {
		Assert.notNull(handlerMethod, "HandlerMethod is required");
		Assert.notNull(handler, "Handler object is required");
		this.bean = handler;
		this.beanFactory = handlerMethod.beanFactory;
		this.method = handlerMethod.method;
		this.bridgedMethod = handlerMethod.bridgedMethod;
		this.parameters = handlerMethod.parameters;
	}


	private MethodParameter[] initMethodParameters() {
		int count = this.bridgedMethod.getParameterTypes().length;
		MethodParameter[] result = new MethodParameter[count];
		for (int i = 0; i < count; i++) {
			result[i] = new HandlerMethodParameter(i);
		}
		return result;
	}
	public Class<?> getBeanType() {
		Class<?> clazz = (this.bean instanceof String ?
				this.beanFactory.getType((String) this.bean) : this.bean.getClass());
		return ClassUtils.getUserClass(clazz);
	}
	public MethodParameter getReturnType() {
		return new HandlerMethodParameter(-1);
	}
	public MethodParameter getReturnValueType(Object returnValue) {
		return new ReturnValueMethodParameter(returnValue);
	}
	public boolean isVoid() {
		return Void.TYPE.equals(getReturnType().getParameterType());
	}
	public <A extends Annotation> A getMethodAnnotation(Class<A> annotationType) {
		return AnnotationUtils.findAnnotation(this.method, annotationType);
	}
	/**
	 * If the provided instance contains a bean name rather than an object instance, the bean name is resolved
	 * before a {@link HandlerMethod} is created and returned.
	 */
	public HandlerMethod createWithResolvedBean() {
		Object handler = this.bean;
		if (this.bean instanceof String) {
			String beanName = (String) this.bean;
			handler = this.beanFactory.getBean(beanName);
		}
		return new HandlerMethod(this, handler);
	}


	// getter ...

	public Object getBean() {
		return this.bean;
	}
	public Method getMethod() {
		return this.method;
	}
	protected Method getBridgedMethod() {
		return this.bridgedMethod;
	}
	public MethodParameter[] getMethodParameters() {
		return this.parameters;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof HandlerMethod) {
			HandlerMethod other = (HandlerMethod) obj;
			return (this.bean.equals(other.bean) && this.method.equals(other.method));
		}
		return false;
	}
	@Override
	public int hashCode() {
		return this.bean.hashCode() * 31 + this.method.hashCode();
	}
	@Override
	public String toString() {
		return this.method.toGenericString();
	}


	/**
	 * 表示方法的入参
	 */
	private class HandlerMethodParameter extends MethodParameter {

		public HandlerMethodParameter(int index) {
			super(HandlerMethod.this.bridgedMethod, index);
		}

		@Override
		public Class<?> getDeclaringClass() {
			return HandlerMethod.this.getBeanType();
		}

		@Override
		public <T extends Annotation> T getMethodAnnotation(Class<T> annotationType) {
			return HandlerMethod.this.getMethodAnnotation(annotationType);
		}
	}

	/**
	 * 表示方法的返回值
	 */
	private class ReturnValueMethodParameter extends HandlerMethodParameter {

		private final Object returnValue;

		public ReturnValueMethodParameter(Object returnValue) {
			super(-1);
			this.returnValue = returnValue;
		}

		@Override
		public Class<?> getParameterType() {
			return (this.returnValue != null ? this.returnValue.getClass() : super.getParameterType());
		}
	}

}
