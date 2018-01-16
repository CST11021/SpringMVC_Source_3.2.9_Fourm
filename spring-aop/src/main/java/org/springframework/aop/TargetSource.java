/*<
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

package org.springframework.aop;

/**
 * A {@code TargetSource} is used to obtain the current "target" of
 * an AOP invocation, which will be invoked via reflection if no around
 * advice chooses to end the interceptor chain itself.
 *
 * <p>If a {@code TargetSource} is "static", it will always return
 * the same target, allowing optimizations in the AOP framework. Dynamic
 * target sources can support pooling, hot swapping, etc.
 *
 * <p>Application developers don't usually need to work with
 * {@code TargetSources} directly: this is an AOP framework interface.
 *
 * @author Rod Johnson
 */


//通常，在使用ProxyFactory的时候，我们都是通过setTarget()方法指定具体的目标对象。使用ProxyFactoryBean也是如此，或者
//ProxyFactoryBean还可以通过setTargetName()指定目标对象在IOC容器中的beanName。但除此之外，还可以通过setTargetSource()来指
//定目标对象。TargetSource的作用就好像是为目标对象在外面加了一个壳，或者说，它就像目标对象的容器。当每个针对目标对象的方法
//调用经历层层拦截而到达调用链的终点的时候，就该调用目标对象上定义的方法了。但这时，Spring AOP做了点手脚，它不是直接调用这
//个目标对象上的方法，而是通过“插足于”调用链于实例目标对象之间的某个TargetSource来取得具体目标对象，然后在调用从
//TargetSource中取得的目标对象上的相应方法。在通常情况下，无论是通过setTarget()，还是通过setTargetName()等方法设置的目标对
//象，框架内部都会通过一个TargetSource实现类对这个设置的目标对象进行封装，也就是，框架内部会以统一的方式处理调用链终点的目
//标对象。
public interface TargetSource extends TargetClassAware {

	// 返回被代理的目标类类型
	Class<?> getTargetClass();

	// 用于表明调用getTarget()接口是否要返回同一个目标对象实例，SingletonTargetSource的这个方法肯定是返回true，其他的实
	// 现根据情况，通常返回false
	boolean isStatic();

	// 获取包含连接点的目标对象
	Object getTarget() throws Exception;

	// 释放 target
	void releaseTarget(Object target) throws Exception;

}
