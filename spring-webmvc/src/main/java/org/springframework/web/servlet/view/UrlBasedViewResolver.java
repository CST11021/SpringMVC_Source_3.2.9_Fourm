
package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeanUtils;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.servlet.View;

// UrlBasedViewResolver：它是对ViewResolver的一种简单实现，而且继承了AbstractCachingViewResolver，主要就是提供的一种拼接URL的方式来解析视图，
// 它可以让我们通过prefix属性指定一个指定的前缀，通过suffix属性指定一个指定的后缀，然后把返回的逻辑视图名称加上指定的前缀和后缀就是指定的视图URL了。比如:
// prefix=/WEB-INF/jsps/，suffix=.jsp，返回的视图名称viewName=test/indx，则UrlBasedViewResolver解析出来的视图URL就是/WEB-INF/jsps/test/index.jsp。默认的prefix和suffix都是空串。
//
// URLBasedViewResolver支持返回的视图名称中包含redirect:前缀，这样就可以支持URL在客户端的跳转，如当返回的视图名称是”redirect:test.do”的时候，
// URLBasedViewResolver发现返回的视图名称包含”redirect:”前缀，于是把返回的视图名称前缀”redirect:”去掉，取后面的test.do组成一个RedirectView，RedirectView中将把请求返回的
// 模型属性组合成查询参数的形式组合到redirect的URL后面，然后调用HttpServletResponse对象的sendRedirect方法进行重定向。
//
// 同样URLBasedViewResolver还支持forword:前缀，对于视图名称中包含forword:前缀的视图名称将会被封装成一个InternalResourceView对象，然后在服务器端利用RequestDispatcher的forword方式跳转到指定的地址。
//
// 使用UrlBasedViewResolver的时候必须指定属性viewClass，表示解析成哪种视图，一般使用较多的就是InternalResourceView，利用它来展现jsp，但是当我们使用JSTL的时候我们必须使用JstlView。下面是一段UrlBasedViewResolver的定义，
// <bean class="org.springframework.web.servlet.view.UrlBasedViewResolver">
// 	 <property name="prefix" value="/WEB-INF/" />  
//	 <property name="suffix" value=".jsp" />  
//	 <property name="viewClass" value="org.springframework.web.servlet.view.InternalResourceView"/>
//	</bean>
// 根据该定义，当返回的逻辑视图名称是test的时候，UrlBasedViewResolver将把逻辑视图名称加上定义好的前缀和后缀，即“/WEB-INF/test.jsp”，然后新建一个viewClass属性指定的视图类型予以返回，
// 即返回一个url为“/WEB-INF/test.jsp”的InternalResourceView对象。
public class UrlBasedViewResolver extends AbstractCachingViewResolver implements Ordered {

	// 关于重定向的问题：
	// 使用servlet重定向有两种方式，一种是forward，另一种就是redirect。
	// forward是服务器内部重定向，客户端并不知道服务器把你当前请求重定向到哪里去了，地址栏的url与你之前访问的url保持不变。
	// redirect则是客户端重定向，是服务器将你当前请求返回，然后给个状态标示给你，告诉你应该去重新请求另外一个url，具体表现就是地址栏的url变成了新的url。
	public static final String REDIRECT_URL_PREFIX = "redirect:";
	public static final String FORWARD_URL_PREFIX = "forward:";

	private Class viewClass;
	private String prefix = "";
	private String suffix = "";
	// 该视图解析器能解析的视图名(这里使用正则表示所有能解析的视图名称)
	private String[] viewNames = null;
	private String contentType;
	private boolean redirectContextRelative = true;
	private boolean redirectHttp10Compatible = true;
	private String requestContextAttribute;
	private int order = Integer.MAX_VALUE;

	// Map of static attributes, keyed by attribute name (String)
	private final Map<String, Object> staticAttributes = new HashMap<String, Object>();
	private Boolean exposePathVariables;


	// 返回该视图解析器能解析的视图类型
	protected Class requiredViewClass() {
		return AbstractUrlBasedView.class;
	}

