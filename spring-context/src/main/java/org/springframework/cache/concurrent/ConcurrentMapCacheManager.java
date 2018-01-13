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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * {@link CacheManager} implementation that lazily builds {@link ConcurrentMapCache}
 * instances for each {@link #getCache} request. Also supports a 'static' mode where
 * the set of cache names is pre-defined through {@link #setCacheNames}, with no
 * dynamic creation of further cache regions at runtime.
 *
 * @author Juergen Hoeller
 * @since 3.1
 */
// 在ConcurrentMapCacheManager内置的缓存管理器中，可以通过配置指定一个boolean类型的allowNullValues属性，用于指定缓存中能
// 否保存null值。因为该管理器是用于spring通过Map实现的内置缓存的管理器实现。在对应的Cache实现类ConcurrentMapCache中可以
// 看到，它是通过ConcurrentHashMap保存所有建值对数据的。然而ConcurrentHashMap并不支持保存null值，直接在ConcurrentHashMap
// 中put空值会抛空指针异常。然而，往缓存中保存空值有时候确实也是有必要的。比如，在从数据库查询某项数据时，因数据不存在，
// 返回了null。这时候如果不把这个null值保存到缓存中去，那么下次再作查询时，缓存就无法命中，从而导致重复查询数据库，这就
// 是所谓的缓存穿透。为了防止这种情况，这就要对null值做一个包装，把它包装成一个非null的而且在业务上认为是无效的对像保存
// 到缓存里面。ConcurrentMapCacheManager中的allowNullValues就是用于指定能否缓存null，如果此值为true，将把自动把null包装
// 成无效对像缓存起来，如果为false，那么需要开发人员自行从业务层上保证不往缓存中保存null数据。
public class ConcurrentMapCacheManager implements CacheManager {

	private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>(16);
	private boolean dynamic = true;


	public ConcurrentMapCacheManager() {
	}
	public ConcurrentMapCacheManager(String... cacheNames) {
		setCacheNames(Arrays.asList(cacheNames));
	}


	/**
	 * Specify the set of cache names for this CacheManager's 'static' mode.
	 * <p>The number of caches and their names will be fixed after a call to this method,
	 * with no creation of further cache regions at runtime.
	 */
	public void setCacheNames(Collection<String> cacheNames) {
		if (cacheNames != null) {
			for (String name : cacheNames) {
				this.cacheMap.put(name, createConcurrentMapCache(name));
			}
			this.dynamic = false;
		}
	}

	public Collection<String> getCacheNames() {
		return Collections.unmodifiableSet(this.cacheMap.keySet());
	}

	public Cache getCache(String name) {
		Cache cache = this.cacheMap.get(name);
		if (cache == null && this.dynamic) {
			synchronized (this.cacheMap) {
				cache = this.cacheMap.get(name);
				if (cache == null) {
					cache = createConcurrentMapCache(name);
					this.cacheMap.put(name, cache);
				}
			}
		}
		return cache;
	}

	/**
	 * Create a new ConcurrentMapCache instance for the specified cache name.
	 * @param name the name of the cache
	 * @return the ConcurrentMapCache (or a decorator thereof)
	 */
	protected Cache createConcurrentMapCache(String name) {
		return new ConcurrentMapCache(name);
	}

}
