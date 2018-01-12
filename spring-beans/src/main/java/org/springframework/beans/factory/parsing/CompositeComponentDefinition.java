/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.beans.factory.parsing;

import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * {@link ComponentDefinition} implementation that holds one or more nested
 * {@link ComponentDefinition} instances, aggregating them into a named group
 * of components.
 *
 * @author Juergen Hoeller
 * @since 2.0.1
 * @see #getNestedComponents()
 */
// 内部封装了多个Bean组件，来用将多个不同组件封装为一个功能更强大的组件
public class CompositeComponentDefinition extends AbstractComponentDefinition {

	// 表示该组合组件的名称
	private final String name;
	// 表示该组合组件的配置源
	private final Object source;
	// 用来保存该组合组件内部包含的组件
	private final List<ComponentDefinition> nestedComponents = new LinkedList<ComponentDefinition>();


	public CompositeComponentDefinition(String name, Object source) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.source = source;
	}


	// 添加一个Bean组件
	public void addNestedComponent(ComponentDefinition component) {
		Assert.notNull(component, "ComponentDefinition must not be null");
		this.nestedComponents.add(component);
	}
	// 获取所有的内部组件
	public ComponentDefinition[] getNestedComponents() {
		return this.nestedComponents.toArray(new ComponentDefinition[this.nestedComponents.size()]);
	}

	public String getName() {
		return this.name;
	}
	public Object getSource() {
		return this.source;
	}

}
