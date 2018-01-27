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

import java.lang.reflect.Field;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link FactoryBean} which retrieves a static or non-static field value.
 *
 * <p>Typically used for retrieving public static final constants. Usage example:
 *
 * <pre class="code">// standard definition for exposing a static field, specifying the "staticField" property
 * &lt;bean id="myField" class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean"&gt;
 *   &lt;property name="staticField" value="java.sql.Connection.TRANSACTION_SERIALIZABLE"/&gt;
 * &lt;/bean&gt;
 *
 * // convenience version that specifies a static field pattern as bean name
 * &lt;bean id="java.sql.Connection.TRANSACTION_SERIALIZABLE"
 *       class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean"/&gt;</pre>
 * </pre>
 *
 * <p>If you are using Spring 2.0, you can also use the following style of configuration for
 * public static fields.
 *
 * <pre class="code">&lt;util:constant static-field="java.sql.Connection.TRANSACTION_SERIALIZABLE"/&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setStaticField
 */

/**
 通过FieldRetrievingFactoryBean类，可以将其他Bean的Field值注入给指定的Bean，或者直接定义新的Bean。下面是配置片段：
 <bean id="son" class="com.abc.service.Son">
	 <property name="age">
	 	<bean id="java.sql.connection.TRANSACTION_SERIALIZABLE" class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean" />
	 </property>
 </bean>

 在这个配置中，son对象的age的值，等于java.sql.Connection.TRANSACTION_SERIALIZABLE的值。在上面的定义中，定义FieldRetrievingFactoryBean
 这个FactoryBean时，指定的id并不是该Bean实例的唯一标识，而是指定Field的表达式（即将要被取出来的值）。

 注意：Field既可以是静态的，也可以是非静态的。上面的配置片段指定的Field表达式是静态的，因此可以通过类名直接访问。
 如果Field值是非静态的，则应该通过容器中已经存在的Bean来访问，即Field表达式的第一个短语应该是容器中已经存在的Bean，Field
 值也可以定义成Bean实例，例如，在配置文件中增加下面一段：
 <bean id="age" class="org.springframework.beans.factory.config.FieldRetrievingFactoryBean">
	 <!-- targetClass指定Field所在的目标类 -->
	 <property name="targetClass" value="java.sql.Connection" />
	 <!-- targetField指定Field名 -->
	 <property name="targetField" value="TRANSACTION_SERIALIZABLE" />
 </bean>

 使用FieldRetrievingFactoryBean获取Field值时，必须指定如下两个属性：
 1、targetClass或targetObject：分别用于指定Field值所在的目标累或目标对象。如果需要获得的Field是静态的，则使用targetClass
 	指定目标累；如果Field是非静态的，则使用targetObject指定目标对象
 2、targetField：指定目标类或目标对象的Field名
 */
public class FieldRetrievingFactoryBean
		implements FactoryBean<Object>, BeanNameAware, BeanClassLoaderAware, InitializingBean {

	private Class targetClass;
	private Object targetObject;
	private String targetField;
	private String staticField;
	private String beanName;
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
	// the field we will retrieve
	private Field fieldObject;


	public void setBeanName(String beanName) {
		this.beanName = StringUtils.trimAllWhitespace(BeanFactoryUtils.originalBeanName(beanName));
	}
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}
	public void afterPropertiesSet() throws ClassNotFoundException, NoSuchFieldException {
		if (this.targetClass != null && this.targetObject != null) {
			throw new IllegalArgumentException("Specify either targetClass or targetObject, not both");
		}

		if (this.targetClass == null && this.targetObject == null) {
			if (this.targetField != null) {
				throw new IllegalArgumentException(
						"Specify targetClass or targetObject in combination with targetField");
			}

			// If no other property specified, consider bean name as static field expression.
			if (this.staticField == null) {
				this.staticField = this.beanName;
			}

			// Try to parse static field into class and field.
			int lastDotIndex = this.staticField.lastIndexOf('.');
			if (lastDotIndex == -1 || lastDotIndex == this.staticField.length()) {
				throw new IllegalArgumentException(
						"staticField must be a fully qualified class plus static field name: " +
						"e.g. 'example.MyExampleClass.MY_EXAMPLE_FIELD'");
			}
			String className = this.staticField.substring(0, lastDotIndex);
			String fieldName = this.staticField.substring(lastDotIndex + 1);
			this.targetClass = ClassUtils.forName(className, this.beanClassLoader);
			this.targetField = fieldName;
		}

		else if (this.targetField == null) {
			// Either targetClass or targetObject specified.
			throw new IllegalArgumentException("targetField is required");
		}

		// Try to get the exact method first.
		Class targetClass = (this.targetObject != null) ? this.targetObject.getClass() : this.targetClass;
		this.fieldObject = targetClass.getField(this.targetField);
	}
	public Object getObject() throws IllegalAccessException {
		if (this.fieldObject == null) {
			throw new FactoryBeanNotInitializedException();
		}
		ReflectionUtils.makeAccessible(this.fieldObject);
		if (this.targetObject != null) {
			// instance field
			return this.fieldObject.get(this.targetObject);
		}
		else{
			// class field
			return this.fieldObject.get(null);
		}
	}
	public Class<?> getObjectType() {
		return (this.fieldObject != null ? this.fieldObject.getType() : null);
	}
	public boolean isSingleton() {
		return false;
	}


	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
	}
	public Class getTargetClass() {
		return targetClass;
	}
	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}
	public Object getTargetObject() {
		return this.targetObject;
	}
	public void setTargetField(String targetField) {
		this.targetField = StringUtils.trimAllWhitespace(targetField);
	}
	public String getTargetField() {
		return this.targetField;
	}
	public void setStaticField(String staticField) {
		this.staticField = StringUtils.trimAllWhitespace(staticField);
	}


}
