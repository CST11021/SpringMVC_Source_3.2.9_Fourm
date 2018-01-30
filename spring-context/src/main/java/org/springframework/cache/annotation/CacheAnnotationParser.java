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

package org.springframework.cache.annotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

import org.springframework.cache.interceptor.CacheOperation;

/**
 * Strategy interface for parsing known caching annotation types.
 * {@link AnnotationCacheOperationSource} delegates to such
 * parsers for supporting specific annotation types such as Spring's own
 * {@link Cacheable}, {@link CachePut} or {@link CacheEvict}.
 *
 * @author Costin Leau
 * @since 3.1
 */
public interface CacheAnnotationParser {

	// AnnotatedElement表示一个Method对象或Class对象，该方法用来解析指定方法或类配置的Spring缓存注解配合信息，缓存注解的
	// 配置信息将被封装为一个CacheOperation对象
	Collection<CacheOperation> parseCacheAnnotations(AnnotatedElement ae);
}
