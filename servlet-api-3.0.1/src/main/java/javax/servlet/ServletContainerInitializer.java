/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 */

package javax.servlet;

import java.util.Set;

/**
 * Interface which allows a library/runtime to be notified of a web
 * application's startup phase and perform any required programmatic
 * registration of servlets, filters, and listeners in response to it.
 *
 * <p>Implementations of this interface may be annotated with
 * {@link javax.servlet.annotation.HandlesTypes HandlesTypes}, in order to
 * receive (at their {@link #onStartup} method) the Set of application
 * classes that implement, extend, or have been annotated with the class
 * types specified by the annotation.
 * 
 * <p>If an implementation of this interface does not use this annotation,
 * or none of the application classes match the ones specified
 * by the annotation, the container must pass a <tt>null</tt> Set of classes
 * to {@link #onStartup}.
 *
 * <p>When examining the classes of an application to
 * see if they match any of the criteria specified by the HandlesTypes
 * annotation of a ServletContainerInitializer, the container may run into
 * classloading problems if any of the application's optional JAR
 * files are missing. Because the container is not in a position to decide
 * whether these types of classloading failures will prevent
 * the application from working correctly, it must ignore them,
 * while at the same time providing a configuration option that would
 * log them. 
 *
 * <p>Implementations of this interface must be declared by a JAR file
 * resource located inside the <tt>META-INF/services</tt> directory and
 * named for the fully qualified class name of this interface, and will be 
 * discovered using the runtime's service provider lookup mechanism
 * or a container specific mechanism that is semantically equivalent to
 * it. In either case, ServletContainerInitializer services from web
 * fragment JAR files excluded from an absolute ordering must be ignored,
 * and the order in which these services are discovered must follow the
 * application's classloading delegation model.
 *
 * @see javax.servlet.annotation.HandlesTypes
 *
 * @since Servlet 3.0
 */
/*
一、概述
    ServletContainerInitializer 是 Servlet 3.0 新增的一个接口，主要用于在容器启动阶段通过编程风格注册Filter, Servlet以及Listener，
    以取代通过web.xml配置注册。这样就利于开发内聚的web应用框架.我们以SpringMVC举例, servlet3.0之前我们需要在web.xml中依据Spring的规范新建一堆配置。
    这样就相当于将框架和容器紧耦合了。而在3.x后注册的功能内聚到Spring里，Spring-web就变成一个纯粹的即插即用的组件，不用依据应用环境定义一套新的配置。

二、原理
    ServletContainerInitializer接口的实现类通过java SPI声明自己是ServletContainerInitializer 的provider.
    容器启动阶段依据java spi获取到所有ServletContainerInitializer的实现类，然后执行其onStartup方法.
    另外在实现ServletContainerInitializer时还可以通过@HandlesTypes注解定义本实现类希望处理的类型，容器会将当前应用中所有这一类型（继承或者实现）
    的类放在ServletContainerInitializer接口的集合参数c中传递进来。如果不定义处理类型，或者应用中不存在相应的实现类，则集合参数c为空.
    这一类实现了 SCI 的接口，如果做为独立的包发布，在打包时，会在JAR 文件的 META-INF/services/javax.servlet.ServletContainerInitializer
    文件中进行注册。 容器在启动时，就会扫描所有带有这些注册信息的类(@HandlesTypes(WebApplicationInitializer.class)这里就是加载WebApplicationInitializer.class类)
    进行解析，启动时会调用其 onStartup方法——也就是说servlet容器负责加载这些指定类, 而ServletContainerInitializer的实现者(例如Spring-web
    中的SpringServletContainerInitializer对接口ServletContainerInitializer的实现中,是可以直接获取到这些类的)


三、Tomcat调用ServletContainerInitializer的时机：
    现在还有一个疑问, 就是 ServletContainerInitializer 的调用时机?, 因为servlet容器除了会回调ServletContainerInitializer之外,
    还有回调诸如servlet, listener等.
    搞清楚这些先后顺序可以帮助我们快速定位和理解某些奇怪的问题.

    这里我们就以Tomcat举例, 以下逻辑总结于Tomcat7.x, 有兴趣的读者可以去StandardContext类中对startInternal的实现中(第5608行 —— 第5618行,
    这也是Tomcat中唯一的调用ServletContainerInitializers接口的onStartup方法的位置)求证下:

    1、解析web.xml
    2、往ServletContext实例中注入<context-param> 参数
    3、回调Servlet3.0的ServletContainerInitializers接口实现类
    4、触发 Listener 事件(beforeContextInitialized, afterContextInitialized); 这里只会触发 ServletContextListener 类型的
    5、初始化 Filter, 调用其init方法
    6、加载 启动时即加载的servlet
 */

public interface ServletContainerInitializer {

    /**
     * Notifies this <tt>ServletContainerInitializer</tt> of the startup
     * of the application represented by the given <tt>ServletContext</tt>.
     *
     * <p>If this <tt>ServletContainerInitializer</tt> is bundled in a JAR
     * file inside the <tt>WEB-INF/lib</tt> directory of an application,
     * its <tt>onStartup</tt> method will be invoked only once during the
     * startup of the bundling application. If this
     * <tt>ServletContainerInitializer</tt> is bundled inside a JAR file
     * outside of any <tt>WEB-INF/lib</tt> directory, but still
     * discoverable as described above, its <tt>onStartup</tt> method
     * will be invoked every time an application is started.
     *
     * @param c the Set of application classes that extend, implement, or
     * have been annotated with the class types specified by the 
     * {@link javax.servlet.annotation.HandlesTypes HandlesTypes} annotation,
     * or <tt>null</tt> if there are no matches, or this
     * <tt>ServletContainerInitializer</tt> has not been annotated with
     * <tt>HandlesTypes</tt>
     *
     * @param ctx the <tt>ServletContext</tt> of the web application that
     * is being started and in which the classes contained in <tt>c</tt>
     * were found
     *
     * @throws ServletException if an error has occurred
     */
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException;
}
