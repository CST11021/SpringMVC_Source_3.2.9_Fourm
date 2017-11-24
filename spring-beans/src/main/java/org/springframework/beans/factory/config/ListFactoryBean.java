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

package org.springframework.beans.factory.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.GenericCollectionTypeResolver;

/**
 * Simple factory for shared List instances. Allows for central setup
 * of Lists via the "list" element in XML bean definitions.
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see SetFactoryBean
 * @see MapFactoryBean
 */

/**
 ListFactoryBean”类为开发者提供了一种在Spring的bean配置文件中创建一个具体的列表集合类(ArrayList和LinkedList)。
 这里有一个 ListFactoryBean 示例，在运行时它将实例化一个ArrayList，并注入到一个 bean 属性。

 <bean id="CustomerBean" class="com.yiibai.common.Customer">
	 <property name="lists">
		 <bean class="org.springframework.beans.factory.config.ListFactoryBean">
			 <property name="targetListClass">
			 	<value>java.util.ArrayList</value>
			 </property>
			 <property name="sourceList">
				 <list>
					 <value>one</value>
					 <value>2</value>
					 <value>three</value>
				 </list>
			 </property>
		 </bean>
	 </property>
 </bean>
 另外，还可以使用 util 模式和<util:list> 来达到同样的目的：
 <bean id="CustomerBean" class="com.yiibai.common.Customer">
	 <property name="lists">
		 <util:list list-class="java.util.ArrayList">
			 <value>one</value>
			 <value>2</value>
			 <value>three</value>
		 </util:list>
	 </property>
 </bean>
 */
public class ListFactoryBean extends AbstractFactoryBean<List> {

	private List sourceList;
	private Class targetListClass;

	public void setSourceList(List sourceList) {
		this.sourceList = sourceList;
	}
	public void setTargetListClass(Class targetListClass) {
		if (targetListClass == null) {
			throw new IllegalArgumentException("'targetListClass' must not be null");
		}
		if (!List.class.isAssignableFrom(targetListClass)) {
			throw new IllegalArgumentException("'targetListClass' must implement [java.util.List]");
		}
		this.targetListClass = targetListClass;
	}


	@Override
	public Class<List> getObjectType() {
		return List.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List createInstance() {
		if (this.sourceList == null) {
			throw new IllegalArgumentException("'sourceList' is required");
		}
		List result = null;
		if (this.targetListClass != null) {
			result = (List) BeanUtils.instantiateClass(this.targetListClass);
		}
		else {
			result = new ArrayList(this.sourceList.size());
		}
		Class valueType = null;
		if (this.targetListClass != null) {
			valueType = GenericCollectionTypeResolver.getCollectionType(this.targetListClass);
		}
		if (valueType != null) {
			TypeConverter converter = getBeanTypeConverter();
			for (Object elem : this.sourceList) {
				result.add(converter.convertIfNecessary(elem, valueType));
			}
		}
		else {
			result.addAll(this.sourceList);
		}
		return result;
	}

}
