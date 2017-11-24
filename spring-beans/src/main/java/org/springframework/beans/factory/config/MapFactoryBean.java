/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.GenericCollectionTypeResolver;

/**
 使用示例：

 <bean id="partnerBean" class="org.springframework.beans.factory.config.MapFactoryBean">
	 <property name="targetMapClass">
		 <value>java.util.HashMap</value>
 	</property>
 	<property name="sourceMap">
		 <map>
			 <entry key="base" value-ref="baseService"></entry>
			 <entry key="baidu" value-ref="baiduService"></entry>
			 <entry key="tencent" value-ref="tencentService"></entry>
			 <entry key="taobao" value-ref="taobaoService"></entry>
		 </map>
	 </property>
 </bean>

 */
public class MapFactoryBean extends AbstractFactoryBean<Map> {

	private Map<?, ?> sourceMap;
	private Class targetMapClass;


	public void setSourceMap(Map sourceMap) {
		this.sourceMap = sourceMap;
	}
	public void setTargetMapClass(Class targetMapClass) {
		if (targetMapClass == null) {
			throw new IllegalArgumentException("'targetMapClass' must not be null");
		}
		if (!Map.class.isAssignableFrom(targetMapClass)) {
			throw new IllegalArgumentException("'targetMapClass' must implement [java.util.Map]");
		}
		this.targetMapClass = targetMapClass;
	}

	@Override
	public Class<Map> getObjectType() {
		return Map.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Map createInstance() {
		if (this.sourceMap == null) {
			throw new IllegalArgumentException("'sourceMap' is required");
		}
		Map result = null;
		if (this.targetMapClass != null) {
			result = (Map) BeanUtils.instantiateClass(this.targetMapClass);
		}
		else {
			result = new LinkedHashMap(this.sourceMap.size());
		}
		Class keyType = null;
		Class valueType = null;
		if (this.targetMapClass != null) {
			keyType = GenericCollectionTypeResolver.getMapKeyType(this.targetMapClass);
			valueType = GenericCollectionTypeResolver.getMapValueType(this.targetMapClass);
		}
		if (keyType != null || valueType != null) {
			TypeConverter converter = getBeanTypeConverter();
			for (Map.Entry entry : this.sourceMap.entrySet()) {
				Object convertedKey = converter.convertIfNecessary(entry.getKey(), keyType);
				Object convertedValue = converter.convertIfNecessary(entry.getValue(), valueType);
				result.put(convertedKey, convertedValue);
			}
		}
		else {
			result.putAll(this.sourceMap);
		}
		return result;
	}

}
