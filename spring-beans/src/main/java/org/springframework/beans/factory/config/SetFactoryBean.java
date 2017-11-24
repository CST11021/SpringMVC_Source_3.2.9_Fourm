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

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.GenericCollectionTypeResolver;

/**
 使用示例：

 <bean id="CustomerBean" class="com.mkyong.common.Customer">
	 <property name="sets">
		 <bean class="org.springframework.beans.factory.config.SetFactoryBean">
			 <property name="targetSetClass">
			 	<value>java.util.HashSet</value>
			 </property>
			 <property name="sourceSet">
				 <list>
					 <value>1</value>
					 <value>2</value>
					 <value>3</value>
				 </list>
			 </property>
		 </bean>
	 </property>
 </bean>

 使用util命名空间：
 <bean id="CustomerBean" class="com.mkyong.common.Customer">
	 <property name="sets">
		 <util:set set-class="java.util.HashSet">
			 <value>1</value>
			 <value>2</value>
			 <value>3</value>
		 </util:set>
	 </property>
 </bean>

 作者：lovePython
 链接：http://www.jianshu.com/p/90710a6e918c
 來源：简书
 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

 */
public class SetFactoryBean extends AbstractFactoryBean<Set> {

	private Set sourceSet;
	private Class targetSetClass;

	public void setSourceSet(Set sourceSet) {
		this.sourceSet = sourceSet;
	}
	public void setTargetSetClass(Class targetSetClass) {
		if (targetSetClass == null) {
			throw new IllegalArgumentException("'targetSetClass' must not be null");
		}
		if (!Set.class.isAssignableFrom(targetSetClass)) {
			throw new IllegalArgumentException("'targetSetClass' must implement [java.util.Set]");
		}
		this.targetSetClass = targetSetClass;
	}

	@Override
	public Class<Set> getObjectType() {
		return Set.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Set createInstance() {
		if (this.sourceSet == null) {
			throw new IllegalArgumentException("'sourceSet' is required");
		}
		Set result = null;
		if (this.targetSetClass != null) {
			result = (Set) BeanUtils.instantiateClass(this.targetSetClass);
		}
		else {
			result = new LinkedHashSet(this.sourceSet.size());
		}
		Class valueType = null;
		if (this.targetSetClass != null) {
			valueType = GenericCollectionTypeResolver.getCollectionType(this.targetSetClass);
		}
		if (valueType != null) {
			TypeConverter converter = getBeanTypeConverter();
			for (Object elem : this.sourceSet) {
				result.add(converter.convertIfNecessary(elem, valueType));
			}
		}
		else {
			result.addAll(this.sourceSet);
		}
		return result;
	}

}
