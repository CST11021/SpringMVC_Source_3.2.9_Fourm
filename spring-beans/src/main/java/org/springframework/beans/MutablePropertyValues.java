
package org.springframework.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;


@SuppressWarnings("serial")
public class MutablePropertyValues implements PropertyValues, Serializable {

	private final List<PropertyValue> propertyValueList;
	private Set<String> processedProperties;
	private volatile boolean converted = false;


	public MutablePropertyValues() {
		this.propertyValueList = new ArrayList<PropertyValue>(0);
	}
	public MutablePropertyValues(PropertyValues original) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		if (original != null) {
			PropertyValue[] pvs = original.getPropertyValues();
			this.propertyValueList = new ArrayList<PropertyValue>(pvs.length);
			for (PropertyValue pv : pvs) {
				this.propertyValueList.add(new PropertyValue(pv));
			}
		}
		else {
			this.propertyValueList = new ArrayList<PropertyValue>(0);
		}
	}
	public MutablePropertyValues(Map<?, ?> original) {
		// We can optimize this because it's all new:
		// There is no replacement of existing property values.
		if (original != null) {
			this.propertyValueList = new ArrayList<PropertyValue>(original.size());
			for (Map.Entry entry : original.entrySet()) {
				this.propertyValueList.add(new PropertyValue(entry.getKey().toString(), entry.getValue()));
			}
		}
		else {
			this.propertyValueList = new ArrayList<PropertyValue>(0);
		}
	}
	public MutablePropertyValues(List<PropertyValue> propertyValueList) {
		this.propertyValueList =
				(propertyValueList != null ? propertyValueList : new ArrayList<PropertyValue>());
	}


	// 返回 propertyValueList.size()
	public int size() {
		return this.propertyValueList.size();
	}
	public List<PropertyValue> getPropertyValueList() {
		return this.propertyValueList;
	}

	// 向 propertyValueList 添加 PropertyValue
	public MutablePropertyValues addPropertyValues(PropertyValues other) {
		if (other != null) {
			PropertyValue[] pvs = other.getPropertyValues();
			for (PropertyValue pv : pvs) {
				addPropertyValue(new PropertyValue(pv));
			}
		}
		return this;
	}
	public MutablePropertyValues addPropertyValues(Map<?, ?> other) {
		if (other != null) {
			for (Map.Entry<?, ?> entry : other.entrySet()) {
				addPropertyValue(new PropertyValue(entry.getKey().toString(), entry.getValue()));
			}
		}
		return this;
	}
	public MutablePropertyValues addPropertyValue(PropertyValue pv) {
		for (int i = 0; i < this.propertyValueList.size(); i++) {
			PropertyValue currentPv = this.propertyValueList.get(i);
			if (currentPv.getName().equals(pv.getName())) {
				pv = mergeIfRequired(pv, currentPv);
				setPropertyValueAt(pv, i);
				return this;
			}
		}
		this.propertyValueList.add(pv);
		return this;
	}
	public void addPropertyValue(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
	}
	public MutablePropertyValues add(String propertyName, Object propertyValue) {
		addPropertyValue(new PropertyValue(propertyName, propertyValue));
		return this;
	}
	public void setPropertyValueAt(PropertyValue pv, int i) {
		this.propertyValueList.set(i, pv);
	}


	private PropertyValue mergeIfRequired(PropertyValue newPv, PropertyValue currentPv) {
		Object value = newPv.getValue();
		if (value instanceof Mergeable) {
			Mergeable mergeable = (Mergeable) value;
			if (mergeable.isMergeEnabled()) {
				Object merged = mergeable.merge(currentPv.getValue());
				return new PropertyValue(newPv.getName(), merged);
			}
		}
		return newPv;
	}


	public void removePropertyValue(PropertyValue pv) {
		this.propertyValueList.remove(pv);
	}
	public void removePropertyValue(String propertyName) {
		this.propertyValueList.remove(getPropertyValue(propertyName));
	}


	// PropertyValues 接口
	public PropertyValue[] getPropertyValues() {
		return this.propertyValueList.toArray(new PropertyValue[this.propertyValueList.size()]);
	}
	public PropertyValue getPropertyValue(String propertyName) {
		for (PropertyValue pv : this.propertyValueList) {
			if (pv.getName().equals(propertyName)) {
				return pv;
			}
		}
		return null;
	}
	public PropertyValues changesSince(PropertyValues old) {
		MutablePropertyValues changes = new MutablePropertyValues();
		if (old == this) {
			return changes;
		}

		// for each property value in the new set
		for (PropertyValue newPv : this.propertyValueList) {
			// if there wasn't an old one, add it
			PropertyValue pvOld = old.getPropertyValue(newPv.getName());
			if (pvOld == null) {
				changes.addPropertyValue(newPv);
			}
			else if (!pvOld.equals(newPv)) {
				// it's changed
				changes.addPropertyValue(newPv);
			}
		}
		return changes;
	}
	public boolean contains(String propertyName) {
		return (getPropertyValue(propertyName) != null ||
				(this.processedProperties != null && this.processedProperties.contains(propertyName)));
	}
	public boolean isEmpty() {
		return this.propertyValueList.isEmpty();
	}


	// 向this.processedProperties 添加一个 propertyName
	public void registerProcessedProperty(String propertyName) {
		if (this.processedProperties == null) {
			this.processedProperties = new HashSet<String>();
		}
		this.processedProperties.add(propertyName);
	}


	public void setConverted() {
		this.converted = true;
	}
	public boolean isConverted() {
		return this.converted;
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MutablePropertyValues)) {
			return false;
		}
		MutablePropertyValues that = (MutablePropertyValues) other;
		return this.propertyValueList.equals(that.propertyValueList);
	}
	@Override
	public int hashCode() {
		return this.propertyValueList.hashCode();
	}
	@Override
	public String toString() {
		PropertyValue[] pvs = getPropertyValues();
		StringBuilder sb = new StringBuilder("PropertyValues: length=").append(pvs.length);
		if (pvs.length > 0) {
			sb.append("; ").append(StringUtils.arrayToDelimitedString(pvs, "; "));
		}
		return sb.toString();
	}

}
