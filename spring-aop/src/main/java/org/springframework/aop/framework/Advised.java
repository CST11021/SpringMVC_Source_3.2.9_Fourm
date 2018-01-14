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

package org.springframework.aop.framework;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetClassAware;
import org.springframework.aop.TargetSource;

/**
 * Interface to be implemented by classes that hold the configuration of a factory of AOP proxies.
 * This configuration includes the Interceptors and other advice, and Advisors, and the proxied interfaces.
 *
 * <p>Any AOP proxy obtained from Spring can be cast to this interface to
 * allow manipulation of its AOP advice.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see org.springframework.aop.framework.AdvisedSupport
 */
// 该接口是由具有AOP代理工厂的配置类实现的。该配置包括Interceptors和advice、Advisors和代理接口。
public interface Advised extends TargetClassAware {

	// 返回代理配置是否被冻结，在这种情况下，增强将不再有任何改变
	boolean isFrozen();
	// 是否代理整个目标类
	boolean isProxyTargetClass();
	// 返回要代理的所有接口
	Class<?>[] getProxiedInterfaces();
	// 确定给定的class是否为被代理接口的一个对象或子类
	boolean isInterfaceProxied(Class<?> intf);

	// 设置这个要代理的目标类，只有在配置没有冻结时才会起作用。
	void setTargetSource(TargetSource targetSource);
	TargetSource getTargetSource();

	/**
	expose-proxy：有时候目标对象内部的自动调用将无法实施切面中的增强，如下示例：
	public interface AService{
		public void a();
		public void b();
	}
	@Servcie()
	public class AServiceImpl1 implements AService{
		@Transactional(propagation = Propagation.REQUIRED)
		public void a(){
			this.b();
		}
		@Transactional(propagation = Propagation.REQUIRES_NEW)
		public void b(){

		}
	}

		此处的this指向目标对象，因此调用this.b()将不会执行b事务切面，即不会执行事务增强，因此b方法的事务定义
	“@Transactional(propagation = Propagation.REQUIRES_NEW)”将不会实施，为了解决这个问题，我们可以这样做：
		<aop:aspectj-autoproxy expose-proxy="true"/>
	然后将以上代码中的“this.b();”修改为“((AService)AopContext.currentProxy()).b();”即可。
	通过以上的修改便可以完成对a和b方法的同时增强。
	 */
	void setExposeProxy(boolean exposeProxy);
	boolean isExposeProxy();


	// 设置这个代理配置是否预先过滤，以便它只包含适用的advisors(匹配这个代理的目标类)，默认设置是“false”。
	// 如果advisor已经预先过滤了，那就把它设为“true”，这意味着在构建代理调用的实际advisor链时，可以跳过ClassFilter检查
	void setPreFiltered(boolean preFiltered);
	boolean isPreFiltered();

	// 返回用于此代理配置的Advisor，Advisor封装了Advice和Pointcut信息
	Advisor[] getAdvisors();
	// 添加此代理的advisor
	void addAdvisor(Advisor advisor) throws AopConfigException;
	void addAdvisor(int pos, Advisor advisor) throws AopConfigException;
	boolean removeAdvisor(Advisor advisor);
	void removeAdvisor(int index) throws AopConfigException;
	// 返回给定advisor的索引(从0开始)，如果没有这样的advisor应用于此代理，则返回-1。
	int indexOf(Advisor advisor);
	// b替换a
	boolean replaceAdvisor(Advisor a, Advisor b) throws AopConfigException;

	// 添加/移除增强
	void addAdvice(Advice advice) throws AopConfigException;
	void addAdvice(int pos, Advice advice) throws AopConfigException;
	boolean removeAdvice(Advice advice);
	int indexOf(Advice advice);

	// 返回代理配置的描述信息
	String toProxyConfigString();

}
