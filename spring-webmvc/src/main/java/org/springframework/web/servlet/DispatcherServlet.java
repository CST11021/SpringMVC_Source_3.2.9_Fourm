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

package org.springframework.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.OrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ui.context.ThemeSource;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;

/**
 * Central dispatcher for HTTP request handlers/controllers, e.g. for web UI controllers or HTTP-based remote service
 * exporters. Dispatches to registered handlers for processing a web request, providing convenient mapping and exception
 * handling facilities.
 *
 * <p>This servlet is very flexible: It can be used with just about any workflow, with the installation of the
 * appropriate adapter classes. It offers the following functionality that distinguishes it from other request-driven
 * web MVC frameworks:
 *
 * <ul> <li>It is based around a JavaBeans configuration mechanism.
 *
 * <li>It can use any {@link HandlerMapping} implementation - pre-built or provided as part of an application - to
 * control the routing of requests to handler objects. Default is {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}
 * and {@link org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping}. HandlerMapping objects
 * can be defined as beans in the servlet's application context, implementing the HandlerMapping interface, overriding
 * the default HandlerMapping if present. HandlerMappings can be given any bean name (they are tested by type).
 *
 * <li>It can use any {@link HandlerAdapter}; this allows for using any handler interface. Default adapters are {@link
 * org.springframework.web.servlet.mvc.HttpRequestHandlerAdapter}, {@link org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter},
 * for Spring's {@link org.springframework.web.HttpRequestHandler} and {@link org.springframework.web.servlet.mvc.Controller}
 * interfaces, respectively. A default {@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter}
 * will be registered as well. HandlerAdapter objects can be added as beans in the application context, overriding the
 * default HandlerAdapters. Like HandlerMappings, HandlerAdapters can be given any bean name (they are tested by type).
 *
 * <li>The dispatcher's exception resolution strategy can be specified via a {@link HandlerExceptionResolver}, for
 * example mapping certain exceptions to error pages. Default are
 * {@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerExceptionResolver},
 * {@link org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver}, and
 * {@link org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver}. These HandlerExceptionResolvers can be overridden
 * through the application context. HandlerExceptionResolver can be given any bean name (they are tested by type).
 *
 * <li>Its view resolution strategy can be specified via a {@link ViewResolver} implementation, resolving symbolic view
 * names into View objects. Default is {@link org.springframework.web.servlet.view.InternalResourceViewResolver}.
 * ViewResolver objects can be added as beans in the application context, overriding the default ViewResolver.
 * ViewResolvers can be given any bean name (they are tested by type).
 *
 * <li>If a {@link View} or view name is not supplied by the user, then the configured {@link
 * RequestToViewNameTranslator} will translate the current request into a view name. The corresponding bean name is
 * "viewNameTranslator"; the default is {@link org.springframework.web.servlet.view.DefaultRequestToViewNameTranslator}.
 *
 * <li>The dispatcher's strategy for resolving multipart requests is determined by a {@link
 * org.springframework.web.multipart.MultipartResolver} implementation. Implementations for Jakarta Commons FileUpload
 * and Jason Hunter's COS are included; the typical choise is {@link org.springframework.web.multipart.commons.CommonsMultipartResolver}.
 * The MultipartResolver bean name is "multipartResolver"; default is none.
 *
 * <li>Its locale resolution strategy is determined by a {@link LocaleResolver}. Out-of-the-box implementations work via
 * HTTP accept header, cookie, or session. The LocaleResolver bean name is "localeResolver"; default is {@link
 * org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver}.
 *
 * <li>Its theme resolution strategy is determined by a {@link ThemeResolver}. Implementations for a fixed theme and for
 * cookie and session storage are included. The ThemeResolver bean name is "themeResolver"; default is {@link
 * org.springframework.web.servlet.theme.FixedThemeResolver}. </ul>
 *
 * <p><b>NOTE: The {@code @RequestMapping} annotation will only be processed if a corresponding
 * {@code HandlerMapping} (for type level annotations) and/or {@code HandlerAdapter} (for method level
 * annotations) is present in the dispatcher.</b> This is the case by default. However, if you are defining custom
 * {@code HandlerMappings} or {@code HandlerAdapters}, then you need to make sure that a corresponding custom
 * {@code DefaultAnnotationHandlerMapping} and/or {@code AnnotationMethodHandlerAdapter} is defined as well -
 * provided that you intend to use {@code @RequestMapping}.
 *
 * <p><b>A web application can define any number of DispatcherServlets.</b> Each servlet will operate in its own
 * namespace, loading its own application context with mappings, handlers, etc. Only the root application context as
 * loaded by {@link org.springframework.web.context.ContextLoaderListener}, if any, will be shared.
 *
 * <p>As of Spring 3.1, {@code DispatcherServlet} may now be injected with a web
 * application context, rather than creating its own internally. This is useful in Servlet
 * 3.0+ environments, which support programmatic registration of servlet instances. See
 * {@link #DispatcherServlet(WebApplicationContext)} Javadoc for details.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Chris Beams
 * @author Rossen Stoyanchev
 * @see org.springframework.web.HttpRequestHandler
 * @see org.springframework.web.servlet.mvc.Controller
 * @see org.springframework.web.context.ContextLoaderListener
 */
@SuppressWarnings("serial")
public class DispatcherServlet extends FrameworkServlet {

	/** Well-known name for the MultipartResolver object in the bean factory for this namespace. */
	public static final String MULTIPART_RESOLVER_BEAN_NAME = "multipartResolver";

