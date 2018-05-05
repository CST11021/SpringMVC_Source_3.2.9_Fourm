/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet;

import java.io.*;
import java.util.*;

/**
 * Defines an object to provide client request information to a servlet.  The
 * servlet container creates a <code>ServletRequest</code> object and passes
 * it as an argument to the servlet's <code>service</code> method.
 *
 * <p>A <code>ServletRequest</code> object provides data including
 * parameter name and values, attributes, and an input stream.
 * Interfaces that extend <code>ServletRequest</code> can provide
 * additional protocol-specific data (for example, HTTP data is
 * provided by {@link javax.servlet.http.HttpServletRequest}.
 * 
 * @author 	Various
 *
 * @see 	javax.servlet.http.HttpServletRequest
 *
 */
public interface ServletRequest {

    /** 返回request中键名为name对应的属性值 */
    public Object getAttribute(String name);
    public void setAttribute(String name, Object o);
    /** 返回所有的AttributeName
    Enumeration 接口是Iterator迭代器的“古老版本”，从JDK 1.0开始，Enumeration接口就已经存在了（Iterator从JDK 1.2才出现）。
    Enumeration接口只有两个方法：
        boolean hasMoreElements()
            如果此迭代器还有剩下的元素，则返回true
        Object nextElement()
            返回该迭代器的下一个元素，如果还有的话(否则抛出异常)
     */
    public Enumeration<String> getAttributeNames();
    public void removeAttribute(String name);


    /** 返回该请求报文主体中使用的字符编码，如果请求没有指定字符编码，则该方法返回null */
    public String getCharacterEncoding();
    /** 覆盖该请求报文主体中使用的字符编码，在读取请求参数或使用getReader()读取输入之前，必须调用此方法。否则无效 */
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException;

    /** 返回请求报文主体的长度，如果长度未知，则返回-1。对于HTTP servlet，与CGI变量内容长度的值相同。 */
    public int getContentLength();

    /** 返回请求报文主体的MIME类型，如果类型未知，则返回null。对于HTTP servlet，与CGI变量内容类型的值相同。 */
    public String getContentType();

    /** 以ServletInputStream 的形式返回请求报文主体的数据，这种方法或getReader可能被调用来读取主体 */
    public ServletInputStream getInputStream() throws IOException;
    public BufferedReader getReader() throws IOException;


    // 获取请求参数，即对应URL中的 key-value

    public String getParameter(String name);
    public Enumeration<String> getParameterNames();
    public String[] getParameterValues(String name);
    public Map<String, String[]> getParameterMap();


    /** 返回这个HTTP请求的协议名称和版本号 */
    public String getProtocol();
    /** 返回用于发出请求的scheme名称，例如：http、https或ftp */
    public String getScheme();

    /** 返回请求的服务器地址 */
    public String getServerName();
    /** 返回请求的服务器端口号 */
    public int getServerPort();


    /** 返回发送请求的客户端或最终代理的Internet协议(IP)地址 */
    public String getRemoteAddr();
    /** 返回客户机的完全限定名或发送请求的最后一个代理 */
    public String getRemoteHost();

    /** 根据accept-language头，返回客户端接受内容的首选语言环境，如果客户端请求没有提供一个接受语言的头部，这个方法将返回服务器的默认语言环境 */
    public Locale getLocale();

    /** 返回基于accept-language头的客户端可接受Locale，如果客户端请求没有提供一个可接受的语言头，该方法将返回一个包含一个地区的枚举，这是服务器的默认语言环境 */
    public Enumeration<Locale> getLocales();

    /** 返回一个布尔值，指示是否使用HTTPS等安全通道进行此请求 */
    public boolean isSecure();

    /**
     *
     * Returns a {@link RequestDispatcher} object that acts as a wrapper for
     * the resource located at the given path.  
     * A <code>RequestDispatcher</code> object can be used to forward
     * a request to the resource or to include the resource in a response.
     * The resource can be dynamic or static.
     *
     * <p>The pathname specified may be relative, although it cannot extend
     * outside the current servlet context.  If the path begins with 
     * a "/" it is interpreted as relative to the current context root.  
     * This method returns <code>null</code> if the servlet container
     * cannot return a <code>RequestDispatcher</code>.
     *
     * <p>The difference between this method and {@link
     * ServletContext#getRequestDispatcher} is that this method can take a
     * relative path.
     *
     * @param path      a <code>String</code> specifying the pathname
     *                  to the resource. If it is relative, it must be
     *                  relative against the current servlet.
     *
     * @return          a <code>RequestDispatcher</code> object
     *                  that acts as a wrapper for the resource
     *                  at the specified path, or <code>null</code>
     *                  if the servlet container cannot return a
     *                  <code>RequestDispatcher</code>
     *
     * @see             RequestDispatcher
     * @see             ServletContext#getRequestDispatcher
     */
    public RequestDispatcher getRequestDispatcher(String path);

