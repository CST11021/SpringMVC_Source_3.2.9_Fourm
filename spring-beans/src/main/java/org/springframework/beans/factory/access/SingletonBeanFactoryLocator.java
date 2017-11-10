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

package org.springframework.beans.factory.access;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

public class SingletonBeanFactoryLocator implements BeanFactoryLocator {

	protected static final Log logger = LogFactory.getLog(SingletonBeanFactoryLocator.class);

	private static final String DEFAULT_RESOURCE_LOCATION = "classpath*:beanRefFactory.xml";
	private static final Map<String, BeanFactoryLocator> instances = new HashMap<String, BeanFactoryLocator>();

	private final String resourceLocation;
	// 用于缓存 resourceLocation 配置文件和它对应的BeanFactoryGroup 的映射关系
	private final Map<String, BeanFactoryGroup> bfgInstancesByKey = new HashMap<String, BeanFactoryGroup>();
	// 用于缓存 resourceLocation相应的BeanFactory 和 BeanFactoryGroup 的映射关系
	private final Map<BeanFactory, BeanFactoryGroup> bfgInstancesByObj = new HashMap<BeanFactory, BeanFactoryGroup>();


	// 构造器
	protected SingletonBeanFactoryLocator(String resourceLocation) {
		this.resourceLocation = resourceLocation;
	}

	public static BeanFactoryLocator getInstance() throws BeansException {
		return getInstance(null);
	}
	public static BeanFactoryLocator getInstance(String selector) throws BeansException {
		String resourceLocation = selector;
		if (resourceLocation == null) {
			resourceLocation = DEFAULT_RESOURCE_LOCATION;
		}

		// For backwards compatibility, we prepend 'classpath*:' to the selector name if there
		// is no other prefix (i.e. classpath*:, classpath:, or some URL prefix.
		if (!ResourcePatternUtils.isUrl(resourceLocation)) {
			resourceLocation = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourceLocation;
		}

		synchronized (instances) {
			if (logger.isTraceEnabled()) {
				logger.trace("SingletonBeanFactoryLocator.getInstance(): instances.hashCode=" +
							 instances.hashCode() + ", instances=" + instances);
			}
			BeanFactoryLocator bfl = instances.get(resourceLocation);
			if (bfl == null) {
				bfl = new SingletonBeanFactoryLocator(resourceLocation);
				instances.put(resourceLocation, bfl);
			}
			return bfl;
		}
	}

