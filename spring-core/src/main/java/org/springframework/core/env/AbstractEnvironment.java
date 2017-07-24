/*
 * Copyright 2002-2014 the original author or authors.
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

import java.security.AccessControlException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.SpringProperties;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static java.lang.String.*;
import static org.springframework.util.StringUtils.*;

/**
 * Abstract base class for {@link Environment} implementations. Supports the notion of
 * reserved default profile names and enables specifying active and default profiles
 * through the {@link #ACTIVE_PROFILES_PROPERTY_NAME} and
 * {@link #DEFAULT_PROFILES_PROPERTY_NAME} properties.
 *
 * <p>Concrete subclasses differ primarily on which {@link PropertySource} objects they
 * add by default. {@code AbstractEnvironment} adds none. Subclasses should contribute
 * property sources through the protected {@link #customizePropertySources(MutablePropertySources)}
 * hook, while clients should customize using {@link ConfigurableEnvironment#getPropertySources()}
 * and working against the {@link MutablePropertySources} API.
 * See {@link ConfigurableEnvironment} javadoc for usage examples.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 * @see ConfigurableEnvironment
 * @see StandardEnvironment
 */
public abstract class AbstractEnvironment implements ConfigurableEnvironment {
	protected final Log logger = LogFactory.getLog(getClass());

	// 指示Spring忽略系统环境变量的系统属性，也就是说，永远不要试图通过 System#getenv() 来检索这样的变量。
	public static final String IGNORE_GETENV_PROPERTY_NAME = "spring.getenv.ignore";
	// 指定活动配置文件的属性名.值可以用逗号分隔。
	public static final String ACTIVE_PROFILES_PROPERTY_NAME = "spring.profiles.active";
	// 用于指定默认激活的配置.值可以用逗号分隔。
	public static final String DEFAULT_PROFILES_PROPERTY_NAME = "spring.profiles.default";
	// 如果没有显式地设置默认配置文件名称，并且没有显式地设置活动概要文件名，那么这个概要文件将在默认情况下自动激活。
	protected static final String RESERVED_DEFAULT_PROFILE_NAME = "default";

	// 用于保存当前环境激活的所有profile组。
	private Set<String> activeProfiles = new LinkedHashSet<String>();
	private Set<String> defaultProfiles = new LinkedHashSet<String>(getReservedDefaultProfiles());

	//可变属性源集合,并发环境下使用的Property容器，用于存放系统的参数变量
	private final MutablePropertySources propertySources = new MutablePropertySources(this.logger);
	private final ConfigurablePropertyResolver propertyResolver = new PropertySourcesPropertyResolver(this.propertySources);



