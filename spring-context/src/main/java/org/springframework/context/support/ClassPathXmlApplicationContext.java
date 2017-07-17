/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;


public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	private Resource[] configResources;

	// 构造器
	public ClassPathXmlApplicationContext() {}
	public ClassPathXmlApplicationContext(ApplicationContext parent) {
		super(parent);
	}
	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[] {configLocation}, true, null);
	}
	public ClassPathXmlApplicationContext(String... configLocations) throws BeansException {
		this(configLocations, true, null);
	}
	public ClassPathXmlApplicationContext(String[] configLocations, ApplicationContext parent) throws BeansException {
		this(configLocations, true, parent);
	}
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh) throws BeansException {
		this(configLocations, refresh, null);
	}
	public ClassPathXmlApplicationContext(String path, Class clazz) throws BeansException {
		this(new String[] {path}, clazz);
	}
	public ClassPathXmlApplicationContext(String[] paths, Class clazz) throws BeansException {
		this(paths, clazz, null);
	}
	// 以下是两个较为核心的构造器
	public ClassPathXmlApplicationContext(String[] paths, Class clazz, ApplicationContext parent) throws BeansException {

		super(parent);
		Assert.notNull(paths, "Path array must not be null");
		Assert.notNull(clazz, "Class argument must not be null");
		this.configResources = new Resource[paths.length];
		for (int i = 0; i < paths.length; i++) {
			this.configResources[i] = new ClassPathResource(paths[i], clazz);
		}
		refresh();
	}
	public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, ApplicationContext parent) throws BeansException {

		super(parent);
		setConfigLocations(configLocations);
		if (refresh) {
			refresh();
		}
	}

	// 返回一个资源对象数组，引用这个上下文应该构建的XML bean定义文件
	@Override
	protected Resource[] getConfigResources() {
		return this.configResources;
	}

}
