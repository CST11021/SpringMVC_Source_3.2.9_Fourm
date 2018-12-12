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

import java.io.Serializable;

import org.springframework.util.StringUtils;

// 实现SortDefinition接口：支持在再次设置相同属性时切换升序值
@SuppressWarnings("serial")
public class MutableSortDefinition implements SortDefinition, Serializable {

	private String property = "";

	// 表示是否忽略大小写
	private boolean ignoreCase = true;

	// 表示升序、false时表示降序
	private boolean ascending = true;

	// 表示是否切换升序属性
	private boolean toggleAscendingOnProperty = false;



	public MutableSortDefinition() {
	}
	public MutableSortDefinition(SortDefinition source) {
		this.property = source.getProperty();
		this.ignoreCase = source.isIgnoreCase();
		this.ascending = source.isAscending();
	}
	public MutableSortDefinition(String property, boolean ignoreCase, boolean ascending) {
		this.property = property;
		this.ignoreCase = ignoreCase;
		this.ascending = ascending;
	}
	public MutableSortDefinition(boolean toggleAscendingOnSameProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnSameProperty;
	}


	public void setProperty(String property) {
		if (!StringUtils.hasLength(property)) {
			this.property = "";
		}
		else {
			// Implicit toggling of ascending?
			if (isToggleAscendingOnProperty()) {
				this.ascending = (!property.equals(this.property) || !this.ascending);
			}
			this.property = property;
		}
	}
	public String getProperty() {
		return this.property;
	}


	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	public boolean isIgnoreCase() {
		return this.ignoreCase;
	}


	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	public boolean isAscending() {
		return this.ascending;
	}


	public void setToggleAscendingOnProperty(boolean toggleAscendingOnProperty) {
		this.toggleAscendingOnProperty = toggleAscendingOnProperty;
	}
	public boolean isToggleAscendingOnProperty() {
		return this.toggleAscendingOnProperty;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SortDefinition)) {
			return false;
		}
		SortDefinition otherSd = (SortDefinition) other;
		return (getProperty().equals(otherSd.getProperty()) &&
				isAscending() == otherSd.isAscending() &&
				isIgnoreCase() == otherSd.isIgnoreCase());
	}
	@Override
	public int hashCode() {
		int hashCode = getProperty().hashCode();
		hashCode = 29 * hashCode + (isIgnoreCase() ? 1 : 0);
		hashCode = 29 * hashCode + (isAscending() ? 1 : 0);
		return hashCode;
	}

}
