/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ReflectionUtils;

/**
 * Internal class for managing injection metadata. Not intended for direct use in applications.
 * 管理注入元数据的内部类。不打算在应用程序中直接使用。
 * <p>Used by {@link AutowiredAnnotationBeanPostProcessor},
 * {@link org.springframework.context.annotation.CommonAnnotationBeanPostProcessor} and
 * {@link org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor}.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
// 封装一个bean对象和一系列被自动装配注解修饰的对象，该类主要用于将类似@Autowire修饰的对象注入到Bean中
public class InjectionMetadata {

	private final Log logger = LogFactory.getLog(InjectionMetadata.class);
	// 表示一个Bean对象类型
	private final Class<?> targetClass;
	// 表示一系列要被注入的属性
	private final Collection<InjectedElement> injectedElements;
	private volatile Set<InjectedElement> checkedElements;

	// 构造器
	public InjectionMetadata(Class<?> targetClass, Collection<InjectedElement> elements) {
		this.targetClass = targetClass;
		this.injectedElements = elements;
	}

	// 将所有配置属性（比如通过@Autowire注入的属性）保存到RootBeanDefinition#externallyManagedConfigMembers
	public void checkConfigMembers(RootBeanDefinition beanDefinition) {
		Set<InjectedElement> checkedElements = new LinkedHashSet<InjectedElement>(this.injectedElements.size());
		for (InjectedElement element : this.injectedElements) {
			Member member = element.getMember();
			if (!beanDefinition.isExternallyManagedConfigMember(member)) {
				beanDefinition.registerExternallyManagedConfigMember(member);
				checkedElements.add(element);
				if (logger.isDebugEnabled()) {
					logger.debug("Registered injected element on class [" + this.targetClass.getName() + "]: " + element);
				}
			}
		}
		this.checkedElements = checkedElements;
	}
	// 将所有@Autowire注解修饰的属性注入到Bean
	public void inject(Object target, String beanName, PropertyValues pvs) throws Throwable {
		Collection<InjectedElement> elementsToIterate =
				(this.checkedElements != null ? this.checkedElements : this.injectedElements);
		if (!elementsToIterate.isEmpty()) {
			boolean debug = logger.isDebugEnabled();
			for (InjectedElement element : elementsToIterate) {
				if (debug) {
					logger.debug("Processing injected method of bean '" + beanName + "': " + element);
				}
				element.inject(target, beanName, pvs);
			}
		}
	}

	// 判断指定的metadata是否为空，如果是空的，或者和当前对象的类型不一样返回true
	public static boolean needsRefresh(InjectionMetadata metadata, Class<?> clazz) {
		return (metadata == null || !metadata.targetClass.equals(clazz));
	}

	// 封装一个成员对象，这个成员对象（可能是属性也可能是方法）是用自动装配的注解修饰的，比如：@Autowire、@Inject
	public static abstract class InjectedElement {
		// 被自动装配注解修饰的对象
		protected final Member member;
		// 判断该对象是否是一个属性
		protected final boolean isField;
		// java.beans.PropertyDescriptor
		protected final PropertyDescriptor pd;
		protected volatile Boolean skip;

		// 构造器
		protected InjectedElement(Member member, PropertyDescriptor pd) {
			this.member = member;
			this.isField = (member instanceof Field);
			this.pd = pd;
		}

		public final Member getMember() {
			return this.member;
		}
		protected final Class<?> getResourceType() {
			if (this.isField) {
				return ((Field) this.member).getType();
			}
			else if (this.pd != null) {
				return this.pd.getPropertyType();
			}
			else {
				return ((Method) this.member).getParameterTypes()[0];
			}
		}
		protected final void checkResourceType(Class<?> resourceType) {
			if (this.isField) {
				Class<?> fieldType = ((Field) this.member).getType();
				if (!(resourceType.isAssignableFrom(fieldType) || fieldType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException("Specified field type [" + fieldType + "] is incompatible with resource type [" + resourceType.getName() + "]");
				}
			}
			else {
				Class<?> paramType =
						(this.pd != null ? this.pd.getPropertyType() : ((Method) this.member).getParameterTypes()[0]);
				if (!(resourceType.isAssignableFrom(paramType) || paramType.isAssignableFrom(resourceType))) {
					throw new IllegalStateException("Specified parameter type [" + paramType + "] is incompatible with resource type [" + resourceType.getName() + "]");
				}
			}
		}
		/** Either this or {@link #getResourceToInject} needs to be overridden. */
		protected void inject(Object target, String requestingBeanName, PropertyValues pvs) throws Throwable {
			if (this.isField) {
				Field field = (Field) this.member;
				ReflectionUtils.makeAccessible(field);
				field.set(target, getResourceToInject(target, requestingBeanName));
			}
			else {
				if (checkPropertySkipping(pvs)) {
					return;
				}
				try {
					Method method = (Method) this.member;
					ReflectionUtils.makeAccessible(method);
					method.invoke(target, getResourceToInject(target, requestingBeanName));
				}
				catch (InvocationTargetException ex) {
					throw ex.getTargetException();
				}
			}
		}
		/** Either this or {@link #inject} needs to be overridden. */
		protected Object getResourceToInject(Object target, String requestingBeanName) {
			return null;
		}
		/**
		 * Checks whether this injector's property needs to be skipped due to
		 * an explicit property value having been specified. Also marks the
		 * affected property as processed for other processors to ignore it.
		 */
		protected boolean checkPropertySkipping(PropertyValues pvs) {
			if (this.skip != null) {
				return this.skip;
			}
			if (pvs == null) {
				this.skip = false;
				return false;
			}
			synchronized (pvs) {
				if (this.skip != null) {
					return this.skip;
				}
				if (this.pd != null) {
					if (pvs.contains(this.pd.getName())) {
						// Explicit value provided as part of the bean definition.
						this.skip = true;
						return true;
					}
					else if (pvs instanceof MutablePropertyValues) {
						((MutablePropertyValues) pvs).registerProcessedProperty(this.pd.getName());
					}
				}
				this.skip = false;
				return false;
			}
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof InjectedElement)) {
				return false;
			}
			InjectedElement otherElement = (InjectedElement) other;
			return this.member.equals(otherElement.member);
		}
		@Override
		public int hashCode() {
			return this.member.getClass().hashCode() * 29 + this.member.getName().hashCode();
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + " for " + this.member;
		}
	}

}
