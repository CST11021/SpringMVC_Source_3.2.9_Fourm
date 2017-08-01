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

/**
 * Delegate interface for a configured AOP proxy, allowing for the creation
 * of actual proxy objects.
 *
 * <p>Out-of-the-box implementations are available for JDK dynamic proxies
 * and for CGLIB proxies, as applied by {@link DefaultAopProxyFactory}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DefaultAopProxyFactory
 */

/*
	Spring AOP框架内使用AopProxy对使用的不同代理实现机制进行了适度的抽象，针对不同的代理实现机制提供相应的AOPProxy子类实现。
	目前，Spring AOP框架内提供了针对JDK的动态代理和CGLIB两种机制的AOPProxy实现。
 */
public interface AopProxy {

	// 创建一个新的代理对象
	Object getProxy();
	Object getProxy(ClassLoader classLoader);

}
