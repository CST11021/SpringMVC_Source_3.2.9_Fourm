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

package org.springframework.remoting.support;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;

import org.springframework.util.ClassUtils;

/**
 *
 * 利用反射技术调用目标方法的一个工具类，该类实现了序列化接口，客户端调用的时候，会将调用信息封装为一个RemoteInvocation对象，
 * 通过网络传输，服务端反序列化该对象，进行方法调用
 *
 * Encapsulates a remote invocation, providing core method invocation properties
 * in a serializable fashion. Used for RMI and HTTP-based serialization invokers.
 *
 * <p>This is an SPI class, typically not used directly by applications.
 * Can be subclassed for additional invocation parameters.
 *
 * @author Juergen Hoeller
 * @since 25.02.2004
 * @see RemoteInvocationResult
 * @see RemoteInvocationFactory
 * @see RemoteInvocationExecutor
 * @see org.springframework.remoting.rmi.RmiProxyFactoryBean
 * @see org.springframework.remoting.rmi.RmiServiceExporter
 * @see org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean
 * @see org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 */
public class RemoteInvocation implements Serializable {

	private static final long serialVersionUID = 6876024250231820554L;
	/** 要调用的方法名 */
	private String methodName;
	/** 调用方法的入参类型 */
	private Class[] parameterTypes;
	/** 调用方法的入参 */
	private Object[] arguments;

	private Map<String, Serializable> attributes;


	public RemoteInvocation() {
	}
	public RemoteInvocation(String methodName, Class[] parameterTypes, Object[] arguments) {
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.arguments = arguments;
	}
	public RemoteInvocation(MethodInvocation methodInvocation) {
		this.methodName = methodInvocation.getMethod().getName();
		this.parameterTypes = methodInvocation.getMethod().getParameterTypes();
		this.arguments = methodInvocation.getArguments();
	}


	/**
	 * 利用反射技术调用目标方法
	 * @param targetObject
	 * @return 返回目标方法调用的执行结果
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public Object invoke(Object targetObject) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method = targetObject.getClass().getMethod(this.methodName, this.parameterTypes);
		return method.invoke(targetObject, this.arguments);
	}


	/**
	 * Add an additional invocation attribute. Useful to add additional
	 * invocation context without having to subclass RemoteInvocation.
	 * <p>Attribute keys have to be unique, and no overriding of existing
	 * attributes is allowed.
	 * <p>The implementation avoids to unnecessarily create the attributes
	 * Map, to minimize serialization size.
	 * @param key the attribute key
	 * @param value the attribute value
	 * @throws IllegalStateException if the key is already bound
	 */
	public void addAttribute(String key, Serializable value) throws IllegalStateException {
		if (this.attributes == null) {
			this.attributes = new HashMap<String, Serializable>();
		}
		if (this.attributes.containsKey(key)) {
			throw new IllegalStateException("There is already an attribute with key '" + key + "' bound");
		}
		this.attributes.put(key, value);
	}
	/**
	 * Retrieve the attribute for the given key, if any.
	 * <p>The implementation avoids to unnecessarily create the attributes
	 * Map, to minimize serialization size.
	 * @param key the attribute key
	 * @return the attribute value, or {@code null} if not defined
	 */
	public Serializable getAttribute(String key) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}




	// getter and setter ...
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodName() {
		return this.methodName;
	}
	public void setParameterTypes(Class[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}
	public Class[] getParameterTypes() {
		return this.parameterTypes;
	}
	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}
	public Object[] getArguments() {
		return this.arguments;
	}
	public void setAttributes(Map<String, Serializable> attributes) {
		this.attributes = attributes;
	}
	public Map<String, Serializable> getAttributes() {
		return this.attributes;
	}

	@Override
	public String toString() {
		return "RemoteInvocation: method name '" + this.methodName + "'; parameter types " +
				ClassUtils.classNamesToString(this.parameterTypes);
	}

}
