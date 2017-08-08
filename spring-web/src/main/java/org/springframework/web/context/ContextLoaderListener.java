
package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ContextLoaderListener的作用就是启动Web容器时，自动装配ApplicationContext的配置信息。因为它实现了ServletContextListener接口，
 * 开发者能够在为客户端请求提供服务之前想ServletContext中添加任意的对象。这个对象在ServletContext启动的时候被初始化，然后在ServletContext整个运行期间都是可见的。
 *
 * ServletContextListener的使用：
 * 	   ServletContextListener是Java.servlet-api中的一个接口，在web应用启动的时候，会调用 ServletContextListener接口的实现类的contextInitialized（）方法，
 * 我们创建ServletContextListener，目的是在系统启动时添加自定义的属性，以便于在全局范围内可以随时调用。
 */
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

	private ContextLoader contextLoader;

	public ContextLoaderListener() {}
	public ContextLoaderListener(WebApplicationContext context) {
		super(context);
	}

	//implements ServletContextListener ...
	//该方法在web应用启动的时候被调用（也就是ServletContext启动之后被调用），并准备好处理客户端请求
	public void contextInitialized(ServletContextEvent event) {
		this.contextLoader = createContextLoader();
		if (this.contextLoader == null) {
			this.contextLoader = this;
		}
		//初始化 WebApplicationContext
		this.contextLoader.initWebApplicationContext(event.getServletContext());
	}
	@Deprecated
	protected ContextLoader createContextLoader() {
		return null;
	}
	@Deprecated
	public ContextLoader getContextLoader() {
		return this.contextLoader;
	}
	public void contextDestroyed(ServletContextEvent event) {
		if (this.contextLoader != null) {
			this.contextLoader.closeWebApplicationContext(event.getServletContext());
		}
		ContextCleanupListener.cleanupAttributes(event.getServletContext());
	}

}
