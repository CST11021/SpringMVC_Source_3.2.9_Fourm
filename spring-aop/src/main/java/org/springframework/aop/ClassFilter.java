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

/**
 * Filter that restricts matching of a pointcut or introduction to
 * a given set of target classes.
 *
 * <p>Can be used as part of a {@link Pointcut} or for the entire
 * targeting of an {@link IntroductionAdvisor}.
 *
 * @author Rod Johnson
 * @see Pointcut
 * @see MethodMatcher
 */

/*
		当织入的目标对象的Class类型与Pointcut所规定的类型相符时，matches方法将会返回true，否则，
	返回false，即意味着不会对这个类型的目标对象进行织入操作。比如，如果我们仅希望对系统中Foo类型的类执行织入，则可以如下这样定义ClassFilter:
	public class FooClassFilter{
		public boolean matches(Class clazz){
			return Foo.class.equals(clazz);
		}
	}
		当然，如果类型对我们所捕捉的切点无所谓，那么切点中使用的ClassFilter可以直接使用“ClassFilter TRUE = TrueClassFilter.INSTANCE;”。
	当切点中返回的ClassFilter类型为该类型实例时，切点的匹配将会针对系统所有的目标类以及他们的实例进行。
 */
// 简单的说 ClassFilter 是用来过滤目标对象，只有匹配了才能给目标对象织入增强逻辑
public interface ClassFilter {

	// 一个与所有类匹配的ClassFilter的规范实例
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

	// 判断增强是否适用于给定的目标类
	boolean matches(Class<?> clazz);



}