    /**
     * @deprecated 	As of Version 2.1 of the Java Servlet API,
     * 			use {@link ServletContext#getRealPath} instead.
     */
    public String getRealPath(String path);

    /** 返回Internet协议(IP)的源端口或发送这个请求的最后一个代理客户机 */
    public int getRemotePort();

    /** 返回接收到该请求的Internet协议(IP)的主机名 */
    public String getLocalName();
    /** 返回接收到该请求的Internet协议(IP)的地址 */
    public String getLocalAddr();
    /** 返回接收到该请求的Internet协议(IP)的端口号 */
    public int getLocalPort();

    /** 获取servlet上下文，这个servlet上下文是最后一次发送的 */
    public ServletContext getServletContext();

    /**
     * Puts this request into asynchronous mode, and initializes its
     * {@link AsyncContext} with the original (unwrapped) ServletRequest
     * and ServletResponse objects.
     *
     * <p>Calling this method will cause committal of the associated
     * response to be delayed until {@link AsyncContext#complete} is
     * called on the returned {@link AsyncContext}, or the asynchronous
     * operation has timed out.
     *
     * <p>Calling {@link AsyncContext#hasOriginalRequestAndResponse()} on
     * the returned AsyncContext will return <code>true</code>. Any filters
     * invoked in the <i>outbound</i> direction after this request was put
     * into asynchronous mode may use this as an indication that any request
     * and/or response wrappers that they added during their <i>inbound</i>
     * invocation need not stay around for the duration of the asynchronous
     * operation, and therefore any of their associated resources may be
     * released.
     *
     * <p>This method clears the list of {@link AsyncListener} instances
     * (if any) that were registered with the AsyncContext returned by the
     * previous call to one of the startAsync methods, after calling each
     * AsyncListener at its {@link AsyncListener#onStartAsync onStartAsync}
     * method.
     *
     * <p>Subsequent invocations of this method, or its overloaded 
     * variant, will return the same AsyncContext instance, reinitialized
     * as appropriate.
     *
     * @return the (re)initialized AsyncContext
     * 
     * @throws IllegalStateException if this request is within the scope of
     * a filter or servlet that does not support asynchronous operations
     * (that is, {@link #isAsyncSupported} returns false),
     * or if this method is called again without any asynchronous dispatch
     * (resulting from one of the {@link AsyncContext#dispatch} methods),
     * is called outside the scope of any such dispatch, or is called again
     * within the scope of the same dispatch, or if the response has
     * already been closed
     *
     * @since Servlet 3.0
     */
    public AsyncContext startAsync() throws IllegalStateException;
    /**
     * Puts this request into asynchronous mode, and initializes its
     * {@link AsyncContext} with the given request and response objects.
     *
     * <p>The ServletRequest and ServletResponse arguments must be
     * the same instances, or instances of {@link ServletRequestWrapper} and
     * {@link ServletResponseWrapper} that wrap them, that were passed to the
     * {@link Servlet#service service} method of the Servlet or the
     * {@link Filter#doFilter doFilter} method of the Filter, respectively,
     * in whose scope this method is being called.
     *
     * <p>Calling this method will cause committal of the associated
     * response to be delayed until {@link AsyncContext#complete} is
     * called on the returned {@link AsyncContext}, or the asynchronous
     * operation has timed out.
     *
     * <p>Calling {@link AsyncContext#hasOriginalRequestAndResponse()} on
     * the returned AsyncContext will return <code>false</code>,
     * unless the passed in ServletRequest and ServletResponse arguments
     * are the original ones or do not carry any application-provided wrappers.
     * Any filters invoked in the <i>outbound</i> direction after this
     * request was put into asynchronous mode may use this as an indication
     * that some of the request and/or response wrappers that they added
     * during their <i>inbound</i> invocation may need to stay in place for
     * the duration of the asynchronous operation, and their associated
     * resources may not be released.
     * A ServletRequestWrapper applied during the <i>inbound</i>
     * invocation of a filter may be released by the <i>outbound</i>
     * invocation of the filter only if the given <code>servletRequest</code>,
     * which is used to initialize the AsyncContext and will be returned by
     * a call to {@link AsyncContext#getRequest()}, does not contain said
     * ServletRequestWrapper. The same holds true for ServletResponseWrapper
     * instances. 
     *
     * <p>This method clears the list of {@link AsyncListener} instances
     * (if any) that were registered with the AsyncContext returned by the
     * previous call to one of the startAsync methods, after calling each
     * AsyncListener at its {@link AsyncListener#onStartAsync onStartAsync}
     * method.
     *
     * <p>Subsequent invocations of this method, or its zero-argument
     * variant, will return the same AsyncContext instance, reinitialized
     * as appropriate. If a call to this method is followed by a call to its
     * zero-argument variant, the specified (and possibly wrapped) request
     * and response objects will remain <i>locked in</i> on the returned
     * AsyncContext.
     *
     * @param servletRequest the ServletRequest used to initialize the
     * AsyncContext
     * @param servletResponse the ServletResponse used to initialize the
     * AsyncContext
     *
     * @return the (re)initialized AsyncContext
     * 
     * @throws IllegalStateException if this request is within the scope of
     * a filter or servlet that does not support asynchronous operations
     * (that is, {@link #isAsyncSupported} returns false),
     * or if this method is called again without any asynchronous dispatch
     * (resulting from one of the {@link AsyncContext#dispatch} methods),
     * is called outside the scope of any such dispatch, or is called again
     * within the scope of the same dispatch, or if the response has
     * already been closed
     *
     * @since Servlet 3.0
     */
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException;
    /**
     * Checks if this request has been put into asynchronous mode.
     *
     * <p>A ServletRequest is put into asynchronous mode by calling
     * {@link #startAsync} or
     * {@link #startAsync(ServletRequest,ServletResponse)} on it.
     * 
     * <p>This method returns <tt>false</tt> if this request was
     * put into asynchronous mode, but has since been dispatched using
     * one of the {@link AsyncContext#dispatch} methods or released
     * from asynchronous mode via a call to {@link AsyncContext#complete}.
     *
     * @return true if this request has been put into asynchronous mode,
     * false otherwise
     *
     * @since Servlet 3.0
     */
    public boolean isAsyncStarted();
    /**
     * Checks if this request supports asynchronous operation.
     *
     * <p>Asynchronous operation is disabled for this request if this request
     * is within the scope of a filter or servlet that has not been annotated
     * or flagged in the deployment descriptor as being able to support
     * asynchronous handling.
     *
     * @return true if this request supports asynchronous operation, false
     * otherwise
     *
     * @since Servlet 3.0
     */
    public boolean isAsyncSupported();
    /**
     * Gets the AsyncContext that was created or reinitialized by the
     * most recent invocation of {@link #startAsync} or
     * {@link #startAsync(ServletRequest,ServletResponse)} on this request.
     *
     * @return the AsyncContext that was created or reinitialized by the
     * most recent invocation of {@link #startAsync} or
     * {@link #startAsync(ServletRequest,ServletResponse)} on
     * this request 
     *
     * @throws IllegalStateException if this request has not been put 
     * into asynchronous mode, i.e., if neither {@link #startAsync} nor
     * {@link #startAsync(ServletRequest,ServletResponse)} has been called
     *
     * @since Servlet 3.0
     */
    public AsyncContext getAsyncContext();

