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

package org.springframework.aop.target;

import java.io.Serializable;

import org.springframework.aop.TargetSource;
import org.springframework.util.ObjectUtils;

/**
 * Canonical {@code TargetSource} when there is no target
 * (or just the target class known), and behavior is supplied
 * by interfaces and advisors only.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class EmptyTargetSource implements TargetSource, Serializable {

	private static final long serialVersionUID = 3680494563553489691L;

	// 静态的工厂方法
	public static final EmptyTargetSource INSTANCE = new EmptyTargetSource(null, true);
	public static EmptyTargetSource forClass(Class targetClass) {
		return forClass(targetClass, true);
	}
	public static EmptyTargetSource forClass(Class targetClass, boolean isStatic) {
		return (targetClass == null && isStatic ? INSTANCE : new EmptyTargetSource(targetClass, isStatic));
	}


	// Instance implementation
	private final Class targetClass;
	private final boolean isStatic;

	// 创建一个新的 EmptyTargetSource 实例
	private EmptyTargetSource(Class targetClass, boolean isStatic) {
		this.targetClass = targetClass;
		this.isStatic = isStatic;
	}

	// Always returns the specified target Class, or {@code null} if none.
	public Class<?> getTargetClass() {
		return this.targetClass;
	}
	// Always returns {@code true}.
	public boolean isStatic() {
		return this.isStatic;
	}
	// Always returns {@code null}.
	public Object getTarget() {
		return null;
	}


	public void releaseTarget(Object target) {}

	// 在没有目标类的情况下，返回反序列化的规范实例，从而保护Singleton的模式
	private Object readResolve() {
		return (this.targetClass == null && this.isStatic ? INSTANCE : this);
	}



	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof EmptyTargetSource)) {
			return false;
		}
		EmptyTargetSource otherTs = (EmptyTargetSource) other;
		return (ObjectUtils.nullSafeEquals(this.targetClass, otherTs.targetClass) && this.isStatic == otherTs.isStatic);
	}
	@Override
	public int hashCode() {
		return EmptyTargetSource.class.hashCode() * 13 + ObjectUtils.nullSafeHashCode(this.targetClass);
	}
	@Override
	public String toString() {
		return "EmptyTargetSource: " +
				(this.targetClass != null ? "target class [" + this.targetClass.getName() + "]" : "no target class") +
				", " + (this.isStatic ? "static" : "dynamic");
	}

}
