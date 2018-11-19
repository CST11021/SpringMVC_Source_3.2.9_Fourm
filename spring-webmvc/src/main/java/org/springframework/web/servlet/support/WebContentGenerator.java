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

package org.springframework.web.servlet.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.support.WebApplicationObjectSupport;

/**
 * Convenient superclass for any kind of web content generator,
 * like {@link org.springframework.web.servlet.mvc.AbstractController}
 * and {@link org.springframework.web.servlet.mvc.WebContentInterceptor}.
 * Can also be used for custom handlers that have their own
 * {@link org.springframework.web.servlet.HandlerAdapter}.
 *
 * <p>Supports HTTP cache control options. The usage of corresponding
 * HTTP headers can be controlled via the "useExpiresHeader",
 * "useCacheControlHeader" and "useCacheControlNoStore" properties.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setCacheSeconds
 * @see #setRequireSession
 */
public abstract class WebContentGenerator extends WebApplicationObjectSupport {

	/** HTTP method "GET" */
	public static final String METHOD_GET = "GET";
	/** HTTP method "HEAD" */
	public static final String METHOD_HEAD = "HEAD";
	/** HTTP method "POST" */
	public static final String METHOD_POST = "POST";


	private static final String HEADER_PRAGMA = "Pragma";

	// 关于Cache-Control和Expires的知识请参考：https://www.jianshu.com/p/f331d5f0b979
	private static final String HEADER_EXPIRES = "Expires";
	private static final String HEADER_CACHE_CONTROL = "Cache-Control";


	/** 表示该控制器支持的Http method，一个处理器可支持多种方法 */
	private Set<String>	supportedMethods;

	private boolean requireSession = false;

	/** Use HTTP 1.0 expires header? */
	private boolean useExpiresHeader = true;

	/** Use HTTP 1.1 cache-control header? */
	private boolean useCacheControlHeader = true;

	/** Use HTTP 1.1 cache-control header value "no-store"? */
	private boolean useCacheControlNoStore = true;

	private int cacheSeconds = -1;

	private boolean alwaysMustRevalidate = false;



	public WebContentGenerator() {
		this(true);
	}
	public WebContentGenerator(boolean restrictDefaultSupportedMethods) {
		if (restrictDefaultSupportedMethods) {
			this.supportedMethods = new HashSet<String>(4);
			this.supportedMethods.add(METHOD_GET);
			this.supportedMethods.add(METHOD_HEAD);
			this.supportedMethods.add(METHOD_POST);
		}
	}
	public WebContentGenerator(String... supportedMethods) {
		this.supportedMethods = new HashSet<String>(Arrays.asList(supportedMethods));
	}


	/**
	 * Set the HTTP methods that this content generator should support.
	 * <p>Default is GET, HEAD and POST for simple form controller types;
	 * unrestricted for general controllers and interceptors.
	 */
	public final void setSupportedMethods(String[] methods) {
		if (methods != null) {
			this.supportedMethods = new HashSet<String>(Arrays.asList(methods));
		}
		else {
			this.supportedMethods = null;
		}
	}

	/**
	 * Return the HTTP methods that this content generator supports.
	 */
	public final String[] getSupportedMethods() {
		return StringUtils.toStringArray(this.supportedMethods);
	}

	/**
	 * Set whether a session should be required to handle requests.
	 */
	public final void setRequireSession(boolean requireSession) {
		this.requireSession = requireSession;
	}

	/**
	 * Return whether a session is required to handle requests.
	 */
	public final boolean isRequireSession() {
		return this.requireSession;
	}

	/**
	 * Set whether to use the HTTP 1.0 expires header. Default is "true".
	 * <p>Note: Cache headers will only get applied if caching is enabled
	 * (or explicitly prevented) for the current request.
	 */
	public final void setUseExpiresHeader(boolean useExpiresHeader) {
		this.useExpiresHeader = useExpiresHeader;
	}

	/**
	 * Return whether the HTTP 1.0 expires header is used.
	 */
	public final boolean isUseExpiresHeader() {
		return this.useExpiresHeader;
	}

	/**
	 * Set whether to use the HTTP 1.1 cache-control header. Default is "true".
	 * <p>Note: Cache headers will only get applied if caching is enabled
	 * (or explicitly prevented) for the current request.
	 */
	public final void setUseCacheControlHeader(boolean useCacheControlHeader) {
		this.useCacheControlHeader = useCacheControlHeader;
	}

	/**
	 * Return whether the HTTP 1.1 cache-control header is used.
	 */
	public final boolean isUseCacheControlHeader() {
		return this.useCacheControlHeader;
	}

	/**
	 * Set whether to use the HTTP 1.1 cache-control header value "no-store"
	 * when preventing caching. Default is "true".
	 */
	public final void setUseCacheControlNoStore(boolean useCacheControlNoStore) {
		this.useCacheControlNoStore = useCacheControlNoStore;
	}

	/**
	 * Return whether the HTTP 1.1 cache-control header value "no-store" is used.
	 */
	public final boolean isUseCacheControlNoStore() {
		return this.useCacheControlNoStore;
	}

