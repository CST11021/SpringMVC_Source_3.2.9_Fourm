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

// Advisor代表Spring中的Aspect，但是，与正常的Aspect不同，Advisor通常只持有一个Pointcut和一个Advice。而理论上，Aspect定义
// 中可以有多个Pointcut和多个Advice，所以，我们可以认为Advisor是一种特殊的Aspect。为了能够更清楚Advisor的实现结构体系，
// 我们可以将Advisor简单划分为两个分支，一个是PointcutAdvisor，另一个是IntroductionAdvisor（引介增强）

// 支持AOP 增强的接口(采取在连接点上的操作)和一个过滤器来决定增强的适用性(比如切入点)
// 这个接口不是供Spring用户使用的，而是允许为不同类型的增强提供支持。
// Advisor用于把Advice和Pointcut结合起来，我们以一个Advisor的实现（DefaultPointcutAdvisor）为例来了解Advisor的工作原理。
// 在DefaultPointcutAdvisor中，有两个属性，分别是Advice和Pointcut，通过这两个属性可以分别配置增强逻辑和要织入的切点信息
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
