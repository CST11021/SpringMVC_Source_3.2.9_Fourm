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

package org.springframework.core.env;

/**
 * PropertyResolver属性解决器，主要具有两个功能：
 * 通过propertyName属性名获取与之对应的propertValue属性值（getProperty）。
 * 把${propertyName:defaultValue}格式的属性占位符，替换为实际的值(resolvePlaceholders)。
 * 注意：getProperty获取的属性值，全都是调用resolvePlaceholders进行占位符替换后的值。
 */
public interface PropertyResolver {

	// 查看是否包含指定属性
	boolean containsProperty(String key);

	// 获取属性值 如果找不到返回null
	String getProperty(String key);
	// 获取属性值，如果找不到返回默认值
	String getProperty(String key, String defaultValue);
	// 获取指定类型的属性值，找不到返回null
	<T> T getProperty(String key, Class<T> targetType);
	// 获取指定类型的属性值，找不到返回默认值
	<T> T getProperty(String key, Class<T> targetType, T defaultValue);
	// 获取指定属性，并转换为对应类型
	<T> Class<T> getPropertyAsClass(String key, Class<T> targetType);

	// 获取属性值，找不到抛出异常IllegalStateException
	String getRequiredProperty(String key) throws IllegalStateException;
	//获取指定类型的属性值，找不到抛出异常IllegalStateException
	<T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

	//替换文本中的占位符（${key}）到属性值，找不到不解析
	String resolvePlaceholders(String text);
	//替换文本中的占位符（${key}）到属性值，找不到抛出异常IllegalArgumentException
	String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}
