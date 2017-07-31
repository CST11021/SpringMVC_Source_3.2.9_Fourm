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

package org.springframework.aop.framework;

import java.io.Serializable;

import org.springframework.util.Assert;

/**
 * Convenience superclass for configuration used in creating proxies, to ensure that all proxy creators have consistent properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
// 用于创建代理的配置的方便超类，以确保所有的代理创建者都具有一致的属性。
public class ProxyConfig implements Serializable {
	//** use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = -8409359707199703185L;


	private boolean proxyTargetClass = false;
	private boolean optimize = false;
	boolean opaque = false;
	boolean exposeProxy = false;
	private boolean frozen = false;


	// 设置是否直接代理目标类，而不是直接代理特定的接口，默认false
	// 如果这个目标类是一个接口，那么将为给定的接口创建一个JDK代理，否则将为给定的类创建一个CGLIB代理。
	// 对应配置中的 <aop:aspectj-autoproxy proxy-target-class="true"/>
	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.proxyTargetClass = proxyTargetClass;
	}
	public boolean isProxyTargetClass() {
		return this.proxyTargetClass;
	}


	// 设置代理是否启动优化
	public void setOptimize(boolean optimize) {
		this.optimize = optimize;
	}
	public boolean isOptimize() {
		return this.optimize;
	}


	// 设置由该配置创建的代理是否应该被禁止将其转换为Advised来查询代理状态
	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}
	public boolean isOpaque() {
		return this.opaque;
	}


	// 设置该代理是否应该由AOP框架公开，作为一个ThreadLocal，通过AopContext类进行检索
	public void setExposeProxy(boolean exposeProxy) {
		this.exposeProxy = exposeProxy;
	}
	public boolean isExposeProxy() {
		return this.exposeProxy;
	}

	// 设置这个配置是否应该被冻结。
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
	public boolean isFrozen() {
		return this.frozen;
	}


	// 复制其他的ProxyConfig对象
	public void copyFrom(ProxyConfig other) {
		Assert.notNull(other, "Other ProxyConfig object must not be null");
		this.proxyTargetClass = other.proxyTargetClass;
		this.optimize = other.optimize;
		this.exposeProxy = other.exposeProxy;
		this.frozen = other.frozen;
		this.opaque = other.opaque;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("proxyTargetClass=").append(this.proxyTargetClass).append("; ");
		sb.append("optimize=").append(this.optimize).append("; ");
		sb.append("opaque=").append(this.opaque).append("; ");
		sb.append("exposeProxy=").append(this.exposeProxy).append("; ");
		sb.append("frozen=").append(this.frozen);
		return sb.toString();
	}

}
