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

// Spring AOP中的Joinpoint（连接点）可以有许多种类型，如果构造方法调用、字段的设置及获取、方法调用、方法执行等。但是，在
// Spring AOP 中，仅支持方法级别的Joinpoint。更确切地说，只支持方法执行类型的Joinpoint。虽然Spring AOP仅提供方法拦截，但
// 是在实际的开发过程中，这已经可以满足80%的开发需求了。所以，我们不用过于担心Spring AOP的能力。

//连接点由两个信息确定：第一是用方法表示的程序执行点；第二是用相对点表示的方位。例如在Test.foo()方法执行前的连接点，执行
// 点就是Test.foo()，方位是该方法执行前的位置。Spring使用切点提供执行点信息，而方位信息由增强提供。

// 切点通过org.springframework.aop.Pointcut接口进行描述，它是指用来定位到某个方法上的信息。（如果要定位到具体连接点，还
// 需提供方位信息，方位信息保存由增强提供org.aopalliance.aop.Advice，Spring中所有的增强都继承自aopalliance.jar包中的Advice）

// ClassFilter和MethodMatcher分别用于匹配将被执行织入操作的对象以及相应的方法。
// 之所以将类型匹配和方法匹配分开定义，是因为可以重用不同级别的匹配定义，并且可以在不同的界别或者相同的级别上进行组合操作，
// 或者强制让某个子类只覆写相应的方法定义等。
public interface Pointcut {

	// 如果Pointcut类型为TruePointcut，默认会对系统中的所有对象，以及对象上所有被支持的切点进行匹配。
	Pointcut TRUE = TruePointcut.INSTANCE;

	// ClassFilter 是用来过滤目标对象，只有匹配了才能给目标对象织入增强逻辑
	ClassFilter getClassFilter();

	// 用来过滤为目标方法，只有匹配了的方法才能织入增强逻辑
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
