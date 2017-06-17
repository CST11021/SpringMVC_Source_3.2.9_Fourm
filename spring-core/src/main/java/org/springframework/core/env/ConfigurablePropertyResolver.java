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

package org.springframework.core.env;

import org.springframework.core.convert.support.ConfigurableConversionService;

/**
 * Configuration interface to be implemented by most if not all {@link PropertyResolver
 * PropertyResolver} types. Provides facilities for accessing and customizing the
 * {@link org.springframework.core.convert.ConversionService ConversionService} used when
 * converting property values from one type to another.
 *
 * @author Chris Beams
 * @since 3.1
 */
public interface ConfigurablePropertyResolver extends PropertyResolver {

	// 类型转换器 getter and setter
	ConfigurableConversionService getConversionService();
	void setConversionService(ConfigurableConversionService conversionService);

	// 设置占位符前缀
	void setPlaceholderPrefix(String placeholderPrefix);
	// 设置占位符后缀
	void setPlaceholderSuffix(String placeholderSuffix);

	// 设置参数分隔符
	void setValueSeparator(String valueSeparator);

	// 设置容器启动时所需的必要参数
	void setRequiredProperties(String... requiredProperties);

	// 校验系统参数是否配置必要的参数
	void validateRequiredProperties() throws MissingRequiredPropertiesException;

	// 设置在不能解析占位符时，是否抛出异常
	void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders);
}
