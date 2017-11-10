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

package org.springframework.beans.factory.wiring;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.Assert;

// 封装自动注入的信息
public class BeanWiringInfo {

	// 表示根据名字自动注入
	public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
	// 表示根据类型自动注入
	public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;


	private String beanName = null;
	private boolean isDefaultBeanName = false;
	private boolean dependencyCheck = false;
	private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;


	public BeanWiringInfo() {
	}
	public BeanWiringInfo(String beanName) {
		this(beanName, false);
	}
	public BeanWiringInfo(String beanName, boolean isDefaultBeanName) {
		Assert.hasText(beanName, "'beanName' must not be empty");
		this.beanName = beanName;
		this.isDefaultBeanName = isDefaultBeanName;
	}
	public BeanWiringInfo(int autowireMode, boolean dependencyCheck) {
		if (autowireMode != AUTOWIRE_BY_NAME && autowireMode != AUTOWIRE_BY_TYPE) {
			throw new IllegalArgumentException("Only constants AUTOWIRE_BY_NAME and AUTOWIRE_BY_TYPE supported");
		}
		this.autowireMode = autowireMode;
		this.dependencyCheck = dependencyCheck;
	}



	public boolean indicatesAutowiring() {
		return (this.beanName == null);
	}


	// getter ...
	public String getBeanName() {
		return this.beanName;
	}
	public boolean isDefaultBeanName() {
		return this.isDefaultBeanName;
	}
	public int getAutowireMode() {
		return this.autowireMode;
	}
	public boolean getDependencyCheck() {
		return this.dependencyCheck;
	}

}
