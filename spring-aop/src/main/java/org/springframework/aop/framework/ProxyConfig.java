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
 * ProxyConfig其实就是一个普通的JavaBean，它定义了5个boolean类型的属性，分别控制在生成代理对象的时候，应该采取哪些行为措施
 * 该类用于创建代理的配置的方便超类，以确保所有的代理创建者都具有一致的属性。
 *
 * 这是一个顶级的配置超类，AOP 的目标类、增强以及切入点配置在其子类 AdvisedSupport 中配置
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AdvisedSupport
 */
public class ProxyConfig implements Serializable {
	//** use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = -8409359707199703185L;

	// 如果设置为true，则ProxyFactory将会使用CGLIB对目标对象进行代理
	private boolean proxyTargetClass = false;
	// 该属性的主要用于告知代理对象是否需要采取进一步的优化措施，如代理对象生成之后，即使为其添加或者移除了相应的Advice，
	// 代理对象也可以忽略这种变动，该配置设置为TRUE后，会使用CGLIB进行代理
	private boolean optimize = false;
	// 该属性用于控制生成的代理对象是否可以强制转型为Advised，默认值为false，表示任何生成的代理对象都可强制转型为Advised，
	// 我们可以通过Advised查询代理对象的一些状态。
	boolean opaque = false;
	// 设置exposeProxy，可以让Spring AOP框架在生成代理对象时，将当前代理对象绑定到ThreadLocal。
	// 如果目标对象需要访问当前代理对象，可以通过AOPContext.currentProxy()取得。处于性能方面的考虑，该属性默认为false
	boolean exposeProxy = false;
	// 如果frozen设置为true，那么一旦针对代理对象生成的各项信息配置完成，则不容许更改。
	// 比如，如果ProxyFactory的设置完毕，并且fronzen为true，则不能对Advice进行任何变动，这样可以优化代理对象生成的性能。
	private boolean frozen = false;


	// 设置是否使用CGLIB对目标对象进行代理，如果是，则直接代理目标类，而不是直接代理特定的接口
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

	// 设置生成的代理对象是否可以强制转型为Advised
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

	// 设置这个代理配置是否冻结，冻结后配置不可以再修改了
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
