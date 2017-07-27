/*
 * Copyright 2002-2010 the original author or authors.
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.io.support.PropertiesLoaderSupport;
import org.springframework.util.ObjectUtils;

/**
 * Allows for configuration of individual bean property values from a property resource,
 * i.e. a properties file. Useful for custom config files targeted at system
 * administrators that override bean properties configured in the application context.
 *
 * <p>Two concrete implementations are provided in the distribution:
 * <ul>
 * <li>{@link PropertyOverrideConfigurer} for "beanName.property=value" style overriding
 * (<i>pushing</i> values from a properties file into bean definitions)
 * <li>{@link PropertyPlaceholderConfigurer} for replacing "${...}" placeholders
 * (<i>pulling</i> values from a properties file into bean definitions)
 * </ul>
 *
 * <p>Property values can be converted after reading them in, through overriding
 * the {@link #convertPropertyValue} method. For example, encrypted values
 * can be detected and decrypted accordingly before processing them.
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see PropertyPlaceholderConfigurer
 */
// 该在继承 PropertiesLoaderSupport 基础上实现了 BeanFactoryPostProcessor 接口，表示可从外部加载配置属性，并应用到BeanFactory中
public abstract class PropertyResourceConfigurer extends PropertiesLoaderSupport implements BeanFactoryPostProcessor, PriorityOrdered {

	private int order = Ordered.LOWEST_PRECEDENCE;  // default: same as non-Ordered

	// 将外部配置的属性文件处理过后应用到BeanFactory中
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		try {
			// 返回一个合并的Properties实例，该实例包含在这个FactoryBean上设置的已加载的属性和属性。
			Properties mergedProps = mergeProperties();
			// 1、做转换
			convertProperties(mergedProps);
			// 2、应用后处理方法
			processProperties(beanFactory, mergedProps);
		}
		catch (IOException ex) {
			throw new BeanInitializationException("Could not load properties", ex);
		}
	}

	// 1、转换属性值(有时候我们会在.properties文件配置加密后字符串，我们可通过子类重写方法来进行解密操作)
	protected void convertProperties(Properties props) {
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = convertProperty(propertyName, propertyValue);
			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}
	protected String convertProperty(String propertyName, String propertyValue) {
		return convertPropertyValue(propertyValue);
	}
	protected String convertPropertyValue(String originalValue) {
		return originalValue;
	}

	// 2、将给定的属性应用到给定的BeanFactory。
	protected abstract void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException;


	// getter and setter
	public void setOrder(int order) {
		this.order = order;
	}
	public int getOrder() {
		return this.order;
	}
}
