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

package org.springframework.beans.factory.config;

import org.springframework.util.Assert;

/**
 * Immutable placeholder class used for a property value object when it's a reference to another bean in the factory, to be resolved at runtime.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.BeanFactory#getBean
 */
// 当占位符类用于属性值的对象时，它指向工厂中另一个bean的引用，并在运行时被解析的。
// 比如Bean A 依赖 B，当Bean还没有实例化之前是以BeanDefinition的形式存在，此时指向B的引用（变量）用一个BeanReference对象来表示
public class RuntimeBeanReference implements BeanReference {

	// 这个运行时bean引用指向的bean名称
	private final String beanName;
	// 表示这个指向的bean是否在双亲容器中
	private final boolean toParent;
	// 表示这个bean的配置源（即这个bean是在哪个文件中定义的）,可能返回null
	private Object source;


	public RuntimeBeanReference(String beanName) {
		this(beanName, false);
	}
	public RuntimeBeanReference(String beanName, boolean toParent) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
		this.toParent = toParent;
	}

	// getter and setter ...
	public String getBeanName() {
		return this.beanName;
	}
	public boolean isToParent() {
		return this.toParent;
	}
	public void setSource(Object source) {
		this.source = source;
	}
	public Object getSource() {
		return this.source;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof RuntimeBeanReference)) {
			return false;
		}
		RuntimeBeanReference that = (RuntimeBeanReference) other;
		return (this.beanName.equals(that.beanName) && this.toParent == that.toParent);
	}
	@Override
	public int hashCode() {
		int result = this.beanName.hashCode();
		result = 29 * result + (this.toParent ? 1 : 0);
		return result;
	}
	@Override
	public String toString() {
		return '<' + getBeanName() + '>';
	}

}
