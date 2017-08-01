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

package org.springframework.util;

import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Miscellaneous class utility methods.
 * Mainly for internal use within the framework.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rob Harrop
 * @author Sam Brannen
 * @since 1.1
 * @see TypeUtils
 * @see ReflectionUtils
 */
public abstract class ClassUtils {
	// Suffix for array class names: "[]"
	public static final String ARRAY_SUFFIX = "[]";
	// Prefix for internal array class names: "["
	private static final String INTERNAL_ARRAY_PREFIX = "[";
	// Prefix for internal non-primitive array class names: "[L"
	private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";
	// The package separator character '.'
	private static final char PACKAGE_SEPARATOR = '.';
	// 内部类生成的class会包含一个$
	private static final char INNER_CLASS_SEPARATOR = '$';
	// CGLIB生成的代理类会包含一个$$
	public static final String CGLIB_CLASS_SEPARATOR = "$$";
	// The ".class" file suffix
	public static final String CLASS_FILE_SUFFIX = ".class";

	// 原始类型对象到相应类型的映射，如：Integer.class -> int.class.
	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(8);
	// for example: int.class -> Integer.class.
	private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new HashMap<Class<?>, Class<?>>(8);

	// "int" -> "int.class".
	private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<String, Class<?>>(32);
	// “java.lang”包下类名到相应的Class 映射
	private static final Map<String, Class<?>> commonClassCache = new HashMap<String, Class<?>>(32);

	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);

		for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
			primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
			registerCommonClasses(entry.getKey());
		}

		Set<Class<?>> primitiveTypes = new HashSet<Class<?>>(32);
		primitiveTypes.addAll(primitiveWrapperTypeMap.values());
		primitiveTypes.addAll(Arrays.asList(new Class<?>[] {
				boolean[].class, byte[].class, char[].class, double[].class,
				float[].class, int[].class, long[].class, short[].class}));
		primitiveTypes.add(void.class);
		for (Class<?> primitiveType : primitiveTypes) {
			primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
		}

		registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class,
				Float[].class, Integer[].class, Long[].class, Short[].class);
		registerCommonClasses(Number.class, Number[].class, String.class, String[].class,
				Object.class, Object[].class, Class.class, Class[].class);
		registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class,
				Error.class, StackTraceElement.class, StackTraceElement[].class);
	}


	/**
	 * Register the given common classes with the ClassUtils cache.
	 */
	private static void registerCommonClasses(Class<?>... commonClasses) {
		for (Class<?> clazz : commonClasses) {
			commonClassCache.put(clazz.getName(), clazz);
		}
	}

	// 获取这个ClassUtils的类加载器
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (Throwable ex) {
			// Cannot access thread context ClassLoader - falling back...
		}
		if (cl == null) {
			// No thread context class loader -> use class loader of this class.
			cl = ClassUtils.class.getClassLoader();
			if (cl == null) {
				// getClassLoader() returning null indicates the bootstrap ClassLoader
				try {
					cl = ClassLoader.getSystemClassLoader();
				}
				catch (Throwable ex) {
					// Cannot access system ClassLoader - oh well, maybe the caller can live with null...
				}
			}
		}
		return cl;
	}
	// 使用classLoaderToUse作用类加载器（覆盖原有默认的加载器）
	public static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
		Thread currentThread = Thread.currentThread();
		ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
		if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
			currentThread.setContextClassLoader(classLoaderToUse);
			return threadContextClassLoader;
		}
		else {
			return null;
		}
	}


	@Deprecated
	public static Class<?> forName(String name) throws ClassNotFoundException, LinkageError {
		return forName(name, getDefaultClassLoader());
	}
	// 代替 Class.forName()，使用指定类加载器创建一个实例
	public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {
		Assert.notNull(name, "Name must not be null");

		Class<?> clazz = resolvePrimitiveClassName(name);
		if (clazz == null) {
			clazz = commonClassCache.get(name);
		}
		if (clazz != null) {
			return clazz;
		}

		// "java.lang.String[]" style arrays
		if (name.endsWith(ARRAY_SUFFIX)) {
			String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
			Class<?> elementClass = forName(elementClassName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		// "[Ljava.lang.String;" style arrays
		if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
			String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
			Class<?> elementClass = forName(elementName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		// "[[I" or "[[Ljava.lang.String;" style arrays
		if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
			String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
			Class<?> elementClass = forName(elementName, classLoader);
			return Array.newInstance(elementClass, 0).getClass();
		}

		ClassLoader clToUse = classLoader;
		if (clToUse == null) {
			clToUse = getDefaultClassLoader();
		}
		try {
			return (clToUse != null ? clToUse.loadClass(name) : Class.forName(name));
		}
		catch (ClassNotFoundException ex) {
			int lastDotIndex = name.lastIndexOf('.');
			if (lastDotIndex != -1) {
				String innerClassName = name.substring(0, lastDotIndex) + '$' + name.substring(lastDotIndex + 1);
				try {
					return (clToUse != null ? clToUse.loadClass(innerClassName) : Class.forName(innerClassName));
				}
				catch (ClassNotFoundException ex2) {
					// swallow - let original exception get through
				}
			}
			throw ex;
		}
	}

	public static Class<?> resolveClassName(String className, ClassLoader classLoader) throws IllegalArgumentException {
		try {
			return forName(className, classLoader);
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Cannot find class [" + className + "]", ex);
		}
		catch (LinkageError ex) {
			throw new IllegalArgumentException(
					"Error loading class [" + className + "]: problem with class file or dependent class.", ex);
		}
	}

	/**
	 * Resolve the given class name as primitive class, if appropriate,
	 * according to the JVM's naming rules for primitive classes.
	 * <p>Also supports the JVM's internal class names for primitive arrays.
	 * Does <i>not</i> support the "[]" suffix notation for primitive arrays;
	 * this is only supported by {@link #forName(String, ClassLoader)}.
	 * @param name the name of the potentially primitive class
	 * @return the primitive class, or {@code null} if the name does not denote
	 * a primitive class or primitive array class
	 */
	public static Class<?> resolvePrimitiveClassName(String name) {
		Class<?> result = null;
		// Most class names will be quite long, considering that they
		// SHOULD sit in a package, so a length check is worthwhile.
		if (name != null && name.length() <= 8) {
			// Could be a primitive - likely.
			result = primitiveTypeNameMap.get(name);
		}
		return result;
	}


	@Deprecated
	public static boolean isPresent(String className) {
		return isPresent(className, getDefaultClassLoader());
	}
	// 判断这个className能否被加载
	public static boolean isPresent(String className, ClassLoader classLoader) {
		try {
			forName(className, classLoader);
			return true;
		}
		catch (Throwable ex) {
			// Class or one of its dependencies is not present...
			return false;
		}
	}

	/**
	 * Return the user-defined class for the given instance: usually simply
	 * the class of the given instance, but the original class in case of a
	 * CGLIB-generated subclass.
	 * @param instance the instance to check
	 * @return the user-defined class
	 */
	public static Class<?> getUserClass(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getUserClass(instance.getClass());
	}

	// 如果这个clazz是使用CGLIB生成的代理类，就返回这个类的父类
	public static Class<?> getUserClass(Class<?> clazz) {
		if (clazz != null && clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && !Object.class.equals(superClass)) {
				return superClass;
			}
		}
		return clazz;
	}

	// 在给定的上下文中检查给定的类是否是缓存安全的，即它是由给定的类加载器加载或是一个父类
	public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader) {
		Assert.notNull(clazz, "Class must not be null");
		ClassLoader target = clazz.getClassLoader();
		if (target == null) {
			return true;
		}
		ClassLoader cur = classLoader;
		if (cur == target) {
			return true;
		}
		while (cur != null) {
			cur = cur.getParent();
			if (cur == target) {
				return true;
			}
		}
		return false;
	}

	// 获取这个类名，剔除包名
	public static String getShortName(String className) {
		Assert.hasLength(className, "Class name must not be empty");
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
		if (nameEndIndex == -1) {
			nameEndIndex = className.length();
		}
		String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
		shortName = shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
		return shortName;
	}
	public static String getShortName(Class<?> clazz) {
		return getShortName(getQualifiedName(clazz));
	}
	// 获取这个类名，剔除包名(首字母小写)
	public static String getShortNameAsProperty(Class<?> clazz) {
		String shortName = ClassUtils.getShortName(clazz);
		int dotIndex = shortName.lastIndexOf('.');
		shortName = (dotIndex != -1 ? shortName.substring(dotIndex + 1) : shortName);
		// decapitalize方法：首字母小写
		return Introspector.decapitalize(shortName);
	}

	// 例如：String.class
	public static String getClassFileName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		String className = clazz.getName();
		int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
	}

	// 获取所在的包名
	public static String getPackageName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return getPackageName(clazz.getName());
	}
	public static String getPackageName(String fqClassName) {
		Assert.notNull(fqClassName, "Class name must not be null");
		int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
		return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
	}

	// 返回clazz.getName()，如果clazz是数组，则添加[]后缀
	public static String getQualifiedName(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		}
		else {
			return clazz.getName();
		}
	}
	private static String getQualifiedNameForArray(Class<?> clazz) {
		StringBuilder result = new StringBuilder();
		while (clazz.isArray()) {
			clazz = clazz.getComponentType();
			result.append(ClassUtils.ARRAY_SUFFIX);
		}
		result.insert(0, clazz.getName());
		return result.toString();
	}
	public static String getQualifiedMethodName(Method method) {
		Assert.notNull(method, "Method must not be null");
		return method.getDeclaringClass().getName() + "." + method.getName();
	}

	// 返回这个value的类型
	public static String getDescriptiveType(Object value) {
		if (value == null) {
			return null;
		}
		Class<?> clazz = value.getClass();
		if (Proxy.isProxyClass(clazz)) {
			StringBuilder result = new StringBuilder(clazz.getName());
			result.append(" implementing ");
			Class<?>[] ifcs = clazz.getInterfaces();
			for (int i = 0; i < ifcs.length; i++) {
				result.append(ifcs[i].getName());
				if (i < ifcs.length - 1) {
					result.append(',');
				}
			}
			return result.toString();
		}
		else if (clazz.isArray()) {
			return getQualifiedNameForArray(clazz);
		}
		else {
			return clazz.getName();
		}
	}

	// 判断是否匹配
	public static boolean matchesTypeName(Class<?> clazz, String typeName) {
		return (typeName != null &&
				(typeName.equals(clazz.getName()) || typeName.equals(clazz.getSimpleName()) ||
				(clazz.isArray() && typeName.equals(getQualifiedNameForArray(clazz)))));
	}

	// 判断是否有指定参数的构造器
	public static boolean hasConstructor(Class<?> clazz, Class<?>... paramTypes) {
		return (getConstructorIfAvailable(clazz, paramTypes) != null);
	}
	public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		try {
			return clazz.getConstructor(paramTypes);
		}
		catch (NoSuchMethodException ex) {
			return null;
		}
	}

	// 判断是否有指定的方法
	public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
	}
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		if (paramTypes != null) {
			try {
				return clazz.getMethod(methodName, paramTypes);
			}
			catch (NoSuchMethodException ex) {
				throw new IllegalStateException("Expected method not found: " + ex);
			}
		}
		else {
			Set<Method> candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (methodName.equals(method.getName())) {
					candidates.add(method);
				}
			}
			if (candidates.size() == 1) {
				return candidates.iterator().next();
			}
			else if (candidates.isEmpty()) {
				throw new IllegalStateException("Expected method not found: " + clazz + "." + methodName);
			}
			else {
				throw new IllegalStateException("No unique method found: " + clazz + "." + methodName);
			}
		}
	}
	public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		if (paramTypes != null) {
			try {
				return clazz.getMethod(methodName, paramTypes);
			}
			catch (NoSuchMethodException ex) {
				return null;
			}
		}
		else {
			Set<Method> candidates = new HashSet<Method>(1);
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (methodName.equals(method.getName())) {
					candidates.add(method);
				}
			}
			if (candidates.size() == 1) {
				return candidates.iterator().next();
			}
			return null;
		}
	}

	// 判断方法的重载个数
	public static int getMethodCountForName(Class<?> clazz, String methodName) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		int count = 0;
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (methodName.equals(method.getName())) {
				count++;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for (Class<?> ifc : ifcs) {
			count += getMethodCountForName(ifc, methodName);
		}
		if (clazz.getSuperclass() != null) {
			count += getMethodCountForName(clazz.getSuperclass(), methodName);
		}
		return count;
	}

	// 判断是否至少存在一个methodName的方法
	public static boolean hasAtLeastOneMethodWithName(Class<?> clazz, String methodName) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		Method[] declaredMethods = clazz.getDeclaredMethods();
		for (Method method : declaredMethods) {
			if (method.getName().equals(methodName)) {
				return true;
			}
		}
		Class<?>[] ifcs = clazz.getInterfaces();
		for (Class<?> ifc : ifcs) {
			if (hasAtLeastOneMethodWithName(ifc, methodName)) {
				return true;
			}
		}
		return (clazz.getSuperclass() != null && hasAtLeastOneMethodWithName(clazz.getSuperclass(), methodName));
	}

	// 此处将查找当前类覆盖的方法
	public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
		if (method != null && isOverridable(method, targetClass) && targetClass != null && !targetClass.equals(method.getDeclaringClass())) {
			try {
				if (Modifier.isPublic(method.getModifiers())) {
					try {
						return targetClass.getMethod(method.getName(), method.getParameterTypes());
					}
					catch (NoSuchMethodException ex) {
						return method;
					}
				}
				else {
					Method specificMethod = ReflectionUtils.findMethod(targetClass, method.getName(), method.getParameterTypes());
					return (specificMethod != null ? specificMethod : method);
				}
			}
			catch (AccessControlException ex) {
				// Security settings are disallowing reflective access; fall back to 'method' below.
			}
		}
		return method;
	}

	// 判断method目标类中是否可覆盖，如果private就不可以重写
	private static boolean isOverridable(Method method, Class<?> targetClass) {
		if (Modifier.isPrivate(method.getModifiers())) {
			return false;
		}
		if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
			return true;
		}
		return getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass));
	}

	/**
	 * Return a public static method of a class.
	 * @param methodName the static method name
	 * @param clazz the class which defines the method
	 * @param args the parameter types to the method
	 * @return the static method, or {@code null} if no static method was found
	 * @throws IllegalArgumentException if the method name is blank or the clazz is null
	 */
	public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>... args) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(methodName, "Method name must not be null");
		try {
			Method method = clazz.getMethod(methodName, args);
			return Modifier.isStatic(method.getModifiers()) ? method : null;
		}
		catch (NoSuchMethodException ex) {
			return null;
		}
	}

	// 判断是否为原始类型
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return primitiveWrapperTypeMap.containsKey(clazz);
	}
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}
	// 原始类型的数组形式
	public static boolean isPrimitiveArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isArray() && clazz.getComponentType().isPrimitive());
	}
	public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
	}

	// 如果是原始类型，则返回原始类型对象，如：Integer.class
	public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		// isPrimitive():判断class是否表示一个基本类型，有9个预定义的基本类型，包括八种原始类型和 void。
		return (clazz.isPrimitive() && clazz != void.class? primitiveTypeToWrapperMap.get(clazz) : clazz);
	}

	/**
	 * Check if the right-hand side type may be assigned to the left-hand side
	 * type, assuming setting by reflection. Considers primitive wrapper
	 * classes as assignable to the corresponding primitive types.
	 * @param lhsType the target type
	 * @param rhsType the value type that should be assigned to the target type
	 * @return if the target type is assignable from the value type
	 * @see TypeUtils#isAssignable
	 */
	public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
		Assert.notNull(lhsType, "Left-hand side type must not be null");
		Assert.notNull(rhsType, "Right-hand side type must not be null");
		if (lhsType.isAssignableFrom(rhsType)) {
			return true;
		}
		if (lhsType.isPrimitive()) {
			Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
			if (resolvedPrimitive != null && lhsType.equals(resolvedPrimitive)) {
				return true;
			}
		}
		else {
			Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
			if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if the given type is assignable from the given value,
	 * assuming setting by reflection. Considers primitive wrapper classes
	 * as assignable to the corresponding primitive types.
	 * @param type the target type
	 * @param value the value that should be assigned to the type
	 * @return if the type is assignable from the value
	 */
	public static boolean isAssignableValue(Class<?> type, Object value) {
		Assert.notNull(type, "Type must not be null");
		return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
	}


	// 将资源路径转为类路径
	public static String convertResourcePathToClassName(String resourcePath) {
		Assert.notNull(resourcePath, "Resource path must not be null");
		return resourcePath.replace('/', '.');
	}
	public static String convertClassNameToResourcePath(String className) {
		Assert.notNull(className, "Class name must not be null");
		return className.replace('.', '/');
	}

	// 返回一个路径 = clazz所在位置 + “/” + resourceName
	public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
		Assert.notNull(resourceName, "Resource name must not be null");
		if (!resourceName.startsWith("/")) {
			return classPackageAsResourcePath(clazz) + "/" + resourceName;
		}
		return classPackageAsResourcePath(clazz) + resourceName;
	}

	// 以资源路径的形式返回这个clazz
	public static String classPackageAsResourcePath(Class<?> clazz) {
		if (clazz == null) {
			return "";
		}
		String className = clazz.getName();
		int packageEndIndex = className.lastIndexOf('.');
		if (packageEndIndex == -1) {
			return "";
		}
		String packageName = className.substring(0, packageEndIndex);
		return packageName.replace('.', '/');
	}


	public static String classNamesToString(Class... classes) {
		return classNamesToString(Arrays.asList(classes));
	}
	// 返回这个集合的字符串形式，如："[com.foo.Bar, com.foo.Baz]"
	public static String classNamesToString(Collection<Class> classes) {
		if (CollectionUtils.isEmpty(classes)) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (Iterator<Class> it = classes.iterator(); it.hasNext(); ) {
			Class clazz = it.next();
			sb.append(clazz.getName());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	// 将集合转为数组
	public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
		if (collection == null) {
			return null;
		}
		return collection.toArray(new Class<?>[collection.size()]);
	}

	// 返回这个类实现的所有接口
	public static Class<?>[] getAllInterfaces(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getAllInterfacesForClass(instance.getClass());
	}
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
		return getAllInterfacesForClass(clazz, null);
	}
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader classLoader) {
		Set<Class> ifcs = getAllInterfacesForClassAsSet(clazz, classLoader);
		return ifcs.toArray(new Class[ifcs.size()]);
	}
	public static Set<Class> getAllInterfacesAsSet(Object instance) {
		Assert.notNull(instance, "Instance must not be null");
		return getAllInterfacesForClassAsSet(instance.getClass());
	}
	public static Set<Class> getAllInterfacesForClassAsSet(Class clazz) {
		return getAllInterfacesForClassAsSet(clazz, null);
	}
	public static Set<Class> getAllInterfacesForClassAsSet(Class clazz, ClassLoader classLoader) {
		Assert.notNull(clazz, "Class must not be null");
		if (clazz.isInterface() && isVisible(clazz, classLoader)) {
			return Collections.singleton(clazz);
		}
		Set<Class> interfaces = new LinkedHashSet<Class>();
		while (clazz != null) {
			Class<?>[] ifcs = clazz.getInterfaces();
			for (Class<?> ifc : ifcs) {
				interfaces.addAll(getAllInterfacesForClassAsSet(ifc, classLoader));
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}

	/**
	 * Create a composite interface Class for the given interfaces,
	 * implementing the given interfaces in one single Class.
	 * <p>This implementation builds a JDK proxy class for the given interfaces.
	 * @param interfaces the interfaces to merge
	 * @param classLoader the ClassLoader to create the composite Class in
	 * @return the merged interface as Class
	 * @see java.lang.reflect.Proxy#getProxyClass
	 */
	public static Class<?> createCompositeInterface(Class<?>[] interfaces, ClassLoader classLoader) {
		Assert.notEmpty(interfaces, "Interfaces must not be empty");
		Assert.notNull(classLoader, "ClassLoader must not be null");
		return Proxy.getProxyClass(classLoader, interfaces);
	}

	// 如果A extends B 则返回A，或返回它们的共同父类
	public static Class<?> determineCommonAncestor(Class<?> clazz1, Class<?> clazz2) {
		if (clazz1 == null) {
			return clazz2;
		}
		if (clazz2 == null) {
			return clazz1;
		}
		if (clazz1.isAssignableFrom(clazz2)) {
			return clazz1;
		}
		if (clazz2.isAssignableFrom(clazz1)) {
			return clazz2;
		}
		Class<?> ancestor = clazz1;
		do {
			ancestor = ancestor.getSuperclass();
			if (ancestor == null || Object.class.equals(ancestor)) {
				return null;
			}
		}
		while (!ancestor.isAssignableFrom(clazz2));
		return ancestor;
	}

	// 检查给定的类在给定的类加载器是否可见
	public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
		if (classLoader == null) {
			return true;
		}
		try {
			Class<?> actualClass = classLoader.loadClass(clazz.getName());
			return (clazz == actualClass);
			// Else: different interface class found...
		}
		catch (ClassNotFoundException ex) {
			// No interface class found...
			return false;
		}
	}

	// 判断是否为CGLIB生成的代理类，如果是CGLIB生成的，className必定包含$$
	public static boolean isCglibProxy(Object object) {
		return ClassUtils.isCglibProxyClass(object.getClass());
	}
	public static boolean isCglibProxyClass(Class<?> clazz) {
		return (clazz != null && isCglibProxyClassName(clazz.getName()));
	}
	public static boolean isCglibProxyClassName(String className) {
		return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
	}

}
