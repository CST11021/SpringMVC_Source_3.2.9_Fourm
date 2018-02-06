/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.cache.annotation;

import java.lang.annotation.*;

/**
 * Group annotation for multiple cache annotations (of different or the same type).
 *
 * @author Costin Leau
 * @author Chris Beams
 * @since 3.1
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Caching {

	/**
		@Caching注解可以让我们在一个方法或者类上同时指定多个Spring Cache相关的注解。其拥有三个属性：cacheable、put
		和evict，分别用于指定@Cacheable、@CachePut和@CacheEvict。
		@Caching(
			cacheable = @Cacheable("users"),
			evict = { @CacheEvict("cache2"), @CacheEvict(value = "cache3", allEntries = true) }
		)
		public User find(Integer id) {
			return null;
		}
	 */

	Cacheable[] cacheable() default {};
	CachePut[] put() default {};
	CacheEvict[] evict() default {};

}
