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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

// 解析Bean标签的replaced-method 属性，如果配置了这个属性，则会一个 ReplaceOverride 对象
public class ReplaceOverride extends MethodOverride {

	private final String methodReplacerBeanName;
	private List<String> typeIdentifiers = new LinkedList<String>();

	public ReplaceOverride(String methodName, String methodReplacerBeanName) {
		super(methodName);
		Assert.notNull(methodName, "Method replacer bean name must not be null");
		this.methodReplacerBeanName = methodReplacerBeanName;
	}


	public String getMethodReplacerBeanName() {
		return this.methodReplacerBeanName;
	}

	/**
	 * Add a fragment of a class string, like "Exception"
	 * or "java.lang.Exc", to identify a parameter type.
	 * @param identifier a substring of the fully qualified class name
	 */
	public void addTypeIdentifier(String identifier) {
		this.typeIdentifiers.add(identifier);
	}


	@Override
	public boolean matches(Method method) {
		// TODO could cache result for efficiency
		if (!method.getName().equals(getMethodName())) {
			// It can't match.
			return false;
		}

		if (!isOverloaded()) {
			// No overloaded: don't worry about arg type matching.
			return true;
		}

		// If we get to here, we need to insist on precise argument matching.
		if (this.typeIdentifiers.size() != method.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < this.typeIdentifiers.size(); i++) {
			String identifier = this.typeIdentifiers.get(i);
			if (!method.getParameterTypes()[i].getName().contains(identifier)) {
				// This parameter cannot match.
				return false;
			}
		}
		return true;
	}


	@Override
	public String toString() {
		return "Replace override for method '" + getMethodName() + "; will call bean '" +
				this.methodReplacerBeanName + "'";
	}
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof ReplaceOverride) || !super.equals(other)) {
			return false;
		}
		ReplaceOverride that = (ReplaceOverride) other;
		return (ObjectUtils.nullSafeEquals(this.methodReplacerBeanName, that.methodReplacerBeanName) &&
				ObjectUtils.nullSafeEquals(this.typeIdentifiers, that.typeIdentifiers));
	}
	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.methodReplacerBeanName);
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.typeIdentifiers);
		return hashCode;
	}

}
