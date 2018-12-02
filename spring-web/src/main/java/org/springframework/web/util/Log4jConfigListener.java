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
 * Bootstrap listener for custom log4j initialization in a web environment.
 * Delegates to {@link Log4jWebConfigurer} (see its javadoc for configuration details).
 *
 * <b>WARNING: Assumes an expanded WAR file</b>, both for loading the configuration
 * file and for writing the log files. If you want to keep your WAR unexpanded or
 * don't need application-specific log files within the WAR directory, don't use
 * log4j setup within the application (thus, don't use Log4jConfigListener or
 * Log4jConfigServlet). Instead, use a global, VM-wide log4j setup (for example,
 * in JBoss) or JDK 1.4's {@code java.util.logging} (which is global too).
 *
 * <p>This listener should be registered before ContextLoaderListener in {@code web.xml}
 * when using custom log4j initialization.
 *
 * @author Juergen Hoeller
 * @since 13.03.2003
 * @see Log4jWebConfigurer
 * @see org.springframework.web.context.ContextLoaderListener
 * @see WebAppRootListener
 */

/*
Spring使用Log4jConfigListener配置Log4j日志

Spring使用Log4jConfigListener的好处：

	1、动态的改变记录级别和策略，不需要重启Web应用。
	2、把log文件定在 /WEB-INF/logs/ 而不需要写绝对路径。因为系统把web目录的路径压入一个叫webapp.root的系统变量。这样写log文件路径时不用写绝对路径了。
	3、可以把log4j.properties和其他properties一起放在/WEB-INF/ ，而不是Class-Path。
	4、设置log4jRefreshInterval时间，开一条watchdog线程每隔段时间扫描一下配置文件的变化。

	<context-param>
		<param-name>webAppRootKey</param-name>
		<param-value>project.root</param-value><!-- 用于定位log文件输出位置在web应用根目录下，log4j配置文件中写输出位置：log4j.appender.FILE.File=${project.root}/logs/project.log -->
	</context-param>
	<context-param>
		<param-name>log4jConfigLocation</param-name>
		<param-value>classpath:log4j.properties</param-value><!-- 载入log4j配置文件 -->
	</context-param>
	<context-param>
		<param-name>log4jRefreshInterval</param-name>
		<param-value>60000</param-value><!--Spring刷新Log4j配置文件的间隔60秒,单位为millisecond-->
	</context-param>

	<listener>
		<listener-class>org.springframework.web.util.Log4jConfigListener</listener-class>
	</listener>
 */

public class Log4jConfigListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		Log4jWebConfigurer.initLogging(event.getServletContext());
	}

	public void contextDestroyed(ServletContextEvent event) {
		Log4jWebConfigurer.shutdownLogging(event.getServletContext());
	}

}
