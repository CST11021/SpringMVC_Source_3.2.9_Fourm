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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * ServletRequest 对象的一个包装类
 *
 * Provides a convenient implementation of the ServletRequest interface that
 * can be subclassed by developers wishing to adapt the request to a Servlet.
 * This class implements the Wrapper or Decorator pattern. Methods default to
 * calling through to the wrapped request object.
 *
 * @see javax.servlet.ServletRequest
 *
 * @since Servlet 2.3
 */
public class ServletRequestWrapper implements ServletRequest {

    /** 表示一个请求 */
    private ServletRequest request;

    public ServletRequestWrapper(ServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");   
        }
        this.request = request;
    }


    public ServletRequest getRequest() {
        return this.request;
    }
    public void setRequest(ServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        this.request = request;
    }

    public Object getAttribute(String name) {
        return this.request.getAttribute(name);
    }
    public Enumeration<String> getAttributeNames() {
        return this.request.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
        this.request.setCharacterEncoding(enc);
    }

    public int getContentLength() {
        return this.request.getContentLength();
    }
    public String getContentType() {
        return this.request.getContentType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return this.request.getInputStream();
    }

    public String getParameter(String name) {
        return this.request.getParameter(name);
    }
    public Map<String, String[]> getParameterMap() {
        return this.request.getParameterMap();
    }

    public Enumeration<String> getParameterNames() {
        return this.request.getParameterNames();
    }
    public String[] getParameterValues(String name) {
        return this.request.getParameterValues(name);
    }


    public String getProtocol() {
        return this.request.getProtocol();
    }

    public String getScheme() {
        return this.request.getScheme();
    }


    public String getServerName() {
        return this.request.getServerName();
    }
    public int getServerPort() {
        return this.request.getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        return this.request.getReader();
    }

    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }
    public String getRemoteHost() {
        return this.request.getRemoteHost();
    }

    public void setAttribute(String name, Object o) {
        this.request.setAttribute(name, o);
    }
    public void removeAttribute(String name) {
        this.request.removeAttribute(name);
    }

    public Locale getLocale() {
        return this.request.getLocale();
    }
    public Enumeration<Locale> getLocales() {
        return this.request.getLocales();
    }

    public boolean isSecure() {
        return this.request.isSecure();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return this.request.getRequestDispatcher(path);
    }

    public String getRealPath(String path) {
        return this.request.getRealPath(path);
    }

    public int getRemotePort(){
        return this.request.getRemotePort();
    }

    public String getLocalName(){
        return this.request.getLocalName();
    }
    public String getLocalAddr(){
        return this.request.getLocalAddr();
    }
    public int getLocalPort(){
        return this.request.getLocalPort();
    }

    public ServletContext getServletContext() {
        return request.getServletContext();
    }


    public AsyncContext startAsync() throws IllegalStateException {
        return request.startAsync();
    }
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return request.startAsync(servletRequest, servletResponse);
    }
    public boolean isAsyncStarted() {
        return request.isAsyncStarted();
    }
    public boolean isAsyncSupported() {
        return request.isAsyncSupported();
    }
    public AsyncContext getAsyncContext() {
        return request.getAsyncContext();
    }

    public DispatcherType getDispatcherType() {
        return request.getDispatcherType();
    }

    // 判断是否为ServletRequestWrapper类型的对象
    public boolean isWrapperFor(ServletRequest wrapped) {
        if (request == wrapped) {
            return true;
        } else if (request instanceof ServletRequestWrapper) {
            return ((ServletRequestWrapper) request).isWrapperFor(wrapped);
        } else {
            return false;
        }
    }
    public boolean isWrapperFor(Class wrappedType) {
        if (!ServletRequest.class.isAssignableFrom(wrappedType)) {
            throw new IllegalArgumentException("Given class " +
                wrappedType.getName() + " not a subinterface of " +
                ServletRequest.class.getName());
        }
        if (wrappedType.isAssignableFrom(request.getClass())) {
            return true;
        } else if (request instanceof ServletRequestWrapper) {
            return ((ServletRequestWrapper) request).isWrapperFor(wrappedType);
        } else {
            return false;
        }
    }




}

