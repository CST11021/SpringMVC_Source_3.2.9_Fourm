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

import org.aopalliance.aop.Advice;

/**
 * Base interface holding AOP advice (action to take at a joinpoint) and a filter determining the applicability of the advice (such as a pointcut).
 * <i>This interface is not for use by Spring users, but to allow for commonality in support for different types of advice.</i>
 *
 * <p>Spring AOP is based around <b>around advice</b> delivered via method
 * <b>interception</b>, compliant with the AOP Alliance interception API.
 * The Advisor interface allows support for different types of advice,
 * such as <b>before</b> and <b>after</b> advice, which need not be
 * implemented using interception.
 *
 * @author Rod Johnson
 */
// 支持AOP 增强的接口(采取在连接点上的操作)和一个过滤器来决定增强的适用性(比如切入点)
// 这个接口不是供Spring用户使用的，而是允许为不同类型的增强提供支持。
public interface Advisor {

	// 返回这个切面的增强。可能是环绕增强、前置建议、抛出异常增强等。
	Advice getAdvice();

	/**
	 * Return whether this advice is associated with a particular instance (for example, creating a mixin) or
	 * shared with all instances of the advised class obtained from the same Spring bean factory.
	 * <p><b>Note that this method is not currently used by the framework.</b>
	 * Typical Advisor implementations always return {@code true}.
	 * Use singleton/prototype bean definitions or appropriate programmatic
	 * proxy creation to ensure that Advisors have the correct lifecycle model.
	 * @return whether this advice is associated with a particular target instance
	 */
	// 判断这个增强是否与特定的目标实例相关联
	boolean isPerInstance();

}
