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

package org.springframework.beans.factory.support;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Utility class that contains various methods useful for the implementation of autowire-capable bean factories.
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Sam Brannen
 * @since 1.1.2
 * @see AbstractAutowireCapableBeanFactory
 */
abstract class AutowireUtils {

	// 给这些构造函数排序：public优先参数数量降序，然后是非public参数数量降序
	public static void sortConstructors(Constructor<?>[] constructors) {
		Arrays.sort(constructors, new Comparator<Constructor<?>>() {
			public int compare(Constructor<?> c1, Constructor<?> c2) {
				boolean p1 = Modifier.isPublic(c1.getModifiers());
				boolean p2 = Modifier.isPublic(c2.getModifiers());
				if (p1 != p2) {
					return (p1 ? -1 : 1);
				}
				int c1pl = c1.getParameterTypes().length;
				int c2pl = c2.getParameterTypes().length;
				return (new Integer(c1pl)).compareTo(c2pl) * -1;
			}
		});
	}

	// 给这些方法排序：public优先参数数量降序，然后是非public参数数量降序
	public static void sortFactoryMethods(Method[] factoryMethods) {
		Arrays.sort(factoryMethods, new Comparator<Method>() {
			public int compare(Method fm1, Method fm2) {
				boolean p1 = Modifier.isPublic(fm1.getModifiers());
				boolean p2 = Modifier.isPublic(fm2.getModifiers());
				if (p1 != p2) {
					return (p1 ? -1 : 1);
				}
				int c1pl = fm1.getParameterTypes().length;
				int c2pl = fm2.getParameterTypes().length;
				return (new Integer(c1pl)).compareTo(c2pl) * -1;
			}
		});
	}

	// 确定给定的bean属性是否被排除在依赖项检查之外。这个实现不包括CGLIB定义的属性。
	public static boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		// 如果该属性不可写，则返回false
		Method wm = pd.getWriteMethod();
		if (wm == null) {
			return false;
		}
		// 这个属性的set方法，不是一个由CGLIB生成的方法返回false
		if (!wm.getDeclaringClass().getName().contains("$$")) {
			return false;
		}