    /**
     * Gets the dispatcher type of this request.
     *
     * <p>The dispatcher type of a request is used by the container to select the filters that need to be applied to the request:
     * Only filters with matching dispatcher type and url patterns will be applied.
     * 
     * <p>Allowing a filter that has been configured for multiple 
     * dispatcher types to query a request for its dispatcher type
     * allows the filter to process the request differently depending on
     * its dispatcher type.
     *
     * <p>The initial dispatcher type of a request is defined as
     * <code>DispatcherType.REQUEST</code>. The dispatcher type of a request
     * dispatched via {@link RequestDispatcher#forward(ServletRequest,
     * ServletResponse)} or {@link RequestDispatcher#include(ServletRequest,
     * ServletResponse)} is given as <code>DispatcherType.FORWARD</code> or
     * <code>DispatcherType.INCLUDE</code>, respectively, while the
     * dispatcher type of an asynchronous request dispatched via
     * one of the {@link AsyncContext#dispatch} methods is given as
     * <code>DispatcherType.ASYNC</code>. Finally, the dispatcher type of a
     * request dispatched to an error page by the container's error handling
     * mechanism is given as <code>DispatcherType.ERROR</code>.
     *
     * @return the dispatcher type of this request
     * 
     * @see DispatcherType
     *
     * @since Servlet 3.0
     */
    // 获取该请求的调度程序类型。DispatcherType用来选择需要应用到请求的过滤器:只需要使用匹配的dispatcher类型和url模式的过滤器
    public DispatcherType getDispatcherType();
}

