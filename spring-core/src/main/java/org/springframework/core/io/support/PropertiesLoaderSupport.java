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

package org.springframework.core.io.support;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.PropertiesPersister;

/**
 * Base class for JavaBean-style components that need to load properties from one or more resources.
 * Supports local properties as well, with configurable overriding.
 *
 * @author Juergen Hoeller
 * @since 1.2.2
 */
// 该类用于读取Spring中.xml文件配置的属性，如：
// <bean id="propertyPlaceholderConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
// 	<property name="properties">
// 		<props>
// 			<prop key="myName">yourName</prop>
// 		</props>
// 	</property>
// </bean>
// 这种配置通过反射机制调用setProperties()方法进行注入，我们称这种属性为本地属性（localProperties）
// 还有一种是通过 loadProperties() 方法加载外部的.propertes文件
public abstract class PropertiesLoaderSupport {
	protected final Log logger = LogFactory.getLog(getClass());

	// 指向外部的.properties文件
	private Resource[] locations;
	// 文件编码
	private String fileEncoding;
	// 本地的配置属性
	protected Properties[] localProperties;
	// 标识是否允许从文件读取的属性来覆盖掉本地默认的属性
	protected boolean localOverride = false;
	// 标识是否忽略没找到的外部文件
	private boolean ignoreResourceNotFound = false;
	// 用于处理Properties对象和外部文件间的相互转换
	private PropertiesPersister propertiesPersister = new DefaultPropertiesPersister();


	// 返回一个合并的Properties实例，该实例包含在这个FactoryBean上设置的已加载的属性和属性。
	protected Properties mergeProperties() throws IOException {
		Properties result = new Properties();

		if (this.localOverride) {
			// 从this.locations指向的文件中加载属性到result对象中
			loadProperties(result);
		}

		// 合并外部的文件属性和本地属性
		if (this.localProperties != null) {
			for (Properties localProp : this.localProperties) {
				CollectionUtils.mergePropertiesIntoMap(localProp, result);
			}
		}

		// 如果允许覆盖掉本地属性，就使用文件中属性覆盖
		if (!this.localOverride) {
			loadProperties(result);
		}

		return result;
	}
	// 解析 this.locations 指向的属性文件，并加载 props 对象中
	protected void loadProperties(Properties props) throws IOException {
		if (this.locations != null) {
			for (Resource location : this.locations) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading properties file from " + location);
				}
				try {
					PropertiesLoaderUtils.fillProperties(props, new EncodedResource(location, this.fileEncoding), this.propertiesPersister);
				}
				catch (IOException ex) {
					if (this.ignoreResourceNotFound) {
						if (logger.isWarnEnabled()) {
							logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
						}
					}
					else {
						throw ex;
					}
				}
			}
		}
	}


	// setter ...
	public void setProperties(Properties properties) {
		this.localProperties = new Properties[] {properties};
	}
	public void setPropertiesArray(Properties[] propertiesArray) {
		this.localProperties = propertiesArray;
	}
	public void setLocation(Resource location) {
		this.locations = new Resource[] {location};
	}
	public void setLocations(Resource[] locations) {
		this.locations = locations;
	}
	public void setLocalOverride(boolean localOverride) {
		this.localOverride = localOverride;
	}
	public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
	}
	public void setFileEncoding(String encoding) {
		this.fileEncoding = encoding;
	}
	public void setPropertiesPersister(PropertiesPersister propertiesPersister) {
		this.propertiesPersister = (propertiesPersister != null ? propertiesPersister : new DefaultPropertiesPersister());
	}
}
