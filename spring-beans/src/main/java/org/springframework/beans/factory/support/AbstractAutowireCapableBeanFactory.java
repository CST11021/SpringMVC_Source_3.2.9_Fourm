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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Abstract bean factory superclass that implements default bean creation,
 * with the full capabilities specified by the {@link RootBeanDefinition} class.
 * Implements the {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * interface in addition to AbstractBeanFactory's {@link #createBean} method.
 *
 * <p>Provides bean creation (with constructor resolution), property population,
 * wiring (including autowiring), and initialization. Handles runtime bean
 * references, resolves managed collections, calls initialization methods, etc.
 * Supports autowiring constructors, properties by name, and properties by type.
 *
 * <p>The main template method to be implemented by subclasses is
 * {@link #resolveDependency(DependencyDescriptor, String, Set, TypeConverter)},
 * used for autowiring by type. In case of a factory which is capable of searching
 * its bean definitions, matching beans will typically be implemented through such
 * a search. For other factory styles, simplified matching algorithms can be implemented.
 *
 * <p>Note that this class does <i>not</i> assume or implement bean definition
 * registry capabilities. See {@link DefaultListableBeanFactory} for an implementation
 * of the {@link org.springframework.beans.factory.ListableBeanFactory} and
 * {@link BeanDefinitionRegistry} interfaces, which represent the API and SPI
 * view of such a factory, respectively.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @author Costin Leau
 * @author Chris Beams
 * @author Sam Brannen
 * @since 13.02.2004
 * @see RootBeanDefinition
 * @see DefaultListableBeanFactory
 * @see BeanDefinitionRegistry
 */
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {

	// 表示创建bean实例的策略接口
	private InstantiationStrategy instantiationStrategy = new CglibSubclassingInstantiationStrategy();
	// 用来获取函数（包括方法和构造器）的参数
	private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
	// 是否允许bean之间的循环引用
	private boolean allowCircularReferences = true;
	// 是否在允许循环引用的情况下使用原始bean实例，即使注入的bean最终被包装了
	private boolean allowRawInjectionDespiteWrapping = false;
	// 使用一个Set对象来存放在依赖检查和自动装配时要忽略的依赖类型，比如：String.默认为空
	private final Set<Class<?>> ignoredDependencyTypes = new HashSet<Class<?>>();
	// 存放要忽略的依赖接口
	private final Set<Class<?>> ignoredDependencyInterfaces = new HashSet<Class<?>>();
	// 缓存还没有完成的FactoryBean实例
	private final Map<String, BeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, BeanWrapper>(16);
	//** Cache of filtered PropertyDescriptors: bean Class -> PropertyDescriptor array */
	private final Map<Class<?>, PropertyDescriptor[]> filteredPropertyDescriptorsCache = new ConcurrentHashMap<Class<?>, PropertyDescriptor[]>(64);

	// 构造器
	public AbstractAutowireCapableBeanFactory() {
		super();
		ignoreDependencyInterface(BeanNameAware.class);
		ignoreDependencyInterface(BeanFactoryAware.class);
		ignoreDependencyInterface(BeanClassLoaderAware.class);
	}
	public AbstractAutowireCapableBeanFactory(BeanFactory parentBeanFactory) {
		this();
		setParentBeanFactory(parentBeanFactory);
	}



	// 设置忽略自动装配的类型
	public void ignoreDependencyType(Class<?> type) {
		this.ignoredDependencyTypes.add(type);
	}
	// 设置忽略自动装配的接口
	public void ignoreDependencyInterface(Class<?> ifc) {
		this.ignoredDependencyInterfaces.add(ifc);
	}
	// 从另一个工厂复制相关的配置信息，比如：bean的实例化策略、是否允许循环依赖等
	@Override
	public void copyConfigurationFrom(ConfigurableBeanFactory otherFactory) {
		super.copyConfigurationFrom(otherFactory);
		if (otherFactory instanceof AbstractAutowireCapableBeanFactory) {
			AbstractAutowireCapableBeanFactory otherAutowireFactory = (AbstractAutowireCapableBeanFactory) otherFactory;
			this.instantiationStrategy = otherAutowireFactory.instantiationStrategy;
			this.allowCircularReferences = otherAutowireFactory.allowCircularReferences;
			this.ignoredDependencyTypes.addAll(otherAutowireFactory.ignoredDependencyTypes);
			this.ignoredDependencyInterfaces.addAll(otherAutowireFactory.ignoredDependencyInterfaces);
		}
	}


	// Typical methods for creating and populating external bean instances
	// 调用AbstractBeanFactory#createBean 方法创建一个bean实例
	@SuppressWarnings("unchecked")
	public <T> T createBean(Class<T> beanClass) throws BeansException {
		// Use prototype bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(beanClass);
		bd.setScope(SCOPE_PROTOTYPE);
		bd.allowCaching = false;
		return (T) createBean(beanClass.getName(), bd, null);
	}
	public void autowireBean(Object existingBean) {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(ClassUtils.getUserClass(existingBean));
		bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		bd.allowCaching = false;
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}
	public Object configureBean(Object existingBean, String beanName) throws BeansException {
		markBeanAsCreated(beanName);
		BeanDefinition mbd = getMergedBeanDefinition(beanName);
		RootBeanDefinition bd = null;
		if (mbd instanceof RootBeanDefinition) {
			RootBeanDefinition rbd = (RootBeanDefinition) mbd;
			bd = (rbd.isPrototype() ? rbd : rbd.cloneBeanDefinition());
		}
		if (!mbd.isPrototype()) {
			if (bd == null) {
				bd = new RootBeanDefinition(mbd);
			}
			bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
			bd.allowCaching = false;
		}
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(beanName, bd, bw);
		return initializeBean(beanName, existingBean, bd);
	}
	public Object resolveDependency(DependencyDescriptor descriptor, String beanName) throws BeansException {
		return resolveDependency(descriptor, beanName, null, null);
	}



	// Specialized methods for fine-grained control over the bean lifecycle
	// 实例化一个bean
	public Object createBean(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		return createBean(beanClass.getName(), bd, null);
	}
	// 根据指定autowireMode实例化策略，初始化这个bean，bean的初始化有多种方式，比如利用反射机制调用构造器实例化，或
	// 通过动态代理的方式实例化，或通过工厂模式进行创建等
	public Object autowire(Class<?> beanClass, int autowireMode, boolean dependencyCheck) throws BeansException {
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		final RootBeanDefinition bd = new RootBeanDefinition(beanClass, autowireMode, dependencyCheck);
		bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		if (bd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR) {
			return autowireConstructor(beanClass.getName(), bd, null, null).getWrappedInstance();
		}
		else {
			Object bean;
			final BeanFactory parent = this;
			if (System.getSecurityManager() != null) {
				bean = AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						return getInstantiationStrategy().instantiate(bd, null, parent);
					}
				}, getAccessControlContext());
			}
			else {
				bean = getInstantiationStrategy().instantiate(bd, null, parent);
			}
			populateBean(beanClass.getName(), bd, new BeanWrapperImpl(bean));
			return bean;
		}
	}
	// 给这个已经实例化的bean进行属性注入
	public void autowireBeanProperties(Object existingBean, int autowireMode, boolean dependencyCheck) throws BeansException {

		if (autowireMode == AUTOWIRE_CONSTRUCTOR) {
			throw new IllegalArgumentException("AUTOWIRE_CONSTRUCTOR not supported for existing bean instance");
		}
		// Use non-singleton bean definition, to avoid registering bean as dependent bean.
		RootBeanDefinition bd =
				new RootBeanDefinition(ClassUtils.getUserClass(existingBean), autowireMode, dependencyCheck);
		bd.setScope(BeanDefinition.SCOPE_PROTOTYPE);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		populateBean(bd.getBeanClass().getName(), bd, bw);
	}
	// 给这个已经实例化的bean进行属性注入
	public void applyBeanPropertyValues(Object existingBean, String beanName) throws BeansException {
		markBeanAsCreated(beanName);
		BeanDefinition bd = getMergedBeanDefinition(beanName);
		BeanWrapper bw = new BeanWrapperImpl(existingBean);
		initBeanWrapper(bw);
		applyPropertyValues(beanName, bd, bw, bd.getPropertyValues());
	}
	public Object initializeBean(Object existingBean, String beanName) {
		return initializeBean(beanName, existingBean, null);
	}
	// 执行后处理器方法：Bean 调用构造函数，实例化之前执行postProcessBeforeInitialization方法
	public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName) throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
			result = beanProcessor.postProcessBeforeInitialization(result, beanName);
			if (result == null) {
				return result;
			}
		}
		return result;
	}




	// Implementation of relevant AbstractBeanFactory template methods 实现AbstractBeanFactory里一些模板方法
	// ------------------------- 实例化Bean的实现，真正的实现方法请看doCreateBean()方法 ------------------------------------------------------------
	@Override
	protected Object createBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) throws BeanCreationException {
		//////////////////////步骤一：根据设置的class属性或者根据className来解析Class
		if (logger.isDebugEnabled()) {
			logger.debug("Creating instance of bean '" + beanName + "'");
		}
		// 根据设置的class属性或根据classname来解析这个bean的class，并判断这个bean是否可以实例化，这个类是否可以通过类装载器来载入
		resolveBeanClass(mbd, beanName);

		//////////////////////步骤二：对Override属性进行标记及验证
		try {
			// 检查是否有配置 lookup-method 和 replace-method 覆盖方法，并判断这个覆盖方法是否存在
			// 在Spring中存在 lookup-method 和 replace-method 的，而这两个配置的加载其实就是将配置统一存放到BeanDefinition中的methodOverride属性里，而这个函数的操作其实也就是针对这两个配置的
			mbd.prepareMethodOverrides();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName, "Validation of method overrides failed", ex);
		}

		//////////////////////步骤三：应用初始化前的后处理器，解析指定bean是否存在初始化前的短路操作
		try {
			// 给 BeanPostProcessors 一个机会来返回代理来替代真正的实例，如果bean配置了PostProcessor，那么这里返回的是一个proxy
			Object bean = resolveBeforeInstantiation(beanName, mbd);
			if (bean != null) {
				return bean;
			}
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, "BeanPostProcessor before instantiation of bean failed", ex);
		}

		//////////////////////步骤四：创建bean
		// 当经历过resolveBeanforeInstantiation方法后，程序有两个选择，如果创建了代理或者说重写了InstantiationAwareBeanPostProcessor的postProcessBeforeInstantiation方法
		// 并在方法postProcessBeforeInstantiation中改变了bean，则直接返回就可以了，否则需要进行常规bean的创建。而这常规bean的创建就是在doCreateBean方法中完成的。
		Object beanInstance = doCreateBean(beanName, mbd, args);
		if (logger.isDebugEnabled()) {
			logger.debug("Finished creating instance of bean '" + beanName + "'");
		}
		return beanInstance;
	}
	// 进行常规bean的创建
	protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final Object[] args) {

		//////////////////////步骤一：如果单例则需要首先清除缓存
		BeanWrapper instanceWrapper = null;
		if (mbd.isSingleton()) {
			// 如果是singleton，先把缓存中的同名bean清除
			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
		}


		////////////////////////步骤二：实例化bean，将 BeanDefinition 转换为 BeanWrapper（将实例化一个Bean实例然后在包装成BeanWrapper对象）
		if (instanceWrapper == null) {
			// 这里是创建bean的地方，由createBeanInstance方法来完成，根据指定bean使用相应的策略（如：工厂方法、构造函数自动注入、简单初始化）创建实例
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		final Object bean = (instanceWrapper != null ? instanceWrapper.getWrappedInstance() : null);
		Class<?> beanType = (instanceWrapper != null ? instanceWrapper.getWrappedClass() : null);


		//////////////////////步骤三：MergedBeanDefinitionPostProcessor 的应用
		// Allow post-processors to modify the merged bean definition.
		synchronized (mbd.postProcessingLock) {
			// 判断这个bean是否已经应用了MergedBeanDefinitionPostProcessor处理器
			if (!mbd.postProcessed) {
				applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				mbd.postProcessed = true;
			}
		}


		//////////////////////步骤四：依赖处理，代码理解详见《Spring源码深度解析》113页
		// Eagerly cache singletons to be able to resolve circular references even when triggered by lifecycle interfaces like BeanFactoryAware.
		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName));
		if (earlySingletonExposure) {
			if (logger.isDebugEnabled()) {
				logger.debug("Eagerly caching bean '" + beanName + "' to allow for resolving potential circular references");
			}
			// 为避免后期循环依赖，可以在bean初始化完成前将创建实例的ObjectFactory加入缓存：
			// 这就是 AbstractBeanFactory#doGetBean 方法中创建单例Bean时，为什么要用 ObjectFactory接口 的形式来创建的原因
			addSingletonFactory(beanName, new ObjectFactory<Object>() {
				public Object getObject() throws BeansException {
					// 对bean再一次依赖引用，主要应用SmartInstantiationAware BeanPostProcessor，其中我们熟知的AOP就是在这里将advice动态织入bean中，
					// 若没有则直接返回bean，不做任何处理
					return getEarlyBeanReference(beanName, mbd, bean);
				}
			});
		}


		//////////////////////步骤五：属性填充，将所有属性填充到bean实例中
		// 这里是对bean的初始化，依赖注入往往在这里发生，这个exposedObject在初始化处理完以后会返回作为依赖注入完成的bean
		// Initialize the bean instance.
		Object exposedObject = bean;
		try {
			//对bean进行填充，将各个属性值注入，其中，可能存在依赖于其他bean的属性，则会递归初始化依赖bean
			populateBean(beanName, mbd, instanceWrapper);
			if (exposedObject != null) {
				//调用初始化方法，比如init-method
				exposedObject = initializeBean(beanName, exposedObject, mbd);
			}
		}
		catch (Throwable ex) {
			if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
				throw (BeanCreationException) ex;
			}
			else {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
			}
		}


		//////////////////////步骤六：循环依赖检查：
		// 在Spring中解决循环依赖只对单例有效，而对于prototype的bean，Spring没有好的解决办法，唯一要做的就是抛出异常。
		// 在这个步骤里会检查已经加载的bean是否已经出现了依赖循环，并判断是否需要抛出异常
		if (earlySingletonExposure) {
			Object earlySingletonReference = getSingleton(beanName, false);
			//earlySingletonReference只有在检测到有循环依赖的情况下才会不为空
			if (earlySingletonReference != null) {
				//如果exposedObject没有在初始化方法中被改变，也就是没有被增强
				if (exposedObject == bean) {
					exposedObject = earlySingletonReference;
				}
				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
					String[] dependentBeans = getDependentBeans(beanName);
					Set<String> actualDependentBeans = new LinkedHashSet<String>(dependentBeans.length);
					for (String dependentBean : dependentBeans) {
						//检测依赖
						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
							actualDependentBeans.add(dependentBean);
						}
					}
					//因为bean创建后其所依赖的bean一定是已经创建的，actualDependentBeans不为空则表示当前bean创建后期依赖的bean却没有没全部创建完，也就是说存在循环依赖
					if (!actualDependentBeans.isEmpty()) {
						throw new BeanCurrentlyInCreationException(beanName,
								"Bean with name '" + beanName + "' has been injected into other beans [" +
								StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
								"] in its raw version as part of a circular reference, but has eventually been " +
								"wrapped. This means that said other beans do not use the final version of the " +
								"bean. This is often the result of over-eager type matching - consider using " +
								"'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
					}
				}
			}
		}

		//////////////////////步骤七：注册DisposableBean，如果配置了destroy-method，这里需要注册以便于在销毁时候调用
		try {
			//根据scopse注册bean
			registerDisposableBeanIfNecessary(beanName, bean, mbd);
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
		}

		//////////////////////步骤八：完成创建并返回
		return exposedObject;
	}
	// 创建一个bean实例，并返回这个bean的BeanWrapper对象
	protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, Object[] args) {
		// 返回这个bean的类型
		Class<?> beanClass = resolveBeanClass(mbd, beanName);

		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
		}

		// ①如果工厂方法不为空，则使用工厂方法初始化策略
		if (mbd.getFactoryMethodName() != null)  {
			// 如果在RootBeanDefinition中存在factoryMethodName属性，或者说在配置文件配置了factory-method，
			// 那么Spring会尝试使用instantiateUsingFactoryMethod方法根据RootBeanDefinition中的配置生成bean实例
			return instantiateUsingFactoryMethod(beanName, mbd, args);
		}

		// ②解析构造函数并进行构造函数的实例化
		boolean resolved = false;// 标识是否已经知道使用哪个构造器来实例化
		boolean autowireNecessary = false;
		if (args == null) {
			synchronized (mbd.constructorArgumentLock) {
				// Spring在根据参数及类型去判断使用哪个构造函数是一个比较消耗性能的步骤，所以采用缓存机制，如果已经解析过则不需要重复解析，
				// 而是直接从 RootBeanDefinition 中 resolvedConstructorOrFactoryMethod 缓存的值去取
				if (mbd.resolvedConstructorOrFactoryMethod != null) {
					// resolvedConstructorOrFactoryMethod 不为空表示已经知道使用哪个构造器来实例化了
					resolved = true;
					autowireNecessary = mbd.constructorArgumentsResolved;
				}
			}
		}

		// 如果已经解析过则使用解析好的构造函数方法不需要再次锁定
		if (resolved) {
			if (autowireNecessary) {
				// 1、构造函数自动注入
				return autowireConstructor(beanName, mbd, null, null);
			}
			else {
				// 2、默认使用构造函数（无参）进行实例化
				return instantiateBean(beanName, mbd);
			}
		}



		// 如果程序走到这里，则表示还不知道使用哪个构造器来进行实例化，所以接下来需要根据参数解析的需要使用的构造函数
		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
		if (ctors != null ||
				mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args))  {
			//构造函数自动注入
			return autowireConstructor(beanName, mbd, ctors, args);
		}

		// 默认使用构造函数（无参）进行实例化
		return instantiateBean(beanName, mbd);
	}


	//---------------------------------------------- Spring 构造BeanWrapper时使用以下三种方式的一种--------------------------------------------
	// 使用工厂bean创建一个bean实例，并返回一个BeanWrapper
	protected BeanWrapper instantiateUsingFactoryMethod(String beanName, RootBeanDefinition mbd, Object[] explicitArgs) {
		return new ConstructorResolver(this).instantiateUsingFactoryMethod(beanName, mbd, explicitArgs);
	}
	// 使用指定构造器自动注入
	protected BeanWrapper autowireConstructor(String beanName, RootBeanDefinition mbd, Constructor<?>[] ctors, Object[] explicitArgs) {
		return new ConstructorResolver(this).autowireConstructor(beanName, mbd, ctors, explicitArgs);
	}
	// 默认使用构造函数（无参）进行实例化：
	// 此方法并没有什么实质性的逻辑，带有参数的实例构造中，Spring把精力都放在了构造函数以及参数的匹配上，
	// 所以如果没有参数的话那将是非常简单的一件事，直接调用实例化策略进行实例化就可以了
	protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
		try {
			Object beanInstance;
			final BeanFactory parent = this;
			if (System.getSecurityManager() != null) {
				beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						return getInstantiationStrategy().instantiate(mbd, beanName, parent);
					}
				}, getAccessControlContext());
			}
			else {
				beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
			}
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			initBeanWrapper(bw);
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
		}
	}
	//---------------------------------------------- Spring 构造BeanWrapper时使用以上三种方式的一种--------------------------------------------



	@Override
	protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
		Class<?> targetType = mbd.getTargetType();
		if (targetType == null) {
			targetType = (mbd.getFactoryMethodName() != null ?
				getTypeForFactoryMethod(beanName, mbd, typesToMatch) : resolveBeanClass(mbd, beanName, typesToMatch));
			if (ObjectUtils.isEmpty(typesToMatch) || getTempClassLoader() == null) {
				mbd.setTargetType(targetType);
			}
		}
		// Apply SmartInstantiationAwareBeanPostProcessors to predict the eventual type after a before-instantiation shortcut.
		if (targetType != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					Class<?> predicted = ibp.predictBeanType(targetType, beanName);
					if (predicted != null && (typesToMatch.length != 1 || !FactoryBean.class.equals(typesToMatch[0]) ||
							FactoryBean.class.isAssignableFrom(predicted))) {
						return predicted;
					}
				}
			}
		}
		return targetType;
	}

	/**
	 * Determine the bean type for the given bean definition which is based on
	 * a factory method. Only called if there is no singleton instance registered
	 * for the target bean already.
	 * <p>This implementation determines the type matching {@link #createBean}'s
	 * different creation strategies. As far as possible, we'll perform static
	 * type checking to avoid creation of the target bean.
	 * @param beanName the name of the bean (for error handling purposes)
	 * @param mbd the merged bean definition for the bean
	 * @param typesToMatch the types to match in case of internal type matching purposes
	 * (also signals that the returned {@code Class} will never be exposed to application code)
	 * @return the type for the bean if determinable, or {@code null} else
	 * @see #createBean
	 */
	protected Class<?> getTypeForFactoryMethod(String beanName, RootBeanDefinition mbd, Class[] typesToMatch) {
		Class<?> factoryClass;
		boolean isStatic = true;

		String factoryBeanName = mbd.getFactoryBeanName();
		if (factoryBeanName != null) {
			if (factoryBeanName.equals(beanName)) {
				throw new BeanDefinitionStoreException(mbd.getResourceDescription(), beanName,
						"factory-bean reference points back to the same bean definition");
			}
			// Check declared factory method return type on factory class.
			factoryClass = getType(factoryBeanName);
			isStatic = false;
		}
		else {
			// Check declared factory method return type on bean class.
			factoryClass = resolveBeanClass(mbd, beanName, typesToMatch);
		}

		if (factoryClass == null) {
			return null;
		}

		// If all factory methods have the same return type, return that type.
		// Can't clearly figure out exact method due to type converting / autowiring!
		Class<?> commonType = null;
		int minNrOfArgs = mbd.getConstructorArgumentValues().getArgumentCount();
		Method[] candidates = ReflectionUtils.getUniqueDeclaredMethods(factoryClass);
		for (Method factoryMethod : candidates) {
			if (Modifier.isStatic(factoryMethod.getModifiers()) == isStatic &&
					factoryMethod.getName().equals(mbd.getFactoryMethodName()) &&
					factoryMethod.getParameterTypes().length >= minNrOfArgs) {
				// No declared type variables to inspect, so just process the standard return type.
				if (factoryMethod.getTypeParameters().length > 0) {
					try {
						// Fully resolve parameter names and argument values.
						Class<?>[] paramTypes = factoryMethod.getParameterTypes();
						String[] paramNames = null;
						ParameterNameDiscoverer pnd = getParameterNameDiscoverer();
						if (pnd != null) {
							paramNames = pnd.getParameterNames(factoryMethod);
						}
						ConstructorArgumentValues cav = mbd.getConstructorArgumentValues();
						Set<ConstructorArgumentValues.ValueHolder> usedValueHolders =
								new HashSet<ConstructorArgumentValues.ValueHolder>(paramTypes.length);
						Object[] args = new Object[paramTypes.length];
						for (int i = 0; i < args.length; i++) {
							ConstructorArgumentValues.ValueHolder valueHolder = cav.getArgumentValue(
									i, paramTypes[i], (paramNames != null ? paramNames[i] : null), usedValueHolders);
							if (valueHolder == null) {
								valueHolder = cav.getGenericArgumentValue(null, null, usedValueHolders);
							}
							if (valueHolder != null) {
								args[i] = valueHolder.getValue();
								usedValueHolders.add(valueHolder);
							}
						}
						Class<?> returnType = AutowireUtils.resolveReturnTypeForFactoryMethod(
								factoryMethod, args, getBeanClassLoader());
						if (returnType != null) {
							commonType = ClassUtils.determineCommonAncestor(returnType, commonType);
						}
					}
					catch (Throwable ex) {
						if (logger.isDebugEnabled()) {
							logger.debug("Failed to resolve generic return type for factory method: " + ex);
						}
					}
				}
				else {
					commonType = ClassUtils.determineCommonAncestor(factoryMethod.getReturnType(), commonType);
				}
			}
		}

		if (commonType != null) {
			// Clear return type found: all factory methods return same type.
			return commonType;
		}
		else {
			// Ambiguous return types found: return null to indicate "not determinable".
			return null;
		}
	}

	/**
	 * This implementation attempts to query the FactoryBean's generic parameter metadata
	 * if present to determine the object type. If not present, i.e. the FactoryBean is
	 * declared as a raw type, checks the FactoryBean's {@code getObjectType} method
	 * on a plain instance of the FactoryBean, without bean properties applied yet.
	 * If this doesn't return a type yet, a full creation of the FactoryBean is
	 * used as fallback (through delegation to the superclass's implementation).
	 * <p>The shortcut check for a FactoryBean is only applied in case of a singleton
	 * FactoryBean. If the FactoryBean instance itself is not kept as singleton,
	 * it will be fully created to check the type of its exposed object.
	 */
	@Override
	protected Class<?> getTypeForFactoryBean(String beanName, RootBeanDefinition mbd) {
		class Holder { Class<?> value = null; }
		final Holder objectType = new Holder();
		String factoryBeanName = mbd.getFactoryBeanName();
		final String factoryMethodName = mbd.getFactoryMethodName();
		if (factoryBeanName != null && factoryMethodName != null) {
			// Try to obtain the FactoryBean's object type without instantiating it at all.
			BeanDefinition fbDef = getBeanDefinition(factoryBeanName);
			if (fbDef instanceof AbstractBeanDefinition && ((AbstractBeanDefinition) fbDef).hasBeanClass()) {
				// CGLIB subclass methods hide generic parameters; look at the original user class.
				Class<?> fbClass = ClassUtils.getUserClass(((AbstractBeanDefinition) fbDef).getBeanClass());
				// Find the given factory method, taking into account that in the case of
				// @Bean methods, there may be parameters present.
				ReflectionUtils.doWithMethods(fbClass,
						new ReflectionUtils.MethodCallback() {
							public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
								if (method.getName().equals(factoryMethodName) &&
										FactoryBean.class.isAssignableFrom(method.getReturnType())) {
									objectType.value = GenericTypeResolver.resolveReturnTypeArgument(method, FactoryBean.class);
								}
							}
						});
				if (objectType.value != null) {
					return objectType.value;
				}
			}
		}

		FactoryBean<?> fb = (mbd.isSingleton() ?
				getSingletonFactoryBeanForTypeCheck(beanName, mbd) :
				getNonSingletonFactoryBeanForTypeCheck(beanName, mbd));

		if (fb != null) {
			// Try to obtain the FactoryBean's object type from this early stage of the instance.
			objectType.value = getTypeForFactoryBean(fb);
			if (objectType.value != null) {
				return objectType.value;
			}
		}

		// No type found - fall back to full creation of the FactoryBean instance.
		return super.getTypeForFactoryBean(beanName, mbd);
	}

	/**
	 	   比如：CircularityA引用CircularityB，CircularityB引用CircularityC，CircularityC引用CircularityA
	 （1） Spring容器创建单例“circularityA” Bean。首先依据无參构造器创建“circularityA” Bean， 并暴露一个exposedObject
	 	   用于返回提前暴露的Bean。并将“circularityA”Bean放到Cache中。然后进行setter注入“circularityB”;
	 （2） Spring容器创建单例“circularityB" Bean。首先依据无參构造器创建“circularityB" Bean，并暴露一个exposedObject用
	 	   于返回提前暴露的Bean。并将“circularityB” Bean放到Cache中，然后进行setter注入“circularityC”;
	 （3） Spring容器创建单例“circularityC” Bean，首先依据无參构造器创建“circularityC” Bean，并暴露一个exposedObject
	 	   用于返回暴露的Bean。并将“circularityC” Bean放入Cache中， 然后进行setter注入“circularityA”。进行注入“circularityA”
	 	   时因为步骤1提前暴露了exposedObject所以从之前的Cache里面拿Bean不用反复创建。
	 （4） 最后在依赖注入“circularityB”和“circularityA”也是从catch里面拿提前暴露的bean。 完毕setter注入。
	 可是对于“prototype”作用域Bean。Spring容器无法完毕依赖注入，由于“prototype”作用域的Bean，Spring容器不进行缓存，因此无法提前暴露一个创建中的Bean。
	 */
	// 该方法中，如果入参bean是 circularityA 这个Bean，则在第一创建circularityA时会返回null，因为该bean还没有被实例化，而
	// 经过一轮循环依赖后，第二次创建 circularityA 调用该方法时，就会返回第一次实例化好并暴露出来的 circularityA 对象
	protected Object getEarlyBeanReference(String beanName, RootBeanDefinition mbd, Object bean) {
		Object exposedObject = bean;
		if (bean != null && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					exposedObject = ibp.getEarlyBeanReference(exposedObject, beanName);
					if (exposedObject == null) {
						return exposedObject;
					}
				}
			}
		}
		return exposedObject;
	}


	// Implementation methods

	/**
	 * Obtain a "shortcut" singleton FactoryBean instance to use for a
	 * {@code getObjectType()} call, without full initialization
	 * of the FactoryBean.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @return the FactoryBean instance, or {@code null} to indicate
	 * that we couldn't obtain a shortcut FactoryBean instance
	 */
	private FactoryBean<?> getSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		synchronized (getSingletonMutex()) {
			BeanWrapper bw = this.factoryBeanInstanceCache.get(beanName);
			if (bw != null) {
				return (FactoryBean<?>) bw.getWrappedInstance();
			}
			if (isSingletonCurrentlyInCreation(beanName)) {
				return null;
			}
			Object instance = null;
			try {
				// Mark this bean as currently in creation, even if just partially.
				beforeSingletonCreation(beanName);
				// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
				instance = resolveBeforeInstantiation(beanName, mbd);
				if (instance == null) {
					bw = createBeanInstance(beanName, mbd, null);
					instance = bw.getWrappedInstance();
				}
			}
			finally {
				// Finished partial creation of this bean.
				afterSingletonCreation(beanName);
			}
			FactoryBean<?> fb = getFactoryBean(beanName, instance);
			if (bw != null) {
				this.factoryBeanInstanceCache.put(beanName, bw);
			}
			return fb;
		}
	}

	/**
	 * Obtain a "shortcut" non-singleton FactoryBean instance to use for a
	 * {@code getObjectType()} call, without full initialization
	 * of the FactoryBean.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @return the FactoryBean instance, or {@code null} to indicate
	 * that we couldn't obtain a shortcut FactoryBean instance
	 */
	private FactoryBean<?> getNonSingletonFactoryBeanForTypeCheck(String beanName, RootBeanDefinition mbd) {
		if (isPrototypeCurrentlyInCreation(beanName)) {
			return null;
		}
		Object instance = null;
		try {
			// Mark this bean as currently in creation, even if just partially.
			beforePrototypeCreation(beanName);
			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
			instance = resolveBeforeInstantiation(beanName, mbd);
			if (instance == null) {
				BeanWrapper bw = createBeanInstance(beanName, mbd, null);
				instance = bw.getWrappedInstance();
			}
		}
		finally {
			// Finished partial creation of this bean.
			afterPrototypeCreation(beanName);
		}
		return getFactoryBean(beanName, instance);
	}

	// bean 被包装为 BeanWrapper 后，会调用该方法，将执行MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition 方法
	protected void applyMergedBeanDefinitionPostProcessors(RootBeanDefinition mbd, Class<?> beanType, String beanName) throws BeansException {

		try {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof MergedBeanDefinitionPostProcessor) {
					MergedBeanDefinitionPostProcessor bdp = (MergedBeanDefinitionPostProcessor) bp;
					bdp.postProcessMergedBeanDefinition(mbd, beanType, beanName);
				}
			}
		}
		catch (Exception ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Post-processing failed of bean type [" + beanType + "] failed", ex);
		}
	}

	// 实例化前处理方法
	protected Object resolveBeforeInstantiation(String beanName, RootBeanDefinition mbd) {
		Object bean = null;
		// 如果尚未被解析
		if (!Boolean.FALSE.equals(mbd.beforeInstantiationResolved)) {
			// 确保这个bean 在是被加载和创建的
			if (mbd.hasBeanClass() && !mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {

				// 执行后处理器的一系列“实例化前处理方法”
				bean = applyBeanPostProcessorsBeforeInstantiation(mbd.getBeanClass(), beanName);
				if (bean != null) {
					// 执行后处理器的一系列“实例化后处理方法”
					bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
				}
			}
			mbd.beforeInstantiationResolved = (bean != null);
		}
		return bean;
	}
	// bean实例化前的后处理器：
	// 在bean实例化前调用，也就是将AbstractBeanDefinition转换为BeanWrapper前的处理。给子类一个修改BeanDefinition的机会，
	// 也就说程序经过这个方法后，Bean可能已经不是我们的认为的Bean了，而是或许成为了一个经过处理的代理bean，可能是通过cglib生成的，也可能是通过其他技术生成的。
	protected Object applyBeanPostProcessorsBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		// 执行这个Bean 的一系列后处理器，如果执行处理器方法的过程中，这个bean就被实例化了，则不再执行后续的处理器方法
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof InstantiationAwareBeanPostProcessor) {
				InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
				// 调用后处理器的 实例化前处理方法
				Object result = ibp.postProcessBeforeInstantiation(beanClass, beanName);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	// bean实例化后的后处理器：
	// 在从缓存中获取单例Bean的时候，Spring中的规则是在bean的初始化后尽可能保证将注册的后处理的postProcessAfterInitialization方法应用到该bean中，
	// 因为如果返回的bean不为空，那么便不会再次经历普通bean的创建创建过程，所以只能在这里应用后处理器的postProcessAfterInitialization方法
	public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName) throws BeansException {
		Object result = existingBean;
		// 执行这个Bean 的一系列后处理器
		for (BeanPostProcessor beanProcessor : getBeanPostProcessors()) {
			// 调用后处理器的 实例化后处理方法
			result = beanProcessor.postProcessAfterInitialization(result, beanName);
			if (result == null) {
				return result;
			}
		}
		return result;
	}



	// 解析这个bean实例化时要使用的构造器
	protected Constructor<?>[] determineConstructorsFromBeanPostProcessors(Class<?> beanClass, String beanName) throws BeansException {

		if (beanClass != null && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof SmartInstantiationAwareBeanPostProcessor) {
					SmartInstantiationAwareBeanPostProcessor ibp = (SmartInstantiationAwareBeanPostProcessor) bp;
					Constructor<?>[] ctors = ibp.determineCandidateConstructors(beanClass, beanName);
					if (ctors != null) {
						return ctors;
					}
				}
			}
		}
		return null;
	}



	// 用给定的包装器填充bean实例中的属性
	// populateBean函数的处理流程：
	// 一、执行InstantiationAwareBeanPostProcessor处理器的postProcessAfterInstantiation方法，该函数可以控制程序是否继
	// 续进行属性填充。
	// 二、根据注入类型(byName/byType)，提取依赖的bean，并统一存入PropertyValues中。
	// 三、执行InstantiationAwareBeanPostProcessor处理器的postProcessPropertyValues方法，对属性获取完毕填充前对属性的
	// 再次处理，典型应用是 requiredAnnotationBeanPostProcessor 类中对属性的验证。
	// 四、将所有PropertyValues中的属性填充至BeanWrapper中
	protected void populateBean(String beanName, RootBeanDefinition mbd, BeanWrapper bw) {
		// 获取配置中property的值，PropertyValues封装了bean中一个属性
		PropertyValues pvs = mbd.getPropertyValues();

		if (bw == null) {
			if (!pvs.isEmpty()) {
				throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
			}
			else {
				// 没有可填充的属性
				return;
			}
		}

		// 第一步：给 InstantiationAwareBeanPostProcessors 最后一次机会在属性设置前来改变bean，如：可以支持属性注入的类型
		boolean continueWithPropertyPopulation = true;

		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
			for (BeanPostProcessor bp : getBeanPostProcessors()) {
				if (bp instanceof InstantiationAwareBeanPostProcessor) {
					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
					//返回值为是否继续填充bean
					if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
						continueWithPropertyPopulation = false;
						break;
					}
				}
			}
		}

		// 如果后处理器发出停止填充命令则终止后续的执行
		if (!continueWithPropertyPopulation) {
			return;
		}

		// 第二步：开始进行依赖注入的过程，这里先处理autowire的注入，默认不使用自动装配
		if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME || mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);

			// 根据名称自动注入
			if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
				autowireByName(beanName, mbd, bw, newPvs);
			}

			// 根据类型自动注入
			if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
				autowireByType(beanName, mbd, bw, newPvs);
			}

			pvs = newPvs;
		}

		//第三步：执行InstantiationAwareBeanPostProcessor处理器的postProcessPropertyValues方法，将@Autowire注解的属性注入
		boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
		//需要依赖检查
		boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);

		if (hasInstAwareBpps || needsDepCheck) {
			PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
			if (hasInstAwareBpps) {
				for (BeanPostProcessor bp : getBeanPostProcessors()) {
					if (bp instanceof InstantiationAwareBeanPostProcessor) {
						InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
						//对所有需要依赖检查的属性进行后处理
						pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
						if (pvs == null) {
							return;
						}
					}
				}
			}
			if (needsDepCheck) {
				//依赖检查，对应的depends-on属性，3.0已经弃用此属性
				checkDependencies(beanName, mbd, filteredPds, pvs);
			}
		}

		// 第四步：对属性进行注入
		applyPropertyValues(beanName, mbd, bw, pvs);
	}
	// 如果将autowire设置为“byName”，则在该工厂中填充任何缺失的属性值。
	protected void autowireByName(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {
		// 返回一组非简单类型的bean属性，该数组中的属性都是映射到其他的bean，并且他们允许被注入的
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			// 判断容器中是否包含propertyName的bean
			if (containsBean(propertyName)) {
				Object bean = getBean(propertyName);
				pvs.add(propertyName, bean);
				// 注册（缓存）bean之间的依赖关系
				registerDependentBean(propertyName, beanName);
				if (logger.isDebugEnabled()) {
					logger.debug("Added autowiring by name from bean name '" + beanName +
							"' via property '" + propertyName + "' to bean named '" + propertyName + "'");
				}
			}
			else {
				if (logger.isTraceEnabled()) {
					logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
							"' by name: no matching bean found");
				}
			}
		}
	}
	// 按类型自动装配
	protected void autowireByType(String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}

		Set<String> autowiredBeanNames = new LinkedHashSet<String>(4);
		// 寻找bw中需要依赖注入的属性
		String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
		for (String propertyName : propertyNames) {
			try {
				PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
				// Don't try autowiring by type for type Object: never makes sense, even if it technically is a unsatisfied, non-simple property.
				if (!Object.class.equals(pd.getPropertyType())) {
					// 获取这个属性描述器对应set方法的MethodParameter对象
					MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
					// Do not allow eager init for type matching in case of a prioritized post-processor.
					// 在处理优先级后处理器的情况下，不要使用急切的init来进行类型匹配。
					boolean eager = !PriorityOrdered.class.isAssignableFrom(bw.getWrappedClass());
					DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
					// 寻找类型匹配的逻辑实现封装在了 resolveDependency 函数中
					// 解析指定beanName的属性所配置的值，并把解析到的属性名称存储在autowiredBeanNames中，当属性存在多个封装bean时，如：
					Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
					if (autowiredArgument != null) {
						pvs.add(propertyName, autowiredArgument);
					}
					for (String autowiredBeanName : autowiredBeanNames) {
						//注册依赖
						registerDependentBean(autowiredBeanName, beanName);
						if (logger.isDebugEnabled()) {
							logger.debug("Autowiring by type from bean name '" + beanName + "' via property '" + propertyName + "' to bean named '" + autowiredBeanName + "'");
						}
					}
					autowiredBeanNames.clear();
				}
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
			}
		}
	}
	/**
	 * Return an array of non-simple bean properties that are unsatisfied.
	 * 返回一组不满足的非简单类型的bean属性。
	 * These are probably unsatisfied references to other beans in the factory. Does not include simple properties like primitives or Strings.
	 * 这些可能是对工厂中其他bean的不满意的引用。不包括简单的属性，如原语或字符串。
	 * @param mbd the merged bean definition the bean was created with
	 * @param bw the BeanWrapper the bean was created with
	 * @return an array of bean property names
	 * @see org.springframework.beans.BeanUtils#isSimpleProperty
	 */
	// 返回一组非简单类型的bean属性，该数组中的属性都是映射到其他的bean，并且他们允许被注入的
	protected String[] unsatisfiedNonSimpleProperties(AbstractBeanDefinition mbd, BeanWrapper bw) {
		Set<String> result = new TreeSet<String>();
		PropertyValues pvs = mbd.getPropertyValues();
		PropertyDescriptor[] pds = bw.getPropertyDescriptors();
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && !isExcludedFromDependencyCheck(pd) && !pvs.contains(pd.getName()) &&
					!BeanUtils.isSimpleProperty(pd.getPropertyType())) {
				result.add(pd.getName());
			}
		}
		return StringUtils.toStringArray(result);
	}

	/**
	 * Extract a filtered set of PropertyDescriptors from the given BeanWrapper,
	 * excluding ignored dependency types or properties defined on ignored dependency interfaces.
	 * @param bw the BeanWrapper the bean was created with
	 * @param cache whether to cache filtered PropertyDescriptors for the given bean Class
	 * @return the filtered PropertyDescriptors
	 * @see #isExcludedFromDependencyCheck
	 * @see #filterPropertyDescriptorsForDependencyCheck(org.springframework.beans.BeanWrapper)
	 */
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw, boolean cache) {
		PropertyDescriptor[] filtered = this.filteredPropertyDescriptorsCache.get(bw.getWrappedClass());
		if (filtered == null) {
			if (cache) {
				synchronized (this.filteredPropertyDescriptorsCache) {
					filtered = this.filteredPropertyDescriptorsCache.get(bw.getWrappedClass());
					if (filtered == null) {
						filtered = filterPropertyDescriptorsForDependencyCheck(bw);
						this.filteredPropertyDescriptorsCache.put(bw.getWrappedClass(), filtered);
					}
				}
			}
			else {
				filtered = filterPropertyDescriptorsForDependencyCheck(bw);
			}
		}
		return filtered;
	}

	/**
	 * Extract a filtered set of PropertyDescriptors from the given BeanWrapper,
	 * excluding ignored dependency types or properties defined on ignored dependency interfaces.
	 * @param bw the BeanWrapper the bean was created with
	 * @return the filtered PropertyDescriptors
	 * @see #isExcludedFromDependencyCheck
	 */
	protected PropertyDescriptor[] filterPropertyDescriptorsForDependencyCheck(BeanWrapper bw) {
		List<PropertyDescriptor> pds =
				new LinkedList<PropertyDescriptor>(Arrays.asList(bw.getPropertyDescriptors()));
		for (Iterator<PropertyDescriptor> it = pds.iterator(); it.hasNext();) {
			PropertyDescriptor pd = it.next();
			if (isExcludedFromDependencyCheck(pd)) {
				it.remove();
			}
		}
		return pds.toArray(new PropertyDescriptor[pds.size()]);
	}

	/**
	 * Determine whether the given bean property is excluded from dependency checks.
	 * <p>This implementation excludes properties defined by CGLIB and
	 * properties whose type matches an ignored dependency type or which
	 * are defined by an ignored dependency interface.
	 * @param pd the PropertyDescriptor of the bean property
	 * @return whether the bean property is excluded
	 * @see #ignoreDependencyType(Class)
	 * @see #ignoreDependencyInterface(Class)
	 */
	protected boolean isExcludedFromDependencyCheck(PropertyDescriptor pd) {
		return (AutowireUtils.isExcludedFromDependencyCheck(pd) ||
				this.ignoredDependencyTypes.contains(pd.getPropertyType()) ||
				AutowireUtils.isSetterDefinedInInterface(pd, this.ignoredDependencyInterfaces));
	}

	/**
	 * Perform a dependency check that all properties exposed have been set,
	 * if desired. Dependency checks can be objects (collaborating beans),
	 * simple (primitives and String), or all (both).
	 * @param beanName the name of the bean
	 * @param mbd the merged bean definition the bean was created with
	 * @param pds the relevant property descriptors for the target bean
	 * @param pvs the property values to be applied to the bean
	 * @see #isExcludedFromDependencyCheck(java.beans.PropertyDescriptor)
	 */
	protected void checkDependencies(String beanName, AbstractBeanDefinition mbd, PropertyDescriptor[] pds, PropertyValues pvs) throws UnsatisfiedDependencyException {

		int dependencyCheck = mbd.getDependencyCheck();
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() != null && !pvs.contains(pd.getName())) {
				boolean isSimple = BeanUtils.isSimpleProperty(pd.getPropertyType());
				boolean unsatisfied = (dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_ALL) ||
						(isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_SIMPLE) ||
						(!isSimple && dependencyCheck == RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
				if (unsatisfied) {
					throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, pd.getName(),
							"Set this property value or disable dependency checking for this bean.");
				}
			}
		}
	}

	// 将属性注入到bean：
	// 程序运行到这里，已经完成了对所有注入属性的获取，但是获取的属性是以PropertyValues形式存在的，还并没有应用到已经
	// 实例化的bean中，这一工作是在applyPropertyValues中。
	protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
		if (pvs == null || pvs.isEmpty()) {
			return;
		}

		MutablePropertyValues mpvs = null;
		List<PropertyValue> original;

		if (System.getSecurityManager() != null) {
			if (bw instanceof BeanWrapperImpl) {
				((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
			}
		}

		if (pvs instanceof MutablePropertyValues) {
			mpvs = (MutablePropertyValues) pvs;
			// 如果mpvs中的值已经被转换为对应的类型那么可以直接设置到BeanWrapper中
			if (mpvs.isConverted()) {
				// Shortcut: use the pre-converted values as-is.
				try {
					bw.setPropertyValues(mpvs);
					return;
				}
				catch (BeansException ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Error setting property values", ex);
				}
			}
			original = mpvs.getPropertyValueList();
		}
		else {
			// 如果pvs不是使用MutablePropertyValues封装的类型，那么直接使用原始的属性获取方法
			original = Arrays.asList(pvs.getPropertyValues());
		}

		TypeConverter converter = getCustomTypeConverter();
		if (converter == null) {
			converter = bw;
		}
		// 获取对应的解析器
		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);

		// Create a deep copy, resolving any references for values.
		List<PropertyValue> deepCopy = new ArrayList<PropertyValue>(original.size());
		boolean resolveNecessary = false;
		// 遍历属性，将属性转换为对应类的对应属性的类型
		for (PropertyValue pv : original) {
			if (pv.isConverted()) {
				deepCopy.add(pv);
			}
			else {
				String propertyName = pv.getName();
				Object originalValue = pv.getValue();
				Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
				Object convertedValue = resolvedValue;
				boolean convertible = bw.isWritableProperty(propertyName) && !PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
				if (convertible) {
					convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
				}
				// Possibly store converted value in merged bean definition,
				// in order to avoid re-conversion for every created bean instance.
				if (resolvedValue == originalValue) {
					if (convertible) {
						pv.setConvertedValue(convertedValue);
					}
					deepCopy.add(pv);
				}
				else if (convertible && originalValue instanceof TypedStringValue &&
						!((TypedStringValue) originalValue).isDynamic() &&
						!(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
					pv.setConvertedValue(convertedValue);
					deepCopy.add(pv);
				}
				else {
					resolveNecessary = true;
					deepCopy.add(new PropertyValue(pv, convertedValue));
				}
			}
		}
		if (mpvs != null && !resolveNecessary) {
			mpvs.setConverted();
		}

		// Set our (possibly massaged) deep copy.
		try {
			bw.setPropertyValues(new MutablePropertyValues(deepCopy));
		}
		catch (BeansException ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Error setting property values", ex);
		}
	}

	/**
	 * Convert the given value for the specified target property.
	 */
	private Object convertForProperty(Object value, String propertyName, BeanWrapper bw, TypeConverter converter) {
		if (converter instanceof BeanWrapperImpl) {
			return ((BeanWrapperImpl) converter).convertForProperty(value, propertyName);
		}
		else {
			PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
			MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
			return converter.convertIfNecessary(value, pd.getPropertyType(), methodParam);
		}
	}

	// 初始化这个bean实例，如果Spring配置文件中Bean有配置init-method属性，则该方法会被调用，并在populateBean方法后被调用
	// 执行相应的工厂回调，初始化方法和后处理器：
	protected Object initializeBean(final String beanName, final Object bean, RootBeanDefinition mbd) {
		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run() {
					// 如果Bean实现了Aware接口（包括：BeanNameAware、BeanClassLoaderAware和BeanFactoryAware）则会在这里
					// 执行自动注入的方法
					invokeAwareMethods(beanName, bean);
					return null;
				}
			}, getAccessControlContext());
		}
		else {
			// 对特殊的bean处理：Aware、BeanClassLoaderAware、BeanFactoryAware
			// 为这个bean执行相应的自动注入方法，比如自动生成beanName，bean实例化的类加载器，工厂容器的注入
			invokeAwareMethods(beanName, bean);
		}

		Object wrappedBean = bean;
		if (mbd == null || !mbd.isSynthetic()) {
			// 应用后处理器（Bean 调用构造函数，实例化之前执行后处理器方法）
			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
		}

		try {
			// 激活用户自定义的init方法
			invokeInitMethods(beanName, wrappedBean, mbd);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(
					(mbd != null ? mbd.getResourceDescription() : null),
					beanName, "Invocation of init method failed", ex);
		}

		if (mbd == null || !mbd.isSynthetic()) {
			// 应用后处理器（Bean 调用构造函数，实例化之后执行后处理器方法）
			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		}
		return wrappedBean;
	}

	// 为这个bean执行相应的自动注入方法，比如自动生成beanName，bean实例化的类加载器，工厂容器的注入
	private void invokeAwareMethods(final String beanName, final Object bean) {
		if (bean instanceof Aware) {
			if (bean instanceof BeanNameAware) {
				((BeanNameAware) bean).setBeanName(beanName);
			}
			if (bean instanceof BeanClassLoaderAware) {
				((BeanClassLoaderAware) bean).setBeanClassLoader(getBeanClassLoader());
			}
			if (bean instanceof BeanFactoryAware) {
				((BeanFactoryAware) bean).setBeanFactory(AbstractAutowireCapableBeanFactory.this);
			}
		}
	}

	// 客户定制的初始化方法处理我们熟知的使用配置init-method 外，还有使自定义的bean实现InitializingBean接口，
	// 并在afterPropertiesSet中实现自己的初始化业务逻辑。init-method 与 afterPropertiesSet 都是在初始化bean时执行，
	// 执行顺序是afterPropertiesSet先执行，而init-method后执行。在invokeInitMethods方法中就实现了这两步骤的初始化方法调用。
	protected void invokeInitMethods(String beanName, final Object bean, RootBeanDefinition mbd) throws Throwable {
		// 首先会检查是否是InitializingBean，如果是的话需要调用afterPropertiesSet方法
		boolean isInitializingBean = (bean instanceof InitializingBean);
		if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
			}
			if (System.getSecurityManager() != null) {
				try {
					AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
						public Object run() throws Exception {
							((InitializingBean) bean).afterPropertiesSet();
							return null;
						}
					}, getAccessControlContext());
				}
				catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			}
			else {
				// 属性初始化后的处理
				((InitializingBean) bean).afterPropertiesSet();
			}
		}

		if (mbd != null) {
			String initMethodName = mbd.getInitMethodName();
			if (initMethodName != null && !(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
					!mbd.isExternallyManagedInitMethod(initMethodName)) {
				// 调用自定义初始化方法
				invokeCustomInitMethod(beanName, bean, mbd);
			}
		}
	}

	/**
	 * Invoke the specified custom init method on the given bean.
	 * Called by invokeInitMethods.
	 * <p>Can be overridden in subclasses for custom resolution of init
	 * methods with arguments.
	 * @see #invokeInitMethods
	 */
	protected void invokeCustomInitMethod(String beanName, final Object bean, RootBeanDefinition mbd) throws Throwable {
		String initMethodName = mbd.getInitMethodName();
		final Method initMethod = (mbd.isNonPublicAccessAllowed() ?
				BeanUtils.findMethod(bean.getClass(), initMethodName) :
				ClassUtils.getMethodIfAvailable(bean.getClass(), initMethodName));
		if (initMethod == null) {
			if (mbd.isEnforceInitMethod()) {
				throw new BeanDefinitionValidationException("Couldn't find an init method named '" +
						initMethodName + "' on bean with name '" + beanName + "'");
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("No default init method named '" + initMethodName +
							"' found on bean with name '" + beanName + "'");
				}
				// Ignore non-existent default lifecycle methods.
				return;
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Invoking init method  '" + initMethodName + "' on bean with name '" + beanName + "'");
		}

		if (System.getSecurityManager() != null) {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
				public Object run() throws Exception {
					ReflectionUtils.makeAccessible(initMethod);
					return null;
				}
			});
			try {
				AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
					public Object run() throws Exception {
						initMethod.invoke(bean);
						return null;
					}
				}, getAccessControlContext());
			}
			catch (PrivilegedActionException pae) {
				InvocationTargetException ex = (InvocationTargetException) pae.getException();
				throw ex.getTargetException();
			}
		}
		else {
			try {
				ReflectionUtils.makeAccessible(initMethod);
				initMethod.invoke(bean);
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
		}
	}


	/**
	 * Applies the {@code postProcessAfterInitialization} callback of all registered BeanPostProcessors,
	 * 适用于所有注册BeanPostProcessors postProcessAfterInitialization回调
	 * giving them a chance to post-process the object obtained from FactoryBeans (for example, to auto-proxy them).
	 * 给他们一个机会来处理从FactoryBeans获得的对象(例如，自动代理他们)。
	 * @see #applyBeanPostProcessorsAfterInitialization
	 */
	@Override
	protected Object postProcessObjectFromFactoryBean(Object object, String beanName) {
		return applyBeanPostProcessorsAfterInitialization(object, beanName);
	}

	/**
	 * Overridden to clear FactoryBean instance cache as well.
	 */
	@Override
	protected void removeSingleton(String beanName) {
		super.removeSingleton(beanName);
		this.factoryBeanInstanceCache.remove(beanName);
	}



	// getter and setter ...
	public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}
	protected InstantiationStrategy getInstantiationStrategy() {
		return this.instantiationStrategy;
	}
	public void setParameterNameDiscoverer(ParameterNameDiscoverer parameterNameDiscoverer) {
		this.parameterNameDiscoverer = parameterNameDiscoverer;
	}
	protected ParameterNameDiscoverer getParameterNameDiscoverer() {
		return this.parameterNameDiscoverer;
	}
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}
	public void setAllowRawInjectionDespiteWrapping(boolean allowRawInjectionDespiteWrapping) {
		this.allowRawInjectionDespiteWrapping = allowRawInjectionDespiteWrapping;
	}

	/**
	 * Special DependencyDescriptor variant for Spring's good old autowire="byType" mode.
	 * Always optional; never considering the parameter name for choosing a primary candidate.
	 */
	@SuppressWarnings("serial")
	private static class AutowireByTypeDependencyDescriptor extends DependencyDescriptor {

		public AutowireByTypeDependencyDescriptor(MethodParameter methodParameter, boolean eager) {
			super(methodParameter, false, eager);
		}

		@Override
		public String getDependencyName() {
			return null;
		}
	}




}
