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

package org.springframework.beans.factory.support;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@SuppressWarnings("serial")
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {

	private static Class<?> javaxInjectProviderClass = null;

	static {
		try {
			javaxInjectProviderClass = ClassUtils.forName("javax.inject.Provider", DefaultListableBeanFactory.class.getClassLoader());
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - Provider interface simply not supported then.
		}
	}

	private static final Map<String, Reference<DefaultListableBeanFactory>> serializableFactories = new ConcurrentHashMap<String, Reference<DefaultListableBeanFactory>>(8);
	// 用于序列化的可选id
	private String serializationId;
	// 是否允许同名的不同bean definition再次进行注册
	private boolean allowBeanDefinitionOverriding = true;
	// 是否允许早期加载bean（ApplicationContext启动时会就会实例化所有bean，除了配置了lazy-init="true"的bean）
	private boolean allowEagerClassLoading = true;
	// 用于检查这个BeanDefinition是否允许自动装配(是一个策略接口，用来决定一个特定的bean definition 是否满足做一个特定依赖的自动绑定的候选项)
	private AutowireCandidateResolver autowireCandidateResolver = new SimpleAutowireCandidateResolver();
	// 定义了依赖类型和其对应的自动绑定值的键值对集合
	private final Map<Class<?>, Object> resolvableDependencies = new HashMap<Class<?>, Object>(16);
	// 解析完配置文件后，会将所有配置的bean信息缓存到这个map中：
	// 一般我们在XML中配置的bean都会以 GenericBeanDefinition 类型存放；
	// 而使用@Controller、@Service和@Repository等注解的bean会以ScannedGenericBeanDefinition的类型存放；
	// 一些默认没有通过显示配置的bean会以RootBeanDefinition的类型缓存到这个map中
	private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, BeanDefinition>(64);
	// 保存bean类型到beanName的映射关系（beanName包括单例和非单例的所有bean）
	private final Map<Class<?>, String[]> allBeanNamesByType = new ConcurrentHashMap<Class<?>, String[]>(64);
	// 保存bean类型到beanName的映射关系（beanName仅包括单例bean）
	private final Map<Class<?>, String[]> singletonBeanNamesByType = new ConcurrentHashMap<Class<?>, String[]>(64);
	// 按注册顺序保存BeanDefinition
	private final List<String> beanDefinitionNames = new ArrayList<String>();
	// 标识该工厂是否被冻结，冻结所有的BeanDefinition意味着已注册的bean定义将不再被修改或后处理
	private boolean configurationFrozen = false;
	// 在冻结配置的情况下，将所有已注册的beanName存放到这里
	private String[] frozenBeanDefinitionNames;


	public DefaultListableBeanFactory() {
		super();
	}
	public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}


	// 指定一个用于序列化的id，如果需要，可以将这个BeanFactory从这个id反序列化到BeanFactory对象中。
	public void setSerializationId(String serializationId) {
		if (serializationId != null) {
			serializableFactories.put(serializationId, new WeakReference<DefaultListableBeanFactory>(this));
		}
		else if (this.serializationId != null) {
			serializableFactories.remove(this.serializationId);
		}
		this.serializationId = serializationId;
	}
	// 设置是否允许同名的不同bean definition再次进行注册，默认为TRUE
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}
	// 设置是否允许早期加载
	public void setAllowEagerClassLoading(boolean allowEagerClassLoading) {
		this.allowEagerClassLoading = allowEagerClassLoading;
	}
	// 设置自动注入候选BeanDefinition的处理器
	public void setAutowireCandidateResolver(final AutowireCandidateResolver autowireCandidateResolver) {
		Assert.notNull(autowireCandidateResolver, "AutowireCandidateResolver must not be null");
		if (autowireCandidateResolver instanceof BeanFactoryAware) {
			if (System.getSecurityManager() != null) {
				final BeanFactory target = this;
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(target);
						return null;
					}
				}, getAccessControlContext());
			}
			else {
				((BeanFactoryAware) autowireCandidateResolver).setBeanFactory(this);
			}
		}
		this.autowireCandidateResolver = autowireCandidateResolver;
	}
	public AutowireCandidateResolver getAutowireCandidateResolver() {
		return this.autowireCandidateResolver;
	}
	// 从另一个工厂复制相关的配置信息
	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof DefaultListableBeanFactory) {
			DefaultListableBeanFactory otherListableFactory = (DefaultListableBeanFactory) otherFactory;
			this.allowBeanDefinitionOverriding = otherListableFactory.allowBeanDefinitionOverriding;
			this.allowEagerClassLoading = otherListableFactory.allowEagerClassLoading;
			this.autowireCandidateResolver = otherListableFactory.autowireCandidateResolver;
			this.resolvableDependencies.putAll(otherListableFactory.resolvableDependencies);
		}
	}



	// Implementation of ListableBeanFactory interface
	// 根据给定的类型返回一个bean
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		Assert.notNull(requiredType, "Required type must not be null");
		String[] beanNames = getBeanNamesForType(requiredType);
		if (beanNames.length > 1) {
			ArrayList<String> autowireCandidates = new ArrayList<String>();
			for (String beanName : beanNames) {
				if (getBeanDefinition(beanName).isAutowireCandidate()) {
					autowireCandidates.add(beanName);
				}
			}
			if (autowireCandidates.size() > 0) {
				beanNames = autowireCandidates.toArray(new String[autowireCandidates.size()]);
			}
		}
		if (beanNames.length == 1) {
			return getBean(beanNames[0], requiredType);
		}
		else if (beanNames.length > 1) {
			T primaryBean = null;
			for (String beanName : beanNames) {
				T beanInstance = getBean(beanName, requiredType);
				if (isPrimary(beanName, beanInstance)) {
					if (primaryBean != null) {
						throw new NoUniqueBeanDefinitionException(requiredType, beanNames.length,
								"more than one 'primary' bean found of required type: " + Arrays.asList(beanNames));
					}
					primaryBean = beanInstance;
				}
			}
			if (primaryBean != null) {
				return primaryBean;
			}
			throw new NoUniqueBeanDefinitionException(requiredType, beanNames);
		}
		else if (getParentBeanFactory() != null) {
			return getParentBeanFactory().getBean(requiredType);
		}
		else {
			throw new NoSuchBeanDefinitionException(requiredType);
		}
	}
	// 判断BeanDefinition注册表中是否包含指定的beanName
	@Override
	public boolean containsBeanDefinition(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return this.beanDefinitionMap.containsKey(beanName);
	}
	// 返回BeanDefinition注册表中的BeanDefinition个数
	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}
	// 获取BeanDefinition注册表中所有的BeanDefinitionName
	public String[] getBeanDefinitionNames() {
		synchronized (this.beanDefinitionMap) {
			if (this.frozenBeanDefinitionNames != null) {
				return this.frozenBeanDefinitionNames;
			}
			else {
				return StringUtils.toStringArray(this.beanDefinitionNames);
			}
		}
	}
	// 根据给定的类型返回beanName
	public String[] getBeanNamesForType(Class<?> type) {
		return getBeanNamesForType(type, true, true);
	}
	// 根据给定的类型返回beanName；
	// includeNonSingetons：是否包括非单例类型的Bean；
	// allowEagerInit：是否包括早期实例化的bean
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		if (!isConfigurationFrozen() || type == null || !allowEagerInit) {
			return doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		}
		Map<Class<?>, String[]> cache =
				(includeNonSingletons ? this.allBeanNamesByType : this.singletonBeanNamesByType);
		String[] resolvedBeanNames = cache.get(type);
		if (resolvedBeanNames != null) {
			return resolvedBeanNames;
		}
		resolvedBeanNames = doGetBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		if (ClassUtils.isCacheSafe(type, getBeanClassLoader())) {
			cache.put(type, resolvedBeanNames);
		}
		return resolvedBeanNames;
	}
	// 根据Type返回beanName到Bean的映射关系
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeansOfType(type, true, true);
	}
	// 根据Type返回beanName到Bean的映射关系
	// includeNonSingetons：是否包括非单例类型的Bean；
	// allowEagerInit：是否包括早期实例化的bean
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {

		String[] beanNames = getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
		Map<String, T> result = new LinkedHashMap<String, T>(beanNames.length);
		for (String beanName : beanNames) {
			try {
				result.put(beanName, getBean(beanName, type));
			}
			catch (BeanCreationException ex) {
				Throwable rootCause = ex.getMostSpecificCause();
				if (rootCause instanceof BeanCurrentlyInCreationException) {
					BeanCreationException bce = (BeanCreationException) rootCause;
					if (isCurrentlyInCreation(bce.getBeanName())) {
						if (this.logger.isDebugEnabled()) {
							this.logger.debug("Ignoring match to currently created bean '" + beanName + "': " +
									ex.getMessage());
						}
						onSuppressedException(ex);
						// Ignore: indicates a circular reference when autowiring constructors.
						// We want to find matches other than the currently created bean itself.
						continue;
					}
				}
				throw ex;
			}
		}
		return result;
	}
	// 返回带有annotationType注解的bean（返回的是beanName到Bean的映射关系）
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		Map<String, Object> results = new LinkedHashMap<String, Object>();
		for (String beanName : getBeanDefinitionNames()) {
			BeanDefinition beanDefinition = getBeanDefinition(beanName);
			if (!beanDefinition.isAbstract() && findAnnotationOnBean(beanName, annotationType) != null) {
				results.put(beanName, getBean(beanName));
			}
		}
		for (String beanName : getSingletonNames()) {
			if (!results.containsKey(beanName) && findAnnotationOnBean(beanName, annotationType) != null) {
				results.put(beanName, getBean(beanName));
			}
		}
		return results;
	}
	// 判断这个bean是否带有annotationType类型的注解，如果有，则返回配置的注解信息；否则返回null
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) throws NoSuchBeanDefinitionException{

		A ann = null;
		Class<?> beanType = getType(beanName);
		if (beanType != null) {
			ann = AnnotationUtils.findAnnotation(beanType, annotationType);
		}
		if (ann == null && containsBeanDefinition(beanName)) {
			BeanDefinition bd = getMergedBeanDefinition(beanName);
			if (bd instanceof AbstractBeanDefinition) {
				AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
				if (abd.hasBeanClass()) {
					ann = AnnotationUtils.findAnnotation(abd.getBeanClass(), annotationType);
				}
			}
		}
		return ann;
	}


	// Implementation of ConfigurableListableBeanFactory interface
	// 定义特殊的bean，这个bean不通过beanFactory进行管理生命周期，beanFactory本身就是一个bean，自身管理自身就有点奇怪，
	// 所以这个方法是注册一些特殊的bean，并且可以进行注入
	public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
		Assert.notNull(dependencyType, "Type must not be null");
		if (autowiredValue != null) {
			Assert.isTrue((autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue)),
					"Value [" + autowiredValue + "] does not implement specified type [" + dependencyType.getName() + "]");
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}
	// 判断这个bean是否可以被自动注入到其他的bean
	public boolean isAutowireCandidate(String beanName, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException {

		// Consider FactoryBeans as autowiring candidates.
		boolean isFactoryBean = (descriptor != null && descriptor.getDependencyType() != null &&
				FactoryBean.class.isAssignableFrom(descriptor.getDependencyType()));
		if (isFactoryBean) {
			beanName = BeanFactoryUtils.transformedBeanName(beanName);
		}

		if (containsBeanDefinition(beanName)) {
			return isAutowireCandidate(beanName, getMergedLocalBeanDefinition(beanName), descriptor);
		}
		else if (containsSingleton(beanName)) {
			return isAutowireCandidate(beanName, new RootBeanDefinition(getType(beanName)), descriptor);
		}
		else if (getParentBeanFactory() instanceof ConfigurableListableBeanFactory) {
			// No bean definition found in this factory -> delegate to parent.
			return ((ConfigurableListableBeanFactory) getParentBeanFactory()).isAutowireCandidate(beanName, descriptor);
		}
		else {
			return true;
		}
	}
	// 判断这个bean是否可以被自动注入到其他的bean
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd, DependencyDescriptor descriptor) {
		resolveBeanClass(mbd, beanName);
		if (mbd.isFactoryMethodUnique) {
			boolean resolve;
			synchronized (mbd.constructorArgumentLock) {
				resolve = (mbd.resolvedConstructorOrFactoryMethod == null);
			}
			if (resolve) {
				new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
			}
		}
		return getAutowireCandidateResolver().isAutowireCandidate(
				new BeanDefinitionHolder(mbd, beanName, getAliases(beanName)), descriptor);
	}
	// 从注册表中查找，并返回beanName对应的BeanDefinition
	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		BeanDefinition bd = this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("No bean named '" + beanName + "' found in " + this);
			}
			throw new NoSuchBeanDefinitionException(beanName);
		}
		return bd;
	}
	// 冻结所有的bean定义，意味着已注册的bean定义将不再被修改或后处理
	public void freezeConfiguration() {
		this.configurationFrozen = true;
		synchronized (this.beanDefinitionMap) {
			this.frozenBeanDefinitionNames = StringUtils.toStringArray(this.beanDefinitionNames);
		}
	}
	// 判断是否冻结bean
	public boolean isConfigurationFrozen() {
		return this.configurationFrozen;
	}
	// 如果工厂的配置被标记为冻结，则认为所有bean都符合元数据缓存的条件.
	@Override
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return (this.configurationFrozen || super.isBeanEligibleForMetadataCaching(beanName));
	}
	// 该方法用于初始化所有的单实例bean
	// ApplicationContext的初始化和BeanFactory有一个重大区别：BeanFactory在初始化容器时，并未实例化Bean，直到第一次访问某个Bean时才实例目标Bean；
	// 而ApplicationContext则在初始化应用上下文时就实例化所有单实例的Bean。因此ApplicationContext的初始化时间会比BeanFactory稍长一些。
	public void preInstantiateSingletons() throws BeansException {
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Pre-instantiating singletons in " + this);
		}
		List<String> beanNames;
		synchronized (this.beanDefinitionMap) {
			// Iterate over a copy to allow for init methods which in turn register new bean definitions.
			// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
			beanNames = new ArrayList<String>(this.beanDefinitionNames);
		}

		// 遍历所有的单实例Bean，并一个一个的实例化
		for (String beanName : beanNames) {
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
				// 判断是否为工厂bean
				if (isFactoryBean(beanName)) {
					final FactoryBean<?> factory = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
					boolean isEagerInit;
					if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
						isEagerInit = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
							public Boolean run() {
								return ((SmartFactoryBean<?>) factory).isEagerInit();
							}
						}, getAccessControlContext());
					}
					else {
						isEagerInit = (factory instanceof SmartFactoryBean && ((SmartFactoryBean<?>) factory).isEagerInit());
					}
					if (isEagerInit) {
						getBean(beanName);
					}
				}
				else {
					getBean(beanName);
				}
			}
		}

	}

	// Implementation of BeanDefinitionRegistry interface
	// 注册BeanDefinition到注册表
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {

		Assert.hasText(beanName, "Bean name must not be empty");
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");

		if (beanDefinition instanceof AbstractBeanDefinition) {
			try {
				((AbstractBeanDefinition) beanDefinition).validate();
			}
			catch (BeanDefinitionValidationException ex) {
				throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
						"Validation of bean definition failed", ex);
			}
		}

		synchronized (this.beanDefinitionMap) {
			Object oldBeanDefinition = this.beanDefinitionMap.get(beanName);
			if (oldBeanDefinition != null) {
				if (!this.allowBeanDefinitionOverriding) {
					throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), beanName,
							"Cannot register bean definition [" + beanDefinition + "] for bean '" + beanName +
							"': There is already [" + oldBeanDefinition + "] bound.");
				}
				else {
					if (this.logger.isInfoEnabled()) {
						this.logger.info("Overriding bean definition for bean '" + beanName +
								"': replacing [" + oldBeanDefinition + "] with [" + beanDefinition + "]");
					}
				}
			}
			else {
				this.beanDefinitionNames.add(beanName);
				this.frozenBeanDefinitionNames = null;
			}
			this.beanDefinitionMap.put(beanName, beanDefinition);
		}

		resetBeanDefinition(beanName);
	}
	// 将对应的bean从BeanDefinition注册表中移除
	public void removeBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		Assert.hasText(beanName, "'beanName' must not be empty");

		synchronized (this.beanDefinitionMap) {
			BeanDefinition bd = this.beanDefinitionMap.remove(beanName);
			if (bd == null) {
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("No bean named '" + beanName + "' found in " + this);
				}
				throw new NoSuchBeanDefinitionException(beanName);
			}
			this.beanDefinitionNames.remove(beanName);
			this.frozenBeanDefinitionNames = null;
		}

		resetBeanDefinition(beanName);
	}
	// 重置给定bean的所有BeanDefinition缓存，包括从其派生的bean的缓存
	protected void resetBeanDefinition(String beanName) {
		// Remove the merged bean definition for the given bean, if already created.
		clearMergedBeanDefinition(beanName);

		// Remove corresponding bean from singleton cache, if any. Shouldn't usually
		// be necessary, rather just meant for overriding a context's default beans
		// (e.g. the default StaticMessageSource in a StaticApplicationContext).
		destroySingleton(beanName);

		// Remove any assumptions about by-type mappings.
		clearByTypeCache();

		// Reset all bean definitions that have the given bean as parent (recursively).
		for (String bdName : this.beanDefinitionNames) {
			if (!beanName.equals(bdName)) {
				BeanDefinition bd = this.beanDefinitionMap.get(bdName);
				if (beanName.equals(bd.getParentName())) {
					resetBeanDefinition(bdName);
				}
			}
		}
	}
	// 是否允许同名的不同bean definition再次进行注册
	@Override
	protected boolean allowAliasOverriding() {
		return this.allowBeanDefinitionOverriding;
	}
	// 注册单例bean，并清除所有beanType到bean实例的映射关系
	@Override
	public void registerSingleton(String beanName, Object singletonObject) throws IllegalStateException {
		super.registerSingleton(beanName, singletonObject);
		clearByTypeCache();
	}
	// 销毁单例bean，并清除所有beanType到bean实例的映射关系
	@Override
	public void destroySingleton(String beanName) {
		super.destroySingleton(beanName);
		clearByTypeCache();
	}
	// 清除所有beanType到bean实例的映射关系
	private void clearByTypeCache() {
		this.allBeanNamesByType.clear();
		this.singletonBeanNamesByType.clear();
	}



	// Dependency resolution functionality
	// 寻找类型匹配的逻辑实现
	public Object resolveDependency(DependencyDescriptor descriptor, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {

		descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
		if (descriptor.getDependencyType().equals(ObjectFactory.class)) {
			return new DependencyObjectFactory(descriptor, beanName);
		}
		else if (descriptor.getDependencyType().equals(javaxInjectProviderClass)) {
			// ObjectFactory类注入的处理
			return new DependencyProviderFactory().createDependencyProvider(descriptor, beanName);
		}
		else {
			//通用逻辑处理
			return doResolveDependency(descriptor, descriptor.getDependencyType(), beanName, autowiredBeanNames, typeConverter);
		}
	}
	protected Object doResolveDependency(DependencyDescriptor descriptor, Class<?> type, String beanName, Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {

		// 用于支持Spring中新增的注解@value
		Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
		if (value != null) {
			if (value instanceof String) {
				// 将value占位符解析为对应的字符串
				String strVal = resolveEmbeddedValue((String) value);
				BeanDefinition bd = (beanName != null && containsBean(beanName) ? getMergedBeanDefinition(beanName) : null);
				// 解析Bean中的一些表达，并返回解析后的值，比如使用#{bean.xxx}的形式来调用相关属性值
				value = evaluateBeanDefinitionString(strVal, bd);
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			// 做类型转换，将value转为Type类型
			return (descriptor.getField() != null ?
					converter.convertIfNecessary(value, type, descriptor.getField()) :
					converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
		}

		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(componentType, "array of " + componentType.getName(), descriptor);
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			return converter.convertIfNecessary(matchingBeans.values(), type);
		}
		else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> elementType = descriptor.getCollectionType();
			if (elementType == null) {
				if (descriptor.isRequired()) {
					throw new FatalBeanException("No element type declared for collection [" + type.getName() + "]");
				}
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(elementType, "collection of " + elementType.getName(), descriptor);
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			return converter.convertIfNecessary(matchingBeans.values(), type);
		}
		else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> keyType = descriptor.getMapKeyType();
			if (keyType == null || !String.class.isAssignableFrom(keyType)) {
				if (descriptor.isRequired()) {
					throw new FatalBeanException("Key type [" + keyType + "] of map [" + type.getName() +
							"] must be assignable to [java.lang.String]");
				}
				return null;
			}
			Class<?> valueType = descriptor.getMapValueType();
			if (valueType == null) {
				if (descriptor.isRequired()) {
					throw new FatalBeanException("No value type declared for map [" + type.getName() + "]");
				}
				return null;
			}
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(valueType, "map with value type " + valueType.getName(), descriptor);
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			return matchingBeans;
		}
		else {
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(type, "", descriptor);
				}
				return null;
			}
			if (matchingBeans.size() > 1) {
				String primaryBeanName = determinePrimaryCandidate(matchingBeans, descriptor);
				if (primaryBeanName == null) {
					throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
				}
				if (autowiredBeanNames != null) {
					autowiredBeanNames.add(primaryBeanName);
				}
				return matchingBeans.get(primaryBeanName);
			}
			// We have exactly one match.
			Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(entry.getKey());
			}
			return entry.getValue();
		}
	}
	// 返回所有符合自动注入的bean的结合（返回 beanName -> bean对象 的映射关系）
	protected Map<String, Object> findAutowireCandidates(String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {

		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				this, requiredType, true, descriptor.isEager());
		Map<String, Object> result = new LinkedHashMap<String, Object>(candidateNames.length);
		for (Class<?> autowiringType : this.resolvableDependencies.keySet()) {
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = this.resolvableDependencies.get(autowiringType);
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		for (String candidateName : candidateNames) {
			if (!candidateName.equals(beanName) && isAutowireCandidate(candidateName, descriptor)) {
				result.put(candidateName, getBean(candidateName));
			}
		}
		return result;
	}
	// 从一组自动注入候选项中选出一个主要的候选项（判断依据：根据bean的primary配置属性）
	protected String determinePrimaryCandidate(Map<String, Object> candidateBeans, DependencyDescriptor descriptor) {
		String primaryBeanName = null;
		String fallbackBeanName = null;
		for (Map.Entry<String, Object> entry : candidateBeans.entrySet()) {
			String candidateBeanName = entry.getKey();
			Object beanInstance = entry.getValue();
			if (isPrimary(candidateBeanName, beanInstance)) {
				if (primaryBeanName != null) {
					boolean candidateLocal = containsBeanDefinition(candidateBeanName);
					boolean primaryLocal = containsBeanDefinition(primaryBeanName);
					if (candidateLocal == primaryLocal) {
						throw new NoUniqueBeanDefinitionException(descriptor.getDependencyType(), candidateBeans.size(),
								"more than one 'primary' bean found among candidates: " + candidateBeans.keySet());
					}
					else if (candidateLocal && !primaryLocal) {
						primaryBeanName = candidateBeanName;
					}
				}
				else {
					primaryBeanName = candidateBeanName;
				}
			}
			if (primaryBeanName == null &&
					(this.resolvableDependencies.values().contains(beanInstance) ||
							matchesBeanName(candidateBeanName, descriptor.getDependencyName()))) {
				fallbackBeanName = candidateBeanName;
			}
		}
		return (primaryBeanName != null ? primaryBeanName : fallbackBeanName);
	}

	// 判断当前这个bean是否被配置为主要的自动注入候选项
	// 自动装配时当出现多个Bean候选者时，被注解为@Primary的Bean将作为首选者，否则将抛出异常（@Primary对应bean配置的中primary属性配置）
	protected boolean isPrimary(String beanName, Object beanInstance) {
		if (containsBeanDefinition(beanName)) {
			return getMergedLocalBeanDefinition(beanName).isPrimary();
		}
		BeanFactory parentFactory = getParentBeanFactory();
		return (parentFactory instanceof DefaultListableBeanFactory &&
				((DefaultListableBeanFactory) parentFactory).isPrimary(beanName, beanInstance));
	}

	// 判断这两个name是否都指向同一个bean（一个bean可以定义多个别名）
	protected boolean matchesBeanName(String beanName, String candidateName) {
		return (candidateName != null &&
				(candidateName.equals(beanName) || ObjectUtils.containsElement(getAliases(beanName), candidateName)));
	}
	// 抛出NoSuchBeanDefinitionException
	private void raiseNoSuchBeanDefinitionException(Class<?> type, String dependencyDescription, DependencyDescriptor descriptor) throws NoSuchBeanDefinitionException {

		throw new NoSuchBeanDefinitionException(type, dependencyDescription,
				"expected at least 1 bean which qualifies as autowire candidate for this dependency. " +
				"Dependency annotations: " + ObjectUtils.nullSafeToString(descriptor.getAnnotations()));
	}

	// 根据给定的type返回对应的beanName
	private String[] doGetBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		List<String> result = new ArrayList<String>();

		// Check all bean definitions.
		String[] beanDefinitionNames = getBeanDefinitionNames();
		for (String beanName : beanDefinitionNames) {
			// Only consider bean as eligible if the bean name
			// is not defined as alias for some other bean.
			if (!isAlias(beanName)) {
				try {
					RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
					// Only check bean definition if it is complete.
					if (!mbd.isAbstract() && (allowEagerInit ||
						((mbd.hasBeanClass() || !mbd.isLazyInit() || this.allowEagerClassLoading)) &&
							!requiresEagerInitForType(mbd.getFactoryBeanName()))) {
						// In case of FactoryBean, match object created by FactoryBean.
						boolean isFactoryBean = isFactoryBean(beanName, mbd);
						boolean matchFound = (allowEagerInit || !isFactoryBean || containsSingleton(beanName)) &&
							(includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type);
						if (!matchFound && isFactoryBean) {
							// In case of FactoryBean, try to match FactoryBean instance itself next.
							beanName = FACTORY_BEAN_PREFIX + beanName;
							matchFound = (includeNonSingletons || mbd.isSingleton()) && isTypeMatch(beanName, type);
						}
						if (matchFound) {
							result.add(beanName);
						}
					}
				}
				catch (CannotLoadBeanClassException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably contains a placeholder: let's ignore it for type matching purposes.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Ignoring bean class loading failure for bean '" + beanName + "'", ex);
					}
					onSuppressedException(ex);
				}
				catch (BeanDefinitionStoreException ex) {
					if (allowEagerInit) {
						throw ex;
					}
					// Probably contains a placeholder: let's ignore it for type matching purposes.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Ignoring unresolvable metadata in bean definition '" + beanName + "'", ex);
					}
					onSuppressedException(ex);
				}
			}
		}

		// Check singletons too, to catch manually registered singletons.
		String[] singletonNames = getSingletonNames();
		for (String beanName : singletonNames) {
			// Only check if manually registered.
			if (!containsBeanDefinition(beanName)) {
				// In case of FactoryBean, match object created by FactoryBean.
				if (isFactoryBean(beanName)) {
					if ((includeNonSingletons || isSingleton(beanName)) && isTypeMatch(beanName, type)) {
						result.add(beanName);
						// Match found for this bean: do not match FactoryBean itself anymore.
						continue;
					}
					// In case of FactoryBean, try to match FactoryBean itself next.
					beanName = FACTORY_BEAN_PREFIX + beanName;
				}
				// Match raw bean instance (might be raw FactoryBean).
				if (isTypeMatch(beanName, type)) {
					result.add(beanName);
				}
			}
		}

		return StringUtils.toStringArray(result);
	}
	// 判断指定的bean是否需要被急切地初始化以确定它的类型，比如工厂bean
	private boolean requiresEagerInitForType(String factoryBeanName) {
		return (factoryBeanName != null && isFactoryBean(factoryBeanName) && !containsSingleton(factoryBeanName));
	}




	// Serialization support
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		throw new NotSerializableException("DefaultListableBeanFactory itself is not deserializable - " +
				"just a SerializedBeanFactoryReference is");
	}
	protected Object writeReplace() throws ObjectStreamException {
		if (this.serializationId != null) {
			return new SerializedBeanFactoryReference(this.serializationId);
		}
		else {
			throw new NotSerializableException("DefaultListableBeanFactory has no serialization id");
		}
	}
	//  Minimal id reference to the factory. Resolved to the actual factory instance on deserialization.
	private static class SerializedBeanFactoryReference implements Serializable {

		private final String id;

		public SerializedBeanFactoryReference(String id) {
			this.id = id;
		}

		private Object readResolve() {
			Reference<?> ref = serializableFactories.get(this.id);
			if (ref == null) {
				throw new IllegalStateException(
						"Cannot deserialize BeanFactory with id " + this.id + ": no factory registered for this id");
			}
			Object result = ref.get();
			if (result == null) {
				throw new IllegalStateException(
						"Cannot deserialize BeanFactory with id " + this.id + ": factory has been garbage-collected");
			}
			return result;
		}
	}
	/**
	 * Serializable ObjectFactory for lazy resolution of a dependency.
	 */
	private class DependencyObjectFactory implements ObjectFactory<Object>, Serializable {

		private final DependencyDescriptor descriptor;
		private final String beanName;

		public DependencyObjectFactory(DependencyDescriptor descriptor, String beanName) {
			this.descriptor = new DependencyDescriptor(descriptor);
			this.descriptor.increaseNestingLevel();
			this.beanName = beanName;
		}

		public Object getObject() throws BeansException {
			return doResolveDependency(this.descriptor, this.descriptor.getDependencyType(), this.beanName, null, null);
		}
	}
	/**
	 * Serializable ObjectFactory for lazy resolution of a dependency.
	 */
	private class DependencyProvider extends DependencyObjectFactory implements Provider<Object> {

		public DependencyProvider(DependencyDescriptor descriptor, String beanName) {
			super(descriptor, beanName);
		}

		public Object get() throws BeansException {
			return getObject();
		}
	}
	/**
	 * Separate inner class for avoiding a hard dependency on the {@code javax.inject} API.
	 */
	private class DependencyProviderFactory {

		public Object createDependencyProvider(DependencyDescriptor descriptor, String beanName) {
			return new DependencyProvider(descriptor, beanName);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(ObjectUtils.identityToString(this));
		sb.append(": defining beans [");
		sb.append(StringUtils.arrayToCommaDelimitedString(getBeanDefinitionNames()));
		sb.append("]; ");
		BeanFactory parent = getParentBeanFactory();
		if (parent == null) {
			sb.append("root of factory hierarchy");
		}
		else {
			sb.append("parent: ").append(ObjectUtils.identityToString(parent));
		}
		return sb.toString();
	}
}
