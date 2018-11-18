/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.web.multipart;

import javax.servlet.http.HttpServletRequest;

/**
 * A strategy interface for multipart file upload resolution in accordance
 * with <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * Implementations are typically usable both within an application context
 * and standalone.
 *
 * <p>There are two concrete implementations included in Spring, as of Spring 3.1:
 * <ul>
 * <li>{@link org.springframework.web.multipart.commons.CommonsMultipartResolver} for Jakarta Commons FileUpload
 * <li>{@link org.springframework.web.multipart.support.StandardServletMultipartResolver} for Servlet 3.0 Part API
 * </ul>
 *
 * <p>There is no default resolver implementation used for Spring
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlets},
 * as an application might choose to parse its multipart requests itself. To define
 * an implementation, create a bean with the id "multipartResolver" in a
 * {@link org.springframework.web.servlet.DispatcherServlet DispatcherServlet's}
 * application context. Such a resolver gets applied to all requests handled
 * by that {@link org.springframework.web.servlet.DispatcherServlet}.
 *
 * <p>If a {@link org.springframework.web.servlet.DispatcherServlet} detects
 * a multipart request, it will resolve it via the configured
 * {@link MultipartResolver} and pass on a
 * wrapped {@link javax.servlet.http.HttpServletRequest}.
 * Controllers can then cast their given request to the
 * {@link MultipartHttpServletRequest}
 * interface, which permits access to any
 * {@link MultipartFile MultipartFiles}.
 * Note that this cast is only supported in case of an actual multipart request.
 *
 * <pre class="code">
 * public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
 *   MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 *   MultipartFile multipartFile = multipartRequest.getFile("image");
 *   ...
 * }</pre>
 *
 * Instead of direct access, command or form controllers can register a
 * {@link org.springframework.web.multipart.support.ByteArrayMultipartFileEditor}
 * or {@link org.springframework.web.multipart.support.StringMultipartFileEditor}
 * with their data binder, to automatically apply multipart content to command
 * bean properties.
 *
 * <p>As an alternative to using a
 * {@link MultipartResolver} with a
 * {@link org.springframework.web.servlet.DispatcherServlet},
 * a {@link org.springframework.web.multipart.support.MultipartFilter} can be
 * registered in {@code web.xml}. It will delegate to a corresponding
 * {@link MultipartResolver} bean in the root
 * application context. This is mainly intended for applications that do not
 * use Spring's own web MVC framework.
 *
 * <p>Note: There is hardly ever a need to access the
 * {@link MultipartResolver} itself
 * from application code. It will simply do its work behind the scenes,
 * making
 * {@link MultipartHttpServletRequest MultipartHttpServletRequests}
 * available to controllers.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartHttpServletRequest
 * @see MultipartFile
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see org.springframework.web.multipart.support.ByteArrayMultipartFileEditor
 * @see org.springframework.web.multipart.support.StringMultipartFileEditor
 * @see org.springframework.web.servlet.DispatcherServlet
 *


从Spring3.1开始，Spring提供了两个MultipartResolver的实现用于处理multipart请求：
	CommonsMultipartResolver使用commons Fileupload来处理multipart请求，所以在使用时，必须要引入相应的jar包；
	StandardServletMultipartResolver是基于Servlet3.0来处理multipart请求的，所以不需要引用其他jar包，但是必须使用支持Servlet3.0的容器才可以。
以tomcat为例，从Tomcat 7.0.x的版本开始就支持Servlet3.0了。

SpringMVC使用该接口时，需要手动配置这个Bean：
	这个bean是在 dispatcherServlet 初始化的时候，通过启动参数<init-param> 读取Spring相关的配置，例如这样的一段 bean配置：
	<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver" p:defaultEncoding="utf-8"/>
 */
public interface MultipartResolver {

	/**
	 * 判断这个request是不是文件上传的请求
	 *
	 * @param request
	 * @return
	 */
	boolean isMultipart(HttpServletRequest request);

	/**
	 * 如果这个request是一个文件上传的请求，则会将改request转为一个MultipartHttpServletRequest对象
	 *
	 * @param request
	 * @return
	 * @throws MultipartException
	 */
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * 释放 multiPartRequest 持有的所有资源
	 *
	 * @param request
	 */
	void cleanupMultipart(MultipartHttpServletRequest request);

}
