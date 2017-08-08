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

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;

/**
 * Convenient superclass for application objects that want to be aware of
 * the application context, e.g. for custom lookup of collaborating beans
 * or for context-specific resource access. It saves the application
 * context reference and provides an initialization callback method.
 * Furthermore, it offers numerous convenience methods for message lookup.
 *
 * <p>There is no requirement to subclass this class: It just makes things
 * a little easier if you need access to the context, e.g. for access to
 * file resources or to the message source. Note that many application
 * objects do not need to be aware of the application context at all,
 * as they can receive collaborating beans via bean references.
 *
 * <p>Many framework classes are derived from this class, particularly
 * within the web support.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.web.context.support.WebApplicationObjectSupport
 */
public abstract class ApplicationObjectSupport implements ApplicationContextAware {
	protected final Log logger = LogFactory.getLog(getClass());

	//** ApplicationContext this object runs in */
	private ApplicationContext applicationContext;
	//** MessageSourceAccessor for easy message access */
	private MessageSourceAccessor messageSourceAccessor;



	public final void setApplicationContext(ApplicationContext context) throws BeansException {
		if (context == null && !isContextRequired()) {
			// Reset internal context state.
			this.applicationContext = null;
			this.messageSourceAccessor = null;
		}
		else if (this.applicationContext == null) {
			// Initialize with passed-in context.
			if (!requiredContextClass().isInstance(context)) {
				throw new ApplicationContextException(
						"Invalid application context: needs to be of type [" + requiredContextClass().getName() + "]");
			}
			this.applicationContext = context;
			this.messageSourceAccessor = new MessageSourceAccessor(context);
			initApplicationContext(context);
		}
		else {
			// Ignore reinitialization if same context passed in.
			if (this.applicationContext != context) {
				throw new ApplicationContextException(
						"Cannot reinitialize with different application context: current one is [" +
						this.applicationContext + "], passed-in one is [" + context + "]");
			}
		}
	}
	public final ApplicationContext getApplicationContext() throws IllegalStateException {
		if (this.applicationContext == null && isContextRequired()) {
			throw new IllegalStateException("ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.applicationContext;
	}
	protected void initApplicationContext(ApplicationContext context) throws BeansException {
		initApplicationContext();
	}
	protected void initApplicationContext() throws BeansException {}

	// 确定这个应用程序对象是否需要在ApplicationContext中运行
	// 默认false，可以覆盖在上下文中的强制执行（例如，如果在上下文之外，在访问器上抛出非法状态异常）
	protected boolean isContextRequired() {
		return false;
	}


	protected Class requiredContextClass() {
		return ApplicationContext.class;
	}


	protected final MessageSourceAccessor getMessageSourceAccessor() throws IllegalStateException {
		if (this.messageSourceAccessor == null && isContextRequired()) {
			throw new IllegalStateException("ApplicationObjectSupport instance [" + this + "] does not run in an ApplicationContext");
		}
		return this.messageSourceAccessor;
	}

}
