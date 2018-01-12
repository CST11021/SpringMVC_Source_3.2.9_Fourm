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
 */
// 缓存管理器接口
public interface CacheManager {

	// 根据这个name获取一个Cache对象，name参数不能为空
	Cache getCache(String name);

	// 返回这个缓存管理器下的所有Cache对象的名称
	Collection<String> getCacheNames();

}
