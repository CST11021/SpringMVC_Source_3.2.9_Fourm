/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.cache.interceptor;

import java.lang.reflect.Method;

/**
 * Cache key generator. Used for creating a key based on the given method
 * (used as context) and its parameters.
 *
 * @author Costin Leau
 * @author Chris Beams
 * @since 3.1

键的生成策略：键的生成策略有两种，一种是默认策略，一种是自定义策略。
	1、默认策略
		默认的key生成策略是通过KeyGenerator生成的，其默认策略如下：
		n  如果方法没有参数，则使用0作为key。
		n  如果只有一个参数的话则使用该参数作为key。
		n  如果参数多余一个的话则使用所有参数的hashCode作为key。

		如果我们需要指定自己的默认策略的话，那么我们可以实现自己的KeyGenerator，然后指定我们的Spring Cache使用的KeyGenerator为我们自己定义的KeyGenerator。
		使用基于注解的配置时是通过cache:annotation-driven指定的.
		<cache:annotation-driven key-generator="userKeyGenerator"/>

		<bean id="userKeyGenerator" class="com.xxx.cache.UserKeyGenerator"/>

		而使用基于XML配置时是通过cache:advice来指定的。
		<cache:advice id="cacheAdvice" cache-manager="cacheManager" key-generator="userKeyGenerator">
		</cache:advice>

		需要注意的是此时我们所有的Cache使用的Key的默认生成策略都是同一个KeyGenerator。

	2、自定义策略
		自定义策略是指我们可以通过Spring的EL表达式来指定我们的key。这里的EL表达式可以使用方法参数及它们对应的属性。使用方法
		参数时我们可以直接使用“#参数名”或者“#p参数index”。下面是几个使用参数作为key的示例。
		@Cacheable(value="users", key="#id")
			public User find(Integer id) {
			return null;
		}

 */
public interface KeyGenerator {

	Object generate(Object target, Method method, Object... params);

}
