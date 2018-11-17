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


/**
 什么是Servlet
     Servlet是一个基于Http协议用Java编写的程序，Servlet 是运行在 Web 服务器或应用服务器上的程序。

 Servlet有什么作用
     Servlet主要是处理客户端的请求并将其结果发送到客户端。它是作为来自 Web 浏览器或其他 HTTP 客户端的请求和 HTTP 服务器上的数据库或应用程序之间的中间层。使用 Servlet，您可以收集来自网页表单的用户输入，呈现来自数据库或者其他源的记录，还可以动态创建网页。Servlet 通常情况下与使用 CGI（Common Gateway Interface，公共网关接口）实现的程序可以达到异曲同工的效果。但是相比于 CGI，Servlet 有以下几点优势：
     性能明显更好。
     Servlet 在 Web 服务器的地址空间内执行。这样它就没有必要再创建一个单独的进程来处理每个客户端请求。
     Servlet 是独立于平台的，因为它们是用 Java 编写的。
     服务器上的 Java 安全管理器执行了一系列限制，以保护服务器计算机上的资源。因此，Servlet 是可信的。
     Java 类库的全部功能对 Servlet 来说都是可用的。它可以通过 sockets 和 RMI 机制与 applets、数据库或其他软件进行交互。

 Servlet的生命周期
    Servlet的生命周期是由Servlet的容器来控制的，它可以分为3个阶段;初始化，运行和销毁。

     初始化阶段：
         1、Servlet容器把servlet类的.class文件中的数据读到内存中。然后创建一个ServletConfig对象，该对象包含了Servlet的初始化配置信息。
         2、Servlet容器创建一个servlet对象。
         3、Servlet容器调用servlet对象的init方法进行初始化。
     运行阶段：
        当servlet容器接收到一个请求时，servlet容器会针对这个请求创建servletRequest和servletResponse对象。然后调用service方法。并把这两个参数传递给service方法。Service方法通过servletRequest对象获得请求的信息。并处理该请求。再通过servletResponse对象生成这个请求的响应结果。然后销毁servletRequest和servletResponse对象。我们不管这个请求是post提交的还是get提交的，最终这个请求都会由service方法来处理。
     销毁阶段：
        当Web应用被终止时，servlet容器会先调用servlet对象的destrory方法，然后再销毁servlet对象，同时也会销毁与servlet对象相关联的servletConfig对象。我们可以在destroy方法的实现中，释放servlet所占用的资源，如关闭数据库连接，关闭文件输入输出流等。
     在这里该注意的地方：
        在servlet生命周期中，servlet的初始化和和销毁阶段只会发生一次，而service方法执行的次数则取决于servlet被客户端访问的次数。

 Servlet如何处理一个请求
    当用户发送一个请求到某个Servlet的时候，Servlet容器会创建一个ServletRequst和ServletResponse对象。ServletRequst封装了用户的请求信息，然后Servlet容器把ServletRequst和ServletResponse对象传给用户所请求的Servlet，Servlet把处理好的结果写在ServletResponse中，然后Servlet容器把响应结果传给用户。

 Servlet与JSP有什么区别
    jsp经编译后就是servlet，jsp本质上就是servlet。jsp更擅长页面(表现)，servlet擅长逻辑编辑这是最核心的区别。在实际应用中采用Servlet来控制业务流程,而采用JSP来生成动态网页.在struts框架中,JSP位于MVC设计模式的视图层,而Servlet位于控制层。
 */
public interface Servlet {


    /**
     * 在servlet类创建后，servlet容器会调用Servlet的init方法，servlet容器只调用一次init方法，并且该方法必须在servlet接受请求之前
     * 执行完毕。可以通过覆盖该方法来写只要运行一次的初始化代码，例如：数据库驱动,值初始化等，通常留空。在调用这个方法时，
     * Servlet容器会传递一个ServletConfig。一般来说，会将ServletConfig赋给一个类级变量，以便Servlet类中的其他方法也可以使用这个对象。
     *
     * @param config
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException;

    /**
     * 当service方法在servlet请求时被容器调用并传递ServletRequest 和ServletResponse 对象，容器可以多次调用该方法。第一次请求Servlet时，
     * Servlet容器会调用init方法和service方法。对于后续的请求，则只调用service方法。
     *
     * @param req
     * @param res
     * @throws ServletException
     * @throws IOException
     */
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;


    /**
     * 当服务移除一个servlet实例的时候，servlet容器调用destroy方法。通常在servlet容器被关闭时或容器需要内容时被调用。一般来说，可以在这个方法编写一些资源清理相关的代码。
     */
    public void destroy();

    /**
     * 该方法返回有Servlet的描述。可以返回可能有用的任意字符串，甚至是null。
     *
     * @return
     */
    public String getServletInfo();

    /**
     * 该方法返回有Servlet容器传给init方法的ServletConfig。但是，为了让getServletConfig返回非null值，你肯定已经为传给init方法的ServletConfig赋给了一个类级变量。
     * @return
     */
    public ServletConfig getServletConfig();


}
