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

package org.springframework.beans;

import java.beans.PropertyEditor;

// 属性编辑器的注册接口，该接口还可以用于获取对应类型的属性编辑器
public interface PropertyEditorRegistry {

	// 注册自定义属性编辑器，propertyPath 可用于找到一个对应的属性编辑器
	void registerCustomEditor(Class<?> requiredType, PropertyEditor propertyEditor);
	void registerCustomEditor(Class<?> requiredType, String propertyPath, PropertyEditor propertyEditor);

	PropertyEditor findCustomEditor(Class<?> requiredType, String propertyPath);

}
