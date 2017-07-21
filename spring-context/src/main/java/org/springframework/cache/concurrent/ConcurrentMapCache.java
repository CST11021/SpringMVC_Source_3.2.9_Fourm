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

package org.springframework.cache.concurrent;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Simple {@link Cache} implementation based on the core JDK
 * {@code java.util.concurrent} package.
 *
 * <p>Useful for testing or simple caching scenarios, typically in combination
 * with {@link org.springframework.cache.support.SimpleCacheManager} or
 * dynamically through {@link ConcurrentMapCacheManager}.
 *
 * <p><b>Note:</b> As {@link ConcurrentHashMap} (the default implementation used)
 * does not allow for {@code null} values to be stored, this class will replace
 * them with a predefined internal object. This behavior can be changed through the
 * {@link #ConcurrentMapCache(String, ConcurrentMap, boolean)} constructor.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 */
public class ConcurrentMapCache implements Cache {
	// 当缓存对象为null是，则使用该对象缓存
	private static final Object NULL_HOLDER = new NullHolder();
	// 用于表示这个Cache的名字
	private final String name;
	// 所有的缓存数据都将被放到这个store
	private final ConcurrentMap<Object, Object> store;
	// 是否允许缓存null值
	private final boolean allowNullValues;

	public ConcurrentMapCache(String name) {
		this(name, new ConcurrentHashMap<Object, Object>(256), true);
	}
	public ConcurrentMapCache(String name, boolean allowNullValues) {
		this(name, new ConcurrentHashMap<Object, Object>(256), allowNullValues);
	}
	public ConcurrentMapCache(String name, ConcurrentMap<Object, Object> store, boolean allowNullValues) {
		this.name = name;
		this.store = store;
		this.allowNullValues = allowNullValues;
	}


	public String getName() {
		return this.name;
	}
	public ConcurrentMap getNativeCache() {
		return this.store;
	}
	public ValueWrapper get(Object key) {
		Object value = this.store.get(key);
		return (value != null ? new SimpleValueWrapper(fromStoreValue(value)) : null);
	}
	public void put(Object key, Object value) {
		this.store.put(key, toStoreValue(value));
	}
	public void evict(Object key) {
		this.store.remove(key);
	}
	public void clear() {
		this.store.clear();
	}

	public boolean isAllowNullValues() {
		return this.allowNullValues;
	}
	// 如果 storeValue 是 NULL_HOLDER，则返回 null
	protected Object fromStoreValue(Object storeValue) {
		if (this.allowNullValues && storeValue == NULL_HOLDER) {
			return null;
		}
		return storeValue;
	}
	// 如果 storeValue 是 null，则返回 NULL_HOLDER
	protected Object toStoreValue(Object userValue) {
		if (this.allowNullValues && userValue == null) {
			return NULL_HOLDER;
		}
		return userValue;
	}


	@SuppressWarnings("serial")
	private static class NullHolder implements Serializable {}

}