	/** Well-known name for the LocaleResolver object in the bean factory for this namespace. */
	public static final String LOCALE_RESOLVER_BEAN_NAME = "localeResolver";

	/** Well-known name for the ThemeResolver object in the bean factory for this namespace. */
	public static final String THEME_RESOLVER_BEAN_NAME = "themeResolver";

	/**
	 * Well-known name for the HandlerMapping object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerMappings" is turned off.
	 * @see #setDetectAllHandlerMappings
	 */
	public static final String HANDLER_MAPPING_BEAN_NAME = "handlerMapping";

	/**
	 * Well-known name for the HandlerAdapter object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerAdapters" is turned off.
	 * @see #setDetectAllHandlerAdapters
	 */
	public static final String HANDLER_ADAPTER_BEAN_NAME = "handlerAdapter";

	/**
	 * Well-known name for the HandlerExceptionResolver object in the bean factory for this namespace.
	 * Only used when "detectAllHandlerExceptionResolvers" is turned off.
	 * @see #setDetectAllHandlerExceptionResolvers
	 */
	public static final String HANDLER_EXCEPTION_RESOLVER_BEAN_NAME = "handlerExceptionResolver";

	/**
	 * Well-known name for the RequestToViewNameTranslator object in the bean factory for this namespace.
	 */
	public static final String REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME = "viewNameTranslator";

	/**
	 * Well-known name for the ViewResolver object in the bean factory for this namespace.
	 * Only used when "detectAllViewResolvers" is turned off.
	 * @see #setDetectAllViewResolvers
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";

	/**
	 * Well-known name for the FlashMapManager object in the bean factory for this namespace.
	 */
	public static final String FLASH_MAP_MANAGER_BEAN_NAME = "flashMapManager";

	/**
	 * Request attribute to hold the current web application context.
	 * Otherwise only the global web app context is obtainable by tags etc.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getWebApplicationContext
	 */
	public static final String WEB_APPLICATION_CONTEXT_ATTRIBUTE = DispatcherServlet.class.getName() + ".CONTEXT";

	/**
	 * Request attribute to hold the current LocaleResolver, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocaleResolver
	 */
	public static final String LOCALE_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".LOCALE_RESOLVER";

	/**
	 * Request attribute to hold the current ThemeResolver, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeResolver
	 */
	public static final String THEME_RESOLVER_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_RESOLVER";

	/**
	 * Request attribute to hold the current ThemeSource, retrievable by views.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getThemeSource
	 */
	public static final String THEME_SOURCE_ATTRIBUTE = DispatcherServlet.class.getName() + ".THEME_SOURCE";

	/**
	 * Name of request attribute that holds a read-only {@code Map<String,?>}
	 * with "input" flash attributes saved by a previous request, if any.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getInputFlashMap(HttpServletRequest)
	 */
	public static final String INPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".INPUT_FLASH_MAP";

	/**
	 * Name of request attribute that holds the "output" {@link FlashMap} with
	 * attributes to save for a subsequent request.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getOutputFlashMap(HttpServletRequest)
	 */
	public static final String OUTPUT_FLASH_MAP_ATTRIBUTE = DispatcherServlet.class.getName() + ".OUTPUT_FLASH_MAP";

	/**
	 * Name of request attribute that holds the {@link FlashMapManager}.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getFlashMapManager(HttpServletRequest)
	 */
	public static final String FLASH_MAP_MANAGER_ATTRIBUTE = DispatcherServlet.class.getName() + ".FLASH_MAP_MANAGER";

	/** Log category to use when no mapped handler is found for a request. */
	public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

	/**
	 * Name of the class path resource (relative to the DispatcherServlet class)
	 * that defines DispatcherServlet's default strategy names.
	 */
	private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";


	/** Additional logger to use when no mapped handler is found for a request. */
	protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

	private static final Properties defaultStrategies;

	static {
		// Load default strategy implementations from properties file.
		// This is currently strictly internal and not meant to be customized
		// by application developers.
		try {
			ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
			defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not load 'DispatcherServlet.properties': " + ex.getMessage());
		}
	}

	/** Detect all HandlerMappings or just expect "handlerMapping" bean? */
	private boolean detectAllHandlerMappings = true;

	/** Detect all HandlerAdapters or just expect "handlerAdapter" bean? */
	private boolean detectAllHandlerAdapters = true;

	/** Detect all HandlerExceptionResolvers or just expect "handlerExceptionResolver" bean? */
	private boolean detectAllHandlerExceptionResolvers = true;

	/** Detect all ViewResolvers or just expect "viewResolver" bean? */
	private boolean detectAllViewResolvers = true;

	/** Perform cleanup of request attributes after include request? */
	private boolean cleanupAfterInclude = true;


	//--------这些属性将在web容器启动时调用initStrategies()方法进行初始化--------

	//这些属性大部分都定义为接口类型，这样可以灵活拓展，这就是所谓的对接口编程
	private MultipartResolver multipartResolver;
	private LocaleResolver localeResolver;
	private ThemeResolver themeResolver;
	private List<HandlerMapping> handlerMappings;
	private List<HandlerAdapter> handlerAdapters;
	private List<HandlerExceptionResolver> handlerExceptionResolvers;
	private RequestToViewNameTranslator viewNameTranslator;
	private FlashMapManager flashMapManager;
	//--------这些属性将在web容器启动时调用initStrategies()方法进行初始化--------



	/** List of ViewResolvers used by this servlet */
	private List<ViewResolver> viewResolvers;

