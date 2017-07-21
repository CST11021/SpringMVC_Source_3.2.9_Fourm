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
// 一个公共的缓存操作接口
public interface Cache {

	// Return the cache name.
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
