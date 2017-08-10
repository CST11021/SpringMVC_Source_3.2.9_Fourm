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

package org.springframework.web.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

// ContextLoader 主要用来启动 Spring容器，并将 Spring容器 记录到ServletContext中，一次来完成Spring上下文（Spring容器）和Web上下文（Web容器）的融合
public class ContextLoader {

	// 以下参数表示可在web.xml中配置的初始化参数
	public static final String CONTEXT_ID_PARAM = "contextId";
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";
	public static final String CONTEXT_CLASS_PARAM = "contextClass";
	public static final String CONTEXT_INITIALIZER_CLASSES_PARAM = "contextInitializerClasses";
	public static final String GLOBAL_INITIALIZER_CLASSES_PARAM = "globalInitializerClasses";
	public static final String LOCATOR_FACTORY_SELECTOR_PARAM = "locatorFactorySelector";
	public static final String LOCATOR_FACTORY_KEY_PARAM = "parentContextKey";
	private static final String INIT_PARAM_DELIMITERS = ",; \t\n";


	// 读取 ContextLoader.properties 文件，记录WebApplicationContext接口的实现类（org.springframework.web.context.support.XmlWebApplicationContext）
	private static final Properties defaultStrategies;
	private static final String DEFAULT_STRATEGIES_PATH = "ContextLoader.properties";
	static {
		// Load default strategy implementations from properties file. This is currently strictly internal and not meant to be customized by application developers.
		try {
			// 在初始化时，首先读取ContextLoader类的同目录下的属性文件 ContextLoader.properties ，
			// 并根据其中的配置提取将要实现WebApplicationContext接口的实现类，并根据这个实现类通过反射的方式进行实例的创建。
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, ContextLoader.class);
			defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load 'ContextLoader.properties': " + ex.getMessage());
		}
	}


	// 注意这里使用了 volatile 表示该 currentContext 是线程间可见的
	private static volatile WebApplicationContext currentContext;
	// 用于映射当前的类加载器与创建的WebApplicationContextLoader实例，以便全局访问
	private static final Map<ClassLoader, WebApplicationContext> currentContextPerThread = new ConcurrentHashMap<ClassLoader, WebApplicationContext>(1);

	// 表示这个 ContextLoader 要管理的 WebApplicationContext 实例
	private WebApplicationContext context;



	//* Holds BeanFactoryReference when loading parent factory via ContextSingletonBeanFactoryLocator.
	private BeanFactoryReference parentContextRef;


	public ContextLoader() {}
	public ContextLoader(WebApplicationContext context) {
		this.context = context;
	}


	/* --------------- 初始化一个 WebApplicationContext 容器 ------------------------------------------------------------------------------------------------ */

	// 系统启动时调用该方法，并传入ServletContext,初始化完WebApplicationContext后，将WebApplicationContext实例存在servletContext中，以供全局访问
	public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		// web.xml中存在多次ContextLoader定义时，则抛出异常（initWebApplicationContext方法是启动Spring容器的入口，我们不允许在还没有加载Spring容器就已经存在了Spring容器实例）
		if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
			throw new IllegalStateException("Cannot initialize context because there is already a root application context present - " + "check whether you have multiple ContextLoader* definitions in your web.xml!");
		}

		Log logger = LogFactory.getLog(ContextLoader.class);
		servletContext.log("Initializing Spring root WebApplicationContext");
		if (logger.isInfoEnabled()) {
			logger.info("Root WebApplicationContext: initialization started");
		}
		long startTime = System.currentTimeMillis();

		try {
			// 将上下文存储在本地实例变量中，以保证它在ServletContext关闭时可用
			if (this.context == null) {
				//第一步：初始化一个 WebApplicationContext 对象（这里返回一个ConfigurableWebApplicationContext的实例）
				this.context = createWebApplicationContext(servletContext);
			}
			if (this.context instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) this.context;
				if (!cwac.isActive()) {
					// 上面仅仅只是构造一个上下文实例，此时上下文还没有刷新（提供诸如设置父上下文、设置应用程序上下文id等服务）
					if (cwac.getParent() == null) {
						// 加载父容器，虽然 initWebApplicationContext 方法是启动Spring容器的入口，
						// 但是我们还是可以通过配置“locatorFactorySelector”和“parentContextKey”这两个初始化参数，来指定这个Spring容器的父容器
						// 一般我们不会在web.xml中去配置这两个参数，所以loadParentContext()方法返回的父容器一般为空。
						ApplicationContext parent = loadParentContext(servletContext);
						cwac.setParent(parent);
					}
					// 获取Spring容器的配置文件，载入ServletContext，并启动这个Spring容器
					configureAndRefreshWebApplicationContext(cwac, servletContext);
				}
			}

			// 第二步：将context记录在servletContext中，以便于 DispatcherServlet 可以通过 getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) 来获取实例
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.context);

			// 第三步：将Spring容器记录到全局变量中
			ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			if (ccl == ContextLoader.class.getClassLoader()) {
				// 情况下，程序会进入到这里
				// 这里涉及到 “Thread.currentThread().getContextClassLoader()”与“ContextLoader.class.getClassLoader()”的区别的一个知识点
				currentContext = this.context;
			}
			else if (ccl != null) {
				currentContextPerThread.put(ccl, this.context);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Published root WebApplicationContext as ServletContext attribute with name [" + WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
			}
			if (logger.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
			}

			return this.context;
		}
		catch (RuntimeException ex) {
			logger.error("Context initialization failed", ex);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
			throw ex;
		}
		catch (Error err) {
			logger.error("Context initialization failed", err);
			servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, err);
			throw err;
		}
	}
	// 创建一个WebApplicationContext实例，返回的是一个 ConfigurableWebApplicationContext 实例
	protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
		Class<?> contextClass = determineContextClass(sc);
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException("Custom context class [" + contextClass.getName() + "] is not of type [" + ConfigurableWebApplicationContext.class.getName() + "]");
		}
		return (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);
	}
	// 使用反射机制来实例化一个 WebApplicationContext接口的实现类，默认是 XmlWebApplicationContext
	protected Class<?> determineContextClass(ServletContext servletContext) {

		/*
		我们可以在 web.xml 中配置要实例化的Spring容器，比如：
		<context-param>
			<param-name>contextClass</param-name>
			<param-value>org.springframework.web.context.support.AnnotationConfigWebApplicationContext</param-value>
		</context-param>
		如果没有配置，则默认使用 ContextLoader.properties 文件中提供的默认容器
		*/
		String contextClassName = servletContext.getInitParameter(CONTEXT_CLASS_PARAM);
		if (contextClassName != null) {
			try {
				return ClassUtils.forName(contextClassName, ClassUtils.getDefaultClassLoader());
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException("Failed to load custom context class [" + contextClassName + "]", ex);
			}
		}
		else {
			// 默认的容器实现是 org.springframework.web.context.support.XmlWebApplicationContext
			contextClassName = defaultStrategies.getProperty(WebApplicationContext.class.getName());
			try {
				return ClassUtils.forName(contextClassName, ContextLoader.class.getClassLoader());
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException("Failed to load default context class [" + contextClassName + "]", ex);
			}
		}
	}
	// 通过 servletContext 的初始化参数，返回这个Spring的父容器（如果没有设置参数，则返回null）
	protected ApplicationContext loadParentContext(ServletContext servletContext) {
		ApplicationContext parentContext = null;
		// 获取“locatorFactorySelector”和“parentContextKey”这两个初始化参数，这两个初始化参数一般在web.xml文件中配置
		// 这两个参数分别指定父容器所在的配置文件路径和父容器的名称
		String locatorFactorySelector = servletContext.getInitParameter(LOCATOR_FACTORY_SELECTOR_PARAM);
		String parentContextKey = servletContext.getInitParameter(LOCATOR_FACTORY_KEY_PARAM);

		if (parentContextKey != null) {
			// locatorFactorySelector 可能为null，表示默认是“classpath:beanrefcontext.xml”
			BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance(locatorFactorySelector);
			Log logger = LogFactory.getLog(ContextLoader.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Getting parent context definition: using parent context key of '" + parentContextKey + "' with BeanFactoryLocator");
			}
			this.parentContextRef = locator.useBeanFactory(parentContextKey);
			parentContext = (ApplicationContext) this.parentContextRef.getFactory();
		}

		return parentContext;
	}
	// 该方法用来给 wac 这个Spring容器设置id、ServletContext以及Spring配置文件
	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac, ServletContext sc) {
		// 给这个 wac 这个Spring容器设置id
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value -> assign a more useful id based on available information
			// 获取 ServletContext 上下文的初始化参数“contextId”
			String idParam = sc.getInitParameter(CONTEXT_ID_PARAM);
			if (idParam != null) {
				wac.setId(idParam);
			}
			else {
				// Generate default id...
				if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
					// Servlet <= 2.4: resort to name specified in web.xml, if any.
					wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + ObjectUtils.getDisplayString(sc.getServletContextName()));
				}
				else {
					wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + ObjectUtils.getDisplayString(sc.getContextPath()));
				}
			}
		}

		// 给这个spring容器设置ServletContext，这样spring容器就和web上下文结合在一起了
		wac.setServletContext(sc);
		// 获取“contextConfigLocation”初始化参数，该参数指定了Spring容器的配置文件路径，为加载Spring容器做准备
		String configLocationParam = sc.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocationParam != null) {
			wac.setConfigLocation(configLocationParam);
		}

		// The wac environment's #initPropertySources will be called in any case when the context is refreshed;
		// do it eagerly here to ensure servlet property sources are in place for use in any post-processing or initialization that occurs below prior to #refresh
		ConfigurableEnvironment env = wac.getEnvironment();
		if (env instanceof ConfigurableWebEnvironment) {
			((ConfigurableWebEnvironment) env).initPropertySources(sc, null);
		}

		customizeContext(sc, wac);
		wac.refresh();
	}
	protected void customizeContext(ServletContext sc, ConfigurableWebApplicationContext wac) {
		List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> initializerClasses = determineContextInitializerClasses(sc);
		if (initializerClasses.isEmpty()) {
			// no ApplicationContextInitializers have been declared -> nothing to do
			return;
		}

		ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> initializerInstances = new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();

		for (Class<ApplicationContextInitializer<ConfigurableApplicationContext>> initializerClass : initializerClasses) {
			Class<?> initializerContextClass = GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
			if (initializerContextClass != null) {
				Assert.isAssignable(initializerContextClass, wac.getClass(), String.format(
						"Could not add context initializer [%s] since its generic parameter [%s] " +
								"is not assignable from the type of application context used by this " +
								"context loader [%s]: ", initializerClass.getName(), initializerContextClass.getName(),
						wac.getClass().getName()));
			}
			initializerInstances.add(BeanUtils.instantiateClass(initializerClass));
		}

		AnnotationAwareOrderComparator.sort(initializerInstances);
		for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : initializerInstances) {
			initializer.initialize(wac);
		}
	}
	protected List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> determineContextInitializerClasses(ServletContext servletContext) {

		List<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>> classes = new ArrayList<Class<ApplicationContextInitializer<ConfigurableApplicationContext>>>();
		// 获取初始化参数：globalInitializerClasses
		String globalClassNames = servletContext.getInitParameter(GLOBAL_INITIALIZER_CLASSES_PARAM);
		if (globalClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
				classes.add(loadInitializerClass(className));
			}
		}
		// 获取初始化参数：contextInitializerClasses
		String localClassNames = servletContext.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM);
		if (localClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(localClassNames, INIT_PARAM_DELIMITERS)) {
				classes.add(loadInitializerClass(className));
			}
		}

		return classes;
	}
	@SuppressWarnings("unchecked")
	private Class<ApplicationContextInitializer<ConfigurableApplicationContext>> loadInitializerClass(String className) {
		try {
			Class<?> clazz = ClassUtils.forName(className, ClassUtils.getDefaultClassLoader());
			Assert.isAssignable(ApplicationContextInitializer.class, clazz);
			return (Class<ApplicationContextInitializer<ConfigurableApplicationContext>>) clazz;
		}
		catch (ClassNotFoundException ex) {
			throw new ApplicationContextException("Failed to load context initializer class [" + className + "]", ex);
		}
	}
	/* --------------- 初始化一个 WebApplicationContext 容器 ------------------------------------------------------------------------------------------------ */




	@Deprecated
	protected WebApplicationContext createWebApplicationContext(ServletContext sc, ApplicationContext parent) {
		return createWebApplicationContext(sc);
	}
	public void closeWebApplicationContext(ServletContext servletContext) {
		servletContext.log("Closing Spring root WebApplicationContext");
		try {
			if (this.context instanceof ConfigurableWebApplicationContext) {
				((ConfigurableWebApplicationContext) this.context).close();
			}
		}
		finally {
			ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			if (ccl == ContextLoader.class.getClassLoader()) {
				currentContext = null;
			}
			else if (ccl != null) {
				currentContextPerThread.remove(ccl);
			}
			servletContext.removeAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
			if (this.parentContextRef != null) {
				this.parentContextRef.release();
			}
		}
	}

	// 该方法是一个静态方法，当Spring容器启动后，我们可以通过ContextLoad.getCurrentWebApplicationContext()方法得到这个Spring容器
	// 注意：这个获取的只是Spring容器，该容器维护的bean并不包括定义在，WEB-INF/xxx-servlet.xml 中的bean
	public static WebApplicationContext getCurrentWebApplicationContext() {
		ClassLoader ccl = Thread.currentThread().getContextClassLoader();
		if (ccl != null) {
			WebApplicationContext ccpt = currentContextPerThread.get(ccl);
			if (ccpt != null) {
				return ccpt;
			}
		}
		return currentContext;
	}

}
