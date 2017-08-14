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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;


/**
 * Defines an object to assist a servlet in sending a response to the client.
 * The servlet container creates a <code>ServletResponse</code> object and
 * passes it as an argument to the servlet's <code>service</code> method.
 *
 * <p>To send binary data in a MIME body response, use
 * the {@link ServletOutputStream} returned by {@link #getOutputStream}.
 * To send character data, use the <code>PrintWriter</code> object 
 * returned by {@link #getWriter}. To mix binary and text data,
 * for example, to create a multipart response, use a
 * <code>ServletOutputStream</code> and manage the character sections
 * manually.
 *
 * <p>The charset for the MIME body response can be specified
 * explicitly using the {@link #setCharacterEncoding} and
 * {@link #setContentType} methods, or implicitly
 * using the {@link #setLocale} method.
 * Explicit specifications take precedence over
 * implicit specifications. If no charset is specified, ISO-8859-1 will be
 * used. The <code>setCharacterEncoding</code>,
 * <code>setContentType</code>, or <code>setLocale</code> method must
 * be called before <code>getWriter</code> and before committing
 * the response for the character encoding to be used.
 * 
 * <p>See the Internet RFCs such as 
 * <a href="http://www.ietf.org/rfc/rfc2045.txt">
 * RFC 2045</a> for more information on MIME. Protocols such as SMTP
 * and HTTP define profiles of MIME, and those standards
 * are still evolving.
 *
 * @author 	Various
 *
 * @see		ServletOutputStream
 *
 */
// 该接口表示一个Servlet响应。在调用一个Servlet的service方法之前，Servlet容器会先创建一个ServletResponse，并将它作为第二
// 个参数传给service方法。ServletResponse隐藏了将响应发给浏览器的复杂性。

// ServletResponse中定义了其中一个方法是getWriter 方法，它返回可以将文本传给客户端的java.io.PrintWriter。在默认情况下，
// ProintWriter对象采用ISO-8859-1编码。在将响应发送给客户端时，通常将它作为HTML发送。还有一个方法可以用来将输出传给浏览器：
// getOutputStream。但是，这个方法是用来传输二进制数据的，因此，在大多数时候，需要使用getWriter，而不是getOutputStream。
public interface ServletResponse {

    // 返回在此响应中发送的主体的字符编码(MIME字符集)，例如：UTF-8
    public String getCharacterEncoding();
    public void setCharacterEncoding(String charset);

    // 返回在此响应中发送的MIME主体所使用的内容类型，response.setContentType(MIME)的作用是使客户端浏览器，区分不同种类的
    // 数据，并根据不同的MIME调用浏览器内不同的程序嵌入模块来处理相应的数据。例如web浏览器就是通过MIME类型来判断文件是GIF
    // 图片。通过MIME类型来处理json字符串。
    public String getContentType();
    public void setContentType(String type);

    public ServletOutputStream getOutputStream() throws IOException;
    public PrintWriter getWriter() throws IOException;

    // 在HTTP servlet中设置内容主体的长度，该方法设置 HTTP Content-Length header
    public void setContentLength(int len);

    // 为响应的主体设置首选缓冲区大小
    public void setBufferSize(int size);
    public int getBufferSize();

    // 将缓冲区中的任何内容写入到客户端。对该方法的调用将自动提交响应，这意味着将写入状态代码和头文件
    public void flushBuffer() throws IOException;
    // 在响应中清除底层缓冲区的内容，而不需要清除标头或状态代码
    public void resetBuffer();

    // 判断响应是否已经提交。一个提交的响应已经有了它的状态代码和头文件
    public boolean isCommitted();

    // 清除缓冲区中存在的任何数据，以及状态代码和头文件，如果该response已经提交，则抛出 IllegalStateException 异常
    public void reset();

    public void setLocale(Locale loc);
    public Locale getLocale();



}





