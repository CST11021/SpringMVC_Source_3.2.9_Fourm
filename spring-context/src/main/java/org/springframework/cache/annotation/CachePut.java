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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.Cache;

/**
 * Annotation indicating that a method (or all methods on a class) trigger(s)
 * a {@link Cache#put(Object, Object)} operation. As opposed to {@link Cacheable} annotation,
 * this annotation does not cause the target method to be skipped - rather it
 * always causes the method to be invoked and its result to be placed into the cache.
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @since 3.1


	在支持Spring Cache的环境下，对于使用@Cacheable标注的方法，Spring在每次执行前都会检查Cache中是否存在相同key的缓存元素，
	如果存在就不再执行该方法，而是直接从缓存中获取结果进行返回，否则才会执行并将返回结果存入指定的缓存中。@CachePut也可以
	声明一个方法支持缓存功能。与@Cacheable不同的是使用@CachePut标注的方法在执行前不会去检查缓存中是否存在之前执行过的结果，
	而是每次都会执行该方法，并将执行结果以键值对的形式存入指定的缓存中。@CachePut也可以标注在类上和方法上。使用@CachePut
	时我们可以指定的属性跟@Cacheable是一样的。

	// 每次调用后会将返回结果加入缓存，但和 @Cacheable 不同的是，它每次都会触发真实方法的调用
 	@CachePut("users")
	public User find(Integer id) {
		return null;
	}
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CachePut {

	// 以下各属性的语义请参照@Cacheable注解的属性说明

	String[] value();
	String key() default "";
	String condition() default "";
	String unless() default "";
}
