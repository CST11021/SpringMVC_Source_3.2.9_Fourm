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

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

// 封装Bean的属性元数据：name表示属性名、value表示属性值、source表示这个bean的配置源
// 元数据 = bean定义文件资源 + bean中定义的属性集合
public class BeanMetadataAttribute implements BeanMetadataElement {

	private final String name;
	private final Object value;
	private Object source;


	public BeanMetadataAttribute(String name, Object value) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.value = value;
	}


	public String getName() {
		return this.name;
	}
	public Object getValue() {
		return this.value;
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
		if (!(other instanceof BeanMetadataAttribute)) {
			return false;
		}
		BeanMetadataAttribute otherMa = (BeanMetadataAttribute) other;
		return (this.name.equals(otherMa.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherMa.value) &&
				ObjectUtils.nullSafeEquals(this.source, otherMa.source));
	}
	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
	}
	@Override
	public String toString() {
		return "metadata attribute '" + this.name + "'";
	}

}
