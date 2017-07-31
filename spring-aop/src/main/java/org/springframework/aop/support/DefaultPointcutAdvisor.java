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

package org.springframework.aop.support;

import java.io.Serializable;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;

/**
 * Convenient Pointcut-driven Advisor implementation.
 *
 * <p>This is the most commonly used Advisor implementation.
 * It can be used with any pointcut and advice type, except for introductions.
 * There is normally no need to subclass this class, or to implement custom Advisors.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setPointcut
 * @see #setAdvice
 */
// 这是最常用的 advisor 实现
@SuppressWarnings("serial")
public class DefaultPointcutAdvisor extends AbstractGenericPointcutAdvisor implements Serializable {

	private Pointcut pointcut = Pointcut.TRUE;

	public DefaultPointcutAdvisor() {}
	public DefaultPointcutAdvisor(Advice advice) {
		this(Pointcut.TRUE, advice);
	}
	public DefaultPointcutAdvisor(Pointcut pointcut, Advice advice) {
		this.pointcut = pointcut;
		setAdvice(advice);
	}


	public void setPointcut(Pointcut pointcut) {
		this.pointcut = (pointcut != null ? pointcut : Pointcut.TRUE);
	}
	public Pointcut getPointcut() {
		return this.pointcut;
	}


	@Override
	public String toString() {
		return getClass().getName() + ": pointcut [" + getPointcut() + "]; advice [" + getAdvice() + "]";
	}

}