	/**
	 * An option to add 'must-revalidate' to every Cache-Control header. This
	 * may be useful with annotated controller methods, which can
	 * programmatically do a lastModified calculation as described in
	 * {@link WebRequest#checkNotModified(long)}. Default is "false",
	 * effectively relying on whether the handler implements
	 * {@link org.springframework.web.servlet.mvc.LastModified} or not.
	 */
	public void setAlwaysMustRevalidate(boolean mustRevalidate) {
		this.alwaysMustRevalidate = mustRevalidate;
	}

	/**
	 * Return whether 'must-revaliate' is added to every Cache-Control header.
	 */
	public boolean isAlwaysMustRevalidate() {
		return alwaysMustRevalidate;
	}

	/**
	 * Cache content for the given number of seconds. Default is -1,
	 * indicating no generation of cache-related headers.
	 * <p>Only if this is set to 0 (no cache) or a positive value (cache for
	 * this many seconds) will this class generate cache headers.
	 * <p>The headers can be overwritten by subclasses, before content is generated.
	 */
	public final void setCacheSeconds(int seconds) {
		this.cacheSeconds = seconds;
	}

	/**
	 * Return the number of seconds that content is cached.
	 */
	public final int getCacheSeconds() {
		return this.cacheSeconds;
	}











	// --------------- 检查控制器是否支持相应的请求方法、检查请求是否需要session和检查是否设置HTTP的缓存机制 ---------------

	protected final void checkAndPrepare(HttpServletRequest request, HttpServletResponse response, boolean lastModified) throws ServletException {
		checkAndPrepare(request, response, this.cacheSeconds, lastModified);
	}
	protected final void checkAndPrepare(HttpServletRequest request, HttpServletResponse response, int cacheSeconds, boolean lastModified) throws ServletException {
		// 检查是否支持该请求方法
		String method = request.getMethod();
		if (this.supportedMethods != null && !this.supportedMethods.contains(method)) {
			throw new HttpRequestMethodNotSupportedException(method, StringUtils.toStringArray(this.supportedMethods));
		}

		// 检查该请求是否一定需要session
		if (this.requireSession) {
			if (request.getSession(false) == null) {
				throw new HttpSessionRequiredException("Pre-existing session required but none found");
			}
		}

		// 执行声明式缓存控制。
		// 重新验证控制器是否支持last-modified。
		applyCacheSeconds(response, cacheSeconds, lastModified);
	}
	// 设置HTTP请求的缓存机制
	protected final void applyCacheSeconds(HttpServletResponse response, int seconds, boolean mustRevalidate) {
		if (seconds > 0) {
			cacheForSeconds(response, seconds, mustRevalidate);
		}
		else if (seconds == 0) {
			preventCaching(response);
		}
		// Leave caching to the client otherwise.
	}
	// 设置请求的Cache-Control和Expires头信息
	protected final void cacheForSeconds(HttpServletResponse response, int seconds, boolean mustRevalidate) {
		if (this.useExpiresHeader) {
			// HTTP 1.0 header
			// 关于Cache-Control和Expires的知识请参考：https://www.jianshu.com/p/f331d5f0b979
			response.setDateHeader(HEADER_EXPIRES, System.currentTimeMillis() + seconds * 1000L);
		}
		if (this.useCacheControlHeader) {
			// HTTP 1.1 header
			String headerValue = "max-age=" + seconds;
			if (mustRevalidate || this.alwaysMustRevalidate) {
				headerValue += ", must-revalidate";
			}
			response.setHeader(HEADER_CACHE_CONTROL, headerValue);
		}
	}
	// 防止响应被缓存。
	protected final void preventCaching(HttpServletResponse response) {
		response.setHeader(HEADER_PRAGMA, "no-cache");
		if (this.useExpiresHeader) {
			// HTTP 1.0 header
			response.setDateHeader(HEADER_EXPIRES, 1L);
		}
		if (this.useCacheControlHeader) {
			// HTTP 1.1 header: "no-cache" is the standard value,
			// "no-store" is necessary to prevent caching on FireFox.
			response.setHeader(HEADER_CACHE_CONTROL, "no-cache");
			if (this.useCacheControlNoStore) {
				response.addHeader(HEADER_CACHE_CONTROL, "no-store");
			}
		}
	}
	/**
	 * Set HTTP headers to allow caching for the given number of seconds.
	 * Does not tell the browser to revalidate the resource.
	 * @param response current HTTP response
	 * @param seconds number of seconds into the future that the response
	 * should be cacheable for
	 * @see #cacheForSeconds(javax.servlet.http.HttpServletResponse, int, boolean)
	 */
	protected final void cacheForSeconds(HttpServletResponse response, int seconds) {
		cacheForSeconds(response, seconds, false);
	}
	/**
	 * Apply the given cache seconds and generate corresponding HTTP headers,
	 * i.e. allow caching for the given number of seconds in case of a positive
	 * value, prevent caching if given a 0 value, do nothing else.
	 * Does not tell the browser to revalidate the resource.
	 * @param response current HTTP response
	 * @param seconds positive number of seconds into the future that the
	 * response should be cacheable for, 0 to prevent caching
	 * @see #cacheForSeconds(javax.servlet.http.HttpServletResponse, int, boolean)
	 */
	protected final void applyCacheSeconds(HttpServletResponse response, int seconds) {
		applyCacheSeconds(response, seconds, false);
	}



}
