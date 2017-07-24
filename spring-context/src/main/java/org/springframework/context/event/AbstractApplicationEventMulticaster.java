/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.context.event;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.OrderComparator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Abstract implementation of the {@link ApplicationEventMulticaster} interface,
 * providing the basic listener registration facility.
 *
 * <p>Doesn't permit multiple instances of the same listener by default,
 * as it keeps listeners in a linked Set. The collection class used to hold
 * ApplicationListener objects can be overridden through the "collectionClass"
 * bean property.
 *
 * <p>Implementing ApplicationEventMulticaster's actual {@link #multicastEvent} method
 * is left to subclasses. {@link SimpleApplicationEventMulticaster} simply multicasts
 * all events to all registered listeners, invoking them in the calling thread.
 * Alternative implementations could be more sophisticated in those respects.
 *
 * @author Juergen Hoeller
 * @since 1.2.3
 * @see #getApplicationListeners(ApplicationEvent)
 * @see SimpleApplicationEventMulticaster
 */
// AbstractApplicationEventMulticaster 实现 ApplicationEventMulticaster 用于管理Spring中的所有监听器，这里的监听器分为两种类型，
// 它们分别是注册为bean形式的监听器，和非bean形式的监听器
public abstract class AbstractApplicationEventMulticaster implements ApplicationEventMulticaster, BeanClassLoaderAware, BeanFactoryAware {

	private BeanFactory beanFactory;
	private ClassLoader beanClassLoader;
	// Spring中监听器的注册表
	private final ListenerRetriever defaultRetriever = new ListenerRetriever(false);
	private final Map<ListenerCacheKey, ListenerRetriever> retrieverCache = new ConcurrentHashMap<ListenerCacheKey, ListenerRetriever>(64);


