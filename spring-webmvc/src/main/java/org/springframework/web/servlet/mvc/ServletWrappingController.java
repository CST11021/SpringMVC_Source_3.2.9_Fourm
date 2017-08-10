/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.web.servlet.mvc;

import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring Controller implementation that wraps a servlet instance which it manages
 * internally. Such a wrapped servlet is not known outside of this controller;
 * its entire lifecycle is covered here (in contrast to {@link ServletForwardingController}).
 *
 * <p>Useful to invoke an existing servlet via Spring's dispatching infrastructure,
 * for example to apply Spring HandlerInterceptors to its requests.
 *
 * <p>Note that Struts has a special requirement in that it parses {@code web.xml}
 * to find its servlet mapping. Therefore, you need to specify the DispatcherServlet's
 * servlet name as "servletName" on this controller, so that Struts finds the
 * DispatcherServlet's mapping (thinking that it refers to the ActionServlet).
 *
 * <p><b>Example:</b> a DispatcherServlet XML context, forwarding "*.do" to the Struts
 * ActionServlet wrapped by a ServletWrappingController. All such requests will go
 * through the configured HandlerInterceptor chain (e.g. an OpenSessionInViewInterceptor).
 * From the Struts point of view, everything will work as usual.
 *
 * <pre class="code">
 * &lt;bean id="urlMapping" class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping"&gt;
 *   &lt;property name="interceptors"&gt;
 *     &lt;list&gt;
 *       &lt;ref bean="openSessionInViewInterceptor"/&gt;
 *     &lt;/list&gt;
 *   &lt;/property&gt;
 *   &lt;property name="mappings"&gt;
 *     &lt;props&gt;
 *       &lt;prop key="*.do"&gt;strutsWrappingController&lt;/prop&gt;
 *     &lt;/props&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * &lt;bean id="strutsWrappingController" class="org.springframework.web.servlet.mvc.ServletWrappingController"&gt;
 *   &lt;property name="servletClass"&gt;
 *     &lt;value&gt;org.apache.struts.action.ActionServlet&lt;/value&gt;
 *   &lt;/property&gt;
 *   &lt;property name="servletName"&gt;
 *     &lt;value&gt;action&lt;/value&gt;
 *   &lt;/property&gt;
 *   &lt;property name="initParameters"&gt;
 *     &lt;props&gt;
 *       &lt;prop key="config"&gt;/WEB-INF/struts-config.xml&lt;/prop&gt;
 *     &lt;/props&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;</pre>
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see ServletForwardingController
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor
 * @see org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter
 */
/*
  	ServletWrappingController：将当前应用中的某个Servlet直接包装为一个Controller，所有到ServletWrappingController的请求
  		实际上是由它内部所包装的这个Servlet 实例来处理的，也就是说内部封装的Servlet实例并不对外开放，对于程序的其他范围
  	是不可见的，适配所有的HTTP请求到内部封装的Servlet实例进行处理。它通常用于对已存的Servlet的逻辑重用。
  	ServletWrappingController是为了Struts专门设计的，作用相当于代理Struts的ActionServlet。请注意，Struts有一个特殊的要求，
  	因为它解析web.xml找到自己的Servlet映射。因此，你需要指定的DispatherServlet作为“servletName”在这个控制器servlet的名字，
  	认为这样的Struts的DispatcherServlet的映射 （它指的是ActionServlet的）。

	Servlet转发控制器(ServletForwardingController)：
		和ServletWrappingController类似，它也是一个Servlet相关的controller，他们都实现将HTTP请求适配到一个已存的Servlet实
	现。但是，简单Servlet处理器适配器需要在Web应用程序环境中定义Servlet Bean，并且Servlet没有机会进行初始化和析构。和
	ServletWrappingController不同的是，ServletForwardingController将所有的HTTP请求转发给一个在web.xml中定义的Servlet。
	Web容器会对这个定义在web.xml的标准Servlet进行初始化和析构。
 */
public class ServletWrappingController extends AbstractController implements BeanNameAware, InitializingBean, DisposableBean {

	private Class<?> servletClass;
	private String servletName;
	private Properties initParameters = new Properties();
	private String beanName;
	private Servlet servletInstance;


	// init --------------- 框架调用后处理方法来包装 Servlet 实例
	public void afterPropertiesSet() throws Exception {
		if (this.servletClass == null) {
			throw new IllegalArgumentException("servletClass is required");
		}
		if (!Servlet.class.isAssignableFrom(this.servletClass)) {
			throw new IllegalArgumentException("servletClass [" + this.servletClass.getName() + "] needs to implement interface [javax.servlet.Servlet]");
		}
		if (this.servletName == null) {
			this.servletName = this.beanName;
		}
		this.servletInstance = (Servlet) this.servletClass.newInstance();
		this.servletInstance.init(new DelegatingServletConfig());
	}
	// service ------------ 调用这个 Servlet 实例来处理请求
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		this.servletInstance.service(request, response);
		return null;
	}
	// destroy ------------ 销毁Servlet实例
	public void destroy() {
		this.servletInstance.destroy();
	}




	// Set the class of the servlet to wrap.
	public void setServletClass(Class<?> servletClass) {
		this.servletClass = servletClass;
	}
	// Set the name of the servlet to wrap. Default is the bean name of this controller.
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}
	// Specify init parameters for the servlet to wrap, as name-value pairs.
	public void setInitParameters(Properties initParameters) {
		this.initParameters = initParameters;
	}
	public void setBeanName(String name) {
		this.beanName = name;
	}



	/**
	 * Internal implementation of the ServletConfig interface, to be passed to the wrapped servlet.
	 * ServletConfig接口的内部实现，用于传递给包装的servlet
	 * Delegates to ServletWrappingController fields and methods to provide init parameters and other environment info.
	 * 代表ServletWrappingController字段和方法提供init参数和其他环境信息
	 */
	private class DelegatingServletConfig implements ServletConfig {

		public String getServletName() {
			return servletName;
		}
		public ServletContext getServletContext() {
			return ServletWrappingController.this.getServletContext();
		}
		public String getInitParameter(String paramName) {
			return initParameters.getProperty(paramName);
		}
		public Enumeration getInitParameterNames() {
			return initParameters.keys();
		}
	}


}
