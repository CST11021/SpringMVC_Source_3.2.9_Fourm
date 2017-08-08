
package org.springframework.web.servlet;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.CallableProcessingInterceptorAdapter;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.context.support.ServletRequestHandledEvent;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.WebUtils;


@SuppressWarnings("serial")
public abstract class FrameworkServlet extends HttpServletBean {

	//* Suffix for WebApplicationContext namespaces. If a servlet of this class is given the name "test" in a context, the namespace used by the servlet will resolve to "test-servlet".
	public static final String DEFAULT_NAMESPACE_SUFFIX = "-servlet";
	// 表示默认的Spring容器
	public static final Class<?> DEFAULT_CONTEXT_CLASS = XmlWebApplicationContext.class;
	// Prefix for the ServletContext attribute for the WebApplicationContext. The completion is the servlet name.
	public static final String SERVLET_CONTEXT_PREFIX = FrameworkServlet.class.getName() + ".CONTEXT.";
	// 表示初始化参数的分隔符
	private static final String INIT_PARAM_DELIMITERS = ",; \t\n";

	// 用于记录存储在 ServletContext 中的 WebApplicationContext 容器
	private String contextAttribute;
	//** 表示将要创建的 Spring 容器
	private Class<?> contextClass = DEFAULT_CONTEXT_CLASS;
	//** WebApplicationContext id to assign */
	private String contextId;
	//** Namespace for this servlet 用于表示该Servlet的命名空间，默认的命名规则是 Servlet名称 +  DEFAULT_NAMESPACE_SUFFIX
	private String namespace;
	//** Explicit context config location */
	private String contextConfigLocation;
	//** Actual ApplicationContextInitializer instances to apply to the context */
	private final ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>> contextInitializers = new ArrayList<ApplicationContextInitializer<ConfigurableApplicationContext>>();
	//** Comma-delimited ApplicationContextInitializer class names set through init param */
	private String contextInitializerClasses;
	//** Should we publish the context as a ServletContext attribute? */
	private boolean publishContext = true;
	//** Should we publish a ServletRequestHandledEvent at the end of each request? */
	private boolean publishEvents = true;
	//** Expose LocaleContext and RequestAttributes as inheritable for child threads? */
	private boolean threadContextInheritable = false;
	//** Should we dispatch an HTTP OPTIONS request to {@link #doService}?
	private boolean dispatchOptionsRequest = false;
	//** Should we dispatch an HTTP TRACE request to {@link #doService}?
	private boolean dispatchTraceRequest = false;
	//** WebApplicationContext for this servlet
	private WebApplicationContext webApplicationContext;
	//** Flag used to detect whether onRefresh has already been called */
	private boolean refreshEventReceived = false;


	public FrameworkServlet() {}
	public FrameworkServlet(WebApplicationContext webApplicationContext) {
		this.webApplicationContext = webApplicationContext;
	}





	/* -------------初始化 WebApplicationContext ----------------------------------------------------------------------------------------------------- */

