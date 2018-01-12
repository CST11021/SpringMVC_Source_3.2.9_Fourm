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

package org.springframework.beans.factory.xml;

import java.util.Stack;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * Context that gets passed along a bean definition parsing process,
 * encapsulating all relevant configuration as well as state.
 * Nested inside an {@link XmlReaderContext}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see XmlReaderContext
 * @see BeanDefinitionParserDelegate
 */
// 通过beanDefinition解析过程传递的上下文,封装所有相关的配置和状态。另外，当BeanDefinition完成注册后，会通知相应的监听器，
// 该操作也是由 ParserContext 来完成的
public final class ParserContext {

	// 该对象用于解析整个配置文件和注册BeanDefinition，如果遇到<bean>标签会委托BeanDefinitionParserDelegate来解析
	private final XmlReaderContext readerContext;
	// BeanDefinitionParserDelegate专门用于解析<bean>标签
	private final BeanDefinitionParserDelegate delegate;

	// 该对象通常为null
	private BeanDefinition containingBeanDefinition;
	private final Stack<ComponentDefinition> containingComponents = new Stack<ComponentDefinition>();

	// 构造器
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate) {
		this.readerContext = readerContext;
		this.delegate = delegate;
	}
	public ParserContext(XmlReaderContext readerContext, BeanDefinitionParserDelegate delegate, BeanDefinition containingBeanDefinition) {
		this.readerContext = readerContext;
		this.delegate = delegate;
		this.containingBeanDefinition = containingBeanDefinition;
	}


	public final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}
	public final BeanDefinitionRegistry getRegistry() {
		return this.readerContext.getRegistry();
	}
	public final BeanDefinitionParserDelegate getDelegate() {
		return this.delegate;
	}
	public final BeanDefinition getContainingBeanDefinition() {
		return this.containingBeanDefinition;
	}
	// 判断 containingBeanDefinition 是否为空
	public final boolean isNested() {
		return (this.containingBeanDefinition != null);
	}

	// 判断Spring中Bean的 lazy-init 配置属性是否默认为TRUE
	public boolean isDefaultLazyInit() {
		return BeanDefinitionParserDelegate.TRUE_VALUE.equals(this.delegate.getDefaults().getLazyInit());
	}

	public Object extractSource(Object sourceCandidate) {
		return this.readerContext.extractSource(sourceCandidate);
	}

	public CompositeComponentDefinition getContainingComponent() {
		return (!this.containingComponents.isEmpty() ?
				(CompositeComponentDefinition) this.containingComponents.lastElement() : null);
	}

	public void pushContainingComponent(CompositeComponentDefinition containingComponent) {
		this.containingComponents.push(containingComponent);
	}

	public CompositeComponentDefinition popContainingComponent() {
		return (CompositeComponentDefinition) this.containingComponents.pop();
	}

	public void popAndRegisterContainingComponent() {
		registerComponent(popContainingComponent());
	}

	public void registerComponent(ComponentDefinition component) {
		CompositeComponentDefinition containingComponent = getContainingComponent();
		if (containingComponent != null) {
			containingComponent.addNestedComponent(component);
		}
		else {
			this.readerContext.fireComponentRegistered(component);
		}
	}

	// 注册对应的bean组件到BeanDefinition注册表中
	public void registerBeanComponent(BeanComponentDefinition component) {
		BeanDefinitionReaderUtils.registerBeanDefinition(component, getRegistry());
		registerComponent(component);
	}

}
