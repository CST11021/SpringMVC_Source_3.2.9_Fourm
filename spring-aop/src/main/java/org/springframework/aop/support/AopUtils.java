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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.aop.Advisor;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.TargetClassAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * Utility methods for AOP support code.
 * Mainly for internal use within Spring's AOP support.
 *
 * <p>See {@link org.springframework.aop.framework.AopProxyUtils} for a
 * collection of framework-specific AOP utility methods which depend
 * on internals of Spring's AOP framework implementation.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see org.springframework.aop.framework.AopProxyUtils
 */
public abstract class AopUtils {

	// 判断 object 这个对象是否为JDK 动态代理 或是 CGLIB代理
	public static boolean isAopProxy(Object object) {
		return (object instanceof SpringProxy &&
				(Proxy.isProxyClass(object.getClass()) || ClassUtils.isCglibProxyClass(object.getClass())));
	}

	// 判断 object 这个对象是否为 JDK 动态代理
	public static boolean isJdkDynamicProxy(Object object) {
		return (object instanceof SpringProxy && Proxy.isProxyClass(object.getClass()));
	}

	// 判断 object 这个对象是否为 CGLIB代理
	public static boolean isCglibProxy(Object object) {
		return (object instanceof SpringProxy && ClassUtils.isCglibProxy(object));
	}

	// 判断 clazz 是否为 CGLIB代理对象生成的class
	@Deprecated
	public static boolean isCglibProxyClass(Class<?> clazz) {
		return ClassUtils.isCglibProxyClass(clazz);
	}

	// 判断这个 className 是否为CGLIB代理生成的，CGLIB代理生成的className一定包含"$$"
	@Deprecated
	public static boolean isCglibProxyClassName(String className) {
		return ClassUtils.isCglibProxyClassName(className);
	}

	// 返回这个 candidate 的目标类，candidate可能是一个AOP代理类
	public static Class<?> getTargetClass(Object candidate) {
		Assert.notNull(candidate, "Candidate object must not be null");
		Class<?> result = null;
		if (candidate instanceof TargetClassAware) {
			result = ((TargetClassAware) candidate).getTargetClass();
		}
		if (result == null) {
			result = (isCglibProxy(candidate) ? candidate.getClass().getSuperclass() : candidate.getClass());
		}
		return result;
	}

	// 判断 method 是否是 equals 方法
	public static boolean isEqualsMethod(Method method) {
		return ReflectionUtils.isEqualsMethod(method);
	}
	// 判断 method 是否是 hashcode 方法
	public static boolean isHashCodeMethod(Method method) {
		return ReflectionUtils.isHashCodeMethod(method);
	}
	// 判断 method 是否是 toString 方法
	public static boolean isToStringMethod(Method method) {
		return ReflectionUtils.isToStringMethod(method);
	}
	// 判断 method 是否是 finalize 方法
	public static boolean isFinalizeMethod(Method method) {
		return (method != null && method.getName().equals("finalize") &&
				method.getParameterTypes().length == 0);
	}

	// 返回最具体的方法
	public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
		Method resolvedMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		// 如果我们处理的是泛型参数的方法，找到原始的方法
		return BridgeMethodResolver.findBridgedMethod(resolvedMethod);
	}

	// 判断能否在指定的目标类上应用指定的切点
	public static boolean canApply(Pointcut pc, Class<?> targetClass) {
		return canApply(pc, targetClass, false);
	}
	// hasIntroductions表示：这个bean的增强链是否有包含任何的引介增强
	public static boolean canApply(Pointcut pc, Class<?> targetClass, boolean hasIntroductions) {
		Assert.notNull(pc, "Pointcut must not be null");
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}

		MethodMatcher methodMatcher = pc.getMethodMatcher();
		IntroductionAwareMethodMatcher introductionAwareMethodMatcher = null;
		if (methodMatcher instanceof IntroductionAwareMethodMatcher) {
			introductionAwareMethodMatcher = (IntroductionAwareMethodMatcher) methodMatcher;
		}

		Set<Class> classes = new HashSet<Class>(ClassUtils.getAllInterfacesForClassAsSet(targetClass));
		classes.add(targetClass);
		for (Class<?> clazz : classes) {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if ((introductionAwareMethodMatcher != null &&
						introductionAwareMethodMatcher.matches(method, targetClass, hasIntroductions)) ||
						methodMatcher.matches(method, targetClass)) {
					return true;
				}
			}
		}

		return false;
	}

	// 判断能否在指定的目标类上应用指定的增强
	public static boolean canApply(Advisor advisor, Class<?> targetClass) {
		return canApply(advisor, targetClass, false);
	}
	public static boolean canApply(Advisor advisor, Class<?> targetClass, boolean hasIntroductions) {
		if (advisor instanceof IntroductionAdvisor) {
			return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
		}
		else if (advisor instanceof PointcutAdvisor) {
			PointcutAdvisor pca = (PointcutAdvisor) advisor;
			return canApply(pca.getPointcut(), targetClass, hasIntroductions);
		}
		else {
			// It doesn't have a pointcut so we assume it applies.
			return true;
		}
	}

	// 判断能否在指定的目标类上应用指定的增强链
	public static List<Advisor> findAdvisorsThatCanApply(List<Advisor> candidateAdvisors, Class<?> clazz) {
		if (candidateAdvisors.isEmpty()) {
			return candidateAdvisors;
		}
		List<Advisor> eligibleAdvisors = new LinkedList<Advisor>();
		// 首先处理引介增强
		for (Advisor candidate : candidateAdvisors) {
			if (candidate instanceof IntroductionAdvisor && canApply(candidate, clazz)) {
				eligibleAdvisors.add(candidate);
			}
		}
		boolean hasIntroductions = !eligibleAdvisors.isEmpty();
		for (Advisor candidate : candidateAdvisors) {
			// 引介增强已经处理
			if (candidate instanceof IntroductionAdvisor) {
				// already processed
				continue;
			}
			// 对于普通bean的处理
			if (canApply(candidate, clazz, hasIntroductions)) {
				eligibleAdvisors.add(candidate);
			}
		}
		return eligibleAdvisors;
	}


	// 调用目标类的method方法
	public static Object invokeJoinpointUsingReflection(Object target, Method method, Object[] args) throws Throwable {

		// Use reflection to invoke the method.
		try {
			ReflectionUtils.makeAccessible(method);
			return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception.
			// We must rethrow it. The client won't see the interceptor.
			throw ex.getTargetException();
		}
		catch (IllegalArgumentException ex) {
			throw new AopInvocationException("AOP configuration seems to be invalid: tried calling method [" + method + "] on target [" + target + "]", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AopInvocationException("Could not access method [" + method + "]", ex);
		}
	}

}
