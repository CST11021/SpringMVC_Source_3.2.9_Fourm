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

package org.springframework.beans.support;

// 用于按属性排序bean实例的定义
public interface SortDefinition {

	// 返回要比较的bean属性的名称。也可以是嵌套的bean属性路径
	String getProperty();

	// 是否忽略String值中的大写和小写
	boolean isIgnoreCase();

	// 返回是升序（true）还是降序（false）
	boolean isAscending();

}
