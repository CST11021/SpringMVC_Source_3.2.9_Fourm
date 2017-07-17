
package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.io.Serializable;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

// 用来保存单个bean属性的信息和值。
// 在这里使用一个对象，而不仅仅是将所有属性存储在以属性名称为键的映射中，允许更灵活，并且能够以优化的方式处理索引属性等。
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

	private final String name;
	private final Object value;
	private Object source;
	private boolean optional = false;
	private boolean converted = false;
	private Object convertedValue;

	//** Package-visible field that indicates whether conversion is necessary */
	volatile Boolean conversionNecessary;
	//** Package-visible field for caching the resolved property path tokens */
	volatile Object resolvedTokens;
	//** Package-visible field for caching the resolved PropertyDescriptor */
	volatile PropertyDescriptor resolvedDescriptor;

	// 构造器
	public PropertyValue(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = original.getValue();
		this.source = original.getSource();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		this.resolvedDescriptor = original.resolvedDescriptor;
		copyAttributesFrom(original);
	}
	public PropertyValue(PropertyValue original, Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.source = original;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		this.resolvedDescriptor = original.resolvedDescriptor;
		copyAttributesFrom(original);
	}

	// 是否进行了类型转换
	public synchronized boolean isConverted() {
		return this.converted;
	}

	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		while (original.source instanceof PropertyValue && original.source != original) {
			original = (PropertyValue) original.source;
		}
		return original;
	}

	// getter and setter ...
	public String getName() {
		return this.name;
	}
	public Object getValue() {
		return this.value;
	}
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	public boolean isOptional() {
		return this.optional;
	}
	public synchronized void setConvertedValue(Object value) {
		this.converted = true;
		this.convertedValue = value;
	}
	public synchronized Object getConvertedValue() {
		return this.convertedValue;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherPv.value) &&
				ObjectUtils.nullSafeEquals(this.source, otherPv.source));
	}
	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
	}
	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}

}