	// 指定factoryKey，返回这个key对应的BeanFactory的一个BeanFactoryReference，BeanFactoryReference用于返回依赖的BeanFactory
	// 这里的factoryKey 可以理解为工厂Bean对应的Name
	public BeanFactoryReference useBeanFactory(String factoryKey) throws BeansException {
		synchronized (this.bfgInstancesByKey) {
			BeanFactoryGroup bfg = this.bfgInstancesByKey.get(this.resourceLocation);

			if (bfg != null) {
				bfg.refCount++;
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Factory group with resource name [" + this.resourceLocation + "] requested. Creating new instance.");
				}

				// 初始化 resourceLocation 对应的 BeanFactory 并缓存到 this.bfgInstancesByKey 中
				BeanFactory groupContext = createDefinition(this.resourceLocation, factoryKey);
				bfg = new BeanFactoryGroup();
				bfg.definition = groupContext;
				bfg.refCount = 1;
				this.bfgInstancesByKey.put(this.resourceLocation, bfg);
				this.bfgInstancesByObj.put(groupContext, bfg);


				try {
					// 加载这个bean工厂的单例bean
					initializeDefinition(groupContext);
				}
				catch (BeansException ex) {
					this.bfgInstancesByKey.remove(this.resourceLocation);
					this.bfgInstancesByObj.remove(groupContext);
					throw new BootstrapException("Unable to initialize group definition. " +
							"Group resource name [" + this.resourceLocation + "], factory key [" + factoryKey + "]", ex);
				}
			}

			try {
				BeanFactory beanFactory;
				// 这里的bfg指 resourceLocation 对应的 BeanFactoryGroup
				if (factoryKey != null) {
					beanFactory = bfg.definition.getBean(factoryKey, BeanFactory.class);
				}
				else {
					beanFactory = bfg.definition.getBean(BeanFactory.class);
				}
				return new CountingBeanFactoryReference(beanFactory, bfg.definition);
			}
			catch (BeansException ex) {
				throw new BootstrapException("Unable to return specified BeanFactory instance: factory key [" +
						factoryKey + "], from group with resource name [" + this.resourceLocation + "]", ex);
			}

		}
	}
	// 指定配置文件创建一个BeanFactory
	protected BeanFactory createDefinition(String resourceLocation, String factoryKey) {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

		try {
			Resource[] configResources = resourcePatternResolver.getResources(resourceLocation);
			if (configResources.length == 0) {
				throw new FatalBeanException("Unable to find resource for specified definition. " +
						"Group resource name [" + this.resourceLocation + "], factory key [" + factoryKey + "]");
			}
			reader.loadBeanDefinitions(configResources);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"Error accessing bean definition resource [" + this.resourceLocation + "]", ex);
		}
		catch (BeanDefinitionStoreException ex) {
			throw new FatalBeanException("Unable to load group definition: " +
					"group resource name [" + this.resourceLocation + "], factory key [" + factoryKey + "]", ex);
		}

		return factory;
	}
	// 实例化groupDef 中的所有单例bean：调用useBeanFactory方法时会进行初始化
	protected void initializeDefinition(BeanFactory groupDef) {
		if (groupDef instanceof ConfigurableListableBeanFactory) {
			((ConfigurableListableBeanFactory) groupDef).preInstantiateSingletons();
		}
	}
	// 销毁groupDef 中的所有单例Bean
	protected void destroyDefinition(BeanFactory groupDef, String selector) {
		if (groupDef instanceof ConfigurableBeanFactory) {
			if (logger.isTraceEnabled()) {
				logger.trace("Factory group with selector '" + selector +
						"' being released, as there are no more references to it");
			}
			((ConfigurableBeanFactory) groupDef).destroySingletons();
		}
	}



	private static class BeanFactoryGroup {
		private BeanFactory definition;

		// refCount：用来记录实例被外部引用的记数，当调用locator.useBeanFactory(parentContextKey)方法时，引用数就会加1，
		// 当调用CountingBeanFactoryReference#release方法时，引用数就会减1，当它变成0时，Spring就会释放掉它占用的内存，
		// 同时也会销毁掉它definition变量引用的BeanFactory。下次再调用locator.useBeanFactory(parentContextKey)就会重新
		// 初始化BeanFactory。
		private int refCount = 0;
	}
	private class CountingBeanFactoryReference implements BeanFactoryReference {
		// groupContextRef 依赖 beanFactory
		private BeanFactory beanFactory;
		private BeanFactory groupContextRef;

		public CountingBeanFactoryReference(BeanFactory beanFactory, BeanFactory groupContext) {
			this.beanFactory = beanFactory;
			this.groupContextRef = groupContext;
		}

		// 返回依赖的BeanFactory
		public BeanFactory getFactory() {
			return this.beanFactory;
		}

		// Note that it's legal to call release more than once!
		public void release() throws FatalBeanException {
			synchronized (bfgInstancesByKey) {
				BeanFactory savedRef = this.groupContextRef;
				if (savedRef != null) {
					this.groupContextRef = null;
					BeanFactoryGroup bfg = bfgInstancesByObj.get(savedRef);
					if (bfg != null) {
						bfg.refCount--;
						if (bfg.refCount == 0) {
							destroyDefinition(savedRef, resourceLocation);
							bfgInstancesByKey.remove(resourceLocation);
							bfgInstancesByObj.remove(savedRef);
						}
					}
					else {
						// This should be impossible.
						logger.warn("Tried to release a SingletonBeanFactoryLocator group definition " +
								"more times than it has actually been used. Resource name [" + resourceLocation + "]");
					}
				}
			}
		}
	}

}
