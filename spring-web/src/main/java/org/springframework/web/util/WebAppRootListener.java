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

package org.springframework.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener that sets a system property to the web application root directory.
 * The key of the system property can be defined with the "webAppRootKey" init
 * parameter at the servlet context level (i.e. context-param in web.xml),
 * the default key is "webapp.root".
 *
 * <p>Can be used for toolkits that support substition with system properties
 * (i.e. System.getProperty values), like log4j's "${key}" syntax within log
 * file locations.
 *
 * <p>Note: This listener should be placed before ContextLoaderListener in {@code web.xml},
 * at least when used for log4j. Log4jConfigListener sets the system property
 * implicitly, so there's no need for this listener in addition to it.
 *
 * <p><b>WARNING</b>: Some containers, e.g. Tomcat, do NOT keep system properties separate
 * per web app. You have to use unique "webAppRootKey" context-params per web app
 * then, to avoid clashes. Other containers like Resin do isolate each web app's
 * system properties: Here you can use the default key (i.e. no "webAppRootKey"
 * context-param at all) without worrying.
 *
 * <p><b>WARNING</b>: The WAR file containing the web application needs to be expanded
 * to allow for setting the web app root system property. This is by default not
 * the case when a WAR file gets deployed to WebLogic, for example. Do not use
 * this listener in such an environment!
 *
 * @author Juergen Hoeller
 * @since 18.04.2003
 * @see WebUtils#setWebAppRootSystemProperty
 * @see Log4jConfigListener
 * @see System#getProperty
 */

/*
WebAppRootListener:

这个listener的作用就是监听web.xml中的配置param-name为webAppRootKey的值：

<context-param>
        <param-name>webAppRootKey</param-name>
        <param-value>myroot</param-value>
</context-param>

然后配置一个然后配置一个监听器：

<listener>
        <listener-class>
            org.springframework.web.util.WebAppRootListener
        </listener-class>
</listener>

这个监听器会在web上下文初始化的时候，cs调用webUtil的对应方法，首先获取根传递进来的servletContext得到物理路径，String path=servletContext.getRealPath("/");  
然后找到context-param的webAooRootKey对应的param-value，把param-value的值作为key，上面配置的是"myroot", 接着执行System.setProperty("myroot",path)。
这样在web中就可以使用System.getProperty("myroot")来获取系统的绝对路径。

注：

1）如果只配置了监听器，没有配置webAppRootKey， 默认wenAppRootKey对应的param-value的值为webapp.root。
2）上面得到系统路径是Spring的做法，和平时自己采用的方法基本一致，都是写一个监听器，然后得到物理路径后手动放入System中，一般还会在这个监听器中加载配置文件，获取配置文件的值。

*/

public class WebAppRootListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		WebUtils.setWebAppRootSystemProperty(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event) {
		WebUtils.removeWebAppRootSystemProperty(event.getServletContext());
	}

}
