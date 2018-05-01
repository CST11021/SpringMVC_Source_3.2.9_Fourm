
package org.springframework.context.support;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.support.ResourceEditorRegistrar;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.LifecycleProcessor;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.context.weaving.LoadTimeWeaverAware;
import org.springframework.context.weaving.LoadTimeWeaverAwareProcessor;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

// ApplicationContext接口的默认实现，即Spring容器抽象父类
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext, DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	static {
		// Eagerly load the ContextClosedEvent class to avoid weird classloader issues on application shutdown in WebLogic 8.1. (Reported by Dustin Woods.)
		// 加载 ContextClosedEvent 类，避免在WebLogic 8.1中应用程序关闭的奇怪类加载问题。
		ContextClosedEvent.class.getName();
	}

	// 工厂中的MessageSource bean的名称。如果没有提供，则将消息解析委托给父容器，默认使用 DelegatingMessageSource。
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";
	// 工厂中的 LifecycleProcessor（生命周期处理器）的bean名称。如果没有提供，默认使用 DefaultLifecycleProcessor 。
	public static final String LIFECYCLE_PROCESSOR_BEAN_NAME = "lifecycleProcessor";
	// ApplicationEventMulticaster bean在工厂的名称。如果没有提供,使用一个默认SimpleApplicationEventMulticaster。
	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";


	// 以“org.springframework.context.support.AbstractApplicationContext + @ + 16进制哈希值串” 作为Spring容器的id
	private String id = ObjectUtils.identityToString(this);
	private String displayName = ObjectUtils.identityToString(this);
	// 表示当前容器的父容器
	private ApplicationContext parent;
	// 用于保存Spring容器的后置处理器
	private final List<BeanFactoryPostProcessor> beanFactoryPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
	// 用于记录容器启动时间
	private long startupDate;

	// 作为一个同步监视器锁：监视容器是否处于“活动”的状态
	private final Object activeMonitor = new Object();
	//AtomicBoolean用于比较两个Boolean类型的值，如果一致，执行方法内的语句。其实就是一个if语句，值得注意的是比较和执行两
	// 个操作是作为一个原子性的事务操作，中间不会出现线程暂停的情况，主要为多线程的控制提供解决的方案。
	private boolean active = false;// 标记容器是否正在启动
	private boolean closed = false;// 标记容器是否是关闭状态


	//** 作为一个同步监视器锁：Synchronization monitor for the "refresh" and "destroy"
	private final Object startupShutdownMonitor = new Object();
	/**
	 在Java程序中可以通过添加关闭钩子，实现在程序退出时关闭资源、平滑退出的功能。
	 使用Runtime.addShutdownHook(Thread hook)方法，可以注册一个JVM关闭的钩子，这个钩子可以在以下几种场景被调用：
	 1. 程序正常退出
	 2. 使用System.exit()
	 3. 终端使用Ctrl+C触发的中断
	 4. 系统关闭
	 5. 使用Kill pid命令干掉进程
	 */
	private Thread shutdownHook;
	//** 用于获取配置文件的Resource对象，该实例是在构造器中初始化的，默认实现是PathMatchingResourcePatternResolver
	private ResourcePatternResolver resourcePatternResolver;
	//** LifecycleProcessor for managing the lifecycle of beans within this context
	private LifecycleProcessor lifecycleProcessor;
	//** MessageSource we delegate our implementation of this interface to
	private MessageSource messageSource;
	//** 事件广播器，Spring的事件广播机制就通过ApplicationEventMulticaster来实现的，该实例在refresh()方法中初始化，默认
	// 实现是SimpleApplicationEventMulticaster
	private ApplicationEventMulticaster applicationEventMulticaster;
	//** Statically specified listeners
	private Set<ApplicationListener<?>> applicationListeners = new LinkedHashSet<ApplicationListener<?>>();

	// 初始化时使用标准的Envirionment实现类 StandardEnvironment
	private ConfigurableEnvironment environment;


	// 构造器
	public AbstractApplicationContext() {
		// 创建一个 resourcePatternResolver
		this.resourcePatternResolver = getResourcePatternResolver();
	}
	public AbstractApplicationContext(ApplicationContext parent) {
		this();
		setParent(parent);
	}


	// ------------------------------------------- 实现 ApplicationContext 接口 ----------------------------------------

	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return this.id;
	}
	public String getApplicationName() {
		return "";
	}
	public void setDisplayName(String displayName) {
		Assert.hasLength(displayName, "Display name must not be empty");
		this.displayName = displayName;
	}
	public String getDisplayName() {
		return this.displayName;
	}
	public ApplicationContext getParent() {
		return this.parent;
	}
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			this.environment = createEnvironment();
		}
		return this.environment;
	}
	public void setEnvironment(ConfigurableEnvironment environment) {
		this.environment = environment;
	}
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		return getBeanFactory();
	}
	public long getStartupDate() {
		return this.startupDate;
	}
	// 当完成ApplicationContext初始化的时候，要通过Spring中的重要事件发布机制来发出ContextRefreshedEvent事件，以保证对应
	// 的监听器可以做进一步的逻辑处理
	public void publishEvent(ApplicationEvent event) {
		Assert.notNull(event, "Event must not be null");
		if (logger.isTraceEnabled()) {
			logger.trace("Publishing event in " + getDisplayName() + ": " + event);
		}
		getApplicationEventMulticaster().multicastEvent(event);
		if (this.parent != null) {
			this.parent.publishEvent(event);
		}
	}
	private ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}
	private LifecycleProcessor getLifecycleProcessor() {
		if (this.lifecycleProcessor == null) {
			throw new IllegalStateException("LifecycleProcessor not initialized - " +
					"call 'refresh' before invoking lifecycle methods via the context: " + this);
		}
		return this.lifecycleProcessor;
	}
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PathMatchingResourcePatternResolver(this);
	}


	// ------------------------------------------- 实现 ConfigurableApplicationContext 接口 ----------------------------

	public void refresh() throws BeansException, IllegalStateException {
		synchronized (this.startupShutdownMonitor) {
			// 1、准备刷新的上下文环境：例如对系统属性及环境变量的初始化及验证，在某种情况下项目的使用需要读取某些系统变
			// 量，而这个变量的设置很可能会影响着系统的正确性，那么 ClassPathXMLApplicationContext 为我们提供的这个准备
			// 函数就显得非常必要，他可以在Spring启动的时候提前对必须的变量存在性验证
			prepareRefresh();

			// 2、初始化BeanFactory，执行obtainFreshBeanFactory()方法后ApplicationContext就拥有BeanFactory的功能
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// 3、注意，程序至此时，配置的Bean还没有进行实例化，只是以BeanDefinition 的形式存储的注册表中，对BeanFactory
			// 进行各种功能模式填充，@Qualifier和@Autowired应该是大家非常熟悉的注解，那么这两个注解正是在这一步骤中增加
			// 的支持。该方法主要功能：设置类加载器、添加SPEL表达式语言的处理器、设置属性编辑器、
			prepareBeanFactory(beanFactory);

			try {
				// 4、子类覆盖方法做额外的处理，默认空实现
				postProcessBeanFactory(beanFactory);

				// 5、执行所有 BeanDefinitionRegistryPostProcessor 和 BeanFactoryPostProcessor 类型的处理器
				// ①BeanDefinitionRegistryPostProcessor其中的一个应用是运用后处理器，处理BeanDefinition注册表中的一些占位符，
				// 因为Spring在解析Bean的时候时候，如果有引用占位符，在解析阶段占位符是不会被翻译的，ApplicationContext 通过
				// 后处理器的方式来进行翻译相应的占位符，如果你使用BeanFactory级别的容器，那么调用getBean方法返回的Bean的属性
				// 信息是永远不会被解析的；
				// ②BeanFactoryProcess 是工厂后处理器，spring容器解析完配置文件（注册了所有的BeanDefinition）之后，并在所有
				// bean实例化之前被调用的
				// 另外，BeanDefinitionRegistryPostProcessor 继承自 BeanFactoryPostProcessor
				invokeBeanFactoryPostProcessors(beanFactory);

				// 6、注册拦截Bean创建的Bean处理器，这里只是注册处理器，处理器执行是在getBean时候
				// BeanPostProcessor 处理器是Bean调用构造函数实例化前和实例化后执行的处理器，还有MergedBeanDefinitionPostProcessor
				// 类型的处理器（该处理器在BeanDefinition 被包装为 BeanWrapper 后，会调用该处理器，将类型为beanType和beanName
				// 对应的bean注入到beanDefinition；
				// MergedBeanDefinitionPostProcessor 继承自 BeanPostProcessor
				registerBeanPostProcessors(beanFactory);

				// 7、为上下文初始化Message源，即不同语言的消息体，国际化处理
				initMessageSource();

				// 8、初始化应用消息广播器，并放入“applicationEventMulticaster”bean中
				initApplicationEventMulticaster();

				// 9、留给子类来初始化其他的Bean
				onRefresh();

				// 10、在所有注册的bean中查找Listener bean，注册到消息广播器中
				registerListeners();

				// 11、初始化剩下的单实例（非惰性）：完成BeanFactoryBean的初始化工作，其中包括ConversionService的设置，
				// 配置冻结以及非延迟加载的bean的初始化工作。
				finishBeanFactoryInitialization(beanFactory);

				// 12、完成刷新过程，通知生命周期处理器lifecycleProcessor 刷新过程，同时发出ContextRefreshEvent通知别人
				finishRefresh();
			}

			catch (BeansException ex) {
				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}
		}
	}
	// 1、准备刷新的上下文环境：例如对系统属性及环境变量的初始化及验证
	protected void prepareRefresh() {
		// 记录容器开始时间，并将容器标志为正在启动的状态
		this.startupDate = System.currentTimeMillis();
		synchronized (this.activeMonitor) {
			this.active = true;
		}

		if (logger.isInfoEnabled()) {
			logger.info("Refreshing " + this);
		}

		// 空方法，留给子类覆盖（比如添加一些必要的系统参数等）
		initPropertySources();

		// 验证一些必要的参数或环境变量的是否已经添加
		getEnvironment().validateRequiredProperties();
	}
	/**
	 * 我们可以子类中实现该方法，添加一些初始化工作，如：
	 * public class MyClassPathXmlApplicationContext extends ClassPathXmlAppliacationContext{
	 *     public MyClassPathXmlApplicationContext(String... configLocations){
	 *         super(configLocations);
	 *     }
	 *     protected void initPropertySources(){
	 *         //添加验证要求
	 *         getEnvironment().setRequiredProperties("VAR");
	 *     }
	 * }
	 *
	 * 这样当容器初始化的时候，程序执行到 getEnvironment().validateRequiredProperties(); 代码的时候，如果系统没有检测到
	 * 对VAR的环境变量，那么将抛出异常。
	 */
	protected void initPropertySources() {
		// For subclasses: do nothing by default.
	}
	// 2、初始化BeanFactory，执行obtainFreshBeanFactory()方法后ApplicationContext就拥有BeanFactory的功能
	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
		// 初始化BeanFactory，并进行XML文件读取，并将得到的BeanFactory实例赋值给记录在当前实体的属性中
		refreshBeanFactory();
		// 返回当前容器的内部 BeanFactory
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (logger.isDebugEnabled()) {
			logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
		}
		return beanFactory;
	}
	// 3、在实例化bean前，我们需要做一些准备工作，如：设置类装载器、BeanDefinition的解析器、添加SPEL表达式语言的处理器、
	// 注册属性编辑器等工作。对BeanFactory进行各种功能模式填充，@Qualifier和@Autowired应该是大家非常熟悉的注解，那么这两
	// 个注解正是在这一步骤中增加的支持。
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 告诉内部 beanFactory 使用上下文的类装载器
		beanFactory.setBeanClassLoader(getClassLoader());
		// 设置解析 BeanDefinition 的表达式解析器
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());
		// 添加属性编辑器
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

		// 添加 BeanPostProcessor 后处理器
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));

		// 设置一些忽略了自动装配依赖的接口
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);

		// BeanFactory interface not registered as resolvable type in a plain factory. BeanFactory接口没有在普通工厂中注册为可解析类型。
		// MessageSource registered (and found for autowiring) as a bean. 使用 autowire 的方式，将 MessageSource 作为bean注入
		// 注册一个与依赖类型相对应的自动绑定值
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// Detect a LoadTimeWeaver and prepare for weaving, if found.
		if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			// Set a temporary ClassLoader for type matching.
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}

		// 注册默认环境（Environment）相关的bean
		if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
		}
	}
	// 4、子类覆盖方法做额外的处理
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {}
	// 5、执行所有BeanDefinitionRegistryPostProcessor和BeanFactoryPostProcessor类型的处理器
	// BeanDefinitionRegistryPostProcessor 的一个典型应用是扫描指定包及其子包下面拥有指定注解的类，你会发现在BeanFactory中并没有
	// 使用到该后处理器，该后处理器为Spring容器扩展而设计的，IOC容器只加载一些常规的Bean配置，而像@Service、@Repository等这些注解
	// 定义的Bean是Spring容器中才扩展出来的，其中 BeanDefinitionRegistryPostProcessor 还有一个典型的应用是Mybatis中的@Mapper.
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {

		// --------------------------------第一步：处理 BeanDefinitionRegistryPostProcessor 类型的处理器-----------------

		Set<String> processedBeans = new HashSet<String>();
		if (beanFactory instanceof BeanDefinitionRegistry) {
			BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			// 保存Spring容器中非BeanDefinitionRegistryPostProcessor类型的处理器
			List<BeanFactoryPostProcessor> regularPostProcessors = new LinkedList<BeanFactoryPostProcessor>();
			// 保存Spring容器中BeanDefinitionRegistryPostProcessor类型的处理器（在完成 BeanDefinition 注册后，实例化bean之前调用）
			List<BeanDefinitionRegistryPostProcessor> registryPostProcessors = new LinkedList<BeanDefinitionRegistryPostProcessor>();

			// 1、遍历Spring容器中所有的处理器，做分类，并执行所有BeanDefinitionRegistryPostProcessor后处理器
			for (BeanFactoryPostProcessor postProcessor : getBeanFactoryPostProcessors()) {
				if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
					BeanDefinitionRegistryPostProcessor registryPostProcessor = (BeanDefinitionRegistryPostProcessor) postProcessor;
					// 执行BeanDefinitionRegistryPostProcessor类型的处理器
					registryPostProcessor.postProcessBeanDefinitionRegistry(registry);
					registryPostProcessors.add(registryPostProcessor);
				}
				else {
					regularPostProcessors.add(postProcessor);
				}
			}

			// 2、执行IOC容器中的 BeanDefinitionRegistryPostProcessor 类型的处理器
			Map<String, BeanDefinitionRegistryPostProcessor> beanMap = beanFactory.getBeansOfType(BeanDefinitionRegistryPostProcessor.class, true, false);
			List<BeanDefinitionRegistryPostProcessor> registryPostProcessorBeans = new ArrayList<BeanDefinitionRegistryPostProcessor>(beanMap.values());
			OrderComparator.sort(registryPostProcessorBeans);
			for (BeanDefinitionRegistryPostProcessor postProcessor : registryPostProcessorBeans) {
				postProcessor.postProcessBeanDefinitionRegistry(registry);
			}


			// 3、注意：invokeBeanFactoryPostProcessors()执行的是BeanFactoryPostProcessor#postProcessBeanFactory()方法，
			// BeanFactoryPostProcessor处理器是在spring容器解析完配置文件（注册了所有BeanDefinition）之后，并在所有的bean实
			// 例化之前被调用的

			// 应用Spring容器中的工厂后处理器
			invokeBeanFactoryPostProcessors(registryPostProcessors, beanFactory);
			// 应用IOC容器中的工厂后处理器
			invokeBeanFactoryPostProcessors(registryPostProcessorBeans, beanFactory);
			// 应用Spring容器中非BeanDefinitionRegistryPostProcessor类型的工厂后处理器
			// （BeanDefinitionRegistryPostProcessor类型的处理应在上面执行过了）
			invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
			processedBeans.addAll(beanMap.keySet());
		}
		else {
			// 如果 beanFactory 不是一个BeanDefinition的注册表对象，就只执行工厂后处理器
			invokeBeanFactoryPostProcessors(getBeanFactoryPostProcessors(), beanFactory);
		}





		// ----------------------------第二步：处理 BeanFactoryPostProcessor 类型的处理器--------------------------------
		// 不要在这里初始化factorybean:我们需要让所有的常规bean都没有初始化，以便对它们使用bean工厂的后处理程序
		// Spring中通过配置一些实现了BeanFactoryPostProcessor后处理接口的Bean，来进行一些操作，这些对于使用Spring的用户来
		// 说是透明，所有有时候会让用户觉得，Spring好神奇。
		// 这里举一个例子，我们常常会配置像 <context:property-placeholder location="classpath:jdbc.properties"/> 这样的
		// 配置，Spring解析这个配置的时候会注册一个名称为“org.springframework.beans.factory.config.PropertyPlaceholderConfigurer#0”
		// 这样的bean，这样我们就可以在引用占位符进行配置了。因为解析占位符的操作是通过后处理器来进行处理的，我们会发现
		// 在 PreferencesPlaceholderConfigurer 类中，该类实现了一个 BeanFactoryPostProcessor 接口。Spring在启动的过程中
		// 会去调用所有后处理器bean，并执行相应的处理，所以说像这种由框架为我们处理的一些我们看不见的操作，对我们来说是透明的。
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

		// 一、将处理器分类
		List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		for (String ppName : postProcessorNames) {
			if (processedBeans.contains(ppName)) {
				// 已经执行过了的处理就跳过
			}
			else if (isTypeMatch(ppName, PriorityOrdered.class)) {
				// 添加按优先级排序的处理器
				priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
			}
			else if (isTypeMatch(ppName, Ordered.class)) {
				// 添加按序号排序的处理器
				orderedPostProcessorNames.add(ppName);
			}
			else {
				// 没有指定顺序的处理器
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 二、按优先级排序，并执行相应的处理器
		OrderComparator.sort(priorityOrderedPostProcessors);
		// 调用后处理器，其中的一个应用是BeanDefinition中的${...}占位符在这里被翻译了
		invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

		// 三、按序号排序，并执行相应的处理器
		List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : orderedPostProcessorNames) {
			orderedPostProcessors.add(getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		OrderComparator.sort(orderedPostProcessors);
		invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

		// 四、执行其他处理器，不需要排序
		List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanFactoryPostProcessor>();
		for (String postProcessorName : nonOrderedPostProcessorNames) {
			nonOrderedPostProcessors.add(getBean(postProcessorName, BeanFactoryPostProcessor.class));
		}
		invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);
	}
	// 将BeanFactory后处理器应用到对应的BeanFactory中，BeanFactoryPostProcessor处理器是在spring容器解析完配置文件（注册了所
	// 有BeanDefinition）之后，并在所有的bean实例化之前被调用的
	private void invokeBeanFactoryPostProcessors(Collection<? extends BeanFactoryPostProcessor> postProcessors, ConfigurableListableBeanFactory beanFactory) {

		for (BeanFactoryPostProcessor postProcessor : postProcessors) {
			postProcessor.postProcessBeanFactory(beanFactory);
		}
	}
	// 6、注册拦截Bean创建的Bean处理器，这里只是注册，真正的调用是在getBean时候
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		// 获取所有处理器bean的名字
		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
		int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// 给处理进行分类
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<BeanPostProcessor>();
		List<String> orderedPostProcessorNames = new ArrayList<String>();
		List<String> nonOrderedPostProcessorNames = new ArrayList<String>();
		for (String ppName : postProcessorNames) {
			// 有优先级顺序的处理器
			if (isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
				// BeanDefinition 被包装为 BeanWrapper 后，会调用该方法，将执行MergedBeanDefinitionPostProcessor处理器，
				// 将类型为beanType和beanName对应的bean注入到beanDefinition
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			}
			// 按顺序执行的处理器
			else if (isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			}
			// 没指定顺序的处理器
			else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// 注册BeanPostProcessor（这里只是注册，不是执行，触发执行动作的交由IOC处理）
		// 1、注册有按优先级执行的处理器
		OrderComparator.sort(priorityOrderedPostProcessors);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// 2、注册按顺序执行的处理器
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		OrderComparator.sort(orderedPostProcessors);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// 3、注册没有指定顺序的处理器
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<BeanPostProcessor>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// 4、注册 MergedBeanDefinitionPostProcessor 类型的处理器
		OrderComparator.sort(internalPostProcessors);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);


		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector());
	}
	/**
	 * Register the given BeanPostProcessor beans.
	 */
	private void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory, List<BeanPostProcessor> postProcessors) {

		for (BeanPostProcessor postProcessor : postProcessors) {
			beanFactory.addBeanPostProcessor(postProcessor);
		}
	}
	// 7、为上下文初始化Message源，即不同语言的消息体，国际化处理
	protected void initMessageSource() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		// 如果已经注册了 MessageSource 的Bean
		if (beanFactory.containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
			this.messageSource = beanFactory.getBean(MESSAGE_SOURCE_BEAN_NAME, MessageSource.class);
			// Make MessageSource aware of parent MessageSource.
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				HierarchicalMessageSource hms = (HierarchicalMessageSource) this.messageSource;
				if (hms.getParentMessageSource() == null) {
					// Only set parent context as parent MessageSource if no parent MessageSource registered already.
					hms.setParentMessageSource(getInternalParentMessageSource());
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Using MessageSource [" + this.messageSource + "]");
			}
		}
		else {
			// Use empty MessageSource to be able to accept getMessage calls.
			DelegatingMessageSource dms = new DelegatingMessageSource();
			dms.setParentMessageSource(getInternalParentMessageSource());
			this.messageSource = dms;
			beanFactory.registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MessageSource with name '" + MESSAGE_SOURCE_BEAN_NAME + "': using default [" + this.messageSource + "]");
			}
		}
	}
	// 8、初始化应用消息广播器，并放入“applicationEventMulticaster”bean中
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster = beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate ApplicationEventMulticaster with name '" +
						APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "': using default [" + this.applicationEventMulticaster + "]");
			}
		}
	}

	// 为容器初始化一个beanName为lifecycleProcessor的生命周期处理器
	protected void initLifecycleProcessor() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		if (beanFactory.containsLocalBean(LIFECYCLE_PROCESSOR_BEAN_NAME)) {
			this.lifecycleProcessor = beanFactory.getBean(LIFECYCLE_PROCESSOR_BEAN_NAME, LifecycleProcessor.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using LifecycleProcessor [" + this.lifecycleProcessor + "]");
			}
		}
		else {
			DefaultLifecycleProcessor defaultProcessor = new DefaultLifecycleProcessor();
			defaultProcessor.setBeanFactory(beanFactory);
			this.lifecycleProcessor = defaultProcessor;
			beanFactory.registerSingleton(LIFECYCLE_PROCESSOR_BEAN_NAME, this.lifecycleProcessor);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate LifecycleProcessor with name '" + LIFECYCLE_PROCESSOR_BEAN_NAME +
						"': using default [" + this.lifecycleProcessor + "]");
			}
		}
	}
	// 9、留给子类来初始化其他的Bean
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
	}
	// 10、在所有注册的bean中查找Listener bean，注册到消息广播器中.
	// Add beans that implement ApplicationListener as listeners. Doesn't affect other listeners, which can be added without being beans.
	protected void registerListeners() {
		// Register statically specified listeners first.
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}
		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String lisName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(lisName);
		}
	}
	/**
	 * Subclasses can invoke this method to register a listener.
	 * Any beans in the context that are listeners are automatically added.
	 * <p>Note: This method only works within an active application context,
	 * i.e. when an ApplicationEventMulticaster is already available. Generally
	 * prefer the use of {@link #addApplicationListener} which is more flexible.
	 * @param listener the listener to register
	 * @deprecated as of Spring 3.0, in favor of {@link #addApplicationListener}
	 */
	@Deprecated
	protected void addListener(ApplicationListener<?> listener) {
		getApplicationEventMulticaster().addApplicationListener(listener);
	}
	// 11、初始化剩下的单实例（非惰性）
	// 完成BeanFactoryBean的初始化工作，其中包括ConversionService的设置，配置冻结以及非延迟加载的bean的初始化工作。
	// ApplicationContext 实现的默认行为就是在启动时将所有单例bean提前进行实例化。提前实例化意味着作为初始化过程的一部分，
	// ApplicationContext实例会创建并配置所有的单例bean。通常情况下这时一件好事，因为这样在配置中的任何错误就会即刻被发现
	// （否则的化可能要花很长时间来排查）。而这个实例化的过程就是在finishBeanFactoryInitialization中完成的。
	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		// Initialize conversion service for this context.
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) && beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
			beanFactory.setConversionService(beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
		}

		// Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
		String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
		for (String weaverAwareName : weaverAwareNames) {
			getBean(weaverAwareName);
		}

		// Stop using the temporary ClassLoader for type matching.
		beanFactory.setTempClassLoader(null);

		// 冻结所有的BeanDefinition，意味着已注册的BeanDefinition将不再被修改或后处理。
		beanFactory.freezeConfiguration();

		// 初始化所有的单实例bean
		beanFactory.preInstantiateSingletons();
	}
	// 12、完成刷新过程，通知生命周期处理器lifecycleProcessor 刷新过程，同时发出ContextRefreshEvent通知别人
	protected void finishRefresh() {
		// 当ApplicationContext 启动或停止时，他会通过LifecycleProcessor来与所有声明的bean的周期做状态更新，
		// 而在LifecycleProcessor的使用前首先需要初始化。
		initLifecycleProcessor();

		// Propagate refresh to lifecycle processor first.
		getLifecycleProcessor().onRefresh();

		// Publish the final event.
		publishEvent(new ContextRefreshedEvent(this));

		// Participate in LiveBeansView MBean, if active.
		LiveBeansView.registerApplicationContext(this);
	}

	/**
	 * Cancel this context's refresh attempt, resetting the {@code active} flag
	 * after an exception got thrown.
	 * @param ex the exception that led to the cancellation
	 */
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.activeMonitor) {
			this.active = false;
		}
	}
	/**
	 * Register a shutdown hook with the JVM runtime, closing this context
	 * on JVM shutdown unless it has already been closed at that time.
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 * @see Runtime#addShutdownHook
	 * @see #close()
	 * @see #doClose()
	 */
	public void registerShutdownHook() {
		if (this.shutdownHook == null) {
			// No shutdown hook registered yet.
			this.shutdownHook = new Thread() {
				@Override
				public void run() {
					doClose();
				}
			};
			Runtime.getRuntime().addShutdownHook(this.shutdownHook);
		}
	}
	/**
	 * DisposableBean callback for destruction of this instance.
	 * Only called when the ApplicationContext itself is running
	 * as a bean in another BeanFactory or ApplicationContext,
	 * which is rather unusual.
	 * <p>The {@code close} method is the native way to
	 * shut down an ApplicationContext.
	 * @see #close()
	 * @see org.springframework.beans.factory.access.SingletonBeanFactoryLocator
	 */
	public void destroy() {
		close();
	}
	/**
	 * Close this application context, destroying all beans in its bean factory.
	 * <p>Delegates to {@code doClose()} for the actual closing procedure.
	 * Also removes a JVM shutdown hook, if registered, as it's not needed anymore.
	 * @see #doClose()
	 * @see #registerShutdownHook()
	 */
	public void close() {
		synchronized (this.startupShutdownMonitor) {
			doClose();
			// If we registered a JVM shutdown hook, we don't need it anymore now:
			// We've already explicitly closed the context.
			if (this.shutdownHook != null) {
				try {
					Runtime.getRuntime().removeShutdownHook(this.shutdownHook);
				}
				catch (IllegalStateException ex) {
					// ignore - VM is already shutting down
				}
			}
		}
	}
	/**
	 * Actually performs context closing: publishes a ContextClosedEvent and
	 * destroys the singletons in the bean factory of this application context.
	 * <p>Called by both {@code close()} and a JVM shutdown hook, if any.
	 * @see org.springframework.context.event.ContextClosedEvent
	 * @see #destroyBeans()
	 * @see #close()
	 * @see #registerShutdownHook()
	 */
	protected void doClose() {
		boolean actuallyClose;
		synchronized (this.activeMonitor) {
			actuallyClose = this.active && !this.closed;
			this.closed = true;
		}

		if (actuallyClose) {
			if (logger.isInfoEnabled()) {
				logger.info("Closing " + this);
			}

			LiveBeansView.unregisterApplicationContext(this);

			try {
				// Publish shutdown event.
				publishEvent(new ContextClosedEvent(this));
			}
			catch (Throwable ex) {
				logger.warn("Exception thrown from ApplicationListener handling ContextClosedEvent", ex);
			}

			// Stop all Lifecycle beans, to avoid delays during individual destruction.
			try {
				getLifecycleProcessor().onClose();
			}
			catch (Throwable ex) {
				logger.warn("Exception thrown from LifecycleProcessor on context close", ex);
			}

			// Destroy all cached singletons in the context's BeanFactory.
			destroyBeans();

			// Close the state of this context itself.
			closeBeanFactory();

			// Let subclasses do some final clean-up if they wish...
			onClose();

			synchronized (this.activeMonitor) {
				this.active = false;
			}
		}
	}
	/**
	 * Template method for destroying all beans that this context manages.
	 * The default implementation destroy all cached singletons in this context,
	 * invoking {@code DisposableBean.destroy()} and/or the specified
	 * "destroy-method".
	 * <p>Can be overridden to add context-specific bean destruction steps
	 * right before or right after standard singleton destruction,
	 * while the context's BeanFactory is still active.
	 * @see #getBeanFactory()
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
	 */
	protected void destroyBeans() {
		getBeanFactory().destroySingletons();
	}
	/**
	 * Template method which can be overridden to add context-specific shutdown work.
	 * The default implementation is empty.
	 * <p>Called at the end of {@link #doClose}'s shutdown procedure, after
	 * this context's BeanFactory has been closed. If custom shutdown logic
	 * needs to execute while the BeanFactory is still active, override
	 * the {@link #destroyBeans()} method instead.
	 */
	protected void onClose() {
		// For subclasses: do nothing by default.
	}
	public boolean isActive() {
		synchronized (this.activeMonitor) {
			return this.active;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>The parent {@linkplain ApplicationContext#getEnvironment() environment} is
	 * {@linkplain ConfigurableEnvironment#merge(ConfigurableEnvironment) merged} with
	 * this (child) application context environment if the parent is non-{@code null} and
	 * its environment is an instance of {@link ConfigurableEnvironment}.
	 * @see ConfigurableEnvironment#merge(ConfigurableEnvironment)
	 */
	public void setParent(ApplicationContext parent) {
		this.parent = parent;
		if (parent != null) {
			Environment parentEnvironment = parent.getEnvironment();
			if (parentEnvironment instanceof ConfigurableEnvironment) {
				getEnvironment().merge((ConfigurableEnvironment) parentEnvironment);
			}
		}
	}
	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
		this.beanFactoryPostProcessors.add(beanFactoryPostProcessor);
	}
	public List<BeanFactoryPostProcessor> getBeanFactoryPostProcessors() {
		return this.beanFactoryPostProcessors;
	}
	public void addApplicationListener(ApplicationListener<?> listener) {
		if (this.applicationEventMulticaster != null) {
			this.applicationEventMulticaster.addApplicationListener(listener);
		}
		else {
			this.applicationListeners.add(listener);
		}
	}
	/**
	 * Return the list of statically specified ApplicationListeners.
	 */
	public Collection<ApplicationListener<?>> getApplicationListeners() {
		return this.applicationListeners;
	}
	// 创建一个 StandardEnvironment 实例
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardEnvironment();
	}


	// ------------------------------------------- 实现 BeanFactory 接口 -----------------------------------------------

	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(requiredType);
	}
	public Object getBean(String name, Object... args) throws BeansException {
		return getBeanFactory().getBean(name, args);
	}
	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isPrototype(name);
	}
	public boolean isTypeMatch(String name, Class<?> targetType) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isTypeMatch(name, targetType);
	}
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getType(name);
	}
	public String[] getAliases(String name) {
		return getBeanFactory().getAliases(name);
	}


	// ------------------------------------------- 实现ListableBeanFactory 接口 ----------------------------------------

	public boolean containsBeanDefinition(String beanName) {
		return getBeanFactory().containsBeanDefinition(beanName);
	}
	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}
	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}
	public String[] getBeanNamesForType(Class<?> type) {
		return getBeanFactory().getBeanNamesForType(type);
	}
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		return getBeanFactory().getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
	}
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeanFactory().getBeansOfType(type);
	}
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit) throws BeansException {

		return getBeanFactory().getBeansOfType(type, includeNonSingletons, allowEagerInit);
	}
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) throws BeansException {

		return getBeanFactory().getBeansWithAnnotation(annotationType);
	}
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType) {
		return getBeanFactory().findAnnotationOnBean(beanName, annotationType);
	}


	// ------------------------------------------- 实现 HierarchicalBeanFactory 接口 -----------------------------------

	// 获取Spring的父容器
	public BeanFactory getParentBeanFactory() {
		return getParent();
	}
	// 判断本地是否包含一个指定的Bean
	public boolean containsLocalBean(String name) {
		return getBeanFactory().containsLocalBean(name);
	}
	// 返回父容器，如果父容器是ConfigurableApplicationContext的对象，则返回一个ConfigurableApplicationContext对象
	protected BeanFactory getInternalParentBeanFactory() {
		return (getParent() instanceof ConfigurableApplicationContext) ?
				((ConfigurableApplicationContext) getParent()).getBeanFactory() : getParent();
	}


	// ------------------------------------------- 实现 MessageSource 接口 ---------------------------------------------

	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return getMessageSource().getMessage(code, args, defaultMessage, locale);
	}
	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(code, args, locale);
	}
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(resolvable, locale);
	}
	// 返回容器内部使用的MessageSource
	private MessageSource getMessageSource() throws IllegalStateException {
		if (this.messageSource == null) {
			throw new IllegalStateException("MessageSource not initialized - " +
					"call 'refresh' before accessing messages via the context: " + this);
		}
		return this.messageSource;
	}
	// 返回父上下文的内部消息源如果这是一个AbstractApplicationContext; 否则，返回父上下文中本身。
	protected MessageSource getInternalParentMessageSource() {
		return (getParent() instanceof AbstractApplicationContext) ?
			((AbstractApplicationContext) getParent()).messageSource : getParent();
	}


	// ------------------------------------------- 实现 ResourcePatternResolver 接口 -----------------------------------

	public Resource[] getResources(String locationPattern) throws IOException {
		return this.resourcePatternResolver.getResources(locationPattern);
	}


	// ------------------------------------------- 实现 Lifecycle 接口 -------------------------------------------------

	public void start() {
		getLifecycleProcessor().start();
		publishEvent(new ContextStartedEvent(this));
	}
	public void stop() {
		getLifecycleProcessor().stop();
		publishEvent(new ContextStoppedEvent(this));
	}
	public boolean isRunning() {
		return getLifecycleProcessor().isRunning();
	}



	// ------------------------------------------- 由子类实现的抽象方法 ------------------------------------------------
	// 由子类实现，初始化一个 BeanFactory 给上层的 ApplicationContext 容器，该方法由 refresh() 调用，如果发现已经有一个
	// BeanFactory 实例了，则销毁全部的bean实例，并关闭容器，然后重新初始化一个BeanFactory
	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;
	protected abstract void closeBeanFactory();
	/**
	 * Subclasses must return their internal bean factory here. They should implement the lookup efficiently, so that it can be called repeatedly without a performance penalty.
	 * 子类必须返回它们的内部bean工厂。它们应该有效地实现查找，这样就可以在不执行性能损失的情况下反复调用它。
	 * <p>Note: Subclasses should check whether the context is still active before returning the internal bean factory.
	 * 在返回内部bean工厂之前，子类应该检查上下文是否仍然处于活动状态。
	 * The internal factory should generally be considered unavailable once the context has been closed.
	 * 一旦上下文关闭，内部工厂通常应该被认为不可用
	 * @return this application context's internal bean factory (never {@code null})
	 * @throws IllegalStateException if the context does not hold an internal bean factory yet
	 * (usually if {@link #refresh()} has never been called) or if the context has been
	 * closed already
	 * @see #refreshBeanFactory()
	 * @see #closeBeanFactory()
	 */
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;




	// 内部类声明

	/**
	 * BeanPostProcessor that logs an info message when a bean is created during BeanPostProcessor instantiation,
	 * 当在BeanPostProcessor实例化过程中创建一个bean时，BeanPostProcessor记录信息消息，
	 * i.e. when a bean is not eligible for getting processed by all BeanPostProcessors.
	 * 也就是说，当bean没有资格被所有的beanpost处理器进行处理时。
	 */
	private class BeanPostProcessorChecker implements BeanPostProcessor {

		private final ConfigurableListableBeanFactory beanFactory;
		private final int beanPostProcessorTargetCount;

		public BeanPostProcessorChecker(ConfigurableListableBeanFactory beanFactory, int beanPostProcessorTargetCount) {
			this.beanFactory = beanFactory;
			this.beanPostProcessorTargetCount = beanPostProcessorTargetCount;
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (bean != null && !(bean instanceof BeanPostProcessor) &&
					this.beanFactory.getBeanPostProcessorCount() < this.beanPostProcessorTargetCount) {
				if (logger.isInfoEnabled()) {
					logger.info("Bean '" + beanName + "' of type [" + bean.getClass() +
							"] is not eligible for getting processed by all BeanPostProcessors " +
							"(for example: not eligible for auto-proxying)");
				}
			}
			return bean;
		}
	}

	/**
	 * BeanPostProcessor that detects beans which implement the ApplicationListener interface.
	 * 用于检测实现ApplicationListener接口的bean的BeanPostProcessor。
	 * This catches beans that can't reliably be detected by getBeanNamesForType.
	 * 这捕获了无法可靠地通过getBeanNamesForType检测到的bean。
	 */
	private class ApplicationListenerDetector implements MergedBeanDefinitionPostProcessor {

		private final Map<String, Boolean> singletonNames = new ConcurrentHashMap<String, Boolean>(64);

		public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
			if (beanDefinition.isSingleton()) {
				this.singletonNames.put(beanName, Boolean.TRUE);
			}
		}

		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (bean instanceof ApplicationListener) {
				// potentially not detected as a listener by getBeanNamesForType retrieval
				Boolean flag = this.singletonNames.get(beanName);
				if (Boolean.TRUE.equals(flag)) {
					// singleton bean (top-level or inner): register on the fly
					addApplicationListener((ApplicationListener<?>) bean);
				}
				else if (flag == null) {
					if (logger.isWarnEnabled() && !containsBean(beanName)) {
						// inner bean with other scope - can't reliably process events
						logger.warn("Inner bean '" + beanName + "' implements ApplicationListener interface " +
								"but is not reachable for event multicasting by its containing ApplicationContext " +
								"because it does not have singleton scope. Only top-level listener beans are allowed " +
								"to be of non-singleton scope.");
					}
					this.singletonNames.put(beanName, Boolean.FALSE);
				}
			}
			return bean;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getDisplayName());
		sb.append(": startup date [").append(new Date(getStartupDate()));
		sb.append("]; ");
		ApplicationContext parent = getParent();
		if (parent == null) {
			sb.append("root of context hierarchy");
		}
		else {
			sb.append("parent: ").append(parent.getDisplayName());
		}
		return sb.toString();
	}
}
