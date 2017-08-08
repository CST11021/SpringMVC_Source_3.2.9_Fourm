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

package org.springframework.web.context.support;

import java.io.File;
import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.WebUtils;

/**
 * Convenient superclass for application objects running in a WebApplicationContext.
 * Provides {@code getWebApplicationContext()}, {@code getServletContext()},
 * and {@code getTempDir()} methods.
 *
 * @author Juergen Hoeller
 * @since 28.08.2003
 * @see SpringBeanAutowiringSupport
 */
public abstract class WebApplicationObjectSupport extends ApplicationObjectSupport implements ServletContextAware {

	private ServletContext servletContext;

	public final void setServletContext(ServletContext servletContext) {
		if (servletContext != this.servletContext) {
			this.servletContext = servletContext;
			if (servletContext != null) {
				initServletContext(servletContext);
			}
		}
	}
	@Override
	protected void initApplicationContext(ApplicationContext context) {
		super.initApplicationContext(context);
		if (this.servletContext == null && context instanceof WebApplicationContext) {
			this.servletContext = ((WebApplicationContext) context).getServletContext();
			if (this.servletContext != null) {
				initServletContext(this.servletContext);
			}
		}
	}
	protected void initServletContext(ServletContext servletContext) {}

	@Override
	protected boolean isContextRequired() {
		return true;
	}

	protected final WebApplicationContext getWebApplicationContext() throws IllegalStateException {
		ApplicationContext ctx = getApplicationContext();
		if (ctx instanceof WebApplicationContext) {
			return (WebApplicationContext) getApplicationContext();
		}
		else if (isContextRequired()) {
			throw new IllegalStateException("WebApplicationObjectSupport instance [" + this + "] does not run in a WebApplicationContext but in: " + ctx);
		}
		else {
			return null;
		}
	}
	protected final ServletContext getServletContext() throws IllegalStateException {
		if (this.servletContext != null) {
			return this.servletContext;
		}
		ServletContext servletContext = getWebApplicationContext().getServletContext();
		if (servletContext == null && isContextRequired()) {
			throw new IllegalStateException("WebApplicationObjectSupport instance [" + this + "] does not run within a ServletContext. Make sure the object is fully configured!");
		}
		return servletContext;
	}

	// 返回当前web应用程序的临时目录，由servlet容器提供
	protected final File getTempDir() throws IllegalStateException {
		return WebUtils.getTempDir(getServletContext());
	}

}
