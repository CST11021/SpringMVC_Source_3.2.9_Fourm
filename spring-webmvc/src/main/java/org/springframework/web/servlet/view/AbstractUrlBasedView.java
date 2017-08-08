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

package org.springframework.web.servlet.view;

import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract base class for URL-based views. Provides a consistent way of
 * holding the URL that a View wraps, in the form of a "url" bean property.
 *
 * @author Juergen Hoeller
 * @since 13.12.2003
 */
public abstract class AbstractUrlBasedView extends AbstractView implements InitializingBean {

	// 这个URL表示这个视图对象在所在的web应用中的路径，比如：/testPage.jsp
	private String url;

	protected AbstractUrlBasedView() {}
	protected AbstractUrlBasedView(String url) {
		this.url = url;
	}



	// 设置该视图所包装的资源的url，这个url必须有对应的具体的视图实现
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return this.url;
	}

	public void afterPropertiesSet() throws Exception {
		if (isUrlRequired() && getUrl() == null) {
			throw new IllegalArgumentException("Property 'url' is required");
		}
	}


	// 返回是否需要“url”属性，默认为ture
	protected boolean isUrlRequired() {
		return true;
	}

	/**
	 * Check whether the underlying resource that the configured URL points to actually exists.
	 * @param locale the desired Locale that we're looking for
	 * @return {@code true} if the resource exists (or is assumed to exist);
	 * {@code false} if we know that it does not exist
	 * @throws Exception if the resource exists but is invalid (e.g. could not be parsed)
	 */
	// 检查配置的URL所指向的底层资源是否实际存在
	public boolean checkResource(Locale locale) throws Exception {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append("; URL [").append(getUrl()).append("]");
		return sb.toString();
	}

}
