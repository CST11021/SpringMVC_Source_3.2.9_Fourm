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

package org.springframework.aop;

/**
 * Superinterface for advisors that perform one or more AOP <b>introductions</b>.
 *
 * <p>This interface cannot be implemented directly; subinterfaces must
 * provide the advice type implementing the introduction.
 *
 * <p>Introduction is the implementation of additional interfaces
 * (not implemented by a target) via AOP advice.
 *
 * @author Rod Johnson
 * @since 04.04.2003
 * @see IntroductionInterceptor
 */
// 引介增强接口
/*
	IntroductionAdvisor与PointcutAdvisor最本质的区别就是，IntroductionAdvisor只能应用于类级别的拦截，
	只能使用Introduction型的Advice，而不能像PointcutAdvisor那样，可以使用任何类型的Pointcut，以及差不
	多任何类型的Advice。也就是说，IntroductionAdvisor纯粹就是为了Introduction而生的。
 */
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

	// 返回该增强的一个ClassFilter
	ClassFilter getClassFilter();

	// 判断将要织入的接口方法是否有被增强实现
	void validateInterfaces() throws IllegalArgumentException;

}
