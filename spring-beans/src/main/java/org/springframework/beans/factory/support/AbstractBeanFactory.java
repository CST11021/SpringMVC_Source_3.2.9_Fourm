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

package org.springframework.beans.factory.support;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.PropertyEditorRegistrySupport;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.BeanIsNotAFactoryException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.CannotLoadBeanClassException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.DecoratingClassLoader;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;


public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {

	// 表示该工厂的父工厂
	private BeanFactory parentBeanFactory;
	// 表示该工厂实例化bean时使用的类加载器
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();
	private ClassLoader tempClassLoader;
	// 是否缓存Bean元数据
	private boolean cacheBeanMetadata = true;
	// 在beandefinition中表达式的解决策略
	private BeanExpressionResolver beanExpressionResolver;
	// 应用于这个 bean工厂的属性编辑器的类型转换器
	private ConversionService conversionService;
	// 自定义的 PropertyEditorRegistrar（PropertyEditorRegistrar接口用于注册自定义属性编辑器）
	private final Set<PropertyEditorRegistrar> propertyEditorRegistrars = new LinkedHashSet<PropertyEditorRegistrar>(4);
	// 自定义TypeConverter使用，覆盖默认的属性机制
	private TypeConverter typeConverter;
	// 应用于这个 bean工厂的属性编辑器
	private final Map<Class<?>, Class<? extends PropertyEditor>> customEditors = new HashMap<Class<?>, Class<? extends PropertyEditor>>(4);
	// 字符串解析器应用例如注释的属性值
	private final List<StringValueResolver> embeddedValueResolvers = new LinkedList<StringValueResolver>();
	// BeanPostProcessor的作用是bean实例化前后可以做一些其他处理
	private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<BeanPostProcessor>();
	// 判断是否有注册 InstantiationAwareBeanPostProcessor 类型的处理器
	private boolean hasInstantiationAwareBeanPostProcessors;
	// 判断是否有注册 DestructionAwareBeanPostProcessor 类型的处理器
	private boolean hasDestructionAwareBeanPostProcessors;
	// 缓存beanName到Scope的映射关系
	private final Map<String, Scope> scopes = new HashMap<String, Scope>(8);
	// Security context used when running with a SecurityManager */
	private SecurityContextProvider securityContextProvider;
	// 用于存放已经转为 RootBeanDefinition 的 BeanDefinition
	private final Map<String, RootBeanDefinition> mergedBeanDefinitions = new ConcurrentHashMap<String, RootBeanDefinition>(64);
	// alreadyCreated中的bean标记为已创建的bean(或即将创建的bean)。
	private final Map<String, Boolean> alreadyCreated = new ConcurrentHashMap<String, Boolean>(64);
	// 表示一个正在创建的原型bean的名称
	private final ThreadLocal<Object> prototypesCurrentlyInCreation = new NamedThreadLocal<Object>("Prototype beans currently in creation");


	public AbstractBeanFactory() {}
	public AbstractBeanFactory(BeanFactory parentBeanFactory) {
		this.parentBeanFactory = parentBeanFactory;
	}



	// Implementation of BeanFactory interface
	public Object getBean(String name) throws BeansException {
		return doGetBean(name, null, null, false);
	}
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return doGetBean(name, requiredType, null, false);
	}
	public Object getBean(String name, Object... args) throws BeansException {
		return doGetBean(name, null, args, false);
	}
	public <T> T getBean(String name, Class<T> requiredType, Object... args) throws BeansException {
		return doGetBean(name, requiredType, args, false);
	}
	// 加载bean的核心方法
	@SuppressWarnings("unchecked")
	protected <T> T doGetBean(final String name, final Class<T> requiredType, final Object[] args, boolean typeCheckOnly) throws BeansException {

		//步骤一： 转换并提取对应的beanName，这里传入的name可能是别名，也可能是FactoryBean，所以需要进行一系列的解析，返回最终的beanName。
		//比如：如果是FactoryBean，name=“&factoryTest”那么会去除&返回name=“factoryTest”
		//再比如：获取alias所表示的最终beanName
		final String beanName = transformedBeanName(name);
		Object bean;

		/**
		 * 检查单例bean缓存或者实例工厂中是否有对应的实例
		 * 为什么首先会使用这段代码呢
		 * 因为在创建单例bean的时候会存在依赖注入的情况，而在创建依赖的时候为了避免循环依赖，
		 * Spring创建Bean的原则是不等bean创建完成就会将创建bean的ObjectFactory提早曝光,也就是将ObjectFactory加入到缓存中，一旦下一个bean创建时候需要依赖上个bean则直接使用ObjectFactory
		 */
		//步骤二： 尝试从单例缓存池 singletonObjects 获取bean，或者从 singletonFactories 中的ObjectFactory中获取
		Object sharedInstance = getSingleton(beanName);
		if (sharedInstance != null && args == null) {
			if (logger.isDebugEnabled()) {
				if (isSingletonCurrentlyInCreation(beanName)) {
					logger.debug("Returning eagerly cached instance of singleton bean '" + beanName + "' that is not fully initialized yet - a consequence of a circular reference");
				}
				else {
					logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
				}
			}
			// 我们要获取的这个bean它有可能是一个由 FactoryBean 类型的bean创建的，如果是这种情况，那我们上面获取的这个
			// sharedInstance 对象实际上是一个 FactoryBean 对象，这时候并不是直接返回实例本身而是返回指定方法返回的实例
			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
		}
		// 如果这个 sharedInstance!=null 直接到步骤9，做下类型检查就可以返回了，否则继续



		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



		//步骤四： 这里的情况是bean是一个原型bean，或是一个未被加载的单例bean，并这个bean它不是由 FactoryBean 生成的，
		// 但是这个bean可能是一个FactoryBean
		else {

			// 这里判断是否是原型bean，和是否存在循环依赖，如果原型模式存在循环依赖，则抛出异常，原型模式不允许循环依赖
			if (isPrototypeCurrentlyInCreation(beanName)) {
				throw new BeanCurrentlyInCreationException(beanName);
			}

			//步骤五：
			// 这里对IOC容器里的BeanDefinition是否存在进行检查，检查是否能在当前的BeanFactory中取到我们需要的bean。
			// 如果在当前的工厂中取不到，则到双亲BeanFactory中去取；如果当前的双亲工厂取不到，那就顺着双亲BeanFactory链
			// 一直向上查找

			// 如果BeanDefinitionMap中也就是在所有已经加载的类中不包括beanName则尝试从ParentBeanFactory中检测，
			// containsBeanDefinition它是在检测当前加载的xml文件中是否包含BeanName对应的配置，如果不包含就到
			// parentBeanFactory去尝试去获取bean了
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// Not found -> check parent.
				String nameToLookup = originalBeanName(name);
				if (args != null) {
					// Delegation to parent with explicit args.
					return (T) parentBeanFactory.getBean(nameToLookup, args);
				}
				else {
					// No args -> delegate to standard getBean method.
					return parentBeanFactory.getBean(nameToLookup, requiredType);
				}
			}

			if (!typeCheckOnly) {
				// 如果不是仅仅做类型检查而是要创建Bean的话，这里要在 alreadyCreated 中进行记录
				markBeanAsCreated(beanName);
			}

			try {
				//步骤六：
				// XML配置文件中显示定义的bean在内存中以GernericBeanDefinition的实例存储，这里Spring将转换为 RootBeanDefinition，
				// 如果指定beanName是子Bean的话同时会合并父Bean的相关属性
				// 因为从xml配置文件中读取的bean信息是存储在GernericBeanDefinition中的，但是所有的bean后续处理是针对于RootBeanDefinition的，所以这里需要进行转换
				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
				// 检查是否是抽象的bean，抽象bean不能被实例化，所以如果是抽象bean则抛出异常
				checkMergedBeanDefinition(mbd, beanName, args);

				//步骤七： 取当前bean的所有依赖bean，这样会触发getBean的递归调用，直至取到一个没有任何依赖的bean为止
				String[] dependsOn = mbd.getDependsOn();
				// 若存在依赖则需要递归实例化依赖的Bean
				if (dependsOn != null) {
					for (String dependsOnBean : dependsOn) {
						getBean(dependsOnBean);
						// 缓存bean之间的依赖关系
						registerDependentBean(dependsOnBean, beanName);
					}
				}

				//步骤八：
				//实例化依赖的bean后便可以实例化mbd本身了
				//①singleton模式的创建，Spring默认使用singleton创建bean
				if (mbd.isSingleton()) {
					// ？？？？？？？？ 这里有疑问百思不得解，为什么要用 ObjectFactory接口 的形式来创建这个bean ？？？？？？？？
					sharedInstance = getSingleton(beanName, new ObjectFactory<Object>() {
						public Object getObject() throws BeansException {
							try {
								// 创建单例bean的真正实现
								return createBean(beanName, mbd, args);
							}
							catch (BeansException ex) {
								// 销毁这个单例bean
								destroySingleton(beanName);
								throw ex;
							}
						}
					});
					// 返回用户真正想要的bean，它可能是一个普通的bean，可能是一个工厂bean
					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
				}

				//②prototype模式的创建（new）
				else if (mbd.isPrototype()) {
					// It's a prototype -> create a new instance.
					Object prototypeInstance = null;
					try {
						// 标记这个原型bean正在创建中
						beforePrototypeCreation(beanName);
						prototypeInstance = createBean(beanName, mbd, args);
					}
					finally {
						// 标记这个原型bean不在创建中
						afterPrototypeCreation(beanName);
					}
					// 返回用户真正想要的bean，它可能是一个普通的bean，可能是一个工厂bean
					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
				}

				//③指定的scope上实例化bean
				else {
					String scopeName = mbd.getScope();
					final Scope scope = this.scopes.get(scopeName);
					if (scope == null) {
						throw new IllegalStateException("No Scope registered for scope '" + scopeName + "'");
					}
					try {
						Object scopedInstance = scope.get(beanName, new ObjectFactory<Object>() {
							public Object getObject() throws BeansException {
								beforePrototypeCreation(beanName);
								try {
									return createBean(beanName, mbd, args);
								}
								finally {
									afterPrototypeCreation(beanName);
								}
							}
						});
						// 返回用户真正想要的bean，它可能是一个普通的bean，可能是一个工厂bean
						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
					}
					catch (IllegalStateException ex) {
						throw new BeanCreationException(beanName, "Scope '" + scopeName + "' is not active for the current thread; " +
								"consider defining a scoped proxy for this bean if you intend to refer to it from a singleton", ex);
					}
				}
			}
			catch (BeansException ex) {
				cleanupAfterBeanCreationFailure(beanName);
				throw ex;
			}
		}






		//步骤九：程序至此已经完成bean的创建，这里是对创建出来的bean进行类型检查，看看是否符合bean的实例类型，如果没有问题，就返回这个新创建出来的bean，这个bean已经是包含了依赖关系的bean
		if (requiredType != null && bean != null && !requiredType.isAssignableFrom(bean.getClass())) {
			try {
				return getTypeConverter().convertIfNecessary(bean, requiredType);
			}
			catch (TypeMismatchException ex) {
				if (logger.isDebugEnabled()) {
					logger.debug("Failed to convert bean '" + name + "' to required type [" + ClassUtils.getQualifiedName(requiredType) + "]", ex);
				}
				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
			}
		}
		return (T) bean;
	}

	// 判断容器是否包含这个name的bean（包括父容器）
	public boolean containsBean(String name) {
		String beanName = transformedBeanName(name);
		if (containsSingleton(beanName) || containsBeanDefinition(beanName)) {
			return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(name));
		}
		// Not found -> check parent.
		BeanFactory parentBeanFactory = getParentBeanFactory();
		return (parentBeanFactory != null && parentBeanFactory.containsBean(originalBeanName(name)));
	}
	// 判断这个bean是否为单例
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean) {
				return (BeanFactoryUtils.isFactoryDereference(name) || ((FactoryBean<?>) beanInstance).isSingleton());
			}
			else {
				return !BeanFactoryUtils.isFactoryDereference(name);
			}
		}
		else if (containsSingleton(beanName)) {
			return true;
		}

		else {
			// No singleton instance found -> check bean definition.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				return parentBeanFactory.isSingleton(originalBeanName(name));
			}

			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

			// In case of FactoryBean, return singleton status of created object if not a dereference.
			if (mbd.isSingleton()) {
				if (isFactoryBean(beanName, mbd)) {
					if (BeanFactoryUtils.isFactoryDereference(name)) {
						return true;
					}
					FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
					return factoryBean.isSingleton();
				}
				else {
					return !BeanFactoryUtils.isFactoryDereference(name);
				}
			}
			else {
				return false;
			}
		}
	}
	// 判断这个bean是否为原型
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		BeanFactory parentBeanFactory = getParentBeanFactory();
		if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
			// No bean definition found in this factory -> delegate to parent.
			return parentBeanFactory.isPrototype(originalBeanName(name));
		}

		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		if (mbd.isPrototype()) {
			// In case of FactoryBean, return singleton status of created object if not a dereference.
			return (!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName, mbd));
		}
		else {
			// Singleton or scoped - not a prototype.
			// However, FactoryBean may still produce a prototype object...
			if (BeanFactoryUtils.isFactoryDereference(name)) {
				return false;
			}
			if (isFactoryBean(beanName, mbd)) {
				final FactoryBean<?> factoryBean = (FactoryBean<?>) getBean(FACTORY_BEAN_PREFIX + beanName);
				if (System.getSecurityManager() != null) {
					return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
						public Boolean run() {
							return ((factoryBean instanceof SmartFactoryBean && ((SmartFactoryBean<?>) factoryBean).isPrototype()) ||
									!factoryBean.isSingleton());
						}
					}, getAccessControlContext());
				}
				else {
					return ((factoryBean instanceof SmartFactoryBean && ((SmartFactoryBean<?>) factoryBean).isPrototype()) ||
							!factoryBean.isSingleton());
				}
			}
			else {
				return false;
			}
		}
	}
	// 判断该bean是否匹配指定的类型
	public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);
		Class<?> typeToMatch = (targetType != null ? targetType : Object.class);

		// Check manually registered singletons.
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					Class<?> type = getTypeForFactoryBean((FactoryBean<?>) beanInstance);
					return (type != null && ClassUtils.isAssignable(typeToMatch, type));
				}
				else {
					return ClassUtils.isAssignableValue(typeToMatch, beanInstance);
				}
			}
			else {
				return !BeanFactoryUtils.isFactoryDereference(name) &&
						ClassUtils.isAssignableValue(typeToMatch, beanInstance);
			}
		}
		else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			// null instance registered
			return false;
		}

		else {
			// No singleton instance found -> check bean definition.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				return parentBeanFactory.isTypeMatch(originalBeanName(name), targetType);
			}

			// Retrieve corresponding bean definition.
			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

			Class[] typesToMatch = (FactoryBean.class.equals(typeToMatch) ?
					new Class[] {typeToMatch} : new Class[] {FactoryBean.class, typeToMatch});

			// Check decorated bean definition, if any: We assume it'll be easier
			// to determine the decorated bean's type than the proxy's type.
			BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
			if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
				RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
				Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd, typesToMatch);
				if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
					return typeToMatch.isAssignableFrom(targetClass);
				}
			}

			Class<?> beanType = predictBeanType(beanName, mbd, typesToMatch);
			if (beanType == null) {
				return false;
			}

			// Check bean class whether we're dealing with a FactoryBean.
			if (FactoryBean.class.isAssignableFrom(beanType)) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					// If it's a FactoryBean, we want to look at what it creates, not the factory class.
					beanType = getTypeForFactoryBean(beanName, mbd);
					if (beanType == null) {
						return false;
					}
				}
			}
			else if (BeanFactoryUtils.isFactoryDereference(name)) {
				// Special case: A SmartInstantiationAwareBeanPostProcessor returned a non-FactoryBean
				// type but we nevertheless are being asked to dereference a FactoryBean...
				// Let's check the original bean class and proceed with it if it is a FactoryBean.
				beanType = predictBeanType(beanName, mbd, FactoryBean.class);
				if (beanType == null || !FactoryBean.class.isAssignableFrom(beanType)) {
					return false;
				}
			}

			return typeToMatch.isAssignableFrom(beanType);
		}
	}
	// 返回这个bean对应的类类型，如果这个bean还没有被加载则返回null
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		// 从已经初始化的单例缓存中获取
		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			if (beanInstance instanceof FactoryBean && !BeanFactoryUtils.isFactoryDereference(name)) {
				return getTypeForFactoryBean((FactoryBean<?>) beanInstance);
			}
			else {
				return beanInstance.getClass();
			}
		}
		else if (containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			// null instance registered
			return null;
		}

		else {
			// No singleton instance found -> check bean definition.
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
				// No bean definition found in this factory -> delegate to parent.
				return parentBeanFactory.getType(originalBeanName(name));
			}

			RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);

			// Check decorated bean definition, if any: We assume it'll be easier
			// to determine the decorated bean's type than the proxy's type.
			BeanDefinitionHolder dbd = mbd.getDecoratedDefinition();
			if (dbd != null && !BeanFactoryUtils.isFactoryDereference(name)) {
				RootBeanDefinition tbd = getMergedBeanDefinition(dbd.getBeanName(), dbd.getBeanDefinition(), mbd);
				Class<?> targetClass = predictBeanType(dbd.getBeanName(), tbd);
				if (targetClass != null && !FactoryBean.class.isAssignableFrom(targetClass)) {
					return targetClass;
				}
			}

			Class<?> beanClass = predictBeanType(beanName, mbd);

			// Check bean class whether we're dealing with a FactoryBean.
			if (beanClass != null && FactoryBean.class.isAssignableFrom(beanClass)) {
				if (!BeanFactoryUtils.isFactoryDereference(name)) {
					// If it's a FactoryBean, we want to look at what it creates, not at the factory class.
					return getTypeForFactoryBean(beanName, mbd);
				}
				else {
					return beanClass;
				}
			}
			else {
				return (!BeanFactoryUtils.isFactoryDereference(name) ? beanClass : null);
			}
		}
	}
	// 获取这个bean对应的一系列别名
	@Override
	public String[] getAliases(String name) {
		String beanName = transformedBeanName(name);
		List<String> aliases = new ArrayList<String>();
		boolean factoryPrefix = name.startsWith(FACTORY_BEAN_PREFIX);
		String fullBeanName = beanName;
		if (factoryPrefix) {
			fullBeanName = FACTORY_BEAN_PREFIX + beanName;
		}
		if (!fullBeanName.equals(name)) {
			aliases.add(fullBeanName);
		}
		String[] retrievedAliases = super.getAliases(beanName);
		for (String retrievedAlias : retrievedAliases) {
			String alias = (factoryPrefix ? FACTORY_BEAN_PREFIX : "") + retrievedAlias;
			if (!alias.equals(name)) {
				aliases.add(alias);
			}
		}
		if (!containsSingleton(beanName) && !containsBeanDefinition(beanName)) {
			BeanFactory parentBeanFactory = getParentBeanFactory();
			if (parentBeanFactory != null) {
				aliases.addAll(Arrays.asList(parentBeanFactory.getAliases(fullBeanName)));
			}
		}
		return StringUtils.toStringArray(aliases);
	}



	// Implementation of HierarchicalBeanFactory interface
	// 获取父容器
	public BeanFactory getParentBeanFactory() {
		return this.parentBeanFactory;
	}
	// 判断当前容器是否有这个name的bean
	public boolean containsLocalBean(String name) {
		String beanName = transformedBeanName(name);
		return ((containsSingleton(beanName) || containsBeanDefinition(beanName)) &&
				(!BeanFactoryUtils.isFactoryDereference(name) || isFactoryBean(beanName)));
	}



	// Implementation of ConfigurableBeanFactory interface
	public void setParentBeanFactory(BeanFactory parentBeanFactory) {
		if (this.parentBeanFactory != null && this.parentBeanFactory != parentBeanFactory) {
			throw new IllegalStateException("Already associated with parent BeanFactory: " + this.parentBeanFactory);
		}
		this.parentBeanFactory = parentBeanFactory;
	}

	// 设置装载bean的类加载器
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = (beanClassLoader != null ? beanClassLoader : ClassUtils.getDefaultClassLoader());
	}
	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	public void setTempClassLoader(ClassLoader tempClassLoader) {
		this.tempClassLoader = tempClassLoader;
	}
	public ClassLoader getTempClassLoader() {
		return this.tempClassLoader;
	}

	// 是否需要缓存bean metadata,比如bean difinition 和解析好的classes.默认开启缓存
	public void setCacheBeanMetadata(boolean cacheBeanMetadata) {
		this.cacheBeanMetadata = cacheBeanMetadata;
	}
	public boolean isCacheBeanMetadata() {
		return this.cacheBeanMetadata;
	}

	// 设置解析bean definition表达式的解析器，比如使用#{bean.xxx}的形式来调用相关属性值
	public void setBeanExpressionResolver(BeanExpressionResolver resolver) {
		this.beanExpressionResolver = resolver;
	}
	public BeanExpressionResolver getBeanExpressionResolver() {
		return this.beanExpressionResolver;
	}

	// 设置类型转换器
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
	public ConversionService getConversionService() {
		return this.conversionService;
	}

	// 添加属性编辑器
	public void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar) {
		Assert.notNull(registrar, "PropertyEditorRegistrar must not be null");
		this.propertyEditorRegistrars.add(registrar);
	}
	public Set<PropertyEditorRegistrar> getPropertyEditorRegistrars() {
		return this.propertyEditorRegistrars;
	}

	// 注册自定义的属性编辑器
	public void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass) {
		Assert.notNull(requiredType, "Required type must not be null");
		Assert.isAssignable(PropertyEditor.class, propertyEditorClass);
		this.customEditors.put(requiredType, propertyEditorClass);
	}
	// 复制编辑器注册表
	public void copyRegisteredEditorsTo(PropertyEditorRegistry registry) {
		registerCustomEditors(registry);
	}
	// 获取所有自定义的属性编辑器
	public Map<Class<?>, Class<? extends PropertyEditor>> getCustomEditors() {
		return this.customEditors;
	}

	// 设置类型转换器
	public void setTypeConverter(TypeConverter typeConverter) {
		this.typeConverter = typeConverter;
	}
	protected TypeConverter getCustomTypeConverter() {
		return this.typeConverter;
	}
	public TypeConverter getTypeConverter() {
		TypeConverter customConverter = getCustomTypeConverter();
		if (customConverter != null) {
			return customConverter;
		}
		else {
			// Build default TypeConverter, registering custom editors.
			SimpleTypeConverter typeConverter = new SimpleTypeConverter();
			typeConverter.setConversionService(getConversionService());
			registerCustomEditors(typeConverter);
			return typeConverter;
		}
	}

	// 添加一个占位符解析器
	public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		this.embeddedValueResolvers.add(valueResolver);
	}

	// 将value占位符解析为字符串
	public String resolveEmbeddedValue(String value) {
		String result = value;
		for (StringValueResolver resolver : this.embeddedValueResolvers) {
			if (result == null) {
				return null;
			}
			result = resolver.resolveStringValue(result);
		}
		return result;
	}

	// 添加一个BeanPostProcessor后处理器
	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		Assert.notNull(beanPostProcessor, "BeanPostProcessor must not be null");
		this.beanPostProcessors.remove(beanPostProcessor);
		this.beanPostProcessors.add(beanPostProcessor);
		if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
			this.hasInstantiationAwareBeanPostProcessors = true;
		}
		if (beanPostProcessor instanceof DestructionAwareBeanPostProcessor) {
			this.hasDestructionAwareBeanPostProcessors = true;
		}
	}

	// 获取后处理器的个数
	public int getBeanPostProcessorCount() {
		return this.beanPostProcessors.size();
	}

	// 获取所有后处理器
	public List<BeanPostProcessor> getBeanPostProcessors() {
		return this.beanPostProcessors;
	}

	// 判断是否有注册 InstantiationAwareBeanPostProcessor 类型的处理器（bean初始化前后执行处理器方法）
	protected boolean hasInstantiationAwareBeanPostProcessors() {
		return this.hasInstantiationAwareBeanPostProcessors;
	}

	// 判断工厂是否持有一个 DestructionAwareBeanPostProcessor 对象，在工厂shutdown的时候DestructionAwareBeanPostProcessor会应用于bean
	protected boolean hasDestructionAwareBeanPostProcessors() {
		return this.hasDestructionAwareBeanPostProcessors;
	}

	// 注册作用域
	public void registerScope(String scopeName, Scope scope) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		Assert.notNull(scope, "Scope must not be null");
		if (SCOPE_SINGLETON.equals(scopeName) || SCOPE_PROTOTYPE.equals(scopeName)) {
			throw new IllegalArgumentException("Cannot replace existing scopes 'singleton' and 'prototype'");
		}
		this.scopes.put(scopeName, scope);
	}

	// 返回所有作用域名称
	public String[] getRegisteredScopeNames() {
		return StringUtils.toStringArray(this.scopes.keySet());
	}

	// 根据作用域名称获取相应的作用域对象Scope
	public Scope getRegisteredScope(String scopeName) {
		Assert.notNull(scopeName, "Scope identifier must not be null");
		return this.scopes.get(scopeName);
	}

	/**
	 * Set the security context provider for this bean factory. If a security manager
	 * is set, interaction with the user code will be executed using the privileged
	 * of the provided security context.
	 */
	public void setSecurityContextProvider(SecurityContextProvider securityProvider) {
		this.securityContextProvider = securityProvider;
	}
	/**
	 * Delegate the creation of the access control context to the
	 * {@link #setSecurityContextProvider SecurityContextProvider}.
	 */
	@Override
	public AccessControlContext getAccessControlContext() {
		return (this.securityContextProvider != null ?
				this.securityContextProvider.getAccessControlContext() :
				AccessController.getContext());
	}

	// 复制相关的容器配置，比如类装载器，BeanDefinition表达式解析器，属性编辑器，后处理器，作用域等
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		Assert.notNull(otherFactory, "BeanFactory must not be null");
		setBeanClassLoader(otherFactory.getBeanClassLoader());
		setCacheBeanMetadata(otherFactory.isCacheBeanMetadata());
		setBeanExpressionResolver(otherFactory.getBeanExpressionResolver());
		if (otherFactory instanceof AbstractBeanFactory) {
			AbstractBeanFactory otherAbstractFactory = (AbstractBeanFactory) otherFactory;
			this.customEditors.putAll(otherAbstractFactory.customEditors);
			this.propertyEditorRegistrars.addAll(otherAbstractFactory.propertyEditorRegistrars);
			this.beanPostProcessors.addAll(otherAbstractFactory.beanPostProcessors);
			this.hasInstantiationAwareBeanPostProcessors = this.hasInstantiationAwareBeanPostProcessors ||
					otherAbstractFactory.hasInstantiationAwareBeanPostProcessors;
			this.hasDestructionAwareBeanPostProcessors = this.hasDestructionAwareBeanPostProcessors ||
					otherAbstractFactory.hasDestructionAwareBeanPostProcessors;
			this.scopes.putAll(otherAbstractFactory.scopes);
			this.securityContextProvider = otherAbstractFactory.securityContextProvider;
		}
		else {
			setTypeConverter(otherFactory.getTypeConverter());
		}
	}

	// 根据beanName获取一个BeanDefinition，如果指定的bean对应的是一个子bean，则需合并父bean的信息
	public BeanDefinition getMergedBeanDefinition(String name) throws BeansException {
		String beanName = transformedBeanName(name);

		// Efficiently check whether bean definition exists in this factory.
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
			return ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(beanName);
		}
		// Resolve merged bean definition locally.
		return getMergedLocalBeanDefinition(beanName);
	}

	// 判断这个name对应的bean是否为一个工厂bean
	public boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException {
		String beanName = transformedBeanName(name);

		Object beanInstance = getSingleton(beanName, false);
		if (beanInstance != null) {
			return (beanInstance instanceof FactoryBean);
		}
		else if (containsSingleton(beanName)) {
			// null instance registered
			return false;
		}

		// No singleton instance found -> check bean definition.
		if (!containsBeanDefinition(beanName) && getParentBeanFactory() instanceof ConfigurableBeanFactory) {
			// No bean definition found in this factory -> delegate to parent.
			return ((ConfigurableBeanFactory) getParentBeanFactory()).isFactoryBean(name);
		}

		return isFactoryBean(beanName, getMergedLocalBeanDefinition(beanName));
	}

	// 判断这个bean是否正在实例化
	@Override
	public boolean isActuallyInCreation(String beanName) {
		return isSingletonCurrentlyInCreation(beanName) || isPrototypeCurrentlyInCreation(beanName);
	}

	// 返回指定的原型bean当前是否处于创建状态
	protected boolean isPrototypeCurrentlyInCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		return (curVal != null &&
				(curVal.equals(beanName) || (curVal instanceof Set && ((Set<?>) curVal).contains(beanName))));
	}

	// 这个原型的bean创建前会调用该方法，默认的实现标志着原型正在创建中
	@SuppressWarnings("unchecked")
	protected void beforePrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal == null) {
			this.prototypesCurrentlyInCreation.set(beanName);
		}
		else if (curVal instanceof String) {
			Set<String> beanNameSet = new HashSet<String>(2);
			beanNameSet.add((String) curVal);
			beanNameSet.add(beanName);
			this.prototypesCurrentlyInCreation.set(beanNameSet);
		}
		else {
			Set<String> beanNameSet = (Set<String>) curVal;
			beanNameSet.add(beanName);
		}
	}

	// 这个原型的bean创建完后会调用该方法，默认的实现标志着原型不在创建中
	@SuppressWarnings("unchecked")
	protected void afterPrototypeCreation(String beanName) {
		Object curVal = this.prototypesCurrentlyInCreation.get();
		if (curVal instanceof String) {
			this.prototypesCurrentlyInCreation.remove();
		}
		else if (curVal instanceof Set) {
			Set<String> beanNameSet = (Set<String>) curVal;
			beanNameSet.remove(beanName);
			if (beanNameSet.isEmpty()) {
				this.prototypesCurrentlyInCreation.remove();
			}
		}
	}

	// 销毁单例bean，并执行相应的后处理器方法
	public void destroyBean(String beanName, Object beanInstance) {
		destroyBean(beanName, beanInstance, getMergedLocalBeanDefinition(beanName));
	}
	// 销毁单例bean，并执行相应的后处理器方法
	protected void destroyBean(String beanName, Object beanInstance, RootBeanDefinition mbd) {
		new DisposableBeanAdapter(beanInstance, beanName, mbd, getBeanPostProcessors(), getAccessControlContext()).destroy();
	}
	// 从作用域缓存中移除这个bean
	public void destroyScopedBean(String beanName) {
		RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
		if (mbd.isSingleton() || mbd.isPrototype()) {
			throw new IllegalArgumentException(
					"Bean name '" + beanName + "' does not correspond to an object in a mutable scope");
		}
		String scopeName = mbd.getScope();
		Scope scope = this.scopes.get(scopeName);
		if (scope == null) {
			throw new IllegalStateException("No Scope SPI registered for scope '" + scopeName + "'");
		}
		Object bean = scope.remove(beanName);
		if (bean != null) {
			destroyBean(beanName, bean, mbd);
		}
	}



	// Implementation methods

	// 返回bean的名字，剥出厂引用前缀，如果必要的话，解决别名的规范名称
	protected String transformedBeanName(String name) {
		return canonicalName(BeanFactoryUtils.transformedBeanName(name));
	}

	// 确定原始的bean名称，将本地定义的别名解析为规范名称
	protected String originalBeanName(String name) {
		String beanName = transformedBeanName(name);
		if (name.startsWith(FACTORY_BEAN_PREFIX)) {
			beanName = FACTORY_BEAN_PREFIX + beanName;
		}
		return beanName;
	}

	// 给这个BeanWrapper 设置类型转换器，并设置自定义属性编辑器
	protected void initBeanWrapper(BeanWrapper bw) {
		bw.setConversionService(getConversionService());
		registerCustomEditors(bw);
	}

	// 注册自定义的属性编辑器
	protected void registerCustomEditors(PropertyEditorRegistry registry) {
		PropertyEditorRegistrySupport registrySupport = (registry instanceof PropertyEditorRegistrySupport ? (PropertyEditorRegistrySupport) registry : null);
		if (registrySupport != null) {
			registrySupport.useConfigValueEditors();
		}

		if (!this.propertyEditorRegistrars.isEmpty()) {
			for (PropertyEditorRegistrar registrar : this.propertyEditorRegistrars) {
				try {
					registrar.registerCustomEditors(registry);
				}
				catch (BeanCreationException ex) {
					Throwable rootCause = ex.getMostSpecificCause();
					if (rootCause instanceof BeanCurrentlyInCreationException) {
						BeanCreationException bce = (BeanCreationException) rootCause;
						if (isCurrentlyInCreation(bce.getBeanName())) {
							if (logger.isDebugEnabled()) {
								logger.debug("PropertyEditorRegistrar [" + registrar.getClass().getName() +
										"] failed because it tried to obtain currently created bean '" +
										ex.getBeanName() + "': " + ex.getMessage());
							}
							onSuppressedException(ex);
							continue;
						}
					}
					throw ex;
				}
			}
		}
		if (!this.customEditors.isEmpty()) {
			for (Map.Entry<Class<?>, Class<? extends PropertyEditor>> entry : this.customEditors.entrySet()) {
				Class<?> requiredType = entry.getKey();
				Class<? extends PropertyEditor> editorClass = entry.getValue();
				registry.registerCustomEditor(requiredType, BeanUtils.instantiateClass(editorClass));
			}
		}
	}

	// 返回一个合并的RootBeanDefinition，如果指定的bean对应的是一个子bean，则需合并父bean的信息。
	protected RootBeanDefinition getMergedLocalBeanDefinition(String beanName) throws BeansException {
		RootBeanDefinition mbd = this.mergedBeanDefinitions.get(beanName);
		if (mbd != null) {
			// 如果这个bean已经是RootBeanDefinition，则直接返回
			return mbd;
		}
		// 这个bean还没有转为RootBeanDefinition，则需要进行合并
		return getMergedBeanDefinition(beanName, getBeanDefinition(beanName));
	}

	// 如果给定bean的定义是子bean定义，则返回给给定的顶层bean的RootBeanDefinition，并将其与父元素合并。
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd) throws BeanDefinitionStoreException {
		return getMergedBeanDefinition(beanName, bd, null);
	}
	protected RootBeanDefinition getMergedBeanDefinition(String beanName, BeanDefinition bd, BeanDefinition containingBd) throws BeanDefinitionStoreException {

		synchronized (this.mergedBeanDefinitions) {
			RootBeanDefinition mbd = null;

			// Check with full lock now in order to enforce the same merged instance.
			if (containingBd == null) {
				mbd = this.mergedBeanDefinitions.get(beanName);
			}

			// 如果这个beanName还没有转为RootBeanDefinition，则进行转换，否则直接返回这个RootBeanDefinition
			if (mbd == null) {
				// 这个bean是一个顶级BeanDefinition，它不需要继承其他Bean属性
				if (bd.getParentName() == null) {
					if (bd instanceof RootBeanDefinition) {
						// 克隆一个BeanDefine，而不是直接转换
						mbd = ((RootBeanDefinition) bd).cloneBeanDefinition();
					}
					else {
						mbd = new RootBeanDefinition(bd);
					}
				}

				// 这个Bean是一个子Bean，它需要合并父Bean的信息
				else {
					BeanDefinition pbd;
					try {
						String parentBeanName = transformedBeanName(bd.getParentName());
						if (!beanName.equals(parentBeanName)) {
							pbd = getMergedBeanDefinition(parentBeanName);
						}
						else {
							// 这里的情况是，父bean的名称经过 transformedBeanName() 方法后的名称和自己的beanName名称一样，这时候就需要从父容器中看看有没有这个bean
							if (getParentBeanFactory() instanceof ConfigurableBeanFactory) {
								pbd = ((ConfigurableBeanFactory) getParentBeanFactory()).getMergedBeanDefinition(parentBeanName);
							}
							else {
								throw new NoSuchBeanDefinitionException(bd.getParentName(), "Parent name '" + bd.getParentName() + "' is equal to bean name '" + beanName +
										"': cannot be resolved without an AbstractBeanFactory parent");
							}
						}
					}
					catch (NoSuchBeanDefinitionException ex) {
						throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName, "Could not resolve parent bean definition '" + bd.getParentName() + "'", ex);
					}
					// Deep copy with overridden values.
					mbd = new RootBeanDefinition(pbd);
					// 程序到这里时，这个mbd可能是一个父bean，所以要把子bean的信息覆盖掉父bean
					mbd.overrideFrom(bd);
				}

				// Set default singleton scope, if not configured before.
				if (!StringUtils.hasLength(mbd.getScope())) {
					mbd.setScope(RootBeanDefinition.SCOPE_SINGLETON);
				}

				// A bean contained in a non-singleton bean cannot be a singleton itself.
				// Let's correct this on the fly here, since this might be the result of
				// parent-child merging for the outer bean, in which case the original inner bean
				// definition will not have inherited the merged outer bean's singleton status.
				if (containingBd != null && !containingBd.isSingleton() && mbd.isSingleton()) {
					mbd.setScope(containingBd.getScope());
				}

				// Only cache the merged bean definition if we're already about to create an
				// instance of the bean, or at least have already created an instance before.
				if (containingBd == null && isCacheBeanMetadata() && isBeanEligibleForMetadataCaching(beanName)) {
					this.mergedBeanDefinitions.put(beanName, mbd);
				}
			}

			return mbd;
		}
	}

	// 检查是否是抽象的bean，抽象bean不能被实例化，所以如果是抽象bean则抛出异常
	protected void checkMergedBeanDefinition(RootBeanDefinition mbd, String beanName, Object[] args) throws BeanDefinitionStoreException {

		// check if bean definition is not abstract
		if (mbd.isAbstract()) {
			throw new BeanIsAbstractException(beanName);
		}

		// Check validity of the usage of the args parameter. This can only be used for prototypes constructed via a factory method.
		// 检查args参数的使用有效性。这只能用于通过工厂方法构造的原型。
		if (args != null && !mbd.isPrototype()) {
			throw new BeanDefinitionStoreException("Can only specify arguments for the getBean method when referring to a prototype bean definition");
		}
	}

	// 移除一个合并了的BeanDefinition
	protected void clearMergedBeanDefinition(String beanName) {
		this.mergedBeanDefinitions.remove(beanName);
	}

	// 返回这个bean的类型
	protected Class<?> resolveBeanClass(final RootBeanDefinition mbd, String beanName, final Class<?>... typesToMatch) throws CannotLoadBeanClassException {
		try {
			// 判断这个bean的beanClass是否是类类型
			if (mbd.hasBeanClass()) {
				// 返回这个bean的类型
				return mbd.getBeanClass();
			}
			if (System.getSecurityManager() != null) {
				return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
					public Class<?> run() throws Exception {
						return doResolveBeanClass(mbd, typesToMatch);
					}
				}, getAccessControlContext());
			}
			else {
				return doResolveBeanClass(mbd, typesToMatch);
			}
		}
		catch (PrivilegedActionException pae) {
			ClassNotFoundException ex = (ClassNotFoundException) pae.getException();
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (ClassNotFoundException ex) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), ex);
		}
		catch (LinkageError err) {
			throw new CannotLoadBeanClassException(mbd.getResourceDescription(), beanName, mbd.getBeanClassName(), err);
		}
	}
	// 返回这个bean的类型
	private Class<?> doResolveBeanClass(RootBeanDefinition mbd, Class<?>... typesToMatch) throws ClassNotFoundException {
		if (!ObjectUtils.isEmpty(typesToMatch)) {
			ClassLoader tempClassLoader = getTempClassLoader();
			if (tempClassLoader != null) {
				if (tempClassLoader instanceof DecoratingClassLoader) {
					DecoratingClassLoader dcl = (DecoratingClassLoader) tempClassLoader;
					for (Class<?> typeToMatch : typesToMatch) {
						dcl.excludeClass(typeToMatch.getName());
					}
				}
				String className = mbd.getBeanClassName();
				return (className != null ? ClassUtils.forName(className, tempClassLoader) : null);
			}
		}
		return mbd.resolveBeanClass(getBeanClassLoader());
	}

	/**
	 * Evaluate the given String as contained in a bean definition, potentially resolving it as an expression.
	 * @param value the value to check
	 * @param beanDefinition the bean definition that the value comes from
	 * @return the resolved value
	 * @see #setBeanExpressionResolver
	 */
	protected Object evaluateBeanDefinitionString(String value, BeanDefinition beanDefinition) {
		if (this.beanExpressionResolver == null) {
			return value;
		}
		Scope scope = (beanDefinition != null ? getRegisteredScope(beanDefinition.getScope()) : null);
		return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
	}

	// 返回这个bean对应的类类型
	protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		if (mbd.getFactoryMethodName() != null) {
			return null;
		}
		return resolveBeanClass(mbd, beanName, typesToMatch);
	}

	// 判断这个bean是否为一个工厂Bean
	protected boolean isFactoryBean(String beanName, RootBeanDefinition mbd) {
		Class<?> beanType = predictBeanType(beanName, mbd, FactoryBean.class);
		return (beanType != null && FactoryBean.class.isAssignableFrom(beanType));
	}

	// 返回这个beanName对应的工厂bean，所创建的bean的类型
	protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
		if (!mbd.isSingleton()) {
			return null;
		}
		try {
			FactoryBean<?> factoryBean = doGetBean(FACTORY_BEAN_PREFIX + beanName, FactoryBean.class, null, true);
			return getTypeForFactoryBean(factoryBean);
		}
		catch (BeanCreationException ex) {
			// Can only happen when getting a FactoryBean.
			if (logger.isDebugEnabled()) {
				logger.debug("Ignoring bean creation exception on FactoryBean type check: " + ex);
			}
			onSuppressedException(ex);
			return null;
		}
	}

	// 将指定的bean标记为已创建的bean(或即将创建的bean)。
	protected void markBeanAsCreated(String beanName) {
		this.alreadyCreated.put(beanName, Boolean.TRUE);
	}

	// bean 创建失败后从this.alreadyCreated中移除beanName
	protected void cleanupAfterBeanCreationFailure(String beanName) {
		this.alreadyCreated.remove(beanName);
	}

	// 判断指定bean是否具有缓存BeanDefinition元数据的资格
	protected boolean isBeanEligibleForMetadataCaching(String beanName) {
		return this.alreadyCreated.containsKey(beanName);
	}

	/**
	 * Remove the singleton instance (if any) for the given bean name,
	 * but only if it hasn't been used for other purposes than type checking.
	 * @param beanName the name of the bean
	 * @return {@code true} if actually removed, {@code false} otherwise
	 */
	protected boolean removeSingletonIfCreatedForTypeCheckOnly(String beanName) {
		if (!this.alreadyCreated.containsKey(beanName)) {
			removeSingleton(beanName);
			return true;
		}
		else {
			return false;
		}
	}

	// 用于检测当前bean是否是FactoryBean类型的Bean，如果是，那么需要调用该bean对应的FactoryBean实例中的getObject方法作为返回值
	protected Object getObjectForBeanInstance(Object beanInstance, String name, String beanName, RootBeanDefinition mbd) {

		// 如果这个bean是工厂bean，那么它必须实现 FactoryBean 接口，否则抛异常
		if (BeanFactoryUtils.isFactoryDereference(name) && !(beanInstance instanceof FactoryBean)) {
			throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
		}

		// 这个已经实例化的bean不是一个 FactoryBean 类型的bean，那么它可以直接返回
		if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
			return beanInstance;
		}

		// 程序至此的情况是：这个已经实例化的bean是一个 FactoryBean 类型的bean，如果是用户想要直接获取工厂实例而不是工厂
		// 的getObject方法对应的实例那么传入的那么应该加入&前缀
		Object object = null;
		if (mbd == null) {
			// 尝试从factoryBeanObjectCache缓存加载bean
			object = getCachedObjectForFactoryBean(beanName);
		}


		if (object == null) {
			// 如果object为空则使用这个工厂bean创建一个bean
			FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
			if (mbd == null && containsBeanDefinition(beanName)) {
				mbd = getMergedLocalBeanDefinition(beanName);
			}
			// isSynthetic判断这个mbd是不是合成的
			boolean synthetic = (mbd != null && mbd.isSynthetic());
			// 如果不是合成的，则factory创建完bean后会调用applyBeanPostProcessorsAfterInitialization方法（bean实例化后的后处理器）
			object = getObjectFromFactoryBean(factory, beanName, !synthetic);
		}
		return object;
	}

	// 确定给定bean名称是否已在该注册表中使用，即是否有本地bean或别名在该名称下注册
	public boolean isBeanNameInUse(String beanName) {
		return isAlias(beanName) || containsLocalBean(beanName) || hasDependentBean(beanName);
	}

	// 判断给定的bean是否需要在关闭的时候销毁
	protected boolean requiresDestruction(Object bean, RootBeanDefinition mbd) {
		return (bean != null &&
				(DisposableBeanAdapter.hasDestroyMethod(bean, mbd) || hasDestructionAwareBeanPostProcessors()));
	}

	// Spring中不但提供了对于初始化方法的扩展入口，同样也提供了销毁方法的扩展入口，对于销毁方法的扩展，
	// 除了我们熟知的配置属性 destroy-method 方法外，用户还可以注册后处理器 DestrctionAwareBeanPostProcessor 来统一处理bean的销毁方法
	protected void registerDisposableBeanIfNecessary(String beanName, Object bean, RootBeanDefinition mbd) {
		AccessControlContext acc = (System.getSecurityManager() != null ? getAccessControlContext() : null);
		if (!mbd.isPrototype() && requiresDestruction(bean, mbd)) {
			if (mbd.isSingleton()) {
				// 单例模式下注册需要销毁的bean，此方法中会处理实现DisposableBean的bean，并且对所有的bean使用 DestructionAwareBeanPostProcessors 处理
				registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
			}
			else {
				// 自定义作用域处理
				Scope scope = this.scopes.get(mbd.getScope());
				if (scope == null) {
					throw new IllegalStateException("No Scope registered for scope '" + mbd.getScope() + "'");
				}
				scope.registerDestructionCallback(beanName, new DisposableBeanAdapter(bean, beanName, mbd, getBeanPostProcessors(), acc));
			}
		}
	}



	// Abstract methods to be implemented by subclasses

	// 判断该工厂是否包含这个bean，仅限于当前容器（即：不考虑父容器）
	protected abstract boolean containsBeanDefinition(String beanName);

	// 从注册表中查找，并返回beanName对应的BeanDefinition
	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	// 单例、原型或者作用域这三种实例化Bean的方式最终都会来调用这个方法，该方法留给子类去实现
	protected abstract Object createBean(String beanName, RootBeanDefinition mbd, Object[] args) throws BeanCreationException;

}
