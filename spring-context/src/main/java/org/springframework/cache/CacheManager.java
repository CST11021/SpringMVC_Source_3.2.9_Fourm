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

package org.springframework.cache;

import java.util.Collection;

/**
 * Spring's central cache manager SPI.
 * Allows for retrieving named {@link Cache} regions.
 *
 * @author Costin Leau
 * @since 3.1

所有被包装的Cache，都由CacheManager实例进行统一管理(在上文的原理分析中可以看到，在<cache:annotation-driven />标签的解
析过程中会自动注入一个CacheManager实例)，他提供一个叫getCache(String name)的方法，跟据名称获得一个被包装的Cache。

在Srping的缓存模块中，spring-context-xxx.jar包中自带一个叫ConcurrentMapCacheManagr的简单实现类，它可以管理上文的提到
的ConcurrentMapCache类。开发人员如果配置了此管理器，也就拥有了本地缓存的能力。另外，为了让应用支持同时存在多个
CacheManager，spring提供了一个CompositeCacheManager的实现类，以组合设计模式的方式统一管理多个CacheManager实例。

CacheManager是Spring定义的一个用来管理Cache的接口。Spring自身已经为我们提供了两种CacheManager的实现，一种是基于Java API
的ConcurrentMap，另一种是基于第三方Cache实现——Ehcache，如果我们需要使用其它类型的缓存时，我们可以自己来实现Spring的
CacheManager接口或AbstractCacheManager抽象类。
 */
public interface CacheManager {

	// 根据这个name获取一个Cache对象，name参数不能为空
	Cache getCache(String name);

	// 返回这个缓存管理器下的所有Cache对象的名称
	Collection<String> getCacheNames();

}
