/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.cache.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Composite {@link CacheManager} implementation that iterates over
 * a given collection of delegate {@link CacheManager} instances.
 *
 * <p>Allows {@link NoOpCacheManager} to be automatically added to the end of
 * the list for handling cache declarations without a backing store. Otherwise,
 * any custom {@link CacheManager} may play that role of the last delegate as
 * well, lazily creating cache regions for any requested name.
 *
 * <p>Note: Regular CacheManagers that this composite manager delegates to need
 * to return {@code null} from {@link #getCache(String)} if they are unaware of
 * the specified cache name, allowing for iteration to the next delegate in line.
 * However, most {@link CacheManager} implementations fall back to lazy creation
 * of named caches once requested; check out the specific configuration details
 * for a 'static' mode with fixed cache names, if available.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 3.1
 * @see #setFallbackToNoOpCache
 * @see org.springframework.cache.concurrent.ConcurrentMapCacheManager#setCacheNames
 */
// 这里简单再说一下具体实现的方式：CompositeCacheManager中维护一个CacheManager列表，用户可以通过配置，把多个CacheManager
// 配置到这个列表中，使得应用可以同时管理多个缓存管理器。这个类对于getCache(String)方法实现是通过遍历这个列表，匹配出name
// 相同的Cache实例并返回。这个类还可以通过配置指定一个boolean的fallbackToNoOpCache标志属性，它的作用就是，当通过
// getCache(string)获取不到Cache实例时，是否不进行任何缓存操作。在默认情况或者fallbackToNoOpCache值为false时，在通过
// getCache(string)获取不到Cache实例时，业务层上可能会抛出运行时异常（比如提示“找不到XXX名称的Cache”）。但如果为true时，
// 这时候不进行任何缓存操作也不抛异常，这种场景主要用于在不具备缓存条件的时候，在不改代码的情况下，禁用缓存。spring对于
// 这种机制的实现，是通过上图中没有画出的两个特殊的类来实现的: NoOpCacheManager和NoOpCache类。这两个类分别是CacheManager
// 类Cache类的子类，表示不进行任何缓存操作。
public class CompositeCacheManager implements CacheManager, InitializingBean {

	private final List<CacheManager> cacheManagers = new ArrayList<CacheManager>();
	private boolean fallbackToNoOpCache = false;


	public CompositeCacheManager() {
	}
	public CompositeCacheManager(CacheManager... cacheManagers) {
		setCacheManagers(Arrays.asList(cacheManagers));
	}


	/**
	 * Specify the CacheManagers to delegate to.
	 */
	public void setCacheManagers(Collection<CacheManager> cacheManagers) {
		this.cacheManagers.clear();  // just here to preserve compatibility with previous behavior
		this.cacheManagers.addAll(cacheManagers);
	}

	/**
	 * Indicate whether a {@link NoOpCacheManager} should be added at the end of the delegate list.
	 * In this case, any {@code getCache} requests not handled by the configured CacheManagers will
	 * be automatically handled by the {@link NoOpCacheManager} (and hence never return {@code null}).
	 */
	public void setFallbackToNoOpCache(boolean fallbackToNoOpCache) {
		this.fallbackToNoOpCache = fallbackToNoOpCache;
	}

	public void afterPropertiesSet() {
		if (this.fallbackToNoOpCache) {
			this.cacheManagers.add(new NoOpCacheManager());
		}
	}


	public Cache getCache(String name) {
		for (CacheManager cacheManager : this.cacheManagers) {
			Cache cache = cacheManager.getCache(name);
			if (cache != null) {
				return cache;
			}
		}
		return null;
	}

	public Collection<String> getCacheNames() {
		Set<String> names = new LinkedHashSet<String>();
		for (CacheManager manager : this.cacheManagers) {
			names.addAll(manager.getCacheNames());
		}
		return Collections.unmodifiableSet(names);
	}

}