	public void addApplicationListener(ApplicationListener listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.add(listener);
			this.retrieverCache.clear();
		}
	}
	public void addApplicationListenerBean(String listenerBeanName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.add(listenerBeanName);
			this.retrieverCache.clear();
		}
	}

	public void removeApplicationListener(ApplicationListener listener) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.remove(listener);
			this.retrieverCache.clear();
		}
	}
	public void removeApplicationListenerBean(String listenerBeanName) {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListenerBeans.remove(listenerBeanName);
			this.retrieverCache.clear();
		}
	}
	public void removeAllListeners() {
		synchronized (this.defaultRetriever) {
			this.defaultRetriever.applicationListeners.clear();
			this.defaultRetriever.applicationListenerBeans.clear();
			this.retrieverCache.clear();
		}
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (this.beanClassLoader == null && beanFactory instanceof ConfigurableBeanFactory) {
			this.beanClassLoader = ((ConfigurableBeanFactory) beanFactory).getBeanClassLoader();
		}
	}
	private BeanFactory getBeanFactory() {
		if (this.beanFactory == null) {
			throw new IllegalStateException("ApplicationEventMulticaster cannot retrieve listener beans " +
					"because it is not associated with a BeanFactory");
		}
		return this.beanFactory;
	}


	// 返回所有已注册的监听器
	protected Collection<ApplicationListener> getApplicationListeners() {
		synchronized (this.defaultRetriever) {
			return this.defaultRetriever.getApplicationListeners();
		}
	}


	// 根据event 的事件类型和源类型来获取监听器
	protected Collection<ApplicationListener> getApplicationListeners(ApplicationEvent event) {
		Class<? extends ApplicationEvent> eventType = event.getClass();
		Object source = event.getSource();
		Class<?> sourceType = (source != null ? source.getClass() : null);
		ListenerCacheKey cacheKey = new ListenerCacheKey(eventType, sourceType);
		ListenerRetriever retriever = this.retrieverCache.get(cacheKey);
		if (retriever != null) {
			return retriever.getApplicationListeners();
		}
		else {
			retriever = new ListenerRetriever(true);
			LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
			Set<ApplicationListener> listeners;
			Set<String> listenerBeans;
			synchronized (this.defaultRetriever) {
				listeners = new LinkedHashSet<ApplicationListener>(this.defaultRetriever.applicationListeners);
				listenerBeans = new LinkedHashSet<String>(this.defaultRetriever.applicationListenerBeans);
			}
			for (ApplicationListener listener : listeners) {
				if (supportsEvent(listener, eventType, sourceType)) {
					retriever.applicationListeners.add(listener);
					allListeners.add(listener);
				}
			}
			if (!listenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : listenerBeans) {
					ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
					if (!allListeners.contains(listener) && supportsEvent(listener, eventType, sourceType)) {
						retriever.applicationListenerBeans.add(listenerBeanName);
						allListeners.add(listener);
					}
				}
			}
			OrderComparator.sort(allListeners);
			if (this.beanClassLoader == null ||
					(ClassUtils.isCacheSafe(eventType, this.beanClassLoader) && (sourceType == null || ClassUtils.isCacheSafe(sourceType, this.beanClassLoader)))) {
				this.retrieverCache.put(cacheKey, retriever);
			}
			return allListeners;
		}
	}

	// 判断给定的监听器是否支持给定的事件
	protected boolean supportsEvent(ApplicationListener listener, Class<? extends ApplicationEvent> eventType, Class sourceType) {
		SmartApplicationListener smartListener = (listener instanceof SmartApplicationListener ?
				(SmartApplicationListener) listener : new GenericApplicationListenerAdapter(listener));
		return (smartListener.supportsEventType(eventType) && smartListener.supportsSourceType(sourceType));
	}


	// 作为ListenerRetriever的缓存键，它封装了事件类型和源类型
	private static class ListenerCacheKey {

		private final Class<?> eventType;
		private final Class<?> sourceType;

		public ListenerCacheKey(Class<?> eventType, Class<?> sourceType) {
			this.eventType = eventType;
			this.sourceType = sourceType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			ListenerCacheKey otherKey = (ListenerCacheKey) other;
			return ObjectUtils.nullSafeEquals(this.eventType, otherKey.eventType) &&
					ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType);
		}
		@Override
		public int hashCode() {
			return ObjectUtils.nullSafeHashCode(this.eventType) * 29 + ObjectUtils.nullSafeHashCode(this.sourceType);
		}
	}

	// 一个帮助类，封装了目标监听器的特定集合，它对bean形式和非bean形式的监听器分开进行管理
	private class ListenerRetriever {
		// 用于存放Spring中所有注册的监听器
		public final Set<ApplicationListener> applicationListeners;
		// 用于存放Spring中所有的监听器Bean（有时候监听器也会被声明为bean）
		public final Set<String> applicationListenerBeans;
		// 标识是否允许重复添加 applicationListenerBean
		private final boolean preFiltered;

		public ListenerRetriever(boolean preFiltered) {
			this.applicationListeners = new LinkedHashSet<ApplicationListener>();
			this.applicationListenerBeans = new LinkedHashSet<String>();
			this.preFiltered = preFiltered;
		}

		// 返回所有已经注册的监听器
		public Collection<ApplicationListener> getApplicationListeners() {
			LinkedList<ApplicationListener> allListeners = new LinkedList<ApplicationListener>();
			for (ApplicationListener listener : this.applicationListeners) {
				allListeners.add(listener);
			}
			if (!this.applicationListenerBeans.isEmpty()) {
				BeanFactory beanFactory = getBeanFactory();
				for (String listenerBeanName : this.applicationListenerBeans) {
					ApplicationListener listener = beanFactory.getBean(listenerBeanName, ApplicationListener.class);
					if (this.preFiltered || !allListeners.contains(listener)) {
						allListeners.add(listener);
					}
				}
			}
			OrderComparator.sort(allListeners);
			return allListeners;
		}
	}

}