	public void setViewClass(Class viewClass) {
		if (viewClass == null || !requiredViewClass().isAssignableFrom(viewClass)) {
			throw new IllegalArgumentException("Given view class [" + (viewClass != null ? viewClass.getName() : null) +
					"] is not of type [" + requiredViewClass().getName() + "]");
		}
		this.viewClass = viewClass;
	}
	protected Class getViewClass() {
		return this.viewClass;
	}
	public void setPrefix(String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}
	protected String getPrefix() {
		return this.prefix;
	}
	public void setSuffix(String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}
	protected String getSuffix() {
		return this.suffix;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	protected String getContentType() {
		return this.contentType;
	}

	public void setRedirectContextRelative(boolean redirectContextRelative) {
		this.redirectContextRelative = redirectContextRelative;
	}
	protected boolean isRedirectContextRelative() {
		return this.redirectContextRelative;
	}
	public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
		this.redirectHttp10Compatible = redirectHttp10Compatible;
	}
	protected boolean isRedirectHttp10Compatible() {
		return this.redirectHttp10Compatible;
	}
	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}
	protected String getRequestContextAttribute() {
		return this.requestContextAttribute;
	}

	/**
	 * Set static attributes from a {@code java.util.Properties} object,
	 * for all views returned by this resolver.
	 * <p>This is the most convenient way to set static attributes. Note that
	 * static attributes can be overridden by dynamic attributes, if a value
	 * with the same name is included in the model.
	 * <p>Can be populated with a String "value" (parsed via PropertiesEditor)
	 * or a "props" element in XML bean definitions.
	 * @see org.springframework.beans.propertyeditors.PropertiesEditor
	 * @see AbstractView#setAttributes
	 */
	public void setAttributes(Properties props) {
		CollectionUtils.mergePropertiesIntoMap(props, this.staticAttributes);
	}
	/**
	 * Set static attributes from a Map, for all views returned by this resolver.
	 * This allows to set any kind of attribute values, for example bean references.
	 * <p>Can be populated with a "map" or "props" element in XML bean definitions.
	 * @param attributes Map with name Strings as keys and attribute objects as values
	 * @see AbstractView#setAttributesMap
	 */
	public void setAttributesMap(Map<String, ?> attributes) {
		if (attributes != null) {
			this.staticAttributes.putAll(attributes);
		}
	}

	/**
	 * Allow Map access to the static attributes for views returned by
	 * this resolver, with the option to add or override specific entries.
	 * <p>Useful for specifying entries directly, for example via
	 * "attributesMap[myKey]". This is particularly useful for
	 * adding or overriding entries in child view definitions.
	 */
	public Map<String, Object> getAttributesMap() {
		return this.staticAttributes;
	}

	/**
	 * Set the view names (or name patterns) that can be handled by this
	 * {@link org.springframework.web.servlet.ViewResolver}. View names can contain
	 * simple wildcards such that 'my*', '*Report' and '*Repo*' will all match the
	 * view name 'myReport'.
	 * @see #canHandle
	 */
	public void setViewNames(String[] viewNames) {
		this.viewNames = viewNames;
	}

	/**
	 * Return the view names (or name patterns) that can be handled by this
	 * {@link org.springframework.web.servlet.ViewResolver}.
	 */
	protected String[] getViewNames() {
		return this.viewNames;
	}

	// 设置视图解析器的优先级顺序
	public void setOrder(int order) {
		this.order = order;
	}
	public int getOrder() {
		return this.order;
	}

	/**
	 * Whether views resolved by this resolver should add path variables the model or not.
	 * The default setting is to allow each View decide (see {@link AbstractView#setExposePathVariables(boolean)}.
	 * However, you can use this property to override that.
	 * @param exposePathVariables
	 * 	<ul>
	 * 		<li>{@code true} - all Views resolved by this resolver will expose path variables
	 * 		<li>{@code false} - no Views resolved by this resolver will expose path variables
	 * 		<li>{@code null} - individual Views can decide for themselves (this is used by the default)
	 * 	<ul>
	 * 	@see AbstractView#setExposePathVariables(boolean)
	 */
	public void setExposePathVariables(Boolean exposePathVariables) {
		this.exposePathVariables = exposePathVariables;
	}

	@Override
	protected void initApplicationContext() {
		super.initApplicationContext();
		if (getViewClass() == null) {
			throw new IllegalArgumentException("Property 'viewClass' is required");
		}
	}

	// This implementation returns just the view name, as this ViewResolver doesn't support localized resolution.
	@Override
	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName;
	}

	// 判断该视图解析器能否解析当前视图名
	protected boolean canHandle(String viewName, Locale locale) {
		String[] viewNames = getViewNames();
		return (viewNames == null || PatternMatchUtils.simpleMatch(viewNames, viewName));
	}


	// 创建视图
	@Override
	protected View createView(String viewName, Locale locale) throws Exception {
		// If this resolver is not supposed to handle the given view, return null to pass on to the next resolver in the chain.
		if (!canHandle(viewName, locale)) {
			return null;
		}
		// Check for special "redirect:" prefix.
		// 如果是redirect:开头的viewName，那么就采用 RedirectView
		if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
			String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
			RedirectView view = new RedirectView(redirectUrl, isRedirectContextRelative(), isRedirectHttp10Compatible());
			return applyLifecycleMethods(viewName, view);
		}
		// Check for special "forward:" prefix.
		// 如果是forward开头的viewName
		if (viewName.startsWith(FORWARD_URL_PREFIX)) {
			String forwardUrl = viewName.substring(FORWARD_URL_PREFIX.length());
			return new InternalResourceView(forwardUrl);
		}
		// Else fall back to superclass implementation: calling loadView.
		// 如果不符合redirect: 和forward:开头那么就调用父类的createView方法来创建View，最终调用 UrlBasedViewResolver的loadView()来加载View对象
		return super.createView(viewName, locale);
	}
	@Override
	protected View loadView(String viewName, Locale locale) throws Exception {
		AbstractUrlBasedView view = buildView(viewName);
		View result = applyLifecycleMethods(viewName, view);
		return (view.checkResource(locale) ? result : null);
	}
	/**
	 * Creates a new View instance of the specified view class and configures it.
	 * Does <i>not</i> perform any lookup for pre-defined View instances.
	 * <p>Spring lifecycle methods as defined by the bean container do not have to
	 * be called here; those will be applied by the {@code loadView} method
	 * after this method returns.
	 * <p>Subclasses will typically call {@code super.buildView(viewName)}
	 * first, before setting further properties themselves. {@code loadView}
	 * will then apply Spring lifecycle methods at the end of this process.
	 * @param viewName the name of the view to build
	 * @return the View instance
	 * @throws Exception if the view couldn't be resolved
	 * @see #loadView(String, java.util.Locale)
	 */
	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		AbstractUrlBasedView view = (AbstractUrlBasedView) BeanUtils.instantiateClass(getViewClass());
		view.setUrl(getPrefix() + viewName + getSuffix());
		String contentType = getContentType();
		if (contentType != null) {
			view.setContentType(contentType);
		}
		view.setRequestContextAttribute(getRequestContextAttribute());
		view.setAttributesMap(getAttributesMap());
		if (this.exposePathVariables != null) {
			view.setExposePathVariables(exposePathVariables);
		}
		return view;
	}
	private View applyLifecycleMethods(String viewName, AbstractView view) {
		return (View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName);
	}



}
