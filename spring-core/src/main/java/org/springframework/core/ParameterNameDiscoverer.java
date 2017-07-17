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

package org.springframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// 用于获取方法和构造函数的参数名称的接口（获取函数的所有参数名）
public interface ParameterNameDiscoverer {

	// 返回这个方法中的参数名
	String[] getParameterNames(Method method);

	// 返回这个构造函数中的参数名
	String[] getParameterNames(Constructor<?> ctor);

}
