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

package org.springframework.beans;

import java.lang.reflect.Field;

import org.springframework.core.MethodParameter;

// spring的类型转换器接口：

// 在spring中一开始是使用PropertyEditor对象来进行类型转换的，PropertyEditor对象有一些问题，只能转换字符串到对象，
// 而且不是线程安全的，所以spring中重新新定义了一个对象Converter由于类型转换，可以进行任意类型的转换，对外使用ConversionService,
// 而TypeConverter对象正好是综合了这两个对象，先尝试是用PropertyEditor转换器转换，如果没找到对应的转换器，会用ConversionService来进行对象转换。

// 接口定义了类型转换的方法。通常（但不一定）使用时会同 propertyeditorregistry 接口一起，由于TypeConverter的实现通常是基于 propertyeditor（不是线程安全的），所以TypeConverters也不是线程安全的。
public interface TypeConverter {

	// 将value转化为指定的requireType类型
	<T> T convertIfNecessary(Object value, Class<T> requiredType) throws TypeMismatchException;
	<T> T convertIfNecessary(Object value, Class<T> requiredType, MethodParameter methodParam) throws TypeMismatchException;
	<T> T convertIfNecessary(Object value, Class<T> requiredType, Field field) throws TypeMismatchException;

}
