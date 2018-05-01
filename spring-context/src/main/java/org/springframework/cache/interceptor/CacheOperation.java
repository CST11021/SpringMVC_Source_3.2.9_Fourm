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

package org.springframework.cache.interceptor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * Base class implementing {@link CacheOperation}.
 *
 * @author Costin Leau
 */
// 该抽象类是CacheEvictOperation、CachePutOperation和CacheableOperation的父类，用来封装Spring缓存注解的配置信息
public abstract class CacheOperation {
	// 表示缓存注解配置的value值，如：@Cacheable(value="accountCache")
	private Set<String> cacheNames = Collections.emptySet();
	// 表示缓存注解配置的condition值
	private String condition = "";
	// 表示缓存注解配置的key值
	private String key = "";
	// 表示缓存注解修饰的方法或类
	private String name = "";


	public Set<String> getCacheNames() {
		return cacheNames;
	}
	public String getCondition() {
		return condition;
	}
	public String getKey() {
		return key;
	}
	public String getName() {
		return name;
	}
	public void setCacheName(String cacheName) {
		Assert.hasText(cacheName);
		this.cacheNames = Collections.singleton(cacheName);
	}
	public void setCacheNames(String[] cacheNames) {
		Assert.notEmpty(cacheNames);
		this.cacheNames = new LinkedHashSet<String>(cacheNames.length);
		for (String string : cacheNames) {
			this.cacheNames.add(string);
		}
	}
	public void setCondition(String condition) {
		Assert.notNull(condition);
		this.condition = condition;
	}
	public void setKey(String key) {
		Assert.notNull(key);
		this.key = key;
	}
	public void setName(String name) {
		Assert.hasText(name);
		this.name = name;
	}


	@Override
	public boolean equals(Object other) {
		return (other instanceof CacheOperation && toString().equals(other.toString()));
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	@Override
	public String toString() {
		return getOperationDescription().toString();
	}
	protected StringBuilder getOperationDescription() {
		StringBuilder result = new StringBuilder();
		result.append(getClass().getSimpleName());
		result.append("[");
		result.append(this.name);
		result.append("] caches=");
		result.append(this.cacheNames);
		result.append(" | key='");
		result.append(this.key);
		result.append("' | condition='");
		result.append(this.condition);
		result.append("'");
		return result;
	}
}
