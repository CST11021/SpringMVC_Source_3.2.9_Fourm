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
 * Core Spring pointcut abstraction.
 *
 * <p>A pointcut is composed of a {@link ClassFilter} and a {@link MethodMatcher}.
 * Both these basic terms and a Pointcut itself can be combined to build up combinations
 * (e.g. through {@link org.springframework.aop.support.ComposablePointcut}).
 *
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */

/*
	Spring AOP中的Joinpoint可以有许多种类型，如果构造方法调用、字段的设置及获取、方法调用、方法执行等。
但是，在Spring AOP中，仅支持方法级别的Joinpoint。更确切地说，只支持方法执行类型的Joinpoint。虽然Spring AOP仅提供方法拦截，
但是在实际的开发过程中，这已经可以满足80%的开发需求了。所以，我们不用过于担心Spring AOP的能力。

 */

// Pointcut接口是Spring AOP中所有切点的最顶层抽象，该接口定义了两个方法用来帮助捕捉系统中相应的切入点，
// 并提供了一个TruePointcut实现。如果Pointcut类型为TruePointcut，默认会对系统中的所有对象，以及对象上所有被支持的切点进行匹配。

// ClassFilter和MethodMatcher分别用于匹配将被执行织入操作的对象以及相应的方法。
// 之所以将类型匹配和方法匹配分开定义，是因为可以重用不同级别的匹配定义，并且可以在不同的界别或者相同的级别上进行组合操作，或者强制让某个子类只覆写相应的方法定义等。
public interface Pointcut {

	// 通常匹配的规范切入点实例
	Pointcut TRUE = TruePointcut.INSTANCE;

	// 返回这个切入点的 ClassFilter
	ClassFilter getClassFilter();
	// 返回这个切入点的 MethodMatcher
	MethodMatcher getMethodMatcher();

}

/*
	以下是常用的几种Pointcut实现：
1. NameMatchMethodPointcut

	这是最简单的Pointcut实现属于 StaticMethodMatcherPointcut 的子类可以根据自身指定的一组方法名称与Joinpoint处的方法的方法名称进行匹配比如
	NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
	pointcut.setMappedName("matches");
	// 或者传入多个方法名
	pointcut.setMappedNames(new String[]{"matches","isRuntime"});

	但是 NameMatchMethodPointcut 无法对重载Overload的方法名进行匹配因为它仅对方法名进行匹配不会考虑参数相关信息而且也没有提供可以指定参数匹配信息的途径。
NameMatchMethodPointcut 除了可以指定方法名以对指定的Joinpoint进行匹配还可以使用“*”通配符实现简单的模糊匹配如下所示：

	pointcut.setMappedNames(new String[]{"match*","*matches","mat*es"});

	如果基于“*”通配符的 NameMatchMethodPointcut 依然无法满足对多个特定Joinpoint的匹配需要那么使用正则表达式好了。



2. JdkRegexpMethodPointcut 和 Perl5RegexpMethodPointcut

	StaticMethodMatcherPointcut 的子类中有一个专门提供基于正则表达式的实现分支以抽象类 AbstractRegexpMethodPointcut为统帅。与
NameMatchMethodPointcut相似AbstractRegexpMethodPointcut声明了pattern和patterns属性可以指定一个或者多个正则表达式的匹配模式Pattern。
其下设 JdkRegexpMethodPointcut 和 Perl5RegexpMethodPointcut 两种具体实现。


更多内容请参考《spring揭秘》P147页。。。。。。。。。。。。。。。。


 */