		// wm方法是由CGLIB声明的，但是如果他是在父类中定义的，我们仍然可以使用autowire注入
		Class<?> superclass = wm.getDeclaringClass().getSuperclass();
		// 如果该wm所在类的父类没有定义该方法，则返回true
		return !ClassUtils.hasMethod(superclass, wm.getName(), wm.getParameterTypes());
	}

	// 判断这个属性的set方法是否被定义在接口中
	public static boolean isSetterDefinedInInterface(PropertyDescriptor pd, Set<Class<?>> interfaces) {
		Method setter = pd.getWriteMethod();
		if (setter != null) {
			Class<?> targetClass = setter.getDeclaringClass();
			for (Class<?> ifc : interfaces) {
				if (ifc.isAssignableFrom(targetClass) &&
						ClassUtils.hasMethod(ifc, setter.getName(), setter.getParameterTypes())) {
					return true;
				}
			}
		}
		return false;
	}

	// 将这个autowiringValue 解析为对应的requiredType 类型返回
	public static Object resolveAutowiringValue(Object autowiringValue, Class<?> requiredType) {
		if (autowiringValue instanceof ObjectFactory && !requiredType.isInstance(autowiringValue)) {
			ObjectFactory<?> factory = (ObjectFactory<?>) autowiringValue;
			if (autowiringValue instanceof Serializable && requiredType.isInterface()) {
				autowiringValue = Proxy.newProxyInstance(requiredType.getClassLoader(), new Class<?>[] {requiredType}, new ObjectFactoryDelegatingInvocationHandler(factory));
			}
			else {
				return factory.getObject();
			}
		}
		return autowiringValue;
	}

	// 返回这个方法的返回类型
	public static Class<?> resolveReturnTypeForFactoryMethod(Method method, Object[] args, ClassLoader classLoader) {
		Assert.notNull(method, "Method must not be null");
		Assert.notNull(args, "Argument array must not be null");
		Assert.notNull(classLoader, "ClassLoader must not be null");

		// 获取method方法的参数类型对象数组
		TypeVariable<Method>[] declaredTypeVariables = method.getTypeParameters();
		// 获取mothod方法返回类型的type对象
		Type genericReturnType = method.getGenericReturnType();
		// 获取method方法的参数类型对象数组
		Type[] methodParameterTypes = method.getGenericParameterTypes();
		Assert.isTrue(args.length == methodParameterTypes.length, "Argument array does not match parameter count");

		// Ensure that the type variable (e.g., T) is declared directly on the method itself (e.g., via <T>), not on the enclosing class or interface.
		// 确保类型变量(例如:T)是直接在方法本身上声明的(例如:via <T>)，而不是在封闭的类或接口上
		boolean locallyDeclaredTypeVariableMatchesReturnType = false;
		for (TypeVariable<Method> currentTypeVariable : declaredTypeVariables) {
			if (currentTypeVariable.equals(genericReturnType)) {
				locallyDeclaredTypeVariableMatchesReturnType = true;
				break;
			}
		}

		if (locallyDeclaredTypeVariableMatchesReturnType) {
			for (int i = 0; i < methodParameterTypes.length; i++) {
				Type methodParameterType = methodParameterTypes[i];
				Object arg = args[i];
				if (methodParameterType.equals(genericReturnType)) {
					if (arg instanceof TypedStringValue) {
						TypedStringValue typedValue = ((TypedStringValue) arg);
						if (typedValue.hasTargetType()) {
							return typedValue.getTargetType();
						}
						try {
							return typedValue.resolveTargetType(classLoader);
						}
						catch (ClassNotFoundException ex) {
							throw new IllegalStateException("Failed to resolve value type [" + typedValue.getTargetTypeName() + "] for factory method argument", ex);
						}
					}
					// Only consider argument type if it is a simple value...
					if (arg != null && !(arg instanceof BeanMetadataElement)) {
						return arg.getClass();
					}
					return method.getReturnType();
				}
				else if (methodParameterType instanceof ParameterizedType) {
					ParameterizedType parameterizedType = (ParameterizedType) methodParameterType;
					Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
					for (Type typeArg : actualTypeArguments) {
						if (typeArg.equals(genericReturnType)) {
							if (arg instanceof Class) {
								return (Class<?>) arg;
							}
							else {
								String className = null;
								if (arg instanceof String) {
									className = (String) arg;
								}
								else if (arg instanceof TypedStringValue) {
									TypedStringValue typedValue = ((TypedStringValue) arg);
									String targetTypeName = typedValue.getTargetTypeName();
									if (targetTypeName == null || Class.class.getName().equals(targetTypeName)) {
										className = typedValue.getValue();
									}
								}
								if (className != null) {
									try {
										return ClassUtils.forName(className, classLoader);
									}
									catch (ClassNotFoundException ex) {
										throw new IllegalStateException("Could not resolve class name [" + arg +
												"] for factory method argument", ex);
									}
								}
								// Consider adding logic to determine the class of the typeArg, if possible.
								// For now, just fall back...
								return method.getReturnType();
							}
						}
					}
				}
			}
		}

		// Fall back...
		return method.getReturnType();
	}


	// Reflective InvocationHandler for lazy access to the current target object.
	@SuppressWarnings("serial")
	private static class ObjectFactoryDelegatingInvocationHandler implements InvocationHandler, Serializable {

		private final ObjectFactory<?> objectFactory;

		public ObjectFactoryDelegatingInvocationHandler(ObjectFactory<?> objectFactory) {
			this.objectFactory = objectFactory;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();
			if (methodName.equals("equals")) {
				// Only consider equal when proxies are identical.
				return (proxy == args[0]);
			}
			else if (methodName.equals("hashCode")) {
				// Use hashCode of proxy.
				return System.identityHashCode(proxy);
			}
			else if (methodName.equals("toString")) {
				return this.objectFactory.toString();
			}
			try {
				return method.invoke(this.objectFactory.getObject(), args);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}

}
