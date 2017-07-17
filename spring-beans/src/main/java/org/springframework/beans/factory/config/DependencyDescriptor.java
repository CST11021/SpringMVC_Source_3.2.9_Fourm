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

package org.springframework.beans.factory.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.Assert;

/**
 * Descriptor for a specific dependency that is about to be injected.
 * 描述一个将要被注入的特定依赖的描述符。
 * Wraps a constructor parameter, a method parameter or a field, allowing unified access to their metadata.
 * 封装构造函数参数、方法参数或字段，允许对其元数据进行统一访问。
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class DependencyDescriptor implements Serializable {

	private transient MethodParameter methodParameter;
	private transient Field field;
	private Class<?> declaringClass;
	private String methodName;
	private Class[] parameterTypes;
	private int parameterIndex;
	private String fieldName;
	private final boolean required;
	private final boolean eager;
	private int nestingLevel = 1;
	private transient Annotation[] fieldAnnotations;

	// 构造器
	public DependencyDescriptor(MethodParameter methodParameter, boolean required) {
		this(methodParameter, required, true);
	}
	public DependencyDescriptor(MethodParameter methodParameter, boolean required, boolean eager) {
		Assert.notNull(methodParameter, "MethodParameter must not be null");
		this.methodParameter = methodParameter;
		this.declaringClass = methodParameter.getDeclaringClass();
		if (this.methodParameter.getMethod() != null) {
			this.methodName = methodParameter.getMethod().getName();
			this.parameterTypes = methodParameter.getMethod().getParameterTypes();
		}
		else {
			this.parameterTypes = methodParameter.getConstructor().getParameterTypes();
		}
		this.parameterIndex = methodParameter.getParameterIndex();
		this.required = required;
		this.eager = eager;
	}
	public DependencyDescriptor(Field field, boolean required) {
		this(field, required, true);
	}
	public DependencyDescriptor(Field field, boolean required, boolean eager) {
		Assert.notNull(field, "Field must not be null");
		this.field = field;
		this.declaringClass = field.getDeclaringClass();
		this.fieldName = field.getName();
		this.required = required;
		this.eager = eager;
	}
	public DependencyDescriptor(DependencyDescriptor original) {
		this.methodParameter = (original.methodParameter != null ? new MethodParameter(original.methodParameter) : null);
		this.field = original.field;
		this.declaringClass = original.declaringClass;
		this.methodName = original.methodName;
		this.parameterTypes = original.parameterTypes;
		this.parameterIndex = original.parameterIndex;
		this.fieldName = original.fieldName;
		this.required = original.required;
		this.eager = original.eager;
		this.nestingLevel = original.nestingLevel;
		this.fieldAnnotations = original.fieldAnnotations;
	}


	// 增加这个描述符的嵌套级别。
	public void increaseNestingLevel() {
		this.nestingLevel++;
		if (this.methodParameter != null) {
			this.methodParameter.increaseNestingLevel();
		}
	}

	// 为这个 this.methodParameter 的 parameterNameDiscoverer 属性赋值
	public void initParameterNameDiscovery(ParameterNameDiscoverer parameterNameDiscoverer) {
		if (this.methodParameter != null) {
			// 为这个methodParameter的parameterNameDiscoverer属性赋值
			this.methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
		}
	}

	// 返回包装参数/字段的名称
	public String getDependencyName() {
		return (this.field != null ? this.field.getName() : this.methodParameter.getParameterName());
	}

	// 返回包装参数/字段的声明(非泛型)类型
	public Class<?> getDependencyType() {
		if (this.field != null) {
			if (this.nestingLevel > 1) {
				Type type = this.field.getGenericType();
				if (type instanceof ParameterizedType) {
					Type arg = ((ParameterizedType) type).getActualTypeArguments()[0];
					if (arg instanceof Class) {
						return (Class) arg;
					}
					else if (arg instanceof ParameterizedType) {
						arg = ((ParameterizedType) arg).getRawType();
						if (arg instanceof Class) {
							return (Class) arg;
						}
					}
				}
				return Object.class;
			}
			else {
				return this.field.getType();
			}
		}
		else {
			return this.methodParameter.getNestedParameterType();
		}
	}

	// 返回封装的集合参数/字段的通用元素类型
	public Class<?> getCollectionType() {
		return (this.field != null ?
				GenericCollectionTypeResolver.getCollectionFieldType(this.field, this.nestingLevel) :
				GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter));
	}

	// 返回包装映射参数/字段的通用键类型
	public Class<?> getMapKeyType() {
		return (this.field != null ?
				GenericCollectionTypeResolver.getMapKeyFieldType(this.field, this.nestingLevel) :
				GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter));
	}
	// 返回包装映射参数/字段的通用值类型
	public Class<?> getMapValueType() {
		return (this.field != null ?
				GenericCollectionTypeResolver.getMapValueFieldType(this.field, this.nestingLevel) :
				GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter));
	}

	// 获取与包装参数/字段相关的注解
	public Annotation[] getAnnotations() {
		if (this.field != null) {
			if (this.fieldAnnotations == null) {
				this.fieldAnnotations = this.field.getAnnotations();
			}
			return this.fieldAnnotations;
		}
		else {
			return this.methodParameter.getParameterAnnotations();
		}
	}


	// Serialization support
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization; just initialize state after deserialization.
		ois.defaultReadObject();

		// Restore reflective handles (which are unfortunately not serializable)
		try {
			if (this.fieldName != null) {
				this.field = this.declaringClass.getDeclaredField(this.fieldName);
			}
			else {
				if (this.methodName != null) {
					this.methodParameter = new MethodParameter(
							this.declaringClass.getDeclaredMethod(this.methodName, this.parameterTypes), this.parameterIndex);
				}
				else {
					this.methodParameter = new MethodParameter(
							this.declaringClass.getDeclaredConstructor(this.parameterTypes), this.parameterIndex);
				}
				for (int i = 1; i < this.nestingLevel; i++) {
					this.methodParameter.increaseNestingLevel();
				}
			}
		}
		catch (Throwable ex) {
			throw new IllegalStateException("Could not find original class structure", ex);
		}
	}



	// getter and setter ...
	public MethodParameter getMethodParameter() {
		return this.methodParameter;
	}
	public Field getField() {
		return this.field;
	}
	public boolean isRequired() {
		return this.required;
	}
	public boolean isEager() {
		return this.eager;
	}
}