	public AbstractEnvironment() {
		//在构造方法中直接调用自定义属性源集合
		customizePropertySources(this.propertySources);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug(format(
					"Initialized %s with PropertySources %s", getClass().getSimpleName(), this.propertySources));
		}
	}

	// 自定义属性源集合，默认空实现，子类可重写，用来配置属性源。
	protected void customizePropertySources(MutablePropertySources propertySources) {}
	// 获取默认profiles组的集合
	protected Set<String> getReservedDefaultProfiles() {
		return Collections.singleton(RESERVED_DEFAULT_PROFILE_NAME);
	}


	// Implementation of ConfigurableEnvironment interface
	public String[] getActiveProfiles() {
		return StringUtils.toStringArray(doGetActiveProfiles());
	}
	/**
	 * Return the set of active profiles as explicitly set through
	 * {@link #setActiveProfiles} or if the current set of active profiles
	 * is empty, check for the presence of the {@value #ACTIVE_PROFILES_PROPERTY_NAME}
	 * property and assign its value to the set of active profiles.
	 * @see #getActiveProfiles()
	 * @see #ACTIVE_PROFILES_PROPERTY_NAME
	 */
	protected Set<String> doGetActiveProfiles() {
		if (this.activeProfiles.isEmpty()) {
			String profiles = getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
			if (StringUtils.hasText(profiles)) {
				setActiveProfiles(commaDelimitedListToStringArray(trimAllWhitespace(profiles)));
			}
		}
		return this.activeProfiles;
	}
	// 将profiles放到Set<String> activeProfiles中
	public void setActiveProfiles(String... profiles) {
		Assert.notNull(profiles, "Profile array must not be null");
		this.activeProfiles.clear();
		for (String profile : profiles) {
			validateProfile(profile);
			this.activeProfiles.add(profile);
		}
	}
	public void addActiveProfile(String profile) {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug(format("Activating profile '%s'", profile));
		}
		validateProfile(profile);
		doGetActiveProfiles();
		this.activeProfiles.add(profile);
	}
	public String[] getDefaultProfiles() {
		return StringUtils.toStringArray(doGetDefaultProfiles());
	}
	/**
	 * Return the set of default profiles explicitly set via
	 * {@link #setDefaultProfiles(String...)} or if the current set of default profiles
	 * consists only of {@linkplain #getReservedDefaultProfiles() reserved default
	 * profiles}, then check for the presence of the
	 * {@value #DEFAULT_PROFILES_PROPERTY_NAME} property and assign its value (if any)
	 * to the set of default profiles.
	 * @see #AbstractEnvironment()
	 * @see #getDefaultProfiles()
	 * @see #DEFAULT_PROFILES_PROPERTY_NAME
	 * @see #getReservedDefaultProfiles()
	 */
	protected Set<String> doGetDefaultProfiles() {
		if (this.defaultProfiles.equals(getReservedDefaultProfiles())) {
			String profiles = getProperty(DEFAULT_PROFILES_PROPERTY_NAME);
			if (StringUtils.hasText(profiles)) {
				setDefaultProfiles(commaDelimitedListToStringArray(trimAllWhitespace(profiles)));
			}
		}
		return this.defaultProfiles;
	}
	/**
	 * {@inheritDoc}
	 * <p>Calling this method removes overrides any reserved default profiles
	 * that may have been added during construction of the environment.
	 * @see #AbstractEnvironment()
	 * @see #getReservedDefaultProfiles()
	 */
	public void setDefaultProfiles(String... profiles) {
		Assert.notNull(profiles, "Profile array must not be null");
		this.defaultProfiles.clear();
		for (String profile : profiles) {
			validateProfile(profile);
			this.defaultProfiles.add(profile);
		}
	}
	public boolean acceptsProfiles(String... profiles) {
		Assert.notEmpty(profiles, "Must specify at least one profile");
		for (String profile : profiles) {
			if (profile != null && profile.length() > 0 && profile.charAt(0) == '!') {
				if (!isProfileActive(profile.substring(1))) {
					return true;
				}
			}
			else if (isProfileActive(profile)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Return whether the given profile is active, or if active profiles are empty
	 * whether the profile should be active by default.
	 * @throws IllegalArgumentException per {@link #validateProfile(String)}
	 */
	protected boolean isProfileActive(String profile) {
		validateProfile(profile);
		return doGetActiveProfiles().contains(profile) ||
				(doGetActiveProfiles().isEmpty() && doGetDefaultProfiles().contains(profile));
	}
	/**
	 * Validate the given profile, called internally prior to adding to the set of
	 * active or default profiles.
	 * <p>Subclasses may override to impose further restrictions on profile syntax.
	 * @throws IllegalArgumentException if the profile is null, empty, whitespace-only or
	 * begins with the profile NOT operator (!).
	 * @see #acceptsProfiles
	 * @see #addActiveProfile
	 * @see #setDefaultProfiles
	 */
	protected void validateProfile(String profile) {
		if (!StringUtils.hasText(profile)) {
			throw new IllegalArgumentException("Invalid profile [" + profile + "]: must contain text");
		}
		if (profile.charAt(0) == '!') {
			throw new IllegalArgumentException("Invalid profile [" + profile + "]: must not begin with ! operator");
		}
	}
	public MutablePropertySources getPropertySources() {
		return this.propertySources;
	}
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSystemEnvironment() {
		if (suppressGetenvAccess()) {
			return Collections.emptyMap();
		}
		try {
			return (Map) System.getenv();
		}
		catch (AccessControlException ex) {
			return (Map) new ReadOnlySystemAttributesMap() {
				@Override
				protected String getSystemAttribute(String attributeName) {
					try {
						return System.getenv(attributeName);
					}
					catch (AccessControlException ex) {
						if (logger.isInfoEnabled()) {
							logger.info(format("Caught AccessControlException when accessing system " +
									"environment variable [%s]; its value will be returned [null]. Reason: %s",
									attributeName, ex.getMessage()));
						}
						return null;
					}
				}
			};
		}
	}
	/**
	 * Determine whether to suppress {@link System#getenv()}/{@link System#getenv(String)}
	 * access for the purposes of {@link #getSystemEnvironment()}.
	 * <p>If this method returns {@code true}, an empty dummy Map will be used instead
	 * of the regular system environment Map, never even trying to call {@code getenv}
	 * and therefore avoiding security manager warnings (if any).
	 * <p>The default implementation checks for the "spring.getenv.ignore" system property,
	 * returning {@code true} if its value equals "true" in any case.
	 * @see #IGNORE_GETENV_PROPERTY_NAME
	 * @see SpringProperties#getFlag
	 */
	protected boolean suppressGetenvAccess() {
		return SpringProperties.getFlag(IGNORE_GETENV_PROPERTY_NAME);
	}
	// 以key-value的形式返回JVM系统参数
	@SuppressWarnings("unchecked")
	public Map<String, Object> getSystemProperties() {
		try {
			return (Map) System.getProperties();
		}
		catch (AccessControlException ex) {
			return (Map) new ReadOnlySystemAttributesMap() {
				@Override
				protected String getSystemAttribute(String attributeName) {
					try {
						return System.getProperty(attributeName);
					}
					catch (AccessControlException ex) {
						if (logger.isInfoEnabled()) {
							logger.info(format("Caught AccessControlException when accessing system " +
									"property [%s]; its value will be returned [null]. Reason: %s",
									attributeName, ex.getMessage()));
						}
						return null;
					}
				}
			};
		}
	}
	public void merge(ConfigurableEnvironment parent) {
		for (PropertySource<?> ps : parent.getPropertySources()) {
			if (!this.propertySources.contains(ps.getName())) {
				this.propertySources.addLast(ps);
			}
		}
		for (String profile : parent.getActiveProfiles()) {
			this.activeProfiles.add(profile);
		}
		if (parent.getDefaultProfiles().length > 0) {
			this.defaultProfiles.remove(RESERVED_DEFAULT_PROFILE_NAME);
			for (String profile : parent.getDefaultProfiles()) {
				this.defaultProfiles.add(profile);
			}
		}
	}


	// Implementation of ConfigurablePropertyResolver interface
	public boolean containsProperty(String key) {
		return this.propertyResolver.containsProperty(key);
	}
	public String getProperty(String key) {
		return this.propertyResolver.getProperty(key);
	}
	public String getProperty(String key, String defaultValue) {
		return this.propertyResolver.getProperty(key, defaultValue);
	}
	public <T> T getProperty(String key, Class<T> targetType) {
		return this.propertyResolver.getProperty(key, targetType);
	}
	public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
		return this.propertyResolver.getProperty(key, targetType, defaultValue);
	}
	public <T> Class<T> getPropertyAsClass(String key, Class<T> targetType) {
		return this.propertyResolver.getPropertyAsClass(key, targetType);
	}
	public String getRequiredProperty(String key) throws IllegalStateException {
		return this.propertyResolver.getRequiredProperty(key);
	}
	public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
		return this.propertyResolver.getRequiredProperty(key, targetType);
	}
	public void setRequiredProperties(String... requiredProperties) {
		this.propertyResolver.setRequiredProperties(requiredProperties);
	}
	public void validateRequiredProperties() throws MissingRequiredPropertiesException {
		this.propertyResolver.validateRequiredProperties();
	}
	public String resolvePlaceholders(String text) {
		return this.propertyResolver.resolvePlaceholders(text);
	}
	public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
		return this.propertyResolver.resolveRequiredPlaceholders(text);
	}
	public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
		this.propertyResolver.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
	}
	public void setConversionService(ConfigurableConversionService conversionService) {
		this.propertyResolver.setConversionService(conversionService);
	}
	public ConfigurableConversionService getConversionService() {
		return this.propertyResolver.getConversionService();
	}
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.propertyResolver.setPlaceholderPrefix(placeholderPrefix);
	}
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.propertyResolver.setPlaceholderSuffix(placeholderSuffix);
	}
	public void setValueSeparator(String valueSeparator) {
		this.propertyResolver.setValueSeparator(valueSeparator);
	}

	@Override
	public String toString() {
		return format("%s {activeProfiles=%s, defaultProfiles=%s, propertySources=%s}",
				getClass().getSimpleName(), this.activeProfiles, this.defaultProfiles,
				this.propertySources);
	}

}
