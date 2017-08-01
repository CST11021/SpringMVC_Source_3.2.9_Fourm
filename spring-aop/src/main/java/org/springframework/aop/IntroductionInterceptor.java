/*
 * Copyright 2002-2007 the original author or authors.
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

import org.aopalliance.intercept.MethodInterceptor;

/**
 * Subinterface of AOP Alliance MethodInterceptor that allows additional interfaces
 * to be implemented by the interceptor, and available via a proxy using that
 * interceptor. This is a fundamental AOP concept called <b>introduction</b>.
 *
 * <p>Introductions are often <b>mixins</b>, enabling the building of composite
 * objects that can achieve many of the goals of multiple inheritance in Java.
 *
 * @author Rod Johnson
 * @see DynamicIntroductionAdvice
 */
// 引介增强接口：
// 引介是一种特殊的增强，它为类添加一些属性和方法。这样，即使一个业务类原本没有实现某个接口，通过AOP的引介功能，
// 我们可以动态地为该业务类添加接口的实现逻辑，让业务类成为这个接口的实现类。
// Introduction可以在不改动目标类定义的情况下，为目标类添加新的属性以及行为。
// 在Spring中，为目标对象添加新的属性和行为必须声明相应的接口以及相应的实现。
// 这样，在通过特定的拦截器将新的接口定义以及实现类中的逻辑附加到目标对象之上。
// 之后，代理对象就拥有了新的状态和行为。这个特定的拦截器就是IntroductionInterceptor
public interface IntroductionInterceptor extends MethodInterceptor, DynamicIntroductionAdvice {

}
