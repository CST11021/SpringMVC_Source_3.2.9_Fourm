
package org.springframework.web.context;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.springframework.context.ConfigurableApplicationContext;

public interface ConfigurableWebApplicationContext extends WebApplicationContext, ConfigurableApplicationContext {

	// Prefix for ApplicationContext ids that refer to context path and/or servlet name.
	String APPLICATION_CONTEXT_ID_PREFIX = WebApplicationContext.class.getName() + ":";
	// Name of the ServletConfig environment bean in the factory.
	String SERVLET_CONFIG_BEAN_NAME = "servletConfig";

	void setServletContext(ServletContext servletContext);

	void setServletConfig(ServletConfig servletConfig);
	ServletConfig getServletConfig();

	void setNamespace(String namespace);
	String getNamespace();

	void setConfigLocation(String configLocation);
	void setConfigLocations(String[] configLocations);
	String[] getConfigLocations();

}
