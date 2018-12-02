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

import java.beans.Introspector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.CachedIntrospectionResults;

/**
 * Listener that flushes the JDK's {@link java.beans.Introspector JavaBeans Introspector}
 * cache on web app shutdown. Register this listener in your {@code web.xml} to
 * guarantee proper release of the web application class loader and its loaded classes.
 *
 * <p><b>If the JavaBeans Introspector has been used to analyze application classes,
 * the system-level Introspector cache will hold a hard reference to those classes.
 * Consequently, those classes and the web application class loader will not be
 * garbage-collected on web app shutdown!</b> This listener performs proper cleanup,
 * to allow for garbage collection to take effect.
 *
 * <p>Unfortunately, the only way to clean up the Introspector is to flush
 * the entire cache, as there is no way to specifically determine the
 * application's classes referenced there. This will remove cached
 * introspection results for all other applications in the server too.
 *
 * <p>Note that this listener is <i>not</i> necessary when using Spring's beans
 * infrastructure within the application, as Spring's own introspection results
 * cache will immediately flush an analyzed class from the JavaBeans Introspector
 * cache and only hold a cache within the application's own ClassLoader.
 *
 * <b>Although Spring itself does not create JDK Introspector leaks, note that this
 * listener should nevertheless be used in scenarios where the Spring framework classes
 * themselves reside in a 'common' ClassLoader (such as the system ClassLoader).</b>
 * In such a scenario, this listener will properly clean up Spring's introspection cache.
 *
 * <p>Application classes hardly ever need to use the JavaBeans Introspector
 * directly, so are normally not the cause of Introspector resource leaks.
 * Rather, many libraries and frameworks do not clean up the Introspector:
 * e.g. Struts and Quartz.
 *
 * <p>Note that a single such Introspector leak will cause the entire web
 * app class loader to not get garbage collected! This has the consequence that
 * you will see all the application's static class resources (like singletons)
 * around after web app shutdown, which is not the fault of those classes!
 *
 * <p><b>This listener should be registered as the first one in {@code web.xml},
 * before any application listeners such as Spring's ContextLoaderListener.</b>
 * This allows the listener to take full effect at the right time of the lifecycle.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see java.beans.Introspector#flushCaches()
 * @see org.springframework.beans.CachedIntrospectionResults#acceptClassLoader
 * @see org.springframework.beans.CachedIntrospectionResults#clearClassLoader
 */

/*
Spring使用IntrospectorCleanupListener清理缓存

	这个监听器的作用是在web应用关闭时刷新JDK的JavaBeans的Introspector缓存，以确保Web应用程序的类加载器以及其加载的类正确的释放资源。

	如果JavaBeans的Introspector已被用来分析应用程序类，系统级的Introspector缓存将持有这些类的一个硬引用。因此，这些类和Web应用程序的类
	加载器在Web应用程序关闭时将不会被垃圾收集器回收！而IntrospectorCleanupListener则会对其进行适当的清理，已使其能够被垃圾收集器回收。

	唯一能够清理Introspector的方法是刷新整个Introspector缓存，没有其他办法来确切指定应用程序所引用的类。这将删除所有其他应用程序在服务器的缓存的Introspector结果。

	在使用Spring内部的bean机制时，不需要使用此监听器，因为Spring自己的introspection results cache将会立即刷新被分析过的JavaBeans Introspector cache，
	而仅仅会在应用程序自己的ClassLoader里面持有一个cache。虽然Spring本身不产生泄漏，注意，即使在Spring框架的类本身驻留在一个“共同”类加载器（如系统的ClassLoader）
	的情况下，也仍然应该使用使用IntrospectorCleanupListener。在这种情况下，这个IntrospectorCleanupListener将会妥善清理Spring的introspection cache。

	应用程序类，几乎不需要直接使用JavaBeans Introspector，所以，通常都不是Introspector resource造成内存泄露。相反，许多库和框架，不清理Introspector，
	例如： Struts和Quartz。

	需要注意的是一个简单Introspector泄漏将会导致整个Web应用程序的类加载器不会被回收！这样做的结果，将会是在web应用程序关闭时，该应用程序
	所有的静态类资源（比如：单实例对象）都没有得到释放。而导致内存泄露的根本原因其实并不是这些未被回收的类！

　　注意：IntrospectorCleanupListener应该注册为web.xml中的第一个Listener，在任何其他Listener之前注册，比如在Spring's ContextLoaderListener注册之前，才能确保IntrospectorCleanupListener在Web应用的生命周期适当时机生效。

	<!-- memory clean -->
	<listener>
		<listener-class>org.springframework.web.util.IntrospectorCleanupListener</listener-class>
	</listener>
 */

public class IntrospectorCleanupListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent event) {
		CachedIntrospectionResults.acceptClassLoader(Thread.currentThread().getContextClassLoader());
	}

	public void contextDestroyed(ServletContextEvent event) {
		CachedIntrospectionResults.clearClassLoader(Thread.currentThread().getContextClassLoader());
		Introspector.flushCaches();
	}

}
