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

package org.springframework.cache;

/**
 * Interface that defines the common cache operations.
 *
 * <b>Note:</b> Due to the generic use of caching, it is recommended that
 * implementations allow storage of <tt>null</tt> values (for example to
 * cache methods that return {@code null}).
 *
 * @author Costin Leau
 * @since 3.1
 */
// 一个公共的缓存操作接口,所有被包装的Cache，都由CacheManager实例进行统一管理
// spring缓存模块对可用的Cache采用适配器模式进行了统一的封装。具体代码在spring-context-xxx.jar包中的
// org.springframework.cache.Cache接口，此接口声明一些储如get(Object key)，put(Objectkey,Object value)等缓存操作的统一
// api。在这个接口中，还声明了一个叫getNativeCache()的方法，返回它适配的具体的缓存实现(比如在集成ehcache时，这个接口实现
// 类的实例调用getNativeCache()时会返回net.sf.ehcache.Cache类型的实例)。每一个Cache实例都通过名称加以区分，所以在Cache接
// 口中，还声明了一个getName()返回此实例的名称。spring提供一个叫ConcurrentMapCache的基于Map的Cache实现类，作为它内置的本
// 地缓存实现方案。
public interface Cache {

	// 返回缓存名
	String getName();

	// Return the the underlying native cache provider.
	Object getNativeCache();

	// 根据key获取一个缓存对象，并包装为 ValueWrapper 对象后返回
	ValueWrapper get(Object key);

	// 添加一个缓存对象
	void put(Object key, Object value);

	// 移除一个缓存对象
	void evict(Object key);

	// 清楚所有缓存
	void clear();


	// 用于保存缓存对象的接口
	interface ValueWrapper {
		// 返回这个缓存中的真实值
		Object get();
	}

}
