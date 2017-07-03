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

package org.springframework.core.type;

import java.util.Map;
import java.util.Set;

// 注解的元数据接口
public interface AnnotationMetadata extends ClassMetadata {

	// 返回这个类的所有注解
	Set<String> getAnnotationTypes();

	// 返回注解的注解（一个可能有多个注解，注解可用于注解，例如：@Target、@Documented可作用于注解上
	Set<String> getMetaAnnotationTypes(String annotationType);

	// 判断这个类是否有指定类型的注解
	boolean hasAnnotation(String annotationType);

	// 判断这个类使用的注解中，是否有使用指定的元注解，比如（@Target、@Documented）
	boolean hasMetaAnnotation(String metaAnnotationType);

	// 判断这个是有使用annotationType这个注解
	boolean isAnnotated(String annotationType);

	// 如果这个类有使用annotationType这个注解，则返回这个注解的所有属性
	Map<String, Object> getAnnotationAttributes(String annotationType);
	Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString);// classValuesAsString表示是否将类类型转为String

	// 判断这个类中是否有使用annotationType这个注解的方法
	boolean hasAnnotatedMethods(String annotationType);

	// 返回使用annotationType这个注解的方法的MethodMetadata（方法元数据）
	Set<MethodMetadata> getAnnotatedMethods(String annotationType);

}
