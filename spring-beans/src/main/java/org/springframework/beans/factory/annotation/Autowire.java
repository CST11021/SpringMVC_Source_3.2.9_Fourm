/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.beans.factory.annotation;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

// 定义spring提供的自动装配类型，Spring自动依赖注入装配默认是按类型装配，如果使用@Qualifier则按名称
public enum Autowire {

	// no：顾名思义， 显式指明不使用Spring的自动装配功能
	// byName：根据属性和组件的名称匹配关系来实现bean的自动装配
	// byType：根据属性和组件的类型匹配关系来实现bean的自动装配，有多个适合类型的对象时装配失败
	NO(AutowireCapableBeanFactory.AUTOWIRE_NO),
	BY_NAME(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME),
	BY_TYPE(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);

	private final int value;

	Autowire(int value) {
		this.value = value;
	}
	public int value() {
		return this.value;
	}

	// 是否使用自动装配
	public boolean isAutowire() {
		return (this == BY_NAME || this == BY_TYPE);
	}

}