	/**
	 * Create a new {@code DispatcherServlet} that will create its own internal web
	 * application context based on defaults and values provided through servlet
	 * init-params. Typically used in Servlet 2.5 or earlier environments, where the only
	 * option for servlet registration is through {@code web.xml} which requires the use
	 * of a no-arg constructor.
	 * <p>Calling {@link #setContextConfigLocation} (init-param 'contextConfigLocation')
	 * will dictate which XML files will be loaded by the
	 * {@linkplain #DEFAULT_CONTEXT_CLASS default XmlWebApplicationContext}
	 * <p>Calling {@link #setContextClass} (init-param 'contextClass') overrides the
	 * default {@code XmlWebApplicationContext} and allows for specifying an alternative class,
	 * such as {@code AnnotationConfigWebApplicationContext}.
	 * <p>Calling {@link #setContextInitializerClasses} (init-param 'contextInitializerClasses')
	 * indicates which {@code ApplicationContextInitializer} classes should be used to
	 * further configure the internal application context prior to refresh().
	 * @see #DispatcherServlet(WebApplicationContext)
	 */
	public DispatcherServlet() {
		super();
	}

	/**
	 * Create a new {@code DispatcherServlet} with the given web application context. This
	 * constructor is useful in Servlet 3.0+ environments where instance-based registration
	 * of servlets is possible through the {@link ServletContext#addServlet} API.
	 * <p>Using this constructor indicates that the following properties / init-params
	 * will be ignored:
	 * <ul>
	 * <li>{@link #setContextClass(Class)} / 'contextClass'</li>
	 * <li>{@link #setContextConfigLocation(String)} / 'contextConfigLocation'</li>
	 * <li>{@link #setContextAttribute(String)} / 'contextAttribute'</li>
	 * <li>{@link #setNamespace(String)} / 'namespace'</li>
	 * </ul>
	 * <p>The given web application context may or may not yet be {@linkplain
	 * ConfigurableApplicationContext#refresh() refreshed}. If it has <strong>not</strong>
	 * already been refreshed (the recommended approach), then the following will occur:
	 * <ul>
	 * <li>If the given context does not already have a {@linkplain
	 * ConfigurableApplicationContext#setParent parent}, the root application context
	 * will be set as the parent.</li>
	 * <li>If the given context has not already been assigned an {@linkplain
	 * ConfigurableApplicationContext#setId id}, one will be assigned to it</li>
	 * <li>{@code ServletContext} and {@code ServletConfig} objects will be delegated to
	 * the application context</li>
	 * <li>{@link #postProcessWebApplicationContext} will be called</li>
	 * <li>Any {@code ApplicationContextInitializer}s specified through the
	 * "contextInitializerClasses" init-param or through the {@link
	 * #setContextInitializers} property will be applied.</li>
	 * <li>{@link ConfigurableApplicationContext#refresh refresh()} will be called if the
	 * context implements {@link ConfigurableApplicationContext}</li>
	 * </ul>
	 * If the context has already been refreshed, none of the above will occur, under the
	 * assumption that the user has performed these actions (or not) per their specific
	 * needs.
	 * <p>See {@link org.springframework.web.WebApplicationInitializer} for usage examples.
	 * @param webApplicationContext the context to use
	 * @see #initWebApplicationContext
	 * @see #configureAndRefreshWebApplicationContext
	 * @see org.springframework.web.WebApplicationInitializer
	 */
	public DispatcherServlet(WebApplicationContext webApplicationContext) {
		super(webApplicationContext);
	}

	/**
	 * Set whether to detect all HandlerMapping beans in this servlet's context. Otherwise,
	 * just a single bean with name "handlerMapping" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * HandlerMapping, despite multiple HandlerMapping beans being defined in the context.
	 */
	public void setDetectAllHandlerMappings(boolean detectAllHandlerMappings) {
		this.detectAllHandlerMappings = detectAllHandlerMappings;
	}

	/**
	 * Set whether to detect all HandlerAdapter beans in this servlet's context. Otherwise,
	 * just a single bean with name "handlerAdapter" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * HandlerAdapter, despite multiple HandlerAdapter beans being defined in the context.
	 */
	public void setDetectAllHandlerAdapters(boolean detectAllHandlerAdapters) {
		this.detectAllHandlerAdapters = detectAllHandlerAdapters;
	}

	/**
	 * Set whether to detect all HandlerExceptionResolver beans in this servlet's context. Otherwise,
	 * just a single bean with name "handlerExceptionResolver" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * HandlerExceptionResolver, despite multiple HandlerExceptionResolver beans being defined in the context.
	 */
	public void setDetectAllHandlerExceptionResolvers(boolean detectAllHandlerExceptionResolvers) {
		this.detectAllHandlerExceptionResolvers = detectAllHandlerExceptionResolvers;
	}

	/**
	 * Set whether to detect all ViewResolver beans in this servlet's context. Otherwise,
	 * just a single bean with name "viewResolver" will be expected.
	 * <p>Default is "true". Turn this off if you want this servlet to use a single
	 * ViewResolver, despite multiple ViewResolver beans being defined in the context.
	 */
	public void setDetectAllViewResolvers(boolean detectAllViewResolvers) {
		this.detectAllViewResolvers = detectAllViewResolvers;
	}

