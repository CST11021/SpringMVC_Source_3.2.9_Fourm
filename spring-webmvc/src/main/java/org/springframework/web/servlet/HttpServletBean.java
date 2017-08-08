
package org.springframework.web.servlet;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.ServletContextResourceLoader;
import org.springframework.web.context.support.StandardServletEnvironment;


//EnvironmentCapable接口提供获取Envirionment的方法，EnvironmentAware接口提供设置Envirionment的方法
@SuppressWarnings("serial")
public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware {
	protected final Log logger = LogFactory.getLog(getClass());

	// 用于存放<init-param>中必须指定的参数，用户可以通过requiredProperties参数的初始化来强制验证某些属性的必要性，这样，在属性封装的过程中，一旦检测到 requiredProperties 中的属性没有指定初始值，就会抛出异常。
	private final Set<String> requiredProperties = new HashSet<String>();
	private ConfigurableEnvironment environment;







	/* ----- Web容器启动的时候会自动执行Servlet的初始化方法---------------------------------------------------------- */

	// 重写父类HttpServlet的方法，Web容器启动的时候会自动来执行该方法
	@Override
	public final void init() throws ServletException {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing servlet '" + getServletName() + "'");
		}

		// Set bean properties from init parameters.
		try {
			// 解析<init-param>并封装到 pvs 中
			PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
			// 将当前的这个Servlet类转化为一个BeanWrapper，从而能够以Spring的方式来对init-param的值进行注入，这些属性如contextAttribute、contextClass、nameSpace、contextConfigLocation等，都可以在web.xml中以初始化参数的方式配置
			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
			ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
			//注册属性编辑器，一旦遇到Resource类型的属性将会使用ResourceEditor进行解析
			bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
			//空实现，留给子类覆盖
			initBeanWrapper(bw);
			//属性注入
			bw.setPropertyValues(pvs, true);
		}
		catch (BeansException ex) {
			logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
			throw ex;
		}

		// Let subclasses do whatever initialization they like.
		initServletBean();

		if (logger.isDebugEnabled()) {
			logger.debug("Servlet '" + getServletName() + "' configured successfully");
		}
	}
	protected void initBeanWrapper(BeanWrapper bw) throws BeansException {}
	//空实现留给子类覆盖该方法
	protected void initServletBean() throws ServletException {}
	/* ----- Web容器启动的时候会自动执行Servlet的初始化方法---------------------------------------------------------- */








	// 重写父类方法，返回这个DispatcherServlet的名称
	@Override
	public final String getServletName() {
		return (getServletConfig() != null ? getServletConfig().getServletName() : null);
	}
	// 重写父类方法，获取web上下文
	@Override
	public final ServletContext getServletContext() {
		return (getServletConfig() != null ? getServletConfig().getServletContext() : null);
	}


	// implements EnvironmentAware 接口方法
	public void setEnvironment(Environment environment) {
		Assert.isInstanceOf(ConfigurableEnvironment.class, environment);
		this.environment = (ConfigurableEnvironment) environment;
	}
	//implements EnvironmentCapable 接口方法
	public ConfigurableEnvironment getEnvironment() {
		if (this.environment == null) {
			this.environment = this.createEnvironment();
		}
		return this.environment;
	}
	protected ConfigurableEnvironment createEnvironment() {
		return new StandardServletEnvironment();
	}


	protected final void addRequiredProperty(String property) {
		this.requiredProperties.add(property);
	}

	// ServletConfigPropertyValues用于封装及验证初始化参数
	private static class ServletConfigPropertyValues extends MutablePropertyValues {
		// 封装属性主要是对初始化的参数进行封装，也就是servlet中配置的 <init-param> 中配置的封装。
		// 用户可以通过requiredProperties参数的初始化来强制验证某些属性的必要性，这样，在属性封装的过程中，一旦检测到 requiredProperties 中的属性没有指定初始值，就会抛出异常。
		public ServletConfigPropertyValues(ServletConfig config, Set<String> requiredProperties) throws ServletException {

			Set<String> missingProps = (requiredProperties != null && !requiredProperties.isEmpty()) ?
					new HashSet<String>(requiredProperties) : null;

			Enumeration en = config.getInitParameterNames();
			while (en.hasMoreElements()) {
				String property = (String) en.nextElement();
				Object value = config.getInitParameter(property);
				addPropertyValue(new PropertyValue(property, value));
				if (missingProps != null) {
					missingProps.remove(property);
				}
			}

			// 如果缺少必要的初始化参数就抛出异常
			// Fail if we are still missing properties.
			if (missingProps != null && missingProps.size() > 0) {
				throw new ServletException("Initialization from ServletConfig for servlet '" + config.getServletName() + "' failed; the following required properties were missing: " +
					StringUtils.collectionToDelimitedString(missingProps, ", "));
			}
		}
	}

}
