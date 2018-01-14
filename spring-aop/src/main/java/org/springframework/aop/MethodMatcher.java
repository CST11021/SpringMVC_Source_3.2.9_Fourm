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

import java.lang.reflect.Method;

/**
 * Part of a {@link Pointcut}: Checks whether the target method is eligible for advice.
 *
 * <p>A MethodMatcher may be evaluated <b>statically</b> or at <b>runtime</b> (dynamically).
 * Static matching involves method and (possibly) method attributes. Dynamic matching
 * also makes arguments for a particular call available, and any effects of running
 * previous advice applying to the joinpoint.
 *
 * <p>If an implementation returns {@code false} from its {@link #isRuntime()}
 * method, evaluation can be performed statically, and the result will be the same
 * for all invocations of this method, whatever their arguments. This means that
 * if the {@link #isRuntime()} method returns {@code false}, the 3-arg
 * {@link #matches(java.lang.reflect.Method, Class, Object[])} method will never be invoked.
 *
 * <p>If an implementation returns {@code true} from its 2-arg
 * {@link #matches(java.lang.reflect.Method, Class)} method and its {@link #isRuntime()} method
 * returns {@code true}, the 3-arg {@link #matches(java.lang.reflect.Method, Class, Object[])}
 * method will be invoked <i>immediately before each potential execution of the related advice</i>,
 * to decide whether the advice should run. All previous advice, such as earlier interceptors
 * in an interceptor chain, will have run, so any state changes they have produced in
 * parameters or ThreadLocal state will be available at the time of evaluation.
 *
 * @author Rod Johnson
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
/*
	相对于 ClassFilter 的简单定义 MethodMatcher则要复杂得多，毕竟Spring主要支持的就是方法级别的拦截。

	MethodMatcher 定义了两个matches方法，而这两个方法的分界线就是isRuntime()方法。
在对对象具体方法进行拦截的时候可以忽略每次方法执行的时候调用者传入的参数，也可以每次都检查这些方法调用的参数以强化拦截条件。
假设对以下方法进行拦截：

	public boolean login(String username,Sring password);

	如果只想在login方法之前插入计数功能，那么login方法的参数对于Joinpoint捕捉就是可以忽略的。而如果想在用户登录的时候对某个
用户做单独处理，如不让其登录或者给予特殊权限那么这个方法的参数就是在匹配Joinpoint的时候必须要考虑的。

	(1) 在前一种情况下isRuntime()返回false表示不会考虑具体Joinpoint的方法参数这种类型的MethodMatcher称之为StaticMethodMatcher。
因为不用每次都检查参数那么对于同样类型的方法匹配结果就可以在框架内部缓存以提高性能。isRuntime方法返回false表明当前的MethodMatcher
为StaticMethodMatcher的时候只有 boolean matches(Method method, Class targetClass);方法将被执行它的匹配结果将会成为其所属的Pointcut主要依据。

	(2) 当isRuntime()方法返回true时表明该MethodMatcher将会每次都对方法调用的参数进行匹配检查这种类型的MethodMatcher称之为DynamicMethodMatcher。
因为每次都要对方法参数进行检查无法对匹配的结果进行缓存所以匹配效率相对于StaticMethodMatcher来说要差。而且大部分情况下StaticMethodMatcher
已经可以满足需要最好避免使用DynamicMethodMatcher类型。如果一个MethodMatcher为DynamicMethodMatcher isRuntime()返回true，并且当方法
boolean matches(Method method, Class targetClass);也返回true的时候三个参数的matches方法将被执行以进一步检查匹配条件。如果方法
boolean matches(Method method,Class targetClass);返回false那么不管这个MethodMatcher是StaticMethodMatcher还是DynamicMethodMatcher
该结果已经是最终的匹配结果——你可以猜得到三个参数的matches方法那铁定是执行不了了。 在MethodMatcher类型的基础上Pointcut可以分为
两类即StaticMethodMatcherPointcut和DynamicMethodMatcherPointcut。因为StaticMethodMatcherPointcut具有明显的性能优势所以Spring为其提供了更多支持。

 */
// MethodMatcher 的作用是用来过滤增强可以织入到目标类的那些方法中
public interface MethodMatcher {

	// 与所有方法匹配的规范实例
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

	// 该方法返回TRUE时，可以对方法入参进行动态匹配，是否为指定的方法织入增强逻辑，如果返回false则不需要判断方法的入参
	boolean isRuntime();
	boolean matches(Method method, Class<?> targetClass);
	boolean matches(Method method, Class<?> targetClass, Object[] args);

}