	/**
	 * Set whether to perform cleanup of request attributes after an include request, that is,
	 * whether to reset the original state of all request attributes after the DispatcherServlet
	 * has processed within an include request. Otherwise, just the DispatcherServlet's own
	 * request attributes will be reset, but not model attributes for JSPs or special attributes
	 * set by views (for example, JSTL's).
	 * <p>Default is "true", which is strongly recommended. Views should not rely on request attributes
	 * having been set by (dynamic) includes. This allows JSP views rendered by an included controller
	 * to use any model attributes, even with the same names as in the main JSP, without causing side
	 * effects. Only turn this off for special needs, for example to deliberately allow main JSPs to
	 * access attributes from JSP views rendered by an included controller.
	 */
	public void setCleanupAfterInclude(boolean cleanupAfterInclude) {
		this.cleanupAfterInclude = cleanupAfterInclude;
	}


	//----------------- 将Spring组件装配到DispatcherServlet中 -------------------//

	//onRefresh()方法将在WebApplicationContext初始化后自动执行，此时Spring上下文中的Bean已经初始化完毕。该方法的工作是通过反射机制查找并装配Spring容器中用户显示自定义的组件Bean，如果找不到在装配默认的组件实例。
	//SpringMVC默认组件定义在org.springframework.web.servlet.jar包下的一个DispatcherServlet.properties配置文件
	@Override
	protected void onRefresh(ApplicationContext context) {
		initStrategies(context);
	}
	protected void initStrategies(ApplicationContext context) {
		initMultipartResolver(context);//初始化上传文件解析器
		initLocaleResolver(context);//初始化本地化解析器
		initThemeResolver(context);//初始化主题解析器
		initHandlerMappings(context);//初始化处理器映射器
		initHandlerAdapters(context);//初始化处理器适配器
		initHandlerExceptionResolvers(context);//初始化处理器异常解析器
		initRequestToViewNameTranslator(context);//初始化请求到视图名的翻译器
		initViewResolvers(context);//初始化试图解析器
		initFlashMapManager(context);
	}
	private void initMultipartResolver(ApplicationContext context) {
		try {
			this.multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using MultipartResolver [" + this.multipartResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this.multipartResolver = null;
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME +
						"': no multipart request handling provided");
			}
		}
	}
	private void initLocaleResolver(ApplicationContext context) {
		try {
			this.localeResolver = context.getBean(LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using LocaleResolver [" + this.localeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.localeResolver = getDefaultStrategy(context, LocaleResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate LocaleResolver with name '" + LOCALE_RESOLVER_BEAN_NAME + "': using default [" + this.localeResolver + "]");
			}
		}
	}
	private void initThemeResolver(ApplicationContext context) {
		try {
			this.themeResolver = context.getBean(THEME_RESOLVER_BEAN_NAME, ThemeResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using ThemeResolver [" + this.themeResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.themeResolver = getDefaultStrategy(context, ThemeResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Unable to locate ThemeResolver with name '" + THEME_RESOLVER_BEAN_NAME + "': using default [" +
								this.themeResolver + "]");
			}
		}
	}
	private void initHandlerMappings(ApplicationContext context) {
		this.handlerMappings = null;

		//判断是否默认添加所有的HandlerMappings,初始值是默认添加的
		if (this.detectAllHandlerMappings) {
			// Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
				//通过@order注解去排序
				OrderComparator.sort(this.handlerMappings);
			}
		}
		//如果不是默认添加所有的，那么就去context中找一个声明的bean
		else {
			try {
				HandlerMapping hm = context.getBean(HANDLER_MAPPING_BEAN_NAME, HandlerMapping.class);
				this.handlerMappings = Collections.singletonList(hm);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerMapping later.
			}
		}

		//如果上面两步没有找到可以使用的handlerMapping，那么就采用默认的handlerMapping,默认的HandlerMapping都定义在了DispatcherServlet.properties中，大致定义了如下两个:
		// org.springframework.web.servlet.HandlerMapping=org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping,
		// org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping
		if (this.handlerMappings == null) {
			this.handlerMappings = getDefaultStrategies(context, HandlerMapping.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerMappings found in servlet '" + getServletName() + "': using default");
			}
		}
	}
	private void initHandlerAdapters(ApplicationContext context) {
		this.handlerAdapters = null;

		if (this.detectAllHandlerAdapters) {
			// Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerAdapter> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
				// We keep HandlerAdapters in sorted order.
				OrderComparator.sort(this.handlerAdapters);
			}
		}
		else {
			try {
				HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
				this.handlerAdapters = Collections.singletonList(ha);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default HandlerAdapter later.
			}
		}

		// Ensure we have at least some HandlerAdapters, by registering
		// default HandlerAdapters if no other adapters are found.
		if (this.handlerAdapters == null) {
			this.handlerAdapters = getDefaultStrategies(context, HandlerAdapter.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerAdapters found in servlet '" + getServletName() + "': using default");
			}
		}
	}
	private void initHandlerExceptionResolvers(ApplicationContext context) {
		this.handlerExceptionResolvers = null;

		if (this.detectAllHandlerExceptionResolvers) {
			// Find all HandlerExceptionResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, HandlerExceptionResolver> matchingBeans = BeanFactoryUtils
					.beansOfTypeIncludingAncestors(context, HandlerExceptionResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.handlerExceptionResolvers = new ArrayList<HandlerExceptionResolver>(matchingBeans.values());
				// We keep HandlerExceptionResolvers in sorted order.
				OrderComparator.sort(this.handlerExceptionResolvers);
			}
		}
		else {
			try {
				HandlerExceptionResolver her =
						context.getBean(HANDLER_EXCEPTION_RESOLVER_BEAN_NAME, HandlerExceptionResolver.class);
				this.handlerExceptionResolvers = Collections.singletonList(her);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, no HandlerExceptionResolver is fine too.
			}
		}

		// Ensure we have at least some HandlerExceptionResolvers, by registering
		// default HandlerExceptionResolvers if no other resolvers are found.
		if (this.handlerExceptionResolvers == null) {
			this.handlerExceptionResolvers = getDefaultStrategies(context, HandlerExceptionResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No HandlerExceptionResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}
	private void initRequestToViewNameTranslator(ApplicationContext context) {
		try {
			this.viewNameTranslator =
					context.getBean(REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME, RequestToViewNameTranslator.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using RequestToViewNameTranslator [" + this.viewNameTranslator + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.viewNameTranslator = getDefaultStrategy(context, RequestToViewNameTranslator.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate RequestToViewNameTranslator with name '" +
						REQUEST_TO_VIEW_NAME_TRANSLATOR_BEAN_NAME + "': using default [" + this.viewNameTranslator +
						"]");
			}
		}
	}
	private void initViewResolvers(ApplicationContext context) {
		this.viewResolvers = null;

		if (this.detectAllViewResolvers) {
			// Find all ViewResolvers in the ApplicationContext, including ancestor contexts.
			Map<String, ViewResolver> matchingBeans =
					BeanFactoryUtils.beansOfTypeIncludingAncestors(context, ViewResolver.class, true, false);
			if (!matchingBeans.isEmpty()) {
				this.viewResolvers = new ArrayList<ViewResolver>(matchingBeans.values());
				// We keep ViewResolvers in sorted order.
				OrderComparator.sort(this.viewResolvers);
			}
		}
		else {
			try {
				ViewResolver vr = context.getBean(VIEW_RESOLVER_BEAN_NAME, ViewResolver.class);
				this.viewResolvers = Collections.singletonList(vr);
			}
			catch (NoSuchBeanDefinitionException ex) {
				// Ignore, we'll add a default ViewResolver later.
			}
		}

		// Ensure we have at least one ViewResolver, by registering
		// a default ViewResolver if no other resolvers are found.
		if (this.viewResolvers == null) {
			this.viewResolvers = getDefaultStrategies(context, ViewResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("No ViewResolvers found in servlet '" + getServletName() + "': using default");
			}
		}
	}
	private void initFlashMapManager(ApplicationContext context) {
		try {
			this.flashMapManager =
					context.getBean(FLASH_MAP_MANAGER_BEAN_NAME, FlashMapManager.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using FlashMapManager [" + this.flashMapManager + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default.
			this.flashMapManager = getDefaultStrategy(context, FlashMapManager.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate FlashMapManager with name '" +
						FLASH_MAP_MANAGER_BEAN_NAME + "': using default [" + this.flashMapManager + "]");
			}
		}
	}
	//----------------- 将Spring组件装配到DispatcherServlet中 -------------------//






	/**
	 * Return this servlet's ThemeSource, if any; else return {@code null}.
	 * <p>Default is to return the WebApplicationContext as ThemeSource,
	 * provided that it implements the ThemeSource interface.
	 * @return the ThemeSource, if any
	 * @see #getWebApplicationContext()
	 */
	public final ThemeSource getThemeSource() {
		if (getWebApplicationContext() instanceof ThemeSource) {
			return (ThemeSource) getWebApplicationContext();
		}
		else {
			return null;
		}
	}

	/**
	 * Obtain this servlet's MultipartResolver, if any.
	 * @return the MultipartResolver used by this servlet, or {@code null} if none
	 * (indicating that no multipart support is available)
	 */
	public final MultipartResolver getMultipartResolver() {
		return this.multipartResolver;
	}

	/**
	 * Return the default strategy object for the given strategy interface.
	 * <p>The default implementation delegates to {@link #getDefaultStrategies},
	 * expecting a single object in the list.
	 * @param context the current WebApplicationContext
	 * @param strategyInterface the strategy interface
	 * @return the corresponding strategy object
	 * @see #getDefaultStrategies
	 */
	protected <T> T getDefaultStrategy(ApplicationContext context, Class<T> strategyInterface) {
		List<T> strategies = getDefaultStrategies(context, strategyInterface);
		if (strategies.size() != 1) {
			throw new BeanInitializationException(
					"DispatcherServlet needs exactly 1 strategy for interface [" + strategyInterface.getName() + "]");
		}
		return strategies.get(0);
	}

	/**
	 * Create a List of default strategy objects for the given strategy interface.
	 * <p>The default implementation uses the "DispatcherServlet.properties" file (in the same
	 * package as the DispatcherServlet class) to determine the class names. It instantiates
	 * the strategy objects through the context's BeanFactory.
	 * @param context the current WebApplicationContext
	 * @param strategyInterface the strategy interface
	 * @return the List of corresponding strategy objects
	 */
	@SuppressWarnings("unchecked")
	protected <T> List<T> getDefaultStrategies(ApplicationContext context, Class<T> strategyInterface) {
		String key = strategyInterface.getName();
		String value = defaultStrategies.getProperty(key);
		if (value != null) {
			String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
			List<T> strategies = new ArrayList<T>(classNames.length);
			for (String className : classNames) {
				try {
					Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
					Object strategy = createDefaultStrategy(context, clazz);
					strategies.add((T) strategy);
				}
				catch (ClassNotFoundException ex) {
					throw new BeanInitializationException(
							"Could not find DispatcherServlet's default strategy class [" + className +
									"] for interface [" + key + "]", ex);
				}
				catch (LinkageError err) {
					throw new BeanInitializationException(
							"Error loading DispatcherServlet's default strategy class [" + className +
									"] for interface [" + key + "]: problem with class file or dependent class", err);
				}
			}
			return strategies;
		}
		else {
			return new LinkedList<T>();
		}
	}

	/**
	 * Create a default strategy.
	 * <p>The default implementation uses {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean}.
	 * @param context the current WebApplicationContext
	 * @param clazz the strategy implementation class to instantiate
	 * @return the fully configured strategy instance
	 * @see org.springframework.context.ApplicationContext#getAutowireCapableBeanFactory()
	 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory#createBean
	 */
	protected Object createDefaultStrategy(ApplicationContext context, Class<?> clazz) {
		return context.getAutowireCapableBeanFactory().createBean(clazz);
	}



	//----------------- DispatcherServlet的请求转发处理-----------------------------------------------------------------

	//doService()方法用于处理所有的合法请求，它最终委托给doDispatch()方法来转发请求
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
			logger.debug("DispatcherServlet with name '" + getServletName() + "'" + resumed + " processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
		}

		//1. 保存现场。保存request 熟悉的快照，以便能在必要时恢复。attributeSnapshot用于保存所有的Attribute信息
		Map<String, Object> attributesSnapshot = null;
		if (WebUtils.isIncludeRequest(request)) {
			attributesSnapshot = new HashMap<String, Object>();
			Enumeration<?> attrNames = request.getAttributeNames();
			while (attrNames.hasMoreElements()) {
				String attrName = (String) attrNames.nextElement();
				if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
					attributesSnapshot.put(attrName, request.getAttribute(attrName));
				}
			}
		}

		// 2. 将框架需要的对象放入request中，以便view和handler使用。
		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
		request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());

		FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
		if (inputFlashMap != null) {
			request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
		}
		request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
		request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);

		try {
			// 3. 请求分发服务.
			doDispatch(request, response);
		}
		finally {
			if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
				return;
			}
			// Restore the original attribute snapshot, in case of an include.
			if (attributesSnapshot != null) {
				// 4. 恢复现场。
				restoreAttributesAfterInclude(request, attributesSnapshot);
			}
		}
	}
	// 所有请求最后都交由doDispatch()方法来处理，doDispatch()负责转发请求，将请求交给对应的处理器进行处理，调用getHandler来相应HTTP请求，然后通过执行Handler的处理方法来得到返回的 ModelAndView 结果，最后把这个ModelAndView对象交给相应的试图对象去呈现。
	// 在这里，可以看到MVC模式核心的实现，同时，也是在这里完成了模型、试图和控制器的紧密结合
	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
		HttpServletRequest processedRequest = request;
		// HandlerExecutionChain 是一个包含了 Handler 和 InterceptorList 的执行链。
		HandlerExecutionChain mappedHandler = null;
		// 标记请求是否转换成了处理 multipart 的请求
		boolean multipartRequestParsed = false;

		// 放一个 asyncManager 到 request 中
		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

		try {
			ModelAndView mv = null;
			Exception dispatchException = null;

			try {
				// 优先判断一下是不是文件上传的 request
				// 对于请求的处理，Spring首先考虑的是对于Multipart的处理，如果是MultipartContent类型的Request，则转换request为MultipartHttpServletRequest类型的request
				processedRequest = checkMultipart(request);
				// 是否真的转换
				multipartRequestParsed = processedRequest != request;

				// 通过 handlerMappings 找到对应的 handler 和 interceptors 并返回执行链 HandlerExecutionChain
				// 静态资源一般都是由ResourceHttpRequestHandler进行处理的
				mappedHandler = getHandler(processedRequest, false);

				// 如果没有找到相应的handler，这里就是大家经常从日志里面看到的这段英文 No mapping found for HTTP request with URI
				// 这个地方没有找到相应的处理handler有两种处理方式，一种（默认）返回404的状态码，一种是直接抛出异常，
				// 第二种方式需要设置初始化参数的 throwExceptionIfNoHandlerFound 这个参数设置成ture即可
				if (mappedHandler == null || mappedHandler.getHandler() == null) {
					noHandlerFound(processedRequest, response);
					return;
				}

				// 通过这个 handler 找到对应的 HandlerAdaptor
				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

				// 检查 lastModified，没有新改动就返回
				String method = request.getMethod();
				boolean isGet = "GET".equals(method);
				if (isGet || "HEAD".equals(method)) {
					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
					if (logger.isDebugEnabled()) {
						logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
					}
					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
						return;
					}
				}

				// 执行HandlerExecutionChain所有Interceptor中的preHandle方法，一旦有一个返回了false那么请求就不进行继续处理，这也是为什么 preHandle 返回了false就可以拦截用户继续执行了
				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
					return;
				}

				try {
					// 用 handlerAdaptor 来处理请求并返回 ModelAndView
					// 如果是http请求会调用相应的HandlerMethod 这里里面包含了具体的方法，通过反射调用，如果有返回的视图，那么ModelAndView会返回相应的视图对象，如果只是普通请求或者json请求会返回null
					mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
				}
				finally {
					// 看 handler 是否是异步处理的
					if (asyncManager.isConcurrentHandlingStarted()) {
						return;
					}
				}

				//校验是否添加了视图，如果没有视图就会添加一个默认的视图，通过url的规则来拼凑
				applyDefaultViewName(request, mv);
				// 这块逻辑是执行HandlerExecutionChain所有Interceptor中的postHandle方法，从这块代码看来，postHandle的执行时机是在执行完方法后，没有把响应写入到response中执行的
				mappedHandler.applyPostHandle(processedRequest, response, mv);
			}
			catch (Exception ex) {
				dispatchException = ex;
			}
			// 处理 handler 的执行结果，并渲染 view 到响应中，有异常则会渲染一个 error view。同时启用执行链里所有拦截器的完结处理。
			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
		}
		catch (Exception ex) {
			// 在异常发生时，启用执行链里所有拦截器的完结处理。
			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
		}
		catch (Error err) {
			// 在错误发生时，启用执行链里所有拦截器的完结处理，总之就是要把拦截器里定义的完结处理给做完。
			triggerAfterCompletionWithError(processedRequest, response, mappedHandler, err);
		}
		finally {
			if (asyncManager.isConcurrentHandlingStarted()) {
				// 如果请求是异步处理的，应用的异步拦截器的后处理。
				// 最后调用HandlerExecutionChain所有Interceptor中的postHandle方法
				mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
				return;
			}
			// 释放 multiPartRequest 持有的所有资源。
			if (multipartRequestParsed) {
				cleanupMultipart(processedRequest);
			}
		}
	}

	//----------------- DispatcherServlet的请求转发处理-----------------------------------------------------------------





	//校验是否添加了视图，如果没有视图就会添加一个默认的视图
	private void applyDefaultViewName(HttpServletRequest request, ModelAndView mv) throws Exception {
		if (mv != null && !mv.hasView()) {
			mv.setViewName(getDefaultViewName(request));
		}
	}
	// 处理 handler 的执行结果，并渲染 view 到响应中，有异常则会渲染一个 error view。同时启用执行链里所有拦截器的完结处理。
	private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, HandlerExecutionChain mappedHandler, ModelAndView mv, Exception exception) throws Exception {

		boolean errorView = false;

		if (exception != null) {
			if (exception instanceof ModelAndViewDefiningException) {
				logger.debug("ModelAndViewDefiningException encountered", exception);
				mv = ((ModelAndViewDefiningException) exception).getModelAndView();
			}
			else {
				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
				mv = processHandlerException(request, response, handler, exception);
				errorView = (mv != null);
			}
		}

		// Did the handler return a view to render?
		if (mv != null && !mv.wasCleared()) {
			render(mv, request, response);
			if (errorView) {
				WebUtils.clearErrorRequestAttributes(request);
			}
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
						"': assuming HandlerAdapter completed request handling");
			}
		}

		if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
			// Concurrent handling started during a forward
			return;
		}

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, null);
		}
	}
	protected ModelAndView processHandlerException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

		// Check registered HandlerExceptionResolvers...
		ModelAndView exMv = null;
		for (HandlerExceptionResolver handlerExceptionResolver : this.handlerExceptionResolvers) {
			exMv = handlerExceptionResolver.resolveException(request, response, handler, ex);
			if (exMv != null) {
				break;
			}
		}
		if (exMv != null) {
			if (exMv.isEmpty()) {
				return null;
			}
			// We might still need view name translation for a plain error model...
			if (!exMv.hasView()) {
				exMv.setViewName(getDefaultViewName(request));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Handler execution resulted in exception - forwarding to resolved error view: " + exMv, ex);
			}
			WebUtils.exposeErrorRequestAttributes(request, ex, getServletName());
			return exMv;
		}

		throw ex;
	}
	//进行视图渲染，并写到 response 中
	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
		// 获取这个请求的区域代码
		Locale locale = this.localeResolver.resolveLocale(request);
		response.setLocale(locale);

		View view;
		// 如果Controller返回的是String类型的视图
		if (mv.isReference()) {
			// 将String类型的视图名，解析为视图对象
			view = resolveViewName(mv.getViewName(), mv.getModelInternal(), locale, request);
			if (view == null) {
				throw new ServletException("Could not resolve view with name '" + mv.getViewName() + "' in servlet with name '" + getServletName() + "'");
			}
		}
		// Controller 返回的是一个 View 的视图对象
		else {
			view = mv.getView();
			if (view == null) {
				throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " + "View object in servlet with name '" + getServletName() + "'");
			}
		}

		// Delegate to the View object for rendering.
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
		}
		try {
			//视图的渲染过程是把model包装成map形式通过request的属性带到服务端
			view.render(mv.getModelInternal(), request, response);
		}
		catch (Exception ex) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'", ex);
			}
			throw ex;
		}
	}
	//DispatcherServlet会根据 视图名 选择合适的试图来进行渲染，而这一功能就是在resolveViewName函数中完成的
	protected View resolveViewName(String viewName, Map<String, Object> model, Locale locale, HttpServletRequest request) throws Exception {

		//通过遍历视图解析器来解析这个 视图名，一旦解析成功则返回视图对象
		for (ViewResolver viewResolver : this.viewResolvers) {
			View view = viewResolver.resolveViewName(viewName, locale);
			if (view != null) {
				return view;
			}
		}
		return null;
	}






	/**
	 * Build a LocaleContext for the given request, exposing the request's primary locale as current locale.
	 * <p>The default implementation uses the dispatcher's LocaleResolver to obtain the current locale,
	 * which might change during a request.
	 * @param request current HTTP request
	 * @return the corresponding LocaleContext
	 */
	@Override
	protected LocaleContext buildLocaleContext(final HttpServletRequest request) {
		return new LocaleContext() {
			public Locale getLocale() {
				return localeResolver.resolveLocale(request);
			}
			public String toString() {
				return getLocale().toString();
			}
		};
	}

	//
	protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
		if (this.multipartResolver != null && this.multipartResolver.isMultipart(request)) {
			if (request instanceof MultipartHttpServletRequest) {
				logger.debug("Request is already a MultipartHttpServletRequest - if not in a forward, " +
						"this typically results from an additional MultipartFilter in web.xml");
			}
			else {
				return this.multipartResolver.resolveMultipart(request);
			}
		}
		// If not returned before: return original request.
		return request;
	}

	/**
	 * Clean up any resources used by the given multipart request (if any).
	 * @param servletRequest current HTTP request
	 * @see MultipartResolver#cleanupMultipart
	 */
	protected void cleanupMultipart(HttpServletRequest servletRequest) {
		MultipartHttpServletRequest req = WebUtils.getNativeRequest(servletRequest, MultipartHttpServletRequest.class);
		if (req != null) {
			this.multipartResolver.cleanupMultipart(req);
		}
	}



	//--------------------用于springMVC请求分发时获取 HandlerExecutionChain ----------------------------------------------------------

	//根据请求获取对应的处理
	@Deprecated
	protected HandlerExecutionChain getHandler(HttpServletRequest request, boolean cache) throws Exception {
		return getHandler(request);
	}
	protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
		for (HandlerMapping hm : this.handlerMappings) {
			if (logger.isTraceEnabled()) {
				logger.trace("Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
			}
			HandlerExecutionChain handler = hm.getHandler(request);
			if (handler != null) {
				return handler;
			}
		}
		return null;
	}
	// 每个请求都应该对应着一Handler，因为每个请求都会在后台有相应的逻辑对应，而逻辑的实现就是在Handler中，
	// 所以一旦遇到没有找到Handler的情况（正常情况下如果没有URL匹配的Handler，开发人员可以设置默认的Handler来处理请求，
	// 但是如果默认请求也未设置就会出现Handler为空的情况），就只能通过response向用户返回错误信息。
	protected void noHandlerFound(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (pageNotFoundLogger.isWarnEnabled()) {
			pageNotFoundLogger.warn("No mapping found for HTTP request with URI [" + getRequestUri(request) + "] in DispatcherServlet with name '" + getServletName() + "'");
		}
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	//--------------------------------------------------------------------------------------------------------------------------------






	/**
	 * Return the HandlerAdapter for this handler object.
	 * @param handler the handler object to find an adapter for
	 * @throws ServletException if no HandlerAdapter can be found for the handler. This is a fatal error.
	 */
	protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
		for (HandlerAdapter ha : this.handlerAdapters) {
			if (logger.isTraceEnabled()) {
				logger.trace("Testing handler adapter [" + ha + "]");
			}
			if (ha.supports(handler)) {
				return ha;
			}
		}
		throw new ServletException("No adapter for handler [" + handler + "]: The DispatcherServlet configuration needs to include a HandlerAdapter that supports this handler");
	}




	/**
	 * Translate the supplied request into a default view name.
	 * @param request current HTTP servlet request
	 * @return the view name (or {@code null} if no default found)
	 * @throws Exception if view name translation failed
	 */
	protected String getDefaultViewName(HttpServletRequest request) throws Exception {
		return this.viewNameTranslator.getViewName(request);
	}


	private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
			HandlerExecutionChain mappedHandler, Exception ex) throws Exception {

		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, ex);
		}
		throw ex;
	}

	private void triggerAfterCompletionWithError(HttpServletRequest request, HttpServletResponse response,
			HandlerExecutionChain mappedHandler, Error error) throws Exception, ServletException {

		ServletException ex = new NestedServletException("Handler processing failed", error);
		if (mappedHandler != null) {
			mappedHandler.triggerAfterCompletion(request, response, ex);
		}
		throw ex;
	}

	/**
	 * Restore the request attributes after an include.
	 * @param request current HTTP request
	 * @param attributesSnapshot the snapshot of the request attributes before the include
	 */
	@SuppressWarnings("unchecked")
	private void restoreAttributesAfterInclude(HttpServletRequest request, Map<?,?> attributesSnapshot) {
		// Need to copy into separate Collection here, to avoid side effects
		// on the Enumeration when removing attributes.
		Set<String> attrsToCheck = new HashSet<String>();
		Enumeration<?> attrNames = request.getAttributeNames();
		while (attrNames.hasMoreElements()) {
			String attrName = (String) attrNames.nextElement();
			if (this.cleanupAfterInclude || attrName.startsWith("org.springframework.web.servlet")) {
				attrsToCheck.add(attrName);
			}
		}

		// Add attributes that may have been removed
		attrsToCheck.addAll((Set<String>) attributesSnapshot.keySet());

		// Iterate over the attributes to check, restoring the original value
		// or removing the attribute, respectively, if appropriate.
		for (String attrName : attrsToCheck) {
			Object attrValue = attributesSnapshot.get(attrName);
			if (attrValue == null){
				request.removeAttribute(attrName);
			}
			else if (attrValue != request.getAttribute(attrName)) {
				request.setAttribute(attrName, attrValue);
			}
		}
	}

	private static String getRequestUri(HttpServletRequest request) {
		String uri = (String) request.getAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
		if (uri == null) {
			uri = request.getRequestURI();
		}
		return uri;
	}

}
