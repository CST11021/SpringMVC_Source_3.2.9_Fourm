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

/**
 * Annotation indicating that a method (or all the methods on a class) can be cached.
 *
 * <p>The method arguments and signature are used for computing the key while the
 * returned instance is used as the cache value.
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @since 3.1

 	@Cacheable可以标记在一个方法上，也可以标记在一个类上。当标记在一个方法上时表示该方法是支持缓存的，当标记在一
	个类上时则表示该类所有的方法都是支持缓存的。对于一个支持缓存的方法，Spring会在其被调用后将其返回值缓存起来，以保证下
	次利用同样的参数来执行该方法时可以直接从缓存中获取结果，而不需要再次执行该方法。Spring在缓存方法的返回值时是以键值对
	进行缓存的，值就是方法的返回结果，至于键的话，Spring又支持两种策略，默认策略和自定义策略。需要注意的是当一个支持缓存
	的方法在对象内部被调用时是不会触发缓存功能的。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cacheable {

	/**
	 	value属性指定Cache名称
	 		value属性是必须指定的，其表示当前方法的返回值是会被缓存在哪个Cache上的，对应Cache的名称。其可以是一个Cache也
	 	可以是多个Cache，当需要指定多个Cache时其是一个数组。
	 	//Cache是发生在cache1上的
		@Cacheable("cache1")
		public User find(Integer id) {
			return null;
		}

		//Cache是发生在cache1和cache2上的
		@Cacheable({"cache1", "cache2"})
		public User find(Integer id) {
			return null;
		}
	 */
	String[] value();

	/**
	 	使用key属性自定义key
	 		key属性是用来指定Spring缓存方法的返回结果时对应的key的。该属性支持SpringEL表达式。
	 		当我们没有指定该属性时，Spring将使用默认策略生成key。
	 		自定义策略是指我们可以通过Spring的EL表达式来指定我们的key。这里的EL表达式可以使用方法参数及它们对应的属性。
	 	使用方法参数时我们可以直接使用“#参数名”或者“#p参数index”。下面是几个使用参数作为key的示例：
			@Cacheable(value="users", key="#id")
			public User find(Integer id) {
				return null;
			}

			@Cacheable(value="users", key="#p0")
				public User find(Integer id) {
				return null;
			}

			@Cacheable(value="users", key="#user.id")
				public User find(User user) {
				return null;
			}

			@Cacheable(value="users", key="#p0.id")
				public User find(User user) {
				return null;
			}

			除了上述使用方法参数作为key之外，Spring还为我们提供了一个root对象可以用来生成key。通过该root对象我们可以获取
		到以下信息。
		属性名称		描述							示例
		methodName		当前方法名						#root.methodName
		method			当前方法						#root.method.name
		target			当前被调用的对象				#root.target
		targetClass		当前被调用的对象的class			#root.targetClass
		args			当前方法参数组成的数组			#root.args[0]
		caches			当前被调用的方法使用的Cache		#root.caches[0].name

		当我们要使用root对象的属性作为key时我们也可以将“#root”省略，因为Spring默认使用的就是root对象的属性。如：
			@Cacheable(value={"users", "xxx"}, key="caches[1].name")
			public User find(User user) {
				return null;
			}
	 */
	String key() default "";

	/**
	 	condition属性指定发生的条件
	 		有的时候我们可能并不希望缓存一个方法所有的返回结果。通过condition属性可以实现这一功能。condition属性默认为空，
	 	表示将缓存所有的调用情形。其值是通过SpringEL表达式来指定的，当为true时表示进行缓存处理；当为false时表示不进行缓
	 	存处理，即每次调用该方法时该表达式都会执行一次。如下示例表示只有当user的id为偶数时才会进行缓存。
		 @Cacheable(value={"users"}, key="#user.id", condition="#user.id%2==0")
		 public User find(User user) {
			 System.out.println("find user by user " + user);
			 return user;
		 }
	 */
	String condition() default "";

	/**	可以使用该属性配置一个EL表达式，该表达式用来判断是否要缓存方法的返回值，和condition不同之处在于，condition是在方
	   	法调用进行判断，而unless是在方法调用后进行判断的，例如：
			@Cacheable(key = "#id", unless="#result == null")
			public XXXPO get(int id) {
				//get from db
			}
		这样，当方法返回null值时，就不会被缓存起来，#result表示方法结果的返回值

	*/
	String unless() default "";
}
