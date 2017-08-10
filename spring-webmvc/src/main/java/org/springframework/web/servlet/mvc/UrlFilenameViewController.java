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

package org.springframework.web.servlet.mvc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Simple {@code Controller} implementation that transforms the virtual
 * path of a URL into a view name and returns that view.
 *
 * <p>Can optionally prepend a {@link #setPrefix prefix} and/or append a
 * {@link #setSuffix suffix} to build the viewname from the URL filename.
 *
 * <p>Find below some examples:
 *
 * <ol>
 *   <li>{@code "/index" -> "index"}</li>
 *   <li>{@code "/index.html" -> "index"}</li>
 *   <li>{@code "/index.html"} + prefix {@code "pre_"} and suffix {@code "_suf" -> "pre_index_suf"}</li>
 *   <li>{@code "/products/view.html" -> "products/view"}</li>
 * </ol>
 *
 * <p>Thanks to David Barri for suggesting prefix/suffix support!
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see #setPrefix
 * @see #setSuffix
 */
/*
	将请求的URL路径转换为逻辑视图名并返回的转换控制器，即不需要功能处理，直接根据URL计算出逻辑视图名，并选择具体视图进行展示：

	根据请求URL路径计算逻辑视图名，如：
	<bean name="/index1/*"
	class="org.springframework.web.servlet.mvc.UrlFilenameViewController"/>
	<bean name="/index2/**"
	class="org.springframework.web.servlet.mvc.UrlFilenameViewController"/>
	<bean name="/*.html"
	class="org.springframework.web.servlet.mvc.UrlFilenameViewController"/>
	<bean name="/index3/*.html"
	class="org.springframework.web.servlet.mvc.UrlFilenameViewController"/>
	/index1/*：可以匹配/index1/demo，但不匹配/index1/demo/demo，如/index1/demo逻辑视图名为demo；
	/index2/**：可以匹配/index2路径下的所有子路径，如匹配/index2/demo，或/index2/demo/demo，“/index2/demo”的逻辑视图名为demo，而“/index2/demo/demo”逻辑视图名为demo/demo；
	/*.html：可以匹配如/abc.html，逻辑视图名为abc，后缀会被删除（不仅仅可以是html）；
	/index3/*.html：可以匹配/index3/abc.html，逻辑视图名也是abc;
 */
public class UrlFilenameViewController extends AbstractUrlViewController {

	private String prefix = "";
	private String suffix = "";

	//** Request URL path String --> view name String */
	private final Map<String, String> viewNameCache = new ConcurrentHashMap<String, String>(256);


	// 根据 request 的url结合视图前后缀名返回视图名
	@Override
	protected String getViewNameForRequest(HttpServletRequest request) {
		String uri = extractOperableUrl(request);
		return getViewNameForUrlPath(uri);
	}
	// 从给定的请求中提取URL路径
	protected String extractOperableUrl(HttpServletRequest request) {
		String urlPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		if (!StringUtils.hasText(urlPath)) {
			urlPath = getUrlPathHelper().getLookupPathForRequest(request);
		}
		return urlPath;
	}
	protected String getViewNameForUrlPath(String uri) {
		String viewName = this.viewNameCache.get(uri);
		if (viewName == null) {
			viewName = extractViewNameFromUrlPath(uri);
			viewName = postProcessViewName(viewName);
			this.viewNameCache.put(uri, viewName);
		}
		return viewName;
	}
	protected String extractViewNameFromUrlPath(String uri) {
		int start = (uri.charAt(0) == '/' ? 1 : 0);
		int lastIndex = uri.lastIndexOf(".");
		int end = (lastIndex < 0 ? uri.length() : lastIndex);
		return uri.substring(start, end);
	}
	// 返回完整的视图路径
	protected String postProcessViewName(String viewName) {
		return getPrefix() + viewName + getSuffix();
	}



	// getter and setter ...
	public void setPrefix(String prefix) {
		this.prefix = (prefix != null ? prefix : "");
	}
	protected String getPrefix() {
		return this.prefix;
	}
	public void setSuffix(String suffix) {
		this.suffix = (suffix != null ? suffix : "");
	}
	protected String getSuffix() {
		return this.suffix;
	}

}
