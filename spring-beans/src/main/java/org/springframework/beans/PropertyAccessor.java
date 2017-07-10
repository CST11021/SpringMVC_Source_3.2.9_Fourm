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

package org.springframework.beans;

import java.util.Map;

import org.springframework.core.convert.TypeDescriptor;

/**
 * Common interface for classes that can access named properties
 * (such as bean properties of an object or fields in an object)
 * Serves as base interface for {@link BeanWrapper}.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see BeanWrapper
 * @see PropertyAccessorFactory#forBeanPropertyAccess
 * @see PropertyAccessorFactory#forDirectFieldAccess
 */
// 属性访问器：可以访问命名属性类的通用接口，作为BeanWrapper基接口
public interface PropertyAccessor {

	/**
	 * Path separator for nested properties.
	 * Follows normal Java conventions: getFoo().getBar() would be "foo.bar".
	 * 嵌套属性的路径分隔符，遵循正常的Java转换：getFoo().getBar() --> "foo.bar".
	 */
	String NESTED_PROPERTY_SEPARATOR = ".";
	char NESTED_PROPERTY_SEPARATOR_CHAR = '.';

	/**
	 * Marker that indicates the start of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 */
	String PROPERTY_KEY_PREFIX = "[";
	char PROPERTY_KEY_PREFIX_CHAR = '[';

	/**
	 * Marker that indicates the end of a property key for an
	 * indexed or mapped property like "person.addresses[0]".
	 */
	String PROPERTY_KEY_SUFFIX = "]";
	char PROPERTY_KEY_SUFFIX_CHAR = ']';


	// 该属性是否可读
	boolean isReadableProperty(String propertyName);

	// 该属性是否可写
	boolean isWritableProperty(String propertyName);

	// 获取属性的类型
	Class getPropertyType(String propertyName) throws BeansException;

	// 获取这个属性的 TypeDescriptor
	TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException;

	// 获取这个属性的值
	Object getPropertyValue(String propertyName) throws BeansException;

	// 设置属性值
	void setPropertyValue(String propertyName, Object value) throws BeansException;
	void setPropertyValue(PropertyValue pv) throws BeansException;
	void setPropertyValues(Map<?, ?> map) throws BeansException;
	void setPropertyValues(PropertyValues pvs) throws BeansException;
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown) throws BeansException;
	void setPropertyValues(PropertyValues pvs, boolean ignoreUnknown, boolean ignoreInvalid) throws BeansException;

}
