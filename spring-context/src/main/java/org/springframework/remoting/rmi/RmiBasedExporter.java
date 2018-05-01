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

package org.springframework.remoting.rmi;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;

import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;

/**
 * Convenient superclass for RMI-based remote exporters. Provides a facility
 * to automatically wrap a given plain Java service object with an
 * RmiInvocationWrapper, exposing the {@link RmiInvocationHandler} remote interface.
 *
 * <p>Using the RMI invoker mechanism, RMI communication operates at the {@link RmiInvocationHandler}
 * level, sharing a common invoker stub for any number of services. Service interfaces are <i>not</i>
 * required to extend {@code java.rmi.Remote} or declare {@code java.rmi.RemoteException}
 * on all service methods. However, in and out parameters still have to be serializable.
 *
 * @author Juergen Hoeller
 * @since 1.2.5
 * @see RmiServiceExporter
 * @see JndiRmiServiceExporter
 */
public abstract class RmiBasedExporter extends RemoteInvocationBasedExporter {

	/**
	 * 当请求RMI服务时会由注册表Registry实例将请求转向之前注册的处理类去处理，也就是之前封装的RMIInvocationWrapper，然后由
	 * RMIInvocationWrapper中的invoke方法进行处理，那么为什么不是在invoke方法中直接使用service，而是通过代理再次将service封装呢？
	 * 这其中的一个关键点是，在创建代理时添加了一个增强拦截器RemoteInvocationTraceInterceptor，目的是为了对方法调用进行打印跟踪，
	 * 但是如果直接在invoke方法硬编码这些日志，会使代码看起来很不优雅，而且耦合度很高，使用代理的方法就会解决这样的问题，而且会有很高的可扩展性。
	 * @return
	 */
	protected Remote getObjectToExport() {
		// determine remote object
		if (getService() instanceof Remote &&
				(getServiceInterface() == null || Remote.class.isAssignableFrom(getServiceInterface()))) {
			// conventional RMI service
			return (Remote) getService();
		} else {
			// RMI invoker
			if (logger.isDebugEnabled()) {
				logger.debug("RMI service [" + getService() + "] is an RMI invoker");
			}
			return new RmiInvocationWrapper(getProxyForService(), this);
		}
	}

	/**
	 *
	 * @param invocation the remote invocation
	 * @param targetObject 这个目标对象其实是服务接口实现的一个代理
	 * @return
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	@Override
	protected Object invoke(RemoteInvocation invocation, Object targetObject)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

		return super.invoke(invocation, targetObject);
	}

}
