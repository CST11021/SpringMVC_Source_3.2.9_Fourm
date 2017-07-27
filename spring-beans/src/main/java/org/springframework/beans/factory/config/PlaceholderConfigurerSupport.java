/*
 * Copyright 2002-2013 the original author or authors.
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

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.util.StringValueResolver;

/**
 * Abstract base class for property resource configurers that resolve placeholders
 * in bean definition property values. Implementations <em>pull</em> values from a
 * properties file or other {@linkplain org.springframework.core.env.PropertySource
 * property source} into bean definitions.
 *
 * <p>The default placeholder syntax follows the Ant / Log4J / JSP EL style:
 *
 *<pre class="code">${...}</pre>
 *
 * Example XML bean definition:
 *
 *<pre class="code">{@code
 *<bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"/>
 *    <property name="driverClassName" value="}${driver}{@code "/>
 *    <property name="url" value="jdbc:}${dbname}{@code "/>
 *</bean>
 *}</pre>
 *
 * Example properties file:
 *
 * <pre class="code"> driver=com.mysql.jdbc.Driver
 * dbname=mysql:mydb</pre>
 *
 * Annotated bean definitions may take advantage of property replacement using
 * the {@link org.springframework.beans.factory.annotation.Value @Value} annotation:
 *
 *<pre class="code">@Value("${person.age}")</pre>
 *
 * Implementations check simple property values, lists, maps, props, and bean names
 * in bean references. Furthermore, placeholder values can also cross-reference
 * other placeholders, like:
 *
 *<pre class="code">rootPath=myrootdir
 *subPath=${rootPath}/subdir</pre>
 *
 * In contrast to {@link PropertyOverrideConfigurer}, subclasses of this type allow
 * filling in of explicit placeholders in bean definitions.
 *
 * <p>If a configurer cannot resolve a placeholder, a {@link BeanDefinitionStoreException}
 * will be thrown. If you want to check against multiple properties files, specify multiple
 * resources via the {@link #setLocations locations} property. You can also define multiple
 * configurers, each with its <em>own</em> placeholder syntax. Use {@link
 * #ignoreUnresolvablePlaceholders} to intentionally suppress throwing an exception if a
 * placeholder cannot be resolved.
 *
 * <p>Default property values can be defined globally for each configurer instance
 * via the {@link #setProperties properties} property, or on a property-by-property basis
 * using the default value separator which is {@code ":"} by default and
 * customizable via {@link #setValueSeparator(String)}.
 *
 * <p>Example XML property with default value:
 *
 *<pre class="code">{@code
 *  <property name="url" value="jdbc:}${dbname:defaultdb}{@code "/>
 *}</pre>
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see PropertyPlaceholderConfigurer
 * @see org.springframework.context.support.PropertySourcesPlaceholderConfigurer
 */
public abstract class PlaceholderConfigurerSupport extends PropertyResourceConfigurer implements BeanNameAware, BeanFactoryAware {

	// 默认的占位符前缀
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";
	// 默认的占位符后缀
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";
	// 默认值的分隔符
	public static final String DEFAULT_VALUE_SEPARATOR = ":";


	protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;
	protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;
	protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;

	// 是否忽略没有解析的占位符
	protected boolean ignoreUnresolvablePlaceholders = false;

	protected String nullValue;
	private BeanFactory beanFactory;
	private String beanName;

	// 该方法的作用是：从beanFactoryToProcess工厂中访问每个BeanDefinition，并替换掉每个BeanDefinition中使用到的${...}占位符，该操作委托给了BeanDefinitionVisitor来实现
	protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess, StringValueResolver valueResolver) {

		BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

		String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
		for (String curName : beanNames) {
			// Check that we're not parsing our own bean definition, to avoid failing on unresolvable placeholders in properties file locations.
			if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
				BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
				try {
					// 翻译BeanDefinition中使用的占位符，并重新赋值
					visitor.visitBeanDefinition(bd);
				}
				catch (Exception ex) {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
				}
			}
		}

		// New in Spring 2.5: resolve placeholders in alias target names and aliases as well.
		beanFactoryToProcess.resolveAliases(valueResolver);

		// New in Spring 3.0: resolve placeholders in embedded values such as annotation attributes.
		beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
	}


	// setter ...
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}
	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}
	public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
