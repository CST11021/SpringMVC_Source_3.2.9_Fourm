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

import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.core.Constants;
import org.springframework.core.SpringProperties;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;

// 比如从jdbc.properties读取属性值，然后赋值给<bean> xml中的类似${password}的占位符。这时就要用到BeanFactoryPostProcessor的实现类。
public class PropertyPlaceholderConfigurer extends PlaceholderConfigurerSupport {

	// 从不检查系统属性
	public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;
	// 检查系统属性是否在指定的属性中不可解析。这是默认的。
	public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;
	// 首先检查系统属性，然后再尝试指定的属性。这允许系统属性覆盖任何其他属性源。
	public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;

	private static final Constants constants = new Constants(PropertyPlaceholderConfigurer.class);
	private int systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;
	private boolean searchSystemEnvironment = !SpringProperties.getFlag(AbstractEnvironment.IGNORE_GETENV_PROPERTY_NAME);


	// 使用给定的属性解析给定的占位符，根据给定的模式执行系统属性检查。
	protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
		String propVal = null;
		if (systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
			propVal = resolveSystemProperty(placeholder);
		}
		if (propVal == null) {
			propVal = resolvePlaceholder(placeholder, props);
		}
		if (propVal == null && systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
			propVal = resolveSystemProperty(placeholder);
		}
		return propVal;
	}
	protected String resolvePlaceholder(String placeholder, Properties props) {
		return props.getProperty(placeholder);
	}

	// 根据key获取系统属性
	protected String resolveSystemProperty(String key) {
		try {
			String value = System.getProperty(key);
			if (value == null && this.searchSystemEnvironment) {
				value = System.getenv(key);
			}
			return value;
		}
		catch (Throwable ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Could not access system property '" + key + "': " + ex);
			}
			return null;
		}
	}

	 //在给定的bean工厂中访问每个BeanDefinition并尝试替换${...}占位符。
	 //Spring启动的时候会去调用所有实现了后处理器的Bean的后处理方法，当我们配置了
	 //<property-placeholder>标签，Spring 就行注册了一个PropertyPlaceholderConfigurer的Bean，这样PropertyPlaceholderConfigurer的后处理方法会来调用
	 //该方法，进行BeanDefinition中占位符的翻译。
	@Override
	protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
		StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(props);
		this.doProcessProperties(beanFactoryToProcess, valueResolver);
	}

	// 使用props替换/解析 strVal 中的占位符
	@Deprecated
	protected String parseStringValue(String strVal, Properties props, Set<?> visitedPlaceholders) {
		PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
		PlaceholderResolver resolver = new PropertyPlaceholderConfigurerResolver(props);
		return helper.replacePlaceholders(strVal, resolver);
	}

	// 设置systemPropertiesMode
	public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
		this.systemPropertiesMode = constants.asNumber(constantName).intValue();
	}
	public void setSystemPropertiesMode(int systemPropertiesMode) {
		this.systemPropertiesMode = systemPropertiesMode;
	}
	public void setSearchSystemEnvironment(boolean searchSystemEnvironment) {
		this.searchSystemEnvironment = searchSystemEnvironment;
	}

	// 占位符解析器
	private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

		private final PropertyPlaceholderHelper helper;
		private final PlaceholderResolver resolver;

		public PlaceholderResolvingStringValueResolver(Properties props) {
			this.helper = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
			this.resolver = new PropertyPlaceholderConfigurerResolver(props);
		}

		public String resolveStringValue(String strVal) throws BeansException {
			String value = this.helper.replacePlaceholders(strVal, this.resolver);
			return (value.equals(nullValue) ? null : value);
		}
	}
	private class PropertyPlaceholderConfigurerResolver implements PlaceholderResolver {

		private final Properties props;

		private PropertyPlaceholderConfigurerResolver(Properties props) {
			this.props = props;
		}

		public String resolvePlaceholder(String placeholderName) {
			return PropertyPlaceholderConfigurer.this.resolvePlaceholder(placeholderName, props, systemPropertiesMode);
		}
	}

}
