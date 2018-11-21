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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * <p>Trivial controller that always returns a named view. The view
 * can be configured using an exposed configuration property. This
 * controller offers an alternative to sending a request straight to a view
 * such as a JSP. The advantage here is that the client is not exposed to
 * the concrete view technology but rather just to the controller URL;
 * the concrete view will be determined by the ViewResolver.
 *
 * <p>An alternative to the ParameterizableViewController is a
 * {@link org.springframework.web.servlet.mvc.multiaction.MultiActionController MultiActionController},
 * which can define a variety of handler methods that just return a plain
 * ModelAndView instance for a given view name.
 *
 * <p><b><a name="workflow">Workflow
 * (<a href="AbstractController.html#workflow">and that defined by superclass</a>):</b><br>
 * <ol>
 *  <li>Request is received by the controller</li>
 *  <li>call to {@link #handleRequestInternal handleRequestInternal} which
 *      just returns the view, named by the configuration property
 *      {@code viewName}. Nothing more, nothing less</li>
 * </ol>
 * </p>
 *
 * <p><b><a name="config">Exposed configuration properties</a>
 * (<a href="AbstractController.html#config">and those defined by superclass</a>):</b><br>
 * <table border="1">
 *  <tr>
 *      <td><b>name</b></td>
 *      <td><b>default</b></td>
 *      <td><b>description</b></td>
 *  </tr>
 *  <tr>
 *      <td>viewName</td>
 *      <td><i>null</i></td>
 *      <td>the name of the view the viewResolver will use to forward to
 *          (if this property is not set, a null view name will be returned
 *          directing the caller to calculate the view name from the current request)</td>
 *  </tr>
 * </table>
 * </p>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Keith Donald
 *

参数化视图控制器，不进行功能处理（即静态视图），根据参数的逻辑视图名直接选择需要展示的视图。
该控制器接收到请求后直接选择参数化的视图，这样的好处是在配置文件中配置，从而避免程序的硬编码，比如像帮助页面等不需要进行功能处理，因此直接使用该控制器映射到视图。

例如：
<bean name="/parameterizableView" class="org.springframework.web.servlet.mvc.ParameterizableViewController">
	<property name="viewName" value="success"/>
</bean>

 */
public class ParameterizableViewController extends AbstractController {

	private String viewName;

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}
	public String getViewName() {
		return this.viewName;
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new ModelAndView(getViewName(), RequestContextUtils.getInputFlashMap(request));
	}

}
