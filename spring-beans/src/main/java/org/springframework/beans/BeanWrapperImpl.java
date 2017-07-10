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

package org.springframework.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.CollectionFactory;
import org.springframework.core.GenericCollectionTypeResolver;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.Property;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Default {@link BeanWrapper} implementation that should be sufficient
 * for all typical use cases. Caches introspection results for efficiency.
 *
 * <p>Note: Auto-registers default property editors from the
 * {@code org.springframework.beans.propertyeditors} package, which apply
 * in addition to the JDK's standard PropertyEditors. Applications can call
 * the {@link #registerCustomEditor(Class, java.beans.PropertyEditor)} method
 * to register an editor for a particular instance (i.e. they are not shared
 * across the application). See the base class
 * {@link PropertyEditorRegistrySupport} for details.
 *
 * <p>{@code BeanWrapperImpl} will convert collection and array values
 * to the corresponding target collections or arrays, if necessary. Custom
 * property editors that deal with collections or arrays can either be
 * written via PropertyEditor's {@code setValue}, or against a
 * comma-delimited String via {@code setAsText}, as String arrays are
 * converted in such a format if the array itself is not assignable.
 *
 * <p><b>NOTE: As of Spring 2.5, this is - for almost all purposes - an
 * internal class.</b> It is just public in order to allow for access from
 * other framework packages. For standard application access purposes, use the
 * {@link PropertyAccessorFactory#forBeanPropertyAccess} factory method instead.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 15 April 2001
 * @see #registerCustomEditor
 * @see #setPropertyValues
 * @see #setPropertyValue
 * @see #getPropertyValue
 * @see #getPropertyType
 * @see BeanWrapper
 * @see PropertyEditorRegistrySupport
 */
public class BeanWrapperImpl extends AbstractPropertyAccessor implements BeanWrapper {
	private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);

	// 表示被包装的对象
	private Object object;
	private String nestedPath = "";
	private Object rootObject;

	// 表示调用属性方法的安全上下文
	private AccessControlContext acc;

	// 换成这个对象的结果
	private CachedIntrospectionResults cachedIntrospectionResults;

	// Map with cached nested BeanWrappers: nested path -> BeanWrapper instance.
	private Map<String, BeanWrapperImpl> nestedBeanWrappers;

	private boolean autoGrowNestedPaths = false;

	private int autoGrowCollectionLimit = Integer.MAX_VALUE;



	public BeanWrapperImpl() {
		this(true);
	}
	public BeanWrapperImpl(boolean registerDefaultEditors) {
		if (registerDefaultEditors) {
			registerDefaultEditors();
		}
		this.typeConverterDelegate = new TypeConverterDelegate(this);
	}
	public BeanWrapperImpl(Object object) {
		registerDefaultEditors();
		setWrappedInstance(object);
	}
	public BeanWrapperImpl(Class<?> clazz) {
		registerDefaultEditors();
		setWrappedInstance(BeanUtils.instantiateClass(clazz));
	}
	public BeanWrapperImpl(Object object, String nestedPath, Object rootObject) {
		registerDefaultEditors();
		setWrappedInstance(object, nestedPath, rootObject);
	}
	private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl superBw) {
		setWrappedInstance(object, nestedPath, superBw.getWrappedInstance());
		setExtractOldValueForEditor(superBw.isExtractOldValueForEditor());
		setAutoGrowNestedPaths(superBw.isAutoGrowNestedPaths());
		setAutoGrowCollectionLimit(superBw.getAutoGrowCollectionLimit());
		setConversionService(superBw.getConversionService());
		setSecurityContext(superBw.acc);
	}



	// Implementation of BeanWrapper interface

	// 设置被包装的object，替换缓存的introspection results
	public void setWrappedInstance(Object object) {
		setWrappedInstance(object, "", null);
	}
	public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
		Assert.notNull(object, "Bean object must not be null");
		this.object = object;
		this.nestedPath = (nestedPath != null ? nestedPath : "");
		this.rootObject = (!"".equals(this.nestedPath) ? rootObject : object);
		this.nestedBeanWrappers = null;
		this.typeConverterDelegate = new TypeConverterDelegate(this, object);
		setIntrospectionClass(object.getClass());
	}

	public final Object getWrappedInstance() {
		return this.object;
	}
	public final Class<?> getWrappedClass() {
		return (this.object != null ? this.object.getClass() : null);
	}

	public final String getNestedPath() {
		return this.nestedPath;
	}

	// 在返回该BeanWrapper的路径上的根对象。
	public final Object getRootInstance() {
		return this.rootObject;
	}
	public final Class<?> getRootClass() {
		return (this.rootObject != null ? this.rootObject.getClass() : null);
	}

	public void setAutoGrowNestedPaths(boolean autoGrowNestedPaths) {
		this.autoGrowNestedPaths = autoGrowNestedPaths;
	}
	public boolean isAutoGrowNestedPaths() {
		return this.autoGrowNestedPaths;
	}

	public void setAutoGrowCollectionLimit(int autoGrowCollectionLimit) {
		this.autoGrowCollectionLimit = autoGrowCollectionLimit;
	}
	public int getAutoGrowCollectionLimit() {
		return this.autoGrowCollectionLimit;
	}

	public void setSecurityContext(AccessControlContext acc) {
		this.acc = acc;
	}
	public AccessControlContext getSecurityContext() {
		return this.acc;
	}

	// 设置 cachedIntrospectionResults 在目标对象改变的时候会被调用
	protected void setIntrospectionClass(Class<?> clazz) {
		if (this.cachedIntrospectionResults != null &&
				!clazz.equals(this.cachedIntrospectionResults.getBeanClass())) {
			this.cachedIntrospectionResults = null;
		}
	}
	private CachedIntrospectionResults getCachedIntrospectionResults() {
		Assert.state(this.object != null, "BeanWrapper does not hold a bean instance");
		if (this.cachedIntrospectionResults == null) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(getWrappedClass());
		}
		return this.cachedIntrospectionResults;
	}

	// 获取属性描述器
	public PropertyDescriptor[] getPropertyDescriptors() {
		return getCachedIntrospectionResults().getPropertyDescriptors();
	}
	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
		if (pd == null) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"No property '" + propertyName + "' found");
		}
		return pd;
	}
	protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) throws BeansException {
		Assert.notNull(propertyName, "Property name must not be null");
		BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
		return nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
	}

	@Override
	public Class<?> getPropertyType(String propertyName) throws BeansException {
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				return pd.getPropertyType();
			}
			else {
				// Maybe an indexed/mapped property...
				Object value = getPropertyValue(propertyName);
				if (value != null) {
					return value.getClass();
				}
				// Check to see if there is a custom editor,
				// which might give an indication on the desired target type.
				Class<?> editorType = guessPropertyTypeFromEditors(propertyName);
				if (editorType != null) {
					return editorType;
				}
			}
		}
		catch (InvalidPropertyException ex) {
			// Consider as not determinable.
		}
		return null;
	}
	public TypeDescriptor getPropertyTypeDescriptor(String propertyName) throws BeansException {
		try {
			BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
			String finalPath = getFinalPath(nestedBw, propertyName);
			PropertyTokenHolder tokens = getPropertyNameTokens(finalPath);
			PropertyDescriptor pd = nestedBw.getCachedIntrospectionResults().getPropertyDescriptor(tokens.actualName);
			if (pd != null) {
				if (tokens.keys != null) {
					if (pd.getReadMethod() != null || pd.getWriteMethod() != null) {
						return TypeDescriptor.nested(property(pd), tokens.keys.length);
					}
				} else {
					if (pd.getReadMethod() != null || pd.getWriteMethod() != null) {
						return new TypeDescriptor(property(pd));
					}
				}
			}
		}
		catch (InvalidPropertyException ex) {
			// Consider as not determinable.
		}
		return null;
	}

	// 判断是否有get方法
	public boolean isReadableProperty(String propertyName) {
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				// 判断这个属性对应的属性描述器是否有getter方法
				if (pd.getReadMethod() != null) {
					return true;
				}
			}
			else {
				// Maybe an indexed/mapped property...
				getPropertyValue(propertyName);
				return true;
			}
		}
		catch (InvalidPropertyException ex) {
			// Cannot be evaluated, so can't be readable.
		}
		return false;
	}
	// 判断是否有set方法
	public boolean isWritableProperty(String propertyName) {
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				if (pd.getWriteMethod() != null) {
					return true;
				}
			}
			else {
				// Maybe an indexed/mapped property...
				getPropertyValue(propertyName);
				return true;
			}
		}
		catch (InvalidPropertyException ex) {
			// Cannot be evaluated, so can't be writable.
		}
		return false;
	}

	private Object convertIfNecessary(String propertyName, Object oldValue, Object newValue, Class<?> requiredType, TypeDescriptor td) throws TypeMismatchException {
		try {
			return this.typeConverterDelegate.convertIfNecessary(propertyName, oldValue, newValue, requiredType, td);
		}
		catch (ConverterNotFoundException ex) {
			PropertyChangeEvent pce =
					new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(pce, td.getType(), ex);
		}
		catch (ConversionException ex) {
			PropertyChangeEvent pce =
					new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
			throw new TypeMismatchException(pce, requiredType, ex);
		}
		catch (IllegalStateException ex) {
			PropertyChangeEvent pce =
					new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
			throw new ConversionNotSupportedException(pce, requiredType, ex);
		}
		catch (IllegalArgumentException ex) {
			PropertyChangeEvent pce =
					new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, newValue);
			throw new TypeMismatchException(pce, requiredType, ex);
		}
	}

	public Object convertForProperty(Object value, String propertyName) throws TypeMismatchException {
		PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(propertyName);
		if (pd == null) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"No property '" + propertyName + "' found");
		}
		return convertForProperty(propertyName, null, value, new TypeDescriptor(property(pd)));
	}
	private Object convertForProperty(String propertyName, Object oldValue, Object newValue, TypeDescriptor td) throws TypeMismatchException {

		return convertIfNecessary(propertyName, oldValue, newValue, td.getType(), td);
	}

	private Property property(PropertyDescriptor pd) {
		GenericTypeAwarePropertyDescriptor typeAware = (GenericTypeAwarePropertyDescriptor) pd;
		return new Property(typeAware.getBeanClass(), typeAware.getReadMethod(), typeAware.getWriteMethod(), typeAware.getName());
	}



	// Implementation methods

	/**
	 * Get the last component of the path. Also works if not nested.
	 * @param bw BeanWrapper to work on
	 * @param nestedPath property path we know is nested
	 * @return last component of the path (the property on the target bean)
	 */
	private String getFinalPath(BeanWrapper bw, String nestedPath) {
		if (bw == this) {
			return nestedPath;
		}
		return nestedPath.substring(PropertyAccessorUtils.getLastNestedPropertySeparatorIndex(nestedPath) + 1);
	}

	/**
	 * Recursively navigate to return a BeanWrapper for the nested property path.	// 递归浏览返回的嵌套属性路径BeanWrapper
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
	protected BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath) {
		int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);// 返回嵌套属性path的第一个分割符索引
		// Handle nested properties recursively.
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
			return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
		}
		else {
			return this;
		}
	}

	/**
	 * Retrieve a BeanWrapper for the given nested property.
	 * Create a new one if not found in the cache.
	 * <p>Note: Caching nested BeanWrappers is necessary now,
	 * to keep registered custom editors for nested properties.
	 * @param nestedProperty property to create the BeanWrapper for
	 * @return the BeanWrapper instance, either cached or newly created
	 */
	private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty) {
		if (this.nestedBeanWrappers == null) {
			this.nestedBeanWrappers = new HashMap<String, BeanWrapperImpl>();
		}
		// Get value of bean property.
		PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
		String canonicalName = tokens.canonicalName;
		Object propertyValue = getPropertyValue(tokens);
		if (propertyValue == null) {
			if (this.autoGrowNestedPaths) {
				propertyValue = setDefaultValue(tokens);
			}
			else {
				throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName);
			}
		}

		// Lookup cached sub-BeanWrapper, create new one if not found.
		BeanWrapperImpl nestedBw = this.nestedBeanWrappers.get(canonicalName);
		if (nestedBw == null || nestedBw.getWrappedInstance() != propertyValue) {
			if (logger.isTraceEnabled()) {
				logger.trace("Creating new nested BeanWrapper for property '" + canonicalName + "'");
			}
			nestedBw = newNestedBeanWrapper(propertyValue, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR);
			// Inherit all type-specific PropertyEditors.
			copyDefaultEditorsTo(nestedBw);
			copyCustomEditorsTo(nestedBw, canonicalName);
			this.nestedBeanWrappers.put(canonicalName, nestedBw);
		}
		else {
			if (logger.isTraceEnabled()) {
				logger.trace("Using cached nested BeanWrapper for property '" + canonicalName + "'");
			}
		}
		return nestedBw;
	}

	private Object setDefaultValue(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		tokens.actualName = propertyName;
		tokens.canonicalName = propertyName;
		return setDefaultValue(tokens);
	}

	private Object setDefaultValue(PropertyTokenHolder tokens) {
		PropertyValue pv = createDefaultPropertyValue(tokens);
		setPropertyValue(tokens, pv);
		return getPropertyValue(tokens);
	}

	private PropertyValue createDefaultPropertyValue(PropertyTokenHolder tokens) {
		Class<?> type = getPropertyTypeDescriptor(tokens.canonicalName).getType();
		if (type == null) {
			throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + tokens.canonicalName,
					"Could not determine property type for auto-growing a default value");
		}
		Object defaultValue = newValue(type, tokens.canonicalName);
		return new PropertyValue(tokens.canonicalName, defaultValue);
	}

	private Object newValue(Class<?> type, String name) {
		try {
			if (type.isArray()) {
				Class<?> componentType = type.getComponentType();
				// TODO - only handles 2-dimensional arrays
				if (componentType.isArray()) {
					Object array = Array.newInstance(componentType, 1);
					Array.set(array, 0, Array.newInstance(componentType.getComponentType(), 0));
					return array;
				}
				else {
					return Array.newInstance(componentType, 0);
				}
			}
			else if (Collection.class.isAssignableFrom(type)) {
				return CollectionFactory.createCollection(type, 16);
			}
			else if (Map.class.isAssignableFrom(type)) {
				return CollectionFactory.createMap(type, 16);
			}
			else {
				return type.newInstance();
			}
		}
		catch (Exception ex) {
			// TODO Root cause exception context is lost here... should we throw another exception type that preserves context instead?
			throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + name,
					"Could not instantiate property type [" + type.getName() + "] to auto-grow nested property path: " + ex);
		}
	}


	// 创建一个新的嵌套BeanWrapper实例
	protected BeanWrapperImpl newNestedBeanWrapper(Object object, String nestedPath) {
		return new BeanWrapperImpl(object, nestedPath, this);
	}

	// 根据指定的propertyName 返回对应的 PropertyTokenHolder对象
	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		String actualName = null;
		List<String> keys = new ArrayList<String>(2);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
					if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		tokens.actualName = (actualName != null ? actualName : propertyName);
		tokens.canonicalName = tokens.actualName;
		if (!keys.isEmpty()) {
			tokens.canonicalName +=
					PROPERTY_KEY_PREFIX +
					StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX) +
					PROPERTY_KEY_SUFFIX;
			tokens.keys = StringUtils.toStringArray(keys);
		}
		return tokens;
	}


	// Implementation of PropertyAccessor interface
	private Object growArrayIfNecessary(Object array, int index, String name) {
		if (!this.autoGrowNestedPaths) {
			return array;
		}
		int length = Array.getLength(array);
		if (index >= length && index < this.autoGrowCollectionLimit) {
			Class<?> componentType = array.getClass().getComponentType();
			Object newArray = Array.newInstance(componentType, index + 1);
			System.arraycopy(array, 0, newArray, 0, length);
			for (int i = length; i < Array.getLength(newArray); i++) {
				Array.set(newArray, i, newValue(componentType, name));
			}
			// TODO this is not efficient because conversion may create a copy ... set directly because we know it is assignable.
			setPropertyValue(name, newArray);
			return getPropertyValue(name);
		}
		else {
			return array;
		}
	}
	private void growCollectionIfNecessary(Collection<Object> collection, int index, String name, PropertyDescriptor pd, int nestingLevel) {

		if (!this.autoGrowNestedPaths) {
			return;
		}
		int size = collection.size();
		if (index >= size && index < this.autoGrowCollectionLimit) {
			Class<?> elementType = GenericCollectionTypeResolver.getCollectionReturnType(pd.getReadMethod(), nestingLevel);
			if (elementType != null) {
				for (int i = collection.size(); i < index + 1; i++) {
					collection.add(newValue(elementType, name));
				}
			}
		}
	}

	// 获取属性值
	@Override
	public Object getPropertyValue(String propertyName) throws BeansException {
		BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
		return nestedBw.getPropertyValue(tokens);
	}
    @SuppressWarnings("unchecked")
	private Object getPropertyValue(PropertyTokenHolder tokens) throws BeansException {
		String propertyName = tokens.canonicalName;
		String actualName = tokens.actualName;
		PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
		if (pd == null || pd.getReadMethod() == null) {
			throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
		}
		final Method readMethod = pd.getReadMethod();
		try {
			if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()) && !readMethod.isAccessible()) {
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged(new PrivilegedAction<Object>() {
						public Object run() {
							readMethod.setAccessible(true);
							return null;
						}
					});
				}
				else {
					readMethod.setAccessible(true);
				}
			}

			Object value;
			if (System.getSecurityManager() != null) {
				try {
					value = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
							return readMethod.invoke(object, (Object[]) null);
						}
					}, acc);
				}
				catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			}
			else {
				value = readMethod.invoke(object, (Object[]) null);
			}

			if (tokens.keys != null) {
				if (value == null) {
					if (this.autoGrowNestedPaths) {
						value = setDefaultValue(tokens.actualName);
					}
					else {
						throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
								"Cannot access indexed value of property referenced in indexed " +
								"property path '" + propertyName + "': returned null");
					}
				}
				String indexedPropertyName = tokens.actualName;
				// apply indexes and map keys
				for (int i = 0; i < tokens.keys.length; i++) {
					String key = tokens.keys[i];
					if (value == null) {
						throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
								"Cannot access indexed value of property referenced in indexed " +
								"property path '" + propertyName + "': returned null");
					}
					else if (value.getClass().isArray()) {
						int index = Integer.parseInt(key);
						value = growArrayIfNecessary(value, index, indexedPropertyName);
						value = Array.get(value, index);
					}
					else if (value instanceof List) {
						int index = Integer.parseInt(key);
						List<Object> list = (List<Object>) value;
						growCollectionIfNecessary(list, index, indexedPropertyName, pd, i + 1);
						value = list.get(index);
					}
					else if (value instanceof Set) {
						// Apply index to Iterator in case of a Set.
						Set<Object> set = (Set<Object>) value;
						int index = Integer.parseInt(key);
						if (index < 0 || index >= set.size()) {
							throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
									"Cannot get element with index " + index + " from Set of size " +
									set.size() + ", accessed using property path '" + propertyName + "'");
						}
						Iterator<Object> it = set.iterator();
						for (int j = 0; it.hasNext(); j++) {
							Object elem = it.next();
							if (j == index) {
								value = elem;
								break;
							}
						}
					}
					else if (value instanceof Map) {
						Map<Object, Object> map = (Map<Object, Object>) value;
						Class<?> mapKeyType = GenericCollectionTypeResolver.getMapKeyReturnType(pd.getReadMethod(), i + 1);
						// IMPORTANT: Do not pass full property name in here - property editors
						// must not kick in for map keys but rather only for map values.
						TypeDescriptor typeDescriptor = (mapKeyType != null ?
								TypeDescriptor.valueOf(mapKeyType) : TypeDescriptor.valueOf(Object.class));
						Object convertedMapKey = convertIfNecessary(null, null, key, mapKeyType, typeDescriptor);
						value = map.get(convertedMapKey);
					}
					else {
						throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
								"Property referenced in indexed property path '" + propertyName +
								"' is neither an array nor a List nor a Set nor a Map; returned value was [" + value + "]");
					}
					indexedPropertyName += PROPERTY_KEY_PREFIX + key + PROPERTY_KEY_SUFFIX;
				}
			}
			return value;
		}
		catch (IndexOutOfBoundsException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Index of out of bounds in property path '" + propertyName + "'", ex);
		}
		catch (NumberFormatException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Invalid index in property path '" + propertyName + "'", ex);
		}
		catch (TypeMismatchException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Invalid index in property path '" + propertyName + "'", ex);
		}
		catch (InvocationTargetException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Getter for property '" + actualName + "' threw exception", ex);
		}
		catch (Exception ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Illegal attempt to get property '" + actualName + "' threw exception", ex);
		}
	}

	// 设置属性值，这个属性可以是一个嵌套的path字符串，如：listOfMaps[0]['luckyNumber']
	// 比如一个 List<Map> listOfMaps;的类属性，我们可以通过setPropertyValue("listOfMaps[0]['luckyNumber']", "9");为其设置属性值
	@Override
	public void setPropertyValue(String propertyName, Object value) throws BeansException {
		BeanWrapperImpl nestedBw;
		try {
			nestedBw = getBeanWrapperForPropertyPath(propertyName);// 根据这个 propertyName 获取一个BeanWrapper
		}
		catch (NotReadablePropertyException ex) {
			throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName, "Nested property in path '" + propertyName + "' does not exist", ex);
		}
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
		nestedBw.setPropertyValue(tokens, new PropertyValue(propertyName, value));
	}
	@Override
	public void setPropertyValue(PropertyValue pv) throws BeansException {
		PropertyTokenHolder tokens = (PropertyTokenHolder) pv.resolvedTokens;
		if (tokens == null) {
			String propertyName = pv.getName();
			BeanWrapperImpl nestedBw;
			try {
				nestedBw = getBeanWrapperForPropertyPath(propertyName);
			}
			catch (NotReadablePropertyException ex) {
				throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
						"Nested property in path '" + propertyName + "' does not exist", ex);
			}
			tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
			if (nestedBw == this) {
				pv.getOriginalPropertyValue().resolvedTokens = tokens;
			}
			nestedBw.setPropertyValue(tokens, pv);
		}
		else {
			setPropertyValue(tokens, pv);
		}
	}
	@SuppressWarnings("unchecked")
	private void setPropertyValue(PropertyTokenHolder tokens, PropertyValue pv) throws BeansException {
		String propertyName = tokens.canonicalName;
		String actualName = tokens.actualName;

		if (tokens.keys != null) {
			// Apply indexes and map keys: fetch value for all keys but the last one.
			PropertyTokenHolder getterTokens = new PropertyTokenHolder();
			getterTokens.canonicalName = tokens.canonicalName;
			getterTokens.actualName = tokens.actualName;
			getterTokens.keys = new String[tokens.keys.length - 1];
			System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);
			Object propValue;
			try {
				propValue = getPropertyValue(getterTokens);
			}
			catch (NotReadablePropertyException ex) {
				throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
						"Cannot access indexed value in property referenced " +
						"in indexed property path '" + propertyName + "'", ex);
			}
			// Set value for last key.
			String key = tokens.keys[tokens.keys.length - 1];
			if (propValue == null) {
				// null map value case
				if (this.autoGrowNestedPaths) {
					// TODO: cleanup, this is pretty hacky
					int lastKeyIndex = tokens.canonicalName.lastIndexOf('[');
					getterTokens.canonicalName = tokens.canonicalName.substring(0, lastKeyIndex);
					propValue = setDefaultValue(getterTokens);
				}
				else {
					throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
							"Cannot access indexed value in property referenced " +
							"in indexed property path '" + propertyName + "': returned null");
				}
			}
			if (propValue.getClass().isArray()) {
				PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
				Class<?> requiredType = propValue.getClass().getComponentType();
				int arrayIndex = Integer.parseInt(key);
				Object oldValue = null;
				try {
					if (isExtractOldValueForEditor() && arrayIndex < Array.getLength(propValue)) {
						oldValue = Array.get(propValue, arrayIndex);
					}
					Object convertedValue = convertIfNecessary(propertyName, oldValue, pv.getValue(),
							requiredType, TypeDescriptor.nested(property(pd), tokens.keys.length));
					Array.set(propValue, arrayIndex, convertedValue);
				}
				catch (IndexOutOfBoundsException ex) {
					throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
							"Invalid array index in property path '" + propertyName + "'", ex);
				}
			}
			else if (propValue instanceof List) {
				PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
				Class<?> requiredType = GenericCollectionTypeResolver.getCollectionReturnType(
						pd.getReadMethod(), tokens.keys.length);
				List<Object> list = (List<Object>) propValue;
				int index = Integer.parseInt(key);
				Object oldValue = null;
				if (isExtractOldValueForEditor() && index < list.size()) {
					oldValue = list.get(index);
				}
				Object convertedValue = convertIfNecessary(propertyName, oldValue, pv.getValue(),
						requiredType, TypeDescriptor.nested(property(pd), tokens.keys.length));
				int size = list.size();
				if (index >= size && index < this.autoGrowCollectionLimit) {
					for (int i = size; i < index; i++) {
						try {
							list.add(null);
						}
						catch (NullPointerException ex) {
							throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
									"Cannot set element with index " + index + " in List of size " +
									size + ", accessed using property path '" + propertyName +
									"': List does not support filling up gaps with null elements");
						}
					}
					list.add(convertedValue);
				}
				else {
					try {
						list.set(index, convertedValue);
					}
					catch (IndexOutOfBoundsException ex) {
						throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
								"Invalid list index in property path '" + propertyName + "'", ex);
					}
				}
			}
			else if (propValue instanceof Map) {
				PropertyDescriptor pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
				Class<?> mapKeyType = GenericCollectionTypeResolver.getMapKeyReturnType(
						pd.getReadMethod(), tokens.keys.length);
				Class<?> mapValueType = GenericCollectionTypeResolver.getMapValueReturnType(
						pd.getReadMethod(), tokens.keys.length);
				Map<Object, Object> map = (Map<Object, Object>) propValue;
				// IMPORTANT: Do not pass full property name in here - property editors
				// must not kick in for map keys but rather only for map values.
				TypeDescriptor typeDescriptor = (mapKeyType != null ?
						TypeDescriptor.valueOf(mapKeyType) : TypeDescriptor.valueOf(Object.class));
				Object convertedMapKey = convertIfNecessary(null, null, key, mapKeyType, typeDescriptor);
				Object oldValue = null;
				if (isExtractOldValueForEditor()) {
					oldValue = map.get(convertedMapKey);
				}
				// Pass full property name and old value in here, since we want full
				// conversion ability for map values.
				Object convertedMapValue = convertIfNecessary(propertyName, oldValue, pv.getValue(),
						mapValueType, TypeDescriptor.nested(property(pd), tokens.keys.length));
				map.put(convertedMapKey, convertedMapValue);
			}
			else {
				throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
						"Property referenced in indexed property path '" + propertyName +
						"' is neither an array nor a List nor a Map; returned value was [" + pv.getValue() + "]");
			}
		}

		else {
			PropertyDescriptor pd = pv.resolvedDescriptor;
			if (pd == null || !pd.getWriteMethod().getDeclaringClass().isInstance(this.object)) {
				pd = getCachedIntrospectionResults().getPropertyDescriptor(actualName);
				if (pd == null || pd.getWriteMethod() == null) {
					if (pv.isOptional()) {
						logger.debug("Ignoring optional value for property '" + actualName +
								"' - property not found on bean class [" + getRootClass().getName() + "]");
						return;
					}
					else {
						PropertyMatches matches = PropertyMatches.forProperty(propertyName, getRootClass());
						throw new NotWritablePropertyException(
								getRootClass(), this.nestedPath + propertyName,
								matches.buildErrorMessage(), matches.getPossibleMatches());
					}
				}
				pv.getOriginalPropertyValue().resolvedDescriptor = pd;
			}

			Object oldValue = null;
			try {
				Object originalValue = pv.getValue();
				Object valueToApply = originalValue;
				if (!Boolean.FALSE.equals(pv.conversionNecessary)) {
					if (pv.isConverted()) {
						valueToApply = pv.getConvertedValue();
					}
					else {
						if (isExtractOldValueForEditor() && pd.getReadMethod() != null) {
							final Method readMethod = pd.getReadMethod();
							if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers()) &&
									!readMethod.isAccessible()) {
								if (System.getSecurityManager()!= null) {
									AccessController.doPrivileged(new PrivilegedAction<Object>() {
										public Object run() {
											readMethod.setAccessible(true);
											return null;
										}
									});
								}
								else {
									readMethod.setAccessible(true);
								}
							}
							try {
								if (System.getSecurityManager() != null) {
									oldValue = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
										public Object run() throws Exception {
											return readMethod.invoke(object);
										}
									}, acc);
								}
								else {
									oldValue = readMethod.invoke(object);
								}
							}
							catch (Exception ex) {
								if (ex instanceof PrivilegedActionException) {
									ex = ((PrivilegedActionException) ex).getException();
								}
								if (logger.isDebugEnabled()) {
									logger.debug("Could not read previous value of property '" +
											this.nestedPath + propertyName + "'", ex);
								}
							}
						}
						valueToApply = convertForProperty(
								propertyName, oldValue, originalValue, new TypeDescriptor(property(pd)));
					}
					pv.getOriginalPropertyValue().conversionNecessary = (valueToApply != originalValue);
				}
				final Method writeMethod = (pd instanceof GenericTypeAwarePropertyDescriptor ?
						((GenericTypeAwarePropertyDescriptor) pd).getWriteMethodForActualAccess() :
						pd.getWriteMethod());
				if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers()) && !writeMethod.isAccessible()) {
					if (System.getSecurityManager()!= null) {
						AccessController.doPrivileged(new PrivilegedAction<Object>() {
							public Object run() {
								writeMethod.setAccessible(true);
								return null;
							}
						});
					}
					else {
						writeMethod.setAccessible(true);
					}
				}
				final Object value = valueToApply;
				if (System.getSecurityManager() != null) {
					try {
						AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
							public Object run() throws Exception {
								writeMethod.invoke(object, value);
								return null;
							}
						}, acc);
					}
					catch (PrivilegedActionException ex) {
						throw ex.getException();
					}
				}
				else {
					writeMethod.invoke(this.object, value);
				}
			}
			catch (TypeMismatchException ex) {
				throw ex;
			}
			catch (InvocationTargetException ex) {
				PropertyChangeEvent propertyChangeEvent =
						new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
				if (ex.getTargetException() instanceof ClassCastException) {
					throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
				}
				else {
					throw new MethodInvocationException(propertyChangeEvent, ex.getTargetException());
				}
			}
			catch (Exception ex) {
				PropertyChangeEvent pce =
						new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, oldValue, pv.getValue());
				throw new MethodInvocationException(pce, ex);
			}
		}
	}

	// Inner class for internal use：比如：propertyName是listOfMaps[0]['luckyNumber']，则它对应的PropertyTokenHolder是
	private static class PropertyTokenHolder {
		public String canonicalName;// listOfMaps[0]['luckyNumber']
		public String actualName;// listOfMaps
		public String[] keys;// {"0","luckyNumber"}
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName());
		if (this.object != null) {
			sb.append(": wrapping object [").append(ObjectUtils.identityToString(this.object)).append("]");
		}
		else {
			sb.append(": no wrapped object set");
		}
		return sb.toString();
	}

}
