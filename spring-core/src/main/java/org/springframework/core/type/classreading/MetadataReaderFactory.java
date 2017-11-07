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

package org.springframework.core.type.classreading;

import java.io.IOException;

import org.springframework.core.io.Resource;

/**
 * Factory interface for {@link MetadataReader} instances.
 * Allows for caching a MetadataReader per original resource.允许缓存每一个 MetadataReader 资源
 *
 * @author Juergen Hoeller
 * @since 2.5
 * @see SimpleMetadataReaderFactory
 * @see CachingMetadataReaderFactory
 */
// 元素数据读取器工厂接口
public interface MetadataReaderFactory {


	// 根据 className 获取类的MetadataReader对象
	MetadataReader getMetadataReader(String className) throws IOException;

	// 根据 resource 获取类的MetadataReader对象，resource表示一个.class文件
	MetadataReader getMetadataReader(Resource resource) throws IOException;

}
