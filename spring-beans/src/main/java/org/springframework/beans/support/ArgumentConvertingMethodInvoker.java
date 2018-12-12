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

package org.springframework.beans.support;

import java.beans.PropertyEditor;
import java.lang.reflect.Method;

import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.MethodInvoker;
import org.springframework.util.ReflectionUtils;

/**
 * Subclass of {@link MethodInvoker} that tries to convert the given
 * arguments for the actual target method via a {@link TypeConverter}.
 *
 * <p>Supports flexible argument conversions, in particular for
 * invoking a specific overloaded method.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see org.springframework.beans.BeanWrapperImpl#convertIfNecessary
 */
public class ArgumentConvertingMethodInvoker extends MethodInvoker {

	// 类型转换器
	private TypeConverter typeConverter;

	// 表示是否使用默认是类型转换器
	private boolean useDefaultConverter = true;

	// 注册自定义的属性编辑器：requiredType表示属性类型、propertyEditor表示自定义的属性编辑器
	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		TypeConverter converter = getTypeConverter();
		if (!(converter instanceof PropertyEditorRegistry)) {
			throw new IllegalStateException("TypeConverter does not implement PropertyEditorRegistry interface: " + converter);
		}
		((PropertyEditorRegistry) converter).registerCustomEditor(requiredType, propertyEditor);
	}

	/**
	 * This implementation looks for a method with matching parameter types.
	 * @see #doFindMatchingMethod
	 */
	@Override
	protected Method findMatchingMethod() {
		Method matchingMethod = super.findMatchingMethod();
		// Second pass: look for method where arguments can be converted to parameter types.
		if (matchingMethod == null) {
			// Interpret argument array as individual method arguments.
			matchingMethod = doFindMatchingMethod(getArguments());
		}
		if (matchingMethod == null) {
			// Interpret argument array as single method argument of array type.
			matchingMethod = doFindMatchingMethod(new Object[] {getArguments()});
		}
		return matchingMethod;
	}

	/**
	 * Actually find a method with matching parameter type, i.e. where each
	 * argument value is assignable to the corresponding parameter type.
	 * @param arguments the argument values to match against method parameters
	 * @return a matching method, or {@code null} if none
	 */
	protected Method doFindMatchingMethod(Object[] arguments) {
		TypeConverter converter = getTypeConverter();
		if (converter != null) {
			String targetMethod = getTargetMethod();
			Method matchingMethod = null;
			int argCount = arguments.length;
			Method[] candidates = ReflectionUtils.getAllDeclaredMethods(getTargetClass());
			int minTypeDiffWeight = Integer.MAX_VALUE;
			Object[] argumentsToUse = null;
			for (Method candidate : candidates) {
				if (candidate.getName().equals(targetMethod)) {
					// Check if the inspected method has the correct number of parameters.
					Class[] paramTypes = candidate.getParameterTypes();
					if (paramTypes.length == argCount) {
						Object[] convertedArguments = new Object[argCount];
						boolean match = true;
						for (int j = 0; j < argCount && match; j++) {
							// Verify that the supplied argument is assignable to the method parameter.
							try {
								convertedArguments[j] = converter.convertIfNecessary(arguments[j], paramTypes[j]);
							}
							catch (TypeMismatchException ex) {
								// Ignore -> simply doesn't match.
								match = false;
							}
						}
						if (match) {
							int typeDiffWeight = getTypeDifferenceWeight(paramTypes, convertedArguments);
							if (typeDiffWeight < minTypeDiffWeight) {
								minTypeDiffWeight = typeDiffWeight;
								matchingMethod = candidate;
								argumentsToUse = convertedArguments;
							}
						}
					}
				}
			}
			if (matchingMethod != null) {
				setArguments(argumentsToUse);
				return matchingMethod;
			}
		}
		return null;
	}


	// getter and setter ...
	public void setTypeConverter(TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
		this.useDefaultConverter = false;
	}
	public TypeConverter getTypeConverter() {
		if (this.typeConverter == null && this.useDefaultConverter) {
			this.typeConverter = getDefaultTypeConverter();
		}
		return this.typeConverter;
	}
	protected TypeConverter getDefaultTypeConverter() {
		return new SimpleTypeConverter();
	}

}
