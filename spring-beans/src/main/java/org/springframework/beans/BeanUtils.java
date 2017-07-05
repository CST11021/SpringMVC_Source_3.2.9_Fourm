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

package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Static convenience methods for JavaBeans: for instantiating beans,
 * checking bean property types, copying bean properties, etc.
 *
 * <p>Mainly for use within the framework, but to some degree also
 * useful for application classes.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 */
public abstract class BeanUtils {

	private static final Log logger = LogFactory.getLog(BeanUtils.class);

	// Effectively using a WeakHashMap as a Set
	private static final Map<Class<?>, Boolean> unknownEditorTypes =
			Collections.synchronizedMap(new WeakHashMap<Class<?>, Boolean>());


	// 使用clazz 的无参构造函数来实例化一个对象(这个构造器必须是public)
	public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface()) {
			throw new BeanInstantiationException(clazz, "Specified class is an interface");
		}
		try {
			return clazz.newInstance();
		}
		catch (InstantiationException ex) {
			throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
		}
	}

	// 使用clazz 的无参构造函数来实例化一个对象(这个构造器必须可以是private)
	public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface()) {
			throw new BeanInstantiationException(clazz, "Specified class is an interface");
		}
		try {
			// getDeclaredConstructor() 返回一个无参的构造器对象，可以是private的构造器
			return instantiateClass(clazz.getDeclaredConstructor());
		}
		catch (NoSuchMethodException ex) {
			throw new BeanInstantiationException(clazz, "No default constructor found", ex);
		}
	}

	// 判断 clazz 是否为 assignableTo 的对象，如果是的话，返回clazz实例
	@SuppressWarnings("unchecked")
	public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo) throws BeanInstantiationException {
		Assert.isAssignable(assignableTo, clazz);
		return (T) instantiateClass(clazz);
	}

	// 使用 Constructor对象和相应的参数构造一个实例
	public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
		Assert.notNull(ctor, "Constructor must not be null");
		try {
			ReflectionUtils.makeAccessible(ctor);
			return ctor.newInstance(args);
		}
		catch (InstantiationException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Is it an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Is the constructor accessible?", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Illegal arguments for constructor", ex);
		}
		catch (InvocationTargetException ex) {
			throw new BeanInstantiationException(ctor.getDeclaringClass(), "Constructor threw exception", ex.getTargetException());
		}
	}

	// 根据给予的方法名和参数信息返回一个Method对象
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(methodName, paramTypes);
		}
		catch (NoSuchMethodException ex) {
			return findDeclaredMethod(clazz, methodName, paramTypes);
		}
	}

	// 根据给予的方法名和参数信息返回一个Method对象(这个方法可以是private)
	public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getDeclaredMethod(methodName, paramTypes);
		}
		catch (NoSuchMethodException ex) {
			if (clazz.getSuperclass() != null) {
				return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
			}
			return null;
		}
	}

	// 根据给定方法名寻找最小参数（最好的情况是没有参数）的方法。
	public static Method findMethodWithMinimalParameters(Class<?> clazz, String methodName) throws IllegalArgumentException {

		Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
		if (targetMethod == null) {
			targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
		}
		return targetMethod;
	}

	// 根据给定方法名寻找最小参数（最好的情况是没有参数）的方法(这个方法可以是private)。
	public static Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName) throws IllegalArgumentException {

		Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
		if (targetMethod == null && clazz.getSuperclass() != null) {
			targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
		}
		return targetMethod;
	}

	// 在给定的方法列表中找到给定方法名和最小参数（最好的情况是没有参数）的方法。
	public static Method findMethodWithMinimalParameters(Method[] methods, String methodName) throws IllegalArgumentException {

		Method targetMethod = null;
		int numMethodsFoundWithCurrentMinimumArgs = 0;
		for (Method method : methods) {
			if (method.getName().equals(methodName)) {
				int numParams = method.getParameterTypes().length;
				if (targetMethod == null || numParams < targetMethod.getParameterTypes().length) {
					targetMethod = method;
					numMethodsFoundWithCurrentMinimumArgs = 1;
				}
				else {
					if (targetMethod.getParameterTypes().length == numParams) {
						// Additional candidate with same length
						numMethodsFoundWithCurrentMinimumArgs++;
					}
				}
			}
		}
		if (numMethodsFoundWithCurrentMinimumArgs > 1) {
			throw new IllegalArgumentException("Cannot resolve method '" + methodName +
					"' to a unique method. Attempted to resolve to overloaded method with " +
					"the least number of parameters, but there were " +
					numMethodsFoundWithCurrentMinimumArgs + " candidates.");
		}
		return targetMethod;
	}

	// 根据这个方法签名，查找对应的方法
	public static Method resolveSignature(String signature, Class<?> clazz) {
		Assert.hasText(signature, "'signature' must not be empty");
		Assert.notNull(clazz, "Class must not be null");
		int firstParen = signature.indexOf("(");
		int lastParen = signature.indexOf(")");
		if (firstParen > -1 && lastParen == -1) {
			throw new IllegalArgumentException("Invalid method signature '" + signature + "': expected closing ')' for args list");
		}
		else if (lastParen > -1 && firstParen == -1) {
			throw new IllegalArgumentException("Invalid method signature '" + signature + "': expected opening '(' for args list");
		}
		else if (firstParen == -1 && lastParen == -1) {
			return findMethodWithMinimalParameters(clazz, signature);
		}
		else {
			String methodName = signature.substring(0, firstParen);
			String[] parameterTypeNames = StringUtils.commaDelimitedListToStringArray(signature.substring(firstParen + 1, lastParen));
			Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
			for (int i = 0; i < parameterTypeNames.length; i++) {
				String parameterTypeName = parameterTypeNames[i].trim();
				try {
					parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
				}
				catch (Throwable ex) {
					throw new IllegalArgumentException("Invalid method signature: unable to resolve type [" +
							parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
				}
			}
			return findMethod(clazz, methodName, parameterTypes);
		}
	}


	// 获取clazz的所有属性描述器（可以从属性描述器中获取get和set方法，如：Method setMethod = propertyDescriptor.getWriteMethod();）
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getPropertyDescriptors();
	}

	// 获取指定属性的属性描述器
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws BeansException {

		CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
		return cr.getPropertyDescriptor(propertyName);
	}

	// 如果这个方法是get或set方法，则这个属性对应的属性描述器
	public static PropertyDescriptor findPropertyForMethod(Method method) throws BeansException {
		Assert.notNull(method, "Method must not be null");
		PropertyDescriptor[] pds = getPropertyDescriptors(method.getDeclaringClass());
		for (PropertyDescriptor pd : pds) {
			if (method.equals(pd.getReadMethod()) || method.equals(pd.getWriteMethod())) {
				return pd;
			}
		}
		return null;
	}

	// 返回targetType对应的属性编辑器，例如："mypackage.MyDomainClass" -> "mypackage.MyDomainClassEditor"
	public static PropertyEditor findEditorByConvention(Class<?> targetType) {
		if (targetType == null || targetType.isArray() || unknownEditorTypes.containsKey(targetType)) {
			return null;
		}
		ClassLoader cl = targetType.getClassLoader();
		if (cl == null) {
			try {
				cl = ClassLoader.getSystemClassLoader();
				if (cl == null) {
					return null;
				}
			}
			catch (Throwable ex) {
				// e.g. AccessControlException on Google App Engine
				if (logger.isDebugEnabled()) {
					logger.debug("Could not access system ClassLoader: " + ex);
				}
				return null;
			}
		}
		String editorName = targetType.getName() + "Editor";
		try {
			Class<?> editorClass = cl.loadClass(editorName);
			if (!PropertyEditor.class.isAssignableFrom(editorClass)) {
				if (logger.isWarnEnabled()) {
					logger.warn("Editor class [" + editorName +
							"] does not implement [java.beans.PropertyEditor] interface");
				}
				unknownEditorTypes.put(targetType, Boolean.TRUE);
				return null;
			}
			return (PropertyEditor) instantiateClass(editorClass);
		}
		catch (ClassNotFoundException ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("No property editor [" + editorName + "] found for type " +
						targetType.getName() + " according to 'Editor' suffix convention");
			}
			unknownEditorTypes.put(targetType, Boolean.TRUE);
			return null;
		}
	}

	// 根据属性名称返回对应的属性类型
	public static Class<?> findPropertyType(String propertyName, Class<?>... beanClasses) {
		if (beanClasses != null) {
			for (Class<?> beanClass : beanClasses) {
				PropertyDescriptor pd = getPropertyDescriptor(beanClass, propertyName);
				if (pd != null) {
					return pd.getPropertyType();
				}
			}
		}
		return Object.class;
	}

	// 获取这个属性描述器对应set方法的MethodParameter对象
	public static MethodParameter getWriteMethodParameter(PropertyDescriptor pd) {
		if (pd instanceof GenericTypeAwarePropertyDescriptor) {
			return new MethodParameter(((GenericTypeAwarePropertyDescriptor) pd).getWriteMethodParameter());
		}
		else {
			return new MethodParameter(pd.getWriteMethod(), 0);
		}
	}

	// 判断这个clazz是否为简单值类型
	public static boolean isSimpleProperty(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
	}

	// 判断这个clazz是否为简单值类型
	public static boolean isSimpleValueType(Class<?> clazz) {
		return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() ||
				CharSequence.class.isAssignableFrom(clazz) ||
				Number.class.isAssignableFrom(clazz) ||
				Date.class.isAssignableFrom(clazz) ||
				clazz.equals(URI.class) || clazz.equals(URL.class) ||
				clazz.equals(Locale.class) || clazz.equals(Class.class);
	}


	// 将source的属性拷贝到target对象中
	public static void copyProperties(Object source, Object target) throws BeansException {
		copyProperties(source, target, null, (String[]) null);
	}
	public static void copyProperties(Object source, Object target, Class<?> editable) throws BeansException {
		copyProperties(source, target, editable, (String[]) null);
	}

	// 将source的属性拷贝到target对象中,但忽略ignoreProperties包含的属性
	public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
		copyProperties(source, target, null, ignoreProperties);
	}
	private static void copyProperties(Object source, Object target, Class<?> editable, String... ignoreProperties) throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Class<?> actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() + "] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;

		for (PropertyDescriptor targetPd : targetPds) {
			Method writeMethod = targetPd.getWriteMethod();
			if (writeMethod != null && (ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null) {
					Method readMethod = sourcePd.getReadMethod();
					if (readMethod != null &&
							ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
						try {
							if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
								readMethod.setAccessible(true);
							}
							Object value = readMethod.invoke(source);
							if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
								writeMethod.setAccessible(true);
							}
							writeMethod.invoke(target, value);
						}
						catch (Throwable ex) {
							throw new FatalBeanException(
									"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
						}
					}
				}
			}
		}
	}

}