	// 覆盖 HttpServletBean 中的方法，该方法设计了计时器来统计初始化的执行时间，而且提供了一个扩展方法 initFrameworkServlet() 用于子类的覆盖操作，而作为关键的初始化逻辑实现委托给了 initWebApplicationContext()方法
	@Override
	protected final void initServletBean() throws ServletException {
		getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
		if (this.logger.isInfoEnabled()) {
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
		}
		long startTime = System.currentTimeMillis();

		try {
			this.webApplicationContext = initWebApplicationContext();
			initFrameworkServlet();//设计为子类覆盖
		}
		catch (ServletException ex) {
			this.logger.error("Context initialization failed", ex);
			throw ex;
		}
		catch (RuntimeException ex) {
			this.logger.error("Context initialization failed", ex);
			throw ex;
		}

		if (this.logger.isInfoEnabled()) {
			long elapsedTime = System.currentTimeMillis() - startTime;
			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " + elapsedTime + " ms");
		}
	}
	// 创建或刷新WebApplicationContext实例并对servlet功能所使用的变量进行初始化
	protected WebApplicationContext initWebApplicationContext() {
		//从 ServletContext 获取一个 WebApplicationContext 的Spring容器
		WebApplicationContext rootContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		WebApplicationContext wac = null;

		if (this.webApplicationContext != null) {
			// A context instance was injected at construction time -> use it
			wac = this.webApplicationContext;
			if (wac instanceof ConfigurableWebApplicationContext) {
				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
				if (!cwac.isActive()) {
					// The context has not yet been refreshed -> provide services such as
					// setting the parent context, setting the application context id, etc
					if (cwac.getParent() == null) {
						// The context instance was injected without an explicit parent -> set
						// the root application context (if any; may be null) as the parent
						cwac.setParent(rootContext);
					}
					configureAndRefreshWebApplicationContext(cwac);
				}
			}
		}
		if (wac == null) {
			// 根据 contextAttribute 属性加载 WebApplicationContext
			wac = findWebApplicationContext();
		}
		if (wac == null) {
			// 以 rootContext（通过ContextLoaderListener监听创建的Spring容器） 做为父容器创建一个 WebApplicationContext
			wac = createWebApplicationContext(rootContext);
		}

		if (!this.refreshEventReceived) {
			// Either the context is not a ConfigurableApplicationContext with refresh
			// support or the context injected at construction time had already been
			// refreshed -> trigger initial onRefresh manually here.
			onRefresh(wac);
		}

		if (this.publishContext) {
			// Publish the context as a servlet context attribute.
			String attrName = getServletContextAttributeName();
			getServletContext().setAttribute(attrName, wac);
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() + "' as ServletContext attribute with name [" + attrName + "]");
			}
		}

		return wac;
	}
	protected WebApplicationContext findWebApplicationContext() {
		String attrName = getContextAttribute();
		if (attrName == null) {
			return null;
		}
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(getServletContext(), attrName);
		if (wac == null) {
			throw new IllegalStateException("No WebApplicationContext found: initializer not registered?");
		}
		return wac;
	}
	protected WebApplicationContext createWebApplicationContext(WebApplicationContext parent) {
		return createWebApplicationContext((ApplicationContext) parent);
	}
	protected WebApplicationContext createWebApplicationContext(ApplicationContext parent) {
		//获取Servlet的初始化参数contextClass，如果没有配置默认为 XMLWebApplicationContext.class
		Class<?> contextClass = getContextClass();
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Servlet with name '" + getServletName() + "' will try to create custom WebApplicationContext context of class '" + contextClass.getName() + "'" + ", using parent context [" + parent + "]");
		}
		if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
			throw new ApplicationContextException("Fatal initialization error in servlet with name '" + getServletName() + "': custom WebApplicationContext class [" + contextClass.getName() + "] is not of type ConfigurableWebApplicationContext");
		}
		//通过反射方式创建一个 contextClass 实例
		ConfigurableWebApplicationContext wac = (ConfigurableWebApplicationContext) BeanUtils.instantiateClass(contextClass);

		wac.setEnvironment(getEnvironment());
		// parent为在ContextLoaderListener中创建的实例
		wac.setParent(parent);
		// 获取contextConfigLocaltion属性，配置在servlet初始化参数中
		wac.setConfigLocation(getContextConfigLocation());
		// 初始化Spring环境包括加载配置文件等
		configureAndRefreshWebApplicationContext(wac);

		return wac;
	}
	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
			// The application context id is still set to its original default value -> assign a more useful id based on available information
			if (this.contextId != null) {
				wac.setId(this.contextId);
			}
			else {
				// Generate default id... 为这个 Spring容器生成一个默认的id
				ServletContext sc = getServletContext();
				if (sc.getMajorVersion() == 2 && sc.getMinorVersion() < 5) {
					// Servlet <= 2.4: resort to name specified in web.xml, if any.
					String servletContextName = sc.getServletContextName();
					if (servletContextName != null) {
						wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + servletContextName + "." + getServletName());
					}
					else {
						wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + getServletName());
					}
				}
				else {
					// Servlet 2.5's getContextPath available!
					wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + ObjectUtils.getDisplayString(sc.getContextPath()) + "/" + getServletName());
				}
			}
		}

		wac.setServletContext(getServletContext());
		wac.setServletConfig(getServletConfig());
		wac.setNamespace(getNamespace());
		//向容器中添加一个监听器
		wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));

		ConfigurableEnvironment env = wac.getEnvironment();
		if (env instanceof ConfigurableWebEnvironment) {
			((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
		}

		postProcessWebApplicationContext(wac);
		applyInitializers(wac);
		//刷新Spring容器， 加载配置文件及整合parent到wac
		wac.refresh();
	}
	//configureAndRefreshWebApplicationContext() 方法中调用容器刷新方法refresh()时会进行事件监听器的注册，当容器完成刷新动作后，发出ContextRefreshEvent通知别人
	private class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {
		public void onApplicationEvent(ContextRefreshedEvent event) {
			FrameworkServlet.this.onApplicationEvent(event);
		}
	}
	public void onApplicationEvent(ContextRefreshedEvent event) {
		this.refreshEventReceived = true;
		onRefresh(event.getApplicationContext());
	}
	protected void postProcessWebApplicationContext(ConfigurableWebApplicationContext wac) {}
	protected void applyInitializers(ConfigurableApplicationContext wac) {
		String globalClassNames = getServletContext().getInitParameter(ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM);
		if (globalClassNames != null) {
			for (String className : StringUtils.tokenizeToStringArray(globalClassNames, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		if (this.contextInitializerClasses != null) {
			for (String className : StringUtils.tokenizeToStringArray(this.contextInitializerClasses, INIT_PARAM_DELIMITERS)) {
				this.contextInitializers.add(loadInitializer(className, wac));
			}
		}

		AnnotationAwareOrderComparator.sort(this.contextInitializers);
		for (ApplicationContextInitializer<ConfigurableApplicationContext> initializer : this.contextInitializers) {
			initializer.initialize(wac);
		}
	}
	@SuppressWarnings("unchecked")
	private ApplicationContextInitializer<ConfigurableApplicationContext> loadInitializer(String className, ConfigurableApplicationContext wac) {
		try {
			Class<?> initializerClass = ClassUtils.forName(className, wac.getClassLoader());
			Class<?> initializerContextClass =
					GenericTypeResolver.resolveTypeArgument(initializerClass, ApplicationContextInitializer.class);
			if (initializerContextClass != null) {
				Assert.isAssignable(initializerContextClass, wac.getClass(), String.format(
						"Could not add context initializer [%s] since its generic parameter [%s] " +
								"is not assignable from the type of application context used by this " +
								"framework servlet [%s]: ", initializerClass.getName(), initializerContextClass.getName(),
						wac.getClass().getName()));
			}
			return BeanUtils.instantiateClass(initializerClass, ApplicationContextInitializer.class);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException(String.format("Could not instantiate class [%s] specified " +
					"via 'contextInitializerClasses' init-param", className), ex);
		}
	}
	public String getServletContextAttributeName() {
		return SERVLET_CONTEXT_PREFIX + getServletName();
	}
	// 留给子类实现
	protected void initFrameworkServlet() throws ServletException {}
	// 留给子类实现
	protected void onRefresh(ApplicationContext context) {
		// For subclasses: do nothing by default.
	}
	/* -------------初始化 WebApplicationContext ----------------------------------------------------------------------------------------------------- */





	/* ----------DispatcherServlet的请求处理实现---------------------------------------------------------------------------------------------------------------------------------- */
	// 我们知道Servlet的生命周期包括：init(),service()和destroy()
	// DispatcherServlet 的init()通过继承HttpServletBean的init()方法来实现，而service()和destroy()通过FrameworkServlet来实现

	// 我们总结一下这个流程，以doGet为例
	// get request->FrameworkServlet(service)->判断是不是patch请求：不是->HttpServlet(service)->判断是什么类型：get->HttpServlet(doGet)->doGet被子类重写->FrameworkServlet(doGet)->FrameworkServlet(processRequest)
	//
	// FrameworkServlet内的其他doXxx请求也是类似的，最终都是processRequest(request, response);来处理，也就是说在HttpServlet的service方法内doXxx先按类型分开，
	// 然后在FrameworkServlet的doxx合并到一个方法处理。绕了一个大弯。
	//
	// 之所以重新合并，原因还没看到，应该是不同类型的请求在spring内部还需要进行统计处理。
	// 之所以不直接利用service进行处理，主要是出于灵活性的考虑吧，写过代码的同学应该都能理解。
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String method = request.getMethod();
//		因为HttpServlet中是没有定义doPatch方法的，所以规定
//		if(是doPatch类型)
//			就交由本类（FrameworkServlet）内的doPatch方法运行。
//		else
//		调用父类（HttpServlet）的service方法。
		if (method.equalsIgnoreCase(RequestMethod.PATCH.name())) {
			processRequest(request, response);
		}
		else {
			super.service(request, response);//交由父类根据请求类型进行处理，因为FrameworkServlet重写了doGet，doPost等方法，所以最后的处理实现还是在FrameworkServlet中实现
		}
	}
	@Override
	protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}
	@Override
	protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}
	@Override
	protected final void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}
	@Override
	protected final void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		processRequest(request, response);
	}
	@Override
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (this.dispatchOptionsRequest) {
			processRequest(request, response);
			if (response.containsHeader("Allow")) {
				// Proper OPTIONS response coming from a handler - we're done.
				return;
			}
		}

		// Use response wrapper for Servlet 2.5 compatibility where
		// the getHeader() method does not exist
		super.doOptions(request, new HttpServletResponseWrapper(response) {
			@Override
			public void setHeader(String name, String value) {
				if ("Allow".equals(name)) {
					value = (StringUtils.hasLength(value) ? value + ", " : "") + RequestMethod.PATCH.name();
				}
				super.setHeader(name, value);
			}
		});
	}
	@Override
	protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		if (this.dispatchTraceRequest) {
			processRequest(request, response);
			if ("message/http".equals(response.getContentType())) {
				// Proper TRACE response coming from a handler - we're done.
				return;
			}
		}
		super.doTrace(request, response);
	}

	//doGet、doPost等请求最终委托 processRequest 来处理
	protected final void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;

		// 先获取LocaleContext与RequestAttributes备份起来，用于在finally中进行恢复
		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
		LocaleContext localeContext = buildLocaleContext(request);

		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

		// 将当前的localeContext，requestAttributes覆盖进去
		initContextHolders(request, localeContext, requestAttributes);

		try {
			// 实际处理请求的地方
			doService(request, response);
		}
		catch (ServletException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Throwable ex) {
			failureCause = ex;
			throw new NestedServletException("Request processing failed", ex);
		}

		finally {
			//恢复相关信息，因为在service可能还有Filter等操作
			resetContextHolders(request, previousLocaleContext, previousAttributes);
			if (requestAttributes != null) {
				requestAttributes.requestCompleted();
			}

			if (logger.isDebugEnabled()) {
				if (failureCause != null) {
					this.logger.debug("Could not complete request", failureCause);
				}
				else {
					if (asyncManager.isConcurrentHandlingStarted()) {
						logger.debug("Leaving response open for concurrent processing");
					}
					else {
						this.logger.debug("Successfully completed request");
					}
				}
			}

			// log...
			// 发布Event，类型是ServletRequestHandledEvent，这个下面有解释
			publishRequestHandledEvent(request, startTime, failureCause);
		}
	}
	/**
	 * Build a LocaleContext for the given request, exposing the request's
	 * primary locale as current locale.
	 * @param request current HTTP request
	 * @return the corresponding LocaleContext, or {@code null} if none to bind
	 * @see LocaleContextHolder#setLocaleContext
	 */
	protected LocaleContext buildLocaleContext(HttpServletRequest request) {
		return new SimpleLocaleContext(request.getLocale());
	}
	/**
	 * Build ServletRequestAttributes for the given request (potentially also
	 * holding a reference to the response), taking pre-bound attributes
	 * (and their type) into consideration.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param previousAttributes pre-bound RequestAttributes instance, if any
	 * @return the ServletRequestAttributes to bind, or {@code null} to preserve
	 * the previously bound instance (or not binding any, if none bound before)
	 * @see RequestContextHolder#setRequestAttributes
	 */
	protected ServletRequestAttributes buildRequestAttributes(HttpServletRequest request, HttpServletResponse response, RequestAttributes previousAttributes) {

		if (previousAttributes == null || previousAttributes instanceof ServletRequestAttributes) {
			return new ServletRequestAttributes(request);
		}
		else {
			return null;  // preserve the pre-bound RequestAttributes instance
		}
	}
	private void initContextHolders(HttpServletRequest request, LocaleContext localeContext, RequestAttributes requestAttributes) {

		if (localeContext != null) {
			LocaleContextHolder.setLocaleContext(localeContext, this.threadContextInheritable);
		}
		if (requestAttributes != null) {
			RequestContextHolder.setRequestAttributes(requestAttributes, this.threadContextInheritable);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Bound request context to thread: " + request);
		}
	}
	private void resetContextHolders(HttpServletRequest request, LocaleContext prevLocaleContext, RequestAttributes previousAttributes) {

		LocaleContextHolder.setLocaleContext(prevLocaleContext, this.threadContextInheritable);
		RequestContextHolder.setRequestAttributes(previousAttributes, this.threadContextInheritable);
		if (logger.isTraceEnabled()) {
			logger.trace("Cleared thread-bound request context: " + request);
		}
	}
	private void publishRequestHandledEvent(HttpServletRequest request, long startTime, Throwable failureCause) {
		if (this.publishEvents) {
			// Whether or not we succeeded, publish an event.
			long processingTime = System.currentTimeMillis() - startTime;
			this.webApplicationContext.publishEvent(
					new ServletRequestHandledEvent(this,
							request.getRequestURI(), request.getRemoteAddr(),
							request.getMethod(), getServletConfig().getServletName(),
							WebUtils.getSessionId(request), getUsernameForRequest(request),
							processingTime, failureCause));
		}
	}
	/**
	 * Determine the username for the given request.
	 * <p>The default implementation takes the name of the UserPrincipal, if any.
	 * Can be overridden in subclasses.
	 * @param request current HTTP request
	 * @return the username, or {@code null} if none found
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	protected String getUsernameForRequest(HttpServletRequest request) {
		Principal userPrincipal = request.getUserPrincipal();
		return (userPrincipal != null ? userPrincipal.getName() : null);
	}
	/**
	 * CallableProcessingInterceptor implementation that initializes and resets
	 * FrameworkServlet's context holders, i.e. LocaleContextHolder and RequestContextHolder.
	 */
	private class RequestBindingInterceptor extends CallableProcessingInterceptorAdapter {

		@Override
		public <T> void preProcess(NativeWebRequest webRequest, Callable<T> task) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				HttpServletResponse response = webRequest.getNativeRequest(HttpServletResponse.class);
				initContextHolders(request, buildLocaleContext(request), buildRequestAttributes(request, response, null));
			}
		}
		@Override
		public <T> void postProcess(NativeWebRequest webRequest, Callable<T> task, Object concurrentResult) {
			HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
			if (request != null) {
				resetContextHolders(request, null, null);
			}
		}
	}
	// doService是实际处理请求的实现，最终交由DispatchServlet自己来实现
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response) throws Exception;
	/* ----------DispatcherServlet的请求处理实现---------------------------------------------------------------------------------------------------------------------------------- */






	public void refresh() {
		WebApplicationContext wac = getWebApplicationContext();
		if (!(wac instanceof ConfigurableApplicationContext)) {
			throw new IllegalStateException("WebApplicationContext does not support refresh: " + wac);
		}
		((ConfigurableApplicationContext) wac).refresh();
	}

	// 当Web应用被终止时，Servlet容器会先调用Servlet对象的destroy方法，然后在销毁Servlet对象，同时也销毁与Servlet对象相关联的ServletConfig对象。
	// 我们可以在destroy方法的实现中，释放Servlet所占用的资源，如关闭数据库连接，关闭文件输入输出流等。
	@Override
	public void destroy() {
		getServletContext().log("Destroying Spring FrameworkServlet '" + getServletName() + "'");
		if (this.webApplicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) this.webApplicationContext).close();
		}
	}










	// getter and setter ...
	public void setContextAttribute(String contextAttribute) {
		this.contextAttribute = contextAttribute;
	}
	public String getContextAttribute() {
		return this.contextAttribute;
	}
	public void setContextClass(Class<?> contextClass) {
		this.contextClass = contextClass;
	}
	public Class<?> getContextClass() {
		return this.contextClass;
	}
	public void setContextId(String contextId) {
		this.contextId = contextId;
	}
	public String getContextId() {
		return this.contextId;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getNamespace() {
		return (this.namespace != null ? this.namespace : getServletName() + DEFAULT_NAMESPACE_SUFFIX);
	}
	public void setContextConfigLocation(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
	}
	public String getContextConfigLocation() {
		return this.contextConfigLocation;
	}
	// Return this servlet's WebApplicationContext.
	public final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}

	@SuppressWarnings("unchecked")
	public void setContextInitializers(ApplicationContextInitializer<? extends ConfigurableApplicationContext>... contextInitializers) {
		for (ApplicationContextInitializer<? extends ConfigurableApplicationContext> initializer : contextInitializers) {
			this.contextInitializers.add((ApplicationContextInitializer<ConfigurableApplicationContext>) initializer);
		}
	}
	public void setContextInitializerClasses(String contextInitializerClasses) {
		this.contextInitializerClasses = contextInitializerClasses;
	}
	public void setPublishContext(boolean publishContext) {
		this.publishContext = publishContext;
	}
	public void setPublishEvents(boolean publishEvents) {
		this.publishEvents = publishEvents;
	}
	public void setThreadContextInheritable(boolean threadContextInheritable) {
		this.threadContextInheritable = threadContextInheritable;
	}
	public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
		this.dispatchOptionsRequest = dispatchOptionsRequest;
	}
	public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
		this.dispatchTraceRequest = dispatchTraceRequest;
	}





}
