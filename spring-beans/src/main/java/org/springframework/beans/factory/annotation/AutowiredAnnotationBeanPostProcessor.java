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

package org.springframework.beans.factory.annotation;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

// 当 Spring 容器启动时，AutowiredAnnotationBeanPostProcessor 将扫描 Spring 容器中所有 Bean，当发现 Bean 中拥有@Autowired
// 注释时就找到和其匹配（默认按类型匹配）的 Bean，并注入到对应的地方中去。
// Bean的属性注入是将Bean包装为BeanWrapper后才将其对应配置的属性注入的，而@Autowired注解的注入时间也是在将Bean包装为BeanWrapper
// 后，AutowiredAnnotationBeanPostProcessor实现了MergedBeanDefinitionPostProcessor接口，改接口是一个后处理接口，在bean包装
// 为 BeanWrapper后执行相应的后处理器，详见：AbstractAutowireCapableBeanFactory#doCreateBean方法
// 使用方法：
//	AutowiredAnnotationBeanPostProcessor postProcessor = new AutowiredAnnotationBeanPostProcessor();
//	postProcessor.setBeanFactory(factory);
//	factory.addBeanPostProcessor(postProcessor);
public class AutowiredAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
		implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware {

	protected final Log logger = LogFactory.getLog(getClass());

	private String requiredParameterName = "required";
	private boolean requiredParameterValue = true;
	private int order = Ordered.LOWEST_PRECEDENCE - 2;
	private ConfigurableListableBeanFactory beanFactory;
	// 自动装配注解的类型，构造器默认初始化@Autowired、@Value和@Inject这三个类型
	private final Set<Class<? extends Annotation>> autowiredAnnotationTypes = new LinkedHashSet<Class<? extends Annotation>>();
	private final Map<Class<?>, Constructor<?>[]> candidateConstructorsCache = new ConcurrentHashMap<Class<?>, Constructor<?>[]>(64);
	private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<String, InjectionMetadata>(64);


	// 构造器
	@SuppressWarnings("unchecked")
	public AutowiredAnnotationBeanPostProcessor() {
		this.autowiredAnnotationTypes.add(Autowired.class);
		this.autowiredAnnotationTypes.add(Value.class);
		try {
			this.autowiredAnnotationTypes.add((Class<? extends Annotation>)
					ClassUtils.forName("javax.inject.Inject", AutowiredAnnotationBeanPostProcessor.class.getClassLoader()));
			logger.info("JSR-330 'javax.inject.Inject' annotation found and supported for autowiring");
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
	}


	// 设置自动装配的注解类型（构造器默认初始化@Autowired、@Value和@Inject这三个类型）
	public void setAutowiredAnnotationType(Class<? extends Annotation> autowiredAnnotationType) {
		Assert.notNull(autowiredAnnotationType, "'autowiredAnnotationType' must not be null");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.add(autowiredAnnotationType);
	}
	public void setAutowiredAnnotationTypes(Set<Class<? extends Annotation>> autowiredAnnotationTypes) {
		Assert.notEmpty(autowiredAnnotationTypes, "'autowiredAnnotationTypes' must not be empty");
		this.autowiredAnnotationTypes.clear();
		this.autowiredAnnotationTypes.addAll(autowiredAnnotationTypes);
	}
	public void setRequiredParameterName(String requiredParameterName) {
		this.requiredParameterName = requiredParameterName;
	}
	public void setRequiredParameterValue(boolean requiredParameterValue) {
		this.requiredParameterValue = requiredParameterValue;
	}

	// 注入工厂
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalArgumentException(
					"AutowiredAnnotationBeanPostProcessor requires a ConfigurableListableBeanFactory");
		}
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	// BeanDefinition 被包装为 BeanWrapper 后，会调用该方法，将执行MergedBeanDefinitionPostProcessor#postProcessMergedBeanDefinition
	// 方法，将类型为beanType和beanName对应的bean注入到beanDefinition，找出所有@Autowirez注解的属性，并封装在为一个
	// InjectionMetadata，并放入this.injectionMetadataCache缓存起来
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		if (beanType != null) {
			// 找出所有@Autowirez注解的属性，并封装在为一个InjectionMetadata，并放入this.injectionMetadataCache缓存起来
			InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType);
			// 将所有配置属性（比如通过@Autowire注入的属性）保存到RootBeanDefinition#externallyManagedConfigMembers
			metadata.checkConfigMembers(beanDefinition);
		}
	}

	// 解析这个bean实例化时要使用的构造器
	@Override
	public Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
		// Quick check on the concurrent map first, with minimal locking.
		Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
		if (candidateConstructors == null) {
			synchronized (this.candidateConstructorsCache) {
				candidateConstructors = this.candidateConstructorsCache.get(beanClass);
				if (candidateConstructors == null) {
					// 获取所有的构造器
					Constructor<?>[] rawCandidates = beanClass.getDeclaredConstructors();

					// 表示候选的构造器
					List<Constructor<?>> candidates = new ArrayList<Constructor<?>>(rawCandidates.length);
					// 表示被标记为 自动装配注解标记为“required”的构造器
					Constructor<?> requiredConstructor = null;
					// 表示默认的构造器，既没有入参的那个构造器
					Constructor<?> defaultConstructor = null;
					// 遍历所有构造器，筛选出 candidates、requiredConstructor和defaultConstructor类型的构造器
					for (Constructor<?> candidate : rawCandidates) {
						// 返回修饰这个构造器的自动装配注解
						Annotation annotation = findAutowiredAnnotation(candidate);
						if (annotation != null) {
							if (requiredConstructor != null) {
								// 自动装配注解修饰构造器不能用来实例化对象
								throw new BeanCreationException("Invalid autowire-marked constructor: " + candidate +
										". Found another constructor with 'required' Autowired annotation: " +
										requiredConstructor);
							}
							if (candidate.getParameterTypes().length == 0) {
								// 自动装配注解修饰的构造器，至少要有一个入参
								throw new IllegalStateException(
										"Autowired annotation requires at least one argument: " + candidate);
							}
							// 返回这个注解的 required 值，如果这个注解没有 required，默认返回true
							boolean required = determineRequiredStatus(annotation);
							if (required) {
								if (!candidates.isEmpty()) {
									throw new BeanCreationException(
											"Invalid autowire-marked constructors: " + candidates +
											". Found another constructor with 'required' Autowired annotation: " +
											requiredConstructor);
								}
								requiredConstructor = candidate;
							}
							candidates.add(candidate);
						}
						else if (candidate.getParameterTypes().length == 0) {
							defaultConstructor = candidate;
						}
					}


					if (!candidates.isEmpty()) {
						// 如果没有被标记为“required”的构造器，就使用默认的构造器
						if (requiredConstructor == null && defaultConstructor != null) {
							candidates.add(defaultConstructor);
						}
						candidateConstructors = candidates.toArray(new Constructor<?>[candidates.size()]);
					}
					else {
						candidateConstructors = new Constructor<?>[0];
					}
					this.candidateConstructorsCache.put(beanClass, candidateConstructors);
				}
			}
		}
		return (candidateConstructors.length > 0 ? candidateConstructors : null);
	}

	// 在BeanWrapper将给定的属性值应用到给定的bean之前，将所有@Autowire注解修饰的属性注入到Bean
	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
		// 找出这个Bean中所有@Autowire注解修饰的属性，并封装为一个InjectionMetadata，以便于后续将这些属性注入到Bean
		InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass());
		try {
			// 将所有@Autowire注解修饰的属性注入到Bean
			metadata.inject(bean, beanName, pvs);
		}
		catch (Throwable ex) {
			throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
		}
		return pvs;
	}


	public void setOrder(int order) {
		this.order = order;
	}
	public int getOrder() {
		return this.order;
	}

	/**
	 * 'Native' processing method for direct calls with an arbitrary target instance,
	 * resolving all of its fields and methods which are annotated with {@code @Autowired}.
	 * @param bean the target instance to process
	 * @throws BeansException if autowiring failed
	 */
	public void processInjection(Object bean) throws BeansException {
		Class<?> clazz = bean.getClass();
		InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz);
		try {
			metadata.inject(bean, null, null);
		}
		catch (Throwable ex) {
			throw new BeanCreationException("Injection of autowired dependencies failed for class [" + clazz + "]", ex);
		}
	}
	// 找出这个Bean中所有@Autowire注解修饰的属性，并封装为一个InjectionMetadata，以便于后续将这些属性注入到Bean
	private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz) {
		// 如果beanName为空，则使用全限定类名
		String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
		InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
		// 判断 metadata 是否放进缓存
		if (InjectionMetadata.needsRefresh(metadata, clazz)) {
			synchronized (this.injectionMetadataCache) {
				metadata = this.injectionMetadataCache.get(cacheKey);
				if (InjectionMetadata.needsRefresh(metadata, clazz)) {
					metadata = buildAutowiringMetadata(clazz);
					// 将所有@Autowire注解的属性封装为一个InjectionMetadata对象，并保存到 this.injectionMetadataCache
					this.injectionMetadataCache.put(cacheKey, metadata);
				}
			}
		}
		return metadata;
	}
	private InjectionMetadata buildAutowiringMetadata(Class<?> clazz) {
		LinkedList<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();
		Class<?> targetClass = clazz;

		do {
			LinkedList<InjectionMetadata.InjectedElement> currElements = new LinkedList<InjectionMetadata.InjectedElement>();
			for (Field field : targetClass.getDeclaredFields()) {
				Annotation annotation = findAutowiredAnnotation(field);
				if (annotation != null) {
					if (Modifier.isStatic(field.getModifiers())) {
						if (logger.isWarnEnabled()) {
							logger.warn("Autowired annotation is not supported on static fields: " + field);
						}
						continue;
					}
					boolean required = determineRequiredStatus(annotation);
					currElements.add(new AutowiredFieldElement(field, required));
				}
			}
			for (Method method : targetClass.getDeclaredMethods()) {
				Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
				Annotation annotation = BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod) ?
						findAutowiredAnnotation(bridgedMethod) : findAutowiredAnnotation(method);
				if (annotation != null && method.equals(ClassUtils.getMostSpecificMethod(method, clazz))) {
					if (Modifier.isStatic(method.getModifiers())) {
						if (logger.isWarnEnabled()) {
							logger.warn("Autowired annotation is not supported on static methods: " + method);
						}
						continue;
					}
					if (method.getParameterTypes().length == 0) {
						if (logger.isWarnEnabled()) {
							logger.warn("Autowired annotation should be used on methods with actual parameters: " + method);
						}
					}
					boolean required = determineRequiredStatus(annotation);
					PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
					currElements.add(new AutowiredMethodElement(method, required, pd));
				}
			}
			elements.addAll(0, currElements);
			targetClass = targetClass.getSuperclass();
		}
		while (targetClass != null && targetClass != Object.class);

		return new InjectionMetadata(clazz, elements);
	}
	// 返回修饰 ao 对象的自动装配注解
	private Annotation findAutowiredAnnotation(AccessibleObject ao) {
		for (Class<? extends Annotation> type : this.autowiredAnnotationTypes) {
			Annotation annotation = AnnotationUtils.getAnnotation(ao, type);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}
	/**
	 * Obtain all beans of the given type as autowire candidates.
	 * @param type the type of the bean
	 * @return the target beans, or an empty Collection if no bean of this type is found
	 * @throws BeansException if bean retrieval failed
	 */
	protected <T> Map<String, T> findAutowireCandidates(Class<T> type) throws BeansException {
		if (this.beanFactory == null) {
			throw new IllegalStateException("No BeanFactory configured - " +
					"override the getBeanOfType method or specify the 'beanFactory' property");
		}
		return BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, type);
	}
	// 返回这个注解的 required 值，如果这个注解没有 required，默认返回true
	protected boolean determineRequiredStatus(Annotation annotation) {
		try {
			// 返回这个注解的 required() 方法
			Method method = ReflectionUtils.findMethod(annotation.annotationType(), this.requiredParameterName);
			if (method == null) {
				// annotations like @Inject and @Value don't have a method (attribute) named "required" -> default to required status
				return true;
			}
			return (this.requiredParameterValue == (Boolean) ReflectionUtils.invokeMethod(method, annotation));
		}
		catch (Exception ex) {
			// an exception was thrown during reflective invocation of the required attribute -> default to required status
			return true;
		}
	}
	// 注册依赖关系，beanName对应的Bean依赖autowiredBeanNames中指定的Bean
	private void registerDependentBeans(String beanName, Set<String> autowiredBeanNames) {
		if (beanName != null) {
			for (String autowiredBeanName : autowiredBeanNames) {
				if (this.beanFactory.containsBean(autowiredBeanName)) {
					this.beanFactory.registerDependentBean(autowiredBeanName, beanName);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Autowiring by type from bean name '" + beanName +
							"' to bean named '" + autowiredBeanName + "'");
				}
			}
		}
	}
	/** Resolve the specified cached method argument or field value. */
	private Object resolvedCachedArgument(String beanName, Object cachedArgument) {
		if (cachedArgument instanceof DependencyDescriptor) {
			DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
			TypeConverter typeConverter = this.beanFactory.getTypeConverter();
			return this.beanFactory.resolveDependency(descriptor, beanName, null, typeConverter);
		}
		else if (cachedArgument instanceof RuntimeBeanReference) {
			return this.beanFactory.getBean(((RuntimeBeanReference) cachedArgument).getBeanName());
		}
		else {
			return cachedArgument;
		}
	}

	// 该内部类用于将@Autowire修饰的属性注入到Bean中
	private class AutowiredFieldElement extends InjectionMetadata.InjectedElement {
		private final boolean required;
		// 是否已经缓存了被自动装配注解修饰的属性值
		private volatile boolean cached = false;
		// 用来缓存被自动装配注解修饰的属性值
		private volatile Object cachedFieldValue;

		public AutowiredFieldElement(Field field, boolean required) {
			super(field, null);
			this.required = required;
		}

		// 将这个被自动装配注解修饰的属性注入到Bean中
		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			Field field = (Field) this.member;
			try {
				Object value;
				if (this.cached) {
					value = resolvedCachedArgument(beanName, this.cachedFieldValue);
				}
				else {
					DependencyDescriptor descriptor = new DependencyDescriptor(field, this.required);
					Set<String> autowiredBeanNames = new LinkedHashSet<String>(1);
					TypeConverter typeConverter = beanFactory.getTypeConverter();
					// 先初始化被自动装配注解修饰的属性对象
					value = beanFactory.resolveDependency(descriptor, beanName, autowiredBeanNames, typeConverter);
					synchronized (this) {
						if (!this.cached) {
							if (value != null || this.required) {
								this.cachedFieldValue = descriptor;
								// 注册bean之间的依赖关系
								registerDependentBeans(beanName, autowiredBeanNames);
								if (autowiredBeanNames.size() == 1) {
									String autowiredBeanName = autowiredBeanNames.iterator().next();
									if (beanFactory.containsBean(autowiredBeanName)) {
										if (beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
											this.cachedFieldValue = new RuntimeBeanReference(autowiredBeanName);
										}
									}
								}
							}
							else {
								this.cachedFieldValue = null;
							}
							this.cached = true;
						}
					}
				}

				// 如果被依赖的对象值不为空的话，就将该属性注入到Bean中
				if (value != null) {
					// 设置这个属性是可访问的
					ReflectionUtils.makeAccessible(field);
					field.set(bean, value);
				}
			}
			catch (Throwable ex) {
				throw new BeanCreationException("Could not autowire field: " + field, ex);
			}
		}
	}
	// 该内部类用于将@Autowire修饰的方法对应的属性注入到Bean中
	private class AutowiredMethodElement extends InjectionMetadata.InjectedElement {
		private final boolean required;
		private volatile boolean cached = false;
		private volatile Object[] cachedMethodArguments;

		public AutowiredMethodElement(Method method, boolean required, PropertyDescriptor pd) {
			super(method, pd);
			this.required = required;
		}

		@Override
		protected void inject(Object bean, String beanName, PropertyValues pvs) throws Throwable {
			if (checkPropertySkipping(pvs)) {
				return;
			}
			Method method = (Method) this.member;
			try {
				Object[] arguments;
				if (this.cached) {
					// Shortcut for avoiding synchronization...
					arguments = resolveCachedArguments(beanName);
				}
				else {
					Class<?>[] paramTypes = method.getParameterTypes();
					arguments = new Object[paramTypes.length];
					DependencyDescriptor[] descriptors = new DependencyDescriptor[paramTypes.length];
					Set<String> autowiredBeanNames = new LinkedHashSet<String>(paramTypes.length);
					TypeConverter typeConverter = beanFactory.getTypeConverter();
					for (int i = 0; i < arguments.length; i++) {
						MethodParameter methodParam = new MethodParameter(method, i);
						GenericTypeResolver.resolveParameterType(methodParam, bean.getClass());
						descriptors[i] = new DependencyDescriptor(methodParam, this.required);
						arguments[i] = beanFactory.resolveDependency(
								descriptors[i], beanName, autowiredBeanNames, typeConverter);
						if (arguments[i] == null && !this.required) {
							arguments = null;
							break;
						}
					}
					synchronized (this) {
						if (!this.cached) {
							if (arguments != null) {
								this.cachedMethodArguments = new Object[arguments.length];
								for (int i = 0; i < arguments.length; i++) {
									this.cachedMethodArguments[i] = descriptors[i];
								}
								registerDependentBeans(beanName, autowiredBeanNames);
								if (autowiredBeanNames.size() == paramTypes.length) {
									Iterator<String> it = autowiredBeanNames.iterator();
									for (int i = 0; i < paramTypes.length; i++) {
										String autowiredBeanName = it.next();
										if (beanFactory.containsBean(autowiredBeanName)) {
											if (beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
												this.cachedMethodArguments[i] = new RuntimeBeanReference(autowiredBeanName);
											}
										}
									}
								}
							}
							else {
								this.cachedMethodArguments = null;
							}
							this.cached = true;
						}
					}
				}
				if (arguments != null) {
					ReflectionUtils.makeAccessible(method);
					method.invoke(bean, arguments);
				}
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
			catch (Throwable ex) {
				throw new BeanCreationException("Could not autowire method: " + method, ex);
			}
		}
		private Object[] resolveCachedArguments(String beanName) {
			if (this.cachedMethodArguments == null) {
				return null;
			}
			Object[] arguments = new Object[this.cachedMethodArguments.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = resolvedCachedArgument(beanName, this.cachedMethodArguments[i]);
			}
			return arguments;
		}
	}

}
