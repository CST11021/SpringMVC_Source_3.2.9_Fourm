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

package org.springframework.web.servlet.handler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.core.Ordered;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract base class for {@link org.springframework.web.servlet.HandlerMapping}
 * implementations. Supports ordering, a default handler, handler interceptors,
 * including handler interceptors mapped by path patterns.
 *
 * <p>Note: This base class does <i>not</i> support exposure of the
 * {@link #PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE}. Support for this attribute
 * is up to concrete subclasses, typically based on request URL mappings.
 *
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 07.04.2003
 * @see #getHandlerInternal
 * @see #setDefaultHandler
 * @see #setAlwaysUseFullPath
 * @see #setUrlDecode
 * @see org.springframework.util.AntPathMatcher
 * @see #setInterceptors
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public abstract class AbstractHandlerMapping extends WebApplicationObjectSupport implements HandlerMapping, Ordered {
	/** 该属性用于排序 */
	private int order = Integer.MAX_VALUE;
	/**
	 * 表示HTTP请求默认对应的 Controller，当从HandlerMapping中找不到对应的控制器时，就会使用该对象作为Controller
	 * （该字段可能是字符串表示这个控制器Bean的BeanName，也可能就是控制器Bean本身）
	 */
	private Object defaultHandler;

	private UrlPathHelper urlPathHelper = new UrlPathHelper();
	private PathMatcher pathMatcher = new AntPathMatcher();
	/** 表示该HandleMapping的拦截器,该拦截器类型包括：HandlerInterceptor，WebRequestInterceptor和MappedInterceptor */
	private final List<Object> interceptors = new ArrayList<Object>();
	/** 表示请求的拦截器，当请求被控制器处理器前，可能会经过多个拦截器进行处理 */
	private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<HandlerInterceptor>();
	private final List<MappedInterceptor> mappedInterceptors = new ArrayList<MappedInterceptor>();


	public void setInterceptors(Object[] interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}



	// 1、初始化拦截器----------------------------------------------------------------------------------------------------

	@Override
	protected void initApplicationContext() throws BeansException {
		extendInterceptors(this.interceptors);
		detectMappedInterceptors(this.mappedInterceptors);
		initInterceptors();
	}
	/**
	 * Extension hook that subclasses can override to register additional interceptors,
	 * given the configured interceptors (see {@link #setInterceptors}).
	 * <p>Will be invoked before {@link #initInterceptors()} adapts the specified
	 * interceptors into {@link HandlerInterceptor} instances.
	 * <p>The default implementation is empty.
	 * @param interceptors the configured interceptor List (never {@code null}), allowing
	 * to add further interceptors before as well as after the existing interceptors
	 */
	protected void extendInterceptors(List<Object> interceptors) {
	}
	/**
	 * Detect beans of type {@link MappedInterceptor} and add them to the list of mapped interceptors.
	 * <p>This is called in addition to any {@link MappedInterceptor}s that may have been provided
	 * via {@link #setInterceptors}, by default adding all beans of type {@link MappedInterceptor}
	 * from the current context and its ancestors. Subclasses can override and refine this policy.
	 * @param mappedInterceptors an empty list to add {@link MappedInterceptor} instances to
	 */
	protected void detectMappedInterceptors(List<MappedInterceptor> mappedInterceptors) {
		mappedInterceptors.addAll(
				BeanFactoryUtils.beansOfTypeIncludingAncestors(
						getApplicationContext(), MappedInterceptor.class, true, false).values());
	}
	/**
	 * Initialize the specified interceptors, checking for {@link MappedInterceptor}s and
	 * adapting {@link HandlerInterceptor}s and {@link WebRequestInterceptor}s if necessary.
	 * @see #setInterceptors
	 * @see #adaptInterceptor
	 */
	protected void initInterceptors() {
		if (!this.interceptors.isEmpty()) {
			for (int i = 0; i < this.interceptors.size(); i++) {
				Object interceptor = this.interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
				}
				if (interceptor instanceof MappedInterceptor) {
					this.mappedInterceptors.add((MappedInterceptor) interceptor);
				}
				else {
					this.adaptedInterceptors.add(adaptInterceptor(interceptor));
				}
			}
		}
	}
	/**
	 * Adapt the given interceptor object to the {@link HandlerInterceptor} interface.
	 * <p>By default, the supported interceptor types are {@link HandlerInterceptor}
	 * and {@link WebRequestInterceptor}. Each given {@link WebRequestInterceptor}
	 * will be wrapped in a {@link WebRequestHandlerInterceptorAdapter}.
	 * Can be overridden in subclasses.
	 * @param interceptor the specified interceptor object
	 * @return the interceptor wrapped as HandlerInterceptor
	 * @see org.springframework.web.servlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 * @see WebRequestHandlerInterceptorAdapter
	 */
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		}
		else if (interceptor instanceof WebRequestInterceptor) {
			return new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor);
		}
		else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}




	// 2、根据请求获取对应的HandlerExecutionChain执行链--------------------------------------------------------------------

	public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
	    // 获取控制器对象
		Object handler = getHandlerInternal(request);
		if (handler == null) {
			handler = getDefaultHandler();
		}
		if (handler == null) {
			return null;
		}

		// Bean name or resolved handler?
		if (handler instanceof String) {
			String handlerName = (String) handler;
			handler = getApplicationContext().getBean(handlerName);
		}
		return getHandlerExecutionChain(handler, request);
	}
	/**
	 * 获取request对应的控制器（即Controller），这里返回的可能是一个控制器Bean，或是控制器BeanName或者HandlerExecutionChain
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected abstract Object getHandlerInternal(HttpServletRequest request) throws Exception;
	/**
	 * 将配置中对应的拦截器加入到执行链中，以保证这些拦截器可以有效地作用于目标对象
	 *
	 * @param handler	表示对应的请求处理器（及Controller类）
	 * @param request	表示本次请求
	 * @return
	 */
	protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
		// 将handler转为HandlerExecutionChain对象，并添加拦截器
		HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
				(HandlerExecutionChain) handler : new HandlerExecutionChain(handler));
		chain.addInterceptors(getAdaptedInterceptors());

		String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
		for (MappedInterceptor mappedInterceptor : this.mappedInterceptors) {
			if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
				chain.addInterceptor(mappedInterceptor.getInterceptor());
			}
		}

		return chain;
	}
	/**
	 * 获取所有配置的拦截器
	 *
	 * @return
	 */
	protected final HandlerInterceptor[] getAdaptedInterceptors() {
		int count = this.adaptedInterceptors.size();
		return (count > 0 ? this.adaptedInterceptors.toArray(new HandlerInterceptor[count]) : null);
	}




	// 3、UrlPathHelper-------------------------------------------------------------------------------------------------

	/**
	 * Set if URL lookup should always use the full path within the current servlet
	 * context. Else, the path within the current servlet mapping is used if applicable
	 * (that is, in the case of a ".../*" servlet mapping in web.xml).
	 * <p>Default is "false".
	 * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
	 */
	public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
		this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
	}
	/**
	 * Set if context path and request URI should be URL-decoded. Both are returned
	 * <i>undecoded</i> by the Servlet API, in contrast to the servlet path.
	 * <p>Uses either the request encoding or the default encoding according
	 * to the Servlet spec (ISO-8859-1).
	 * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
	 */
	public void setUrlDecode(boolean urlDecode) {
		this.urlPathHelper.setUrlDecode(urlDecode);
	}
	/**
	 * Set if ";" (semicolon) content should be stripped from the request URI.
	 * <p>The default value is {@code true}.
	 * @see org.springframework.web.util.UrlPathHelper#setRemoveSemicolonContent(boolean)
	 */
	public void setRemoveSemicolonContent(boolean removeSemicolonContent) {
		this.urlPathHelper.setRemoveSemicolonContent(removeSemicolonContent);
	}



	// getter and setter ...
	public final void setOrder(int order) {
		this.order = order;
	}
	public final int getOrder() {
		return this.order;
	}
	public void setDefaultHandler(Object defaultHandler) {
		this.defaultHandler = defaultHandler;
	}
	public Object getDefaultHandler() {
		return this.defaultHandler;
	}
	public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
		Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
		this.urlPathHelper = urlPathHelper;
	}
	public UrlPathHelper getUrlPathHelper() {
		return urlPathHelper;
	}
	public void setPathMatcher(PathMatcher pathMatcher) {
		Assert.notNull(pathMatcher, "PathMatcher must not be null");
		this.pathMatcher = pathMatcher;
	}
	public PathMatcher getPathMatcher() {
		return this.pathMatcher;
	}
	protected final MappedInterceptor[] getMappedInterceptors() {
		int count = this.mappedInterceptors.size();
		return (count > 0 ? this.mappedInterceptors.toArray(new MappedInterceptor[count]) : null);
	}

}
