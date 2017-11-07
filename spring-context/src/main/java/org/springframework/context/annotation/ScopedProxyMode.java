/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.context.annotation;

/**
 * Enumerates the various scoped-proxy options.
 *
 * <p>For a fuller discussion of exactly what a scoped-proxy is, see that
 * section of the Spring reference documentation entitled 'Scoped beans as dependencies'.
 *
 * @author Mark Fisher
 * @since 2.5
 * @see ScopeMetadata
 */

// @Scope注解

//@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)这个是说在每次注入的时候回自动创建一个新的bean实例
//@Scope(value=ConfigurableBeanFactory.SCOPE_SINGLETON)单例模式，在整个应用中只能创建一个实例
//@Scope(value=WebApplicationContext.SCOPE_GLOBAL_SESSION)全局session中的一般不常用
//@Scope(value=WebApplicationContext.SCOPE_APPLICATION)在一个web应用中只创建一个实例
//@Scope(value=WebApplicationContext.SCOPE_REQUEST)在一个请求中创建一个实例
//@Scope(value=WebApplicationContext.SCOPE_SESSION)每次创建一个会话中创建一个实例

//里面还有个属性

//		proxyMode=ScopedProxyMode.INTERFACES创建一个JDK代理模式
//		proxyMode=ScopedProxyMode.TARGET_CLASS基于类的代理模式
//		proxyMode=ScopedProxyMode.NO（默认）不进行代理
public enum ScopedProxyMode {

	/**
	 * Default typically equals {@link #NO}, unless a different default
	 * has been configured at the component-scan instruction level.
	 */
	DEFAULT,

	/**
	 * Do not create a scoped proxy.
	 * <p>This proxy-mode is not typically useful when used with a
	 * non-singleton scoped instance, which should favor the use of the
	 * {@link #INTERFACES} or {@link #TARGET_CLASS} proxy-modes instead if it
	 * is to be used as a dependency.
	 */
	NO,

	/**
	 * Create a JDK dynamic proxy implementing <i>all</i> interfaces exposed by
	 * the class of the target object.
	 */
	INTERFACES,

	/**
	 * Create a class-based proxy (requires CGLIB).
	 */
	TARGET_CLASS

}
