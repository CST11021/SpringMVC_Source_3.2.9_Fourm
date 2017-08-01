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

package org.springframework.aop.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.IntroductionInfo;
import org.springframework.util.ClassUtils;

/**
 * Support for implementations of {@link org.springframework.aop.IntroductionInfo}.
 *
 * <p>Allows subclasses to conveniently add all interfaces from a given object,
 * and to suppress interfaces that should not be added. Also allows for querying
 * all introduced interfaces.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
@SuppressWarnings("serial")
public class IntroductionInfoSupport implements IntroductionInfo, Serializable {

	protected final Set<Class> publishedInterfaces = new HashSet<Class>();
	// 如果 键值对应的 Method 是 publishedInterfaces 中的接口方法，则value对应的值为true
	private transient Map<Method, Boolean> rememberedMethods = new ConcurrentHashMap<Method, Boolean>(32);

	// 从 this.publishedInterfaces 移除 intf 接口
	public void suppressInterface(Class intf) {
		this.publishedInterfaces.remove(intf);
	}
	// 返回 this.publishedInterfaces
	public Class[] getInterfaces() {
		return this.publishedInterfaces.toArray(new Class[this.publishedInterfaces.size()]);
	}
	// 判断 this.publishedInterfaces 中是否包含指定的ifc接口
	public boolean implementsInterface(Class ifc) {
		for (Class pubIfc : this.publishedInterfaces) {
			if (ifc.isInterface() && ifc.isAssignableFrom(pubIfc)) {
				return true;
			}
		}
		return false;
	}
	// 将delegate中所实现的所有接口添加到this.publishedInterfaces中
	protected void implementInterfacesOnObject(Object delegate) {
		this.publishedInterfaces.addAll(ClassUtils.getAllInterfacesAsSet(delegate));
	}
	// 判断 mi 方法是否为 this.publishedInterfaces 中的接口方法
	protected final boolean isMethodOnIntroducedInterface(MethodInvocation mi) {
		Boolean rememberedResult = this.rememberedMethods.get(mi.getMethod());
		if (rememberedResult != null) {
			return rememberedResult;
		}
		else {
			// Work it out and cache it.
			boolean result = implementsInterface(mi.getMethod().getDeclaringClass());
			this.rememberedMethods.put(mi.getMethod(), result);
			return result;
		}
	}



	// Serialization support
	/**
	 * This method is implemented only to restore the logger.
	 * We don't make the logger static as that would mean that subclasses
	 * would use this class's log category.
	 */
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		// Rely on default serialization; just initialize state after deserialization.
		ois.defaultReadObject();
		// Initialize transient fields.
		this.rememberedMethods = new ConcurrentHashMap<Method, Boolean>(32);
	}

}
