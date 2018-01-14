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

package org.springframework.aop.framework.adapter;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.Advisor;

/**
 * Interface allowing extension to the Spring AOP framework to allow
 * handling of new Advisors and Advice types.
 *
 * <p>Implementing objects can create AOP Alliance Interceptors from
 * custom advice types, enabling these advice types to be used
 * in the Spring AOP framework, which uses interception under the covers.
 *
 * <p>There is no need for most Spring users to implement this interface;
 * do so only if you need to introduce more Advisor or Advice types to Spring.
 *
 * @author Rod Johnson
 */
// 连接点由两个信息确定：
// 第一是用方法表示的程序执行点；
// 第二是用相对点表示的方位。
// 例如在Test.foo()方法执行前的连接点，执行点就是Test.foo()，方位是该方法执行前的位置。Spring使用切点提供执行点信息，而方位信息由增强提供。
// Pointcut接口是指用来定位到某个方法上的信息，如果要定位到具体连接点，还需提供方位信息，方位信息由Advice提供
public interface AdvisorAdapter {

	// 判断这个Advisor是否支持Advice，Advisor封装了Advice和Pointcut对象
	boolean supportsAdvice(Advice advice);

	// 获取advisor的增强对象，并封装为一个拦截器对象返回
	MethodInterceptor getInterceptor(Advisor advisor);

}
