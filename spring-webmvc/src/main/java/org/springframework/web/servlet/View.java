
package org.springframework.web.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;

/**
  	View接口是SpringMVC中将原本存在于DispatcherServlet中的视图渲染逻辑得以剥离出来的关键组件。通过引入该策略抽象接口，
 我们可以极具灵活地支持各种视图渲染技术。各种View实现类主要的职责就是在render(..)方法中实现最终的视图渲染工作，但这些
 对于DispatcherServlet来说是透明的，DispatcherServlet只是直接接触ViewResolver所返回的View接口，获得相应引用后把视图渲
 染工作转交给返回的View实例即可。至于该View实例是什么类型，具体如何完成工作，DispatcherServlet是无须关心的。
 */
public interface View {

	/**
	 * Name of the {@link HttpServletRequest} attribute that contains the response status code.
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 */
	String RESPONSE_STATUS_ATTRIBUTE = View.class.getName() + ".responseStatus";// org.springframework.web.servlet.View.responseStatus
	/**
	 * Name of the {@link HttpServletRequest} attribute that contains a Map with path variables.
	 * The map consists of String-based URI template variable names as keys and their corresponding
	 * Object-based values -- extracted from segments of the URL and type converted.
	 *
	 * <p>Note: This attribute is not required to be supported by all View implementations.
	 */
	String PATH_VARIABLES = View.class.getName() + ".pathVariables";// org.springframework.web.servlet.View.pathVariables
	/**
	 * The {@link MediaType} selected during content negotiation, which may be
	 * more specific than the one the View is configured with. For example:
	 * "application/vnd.example-v1+xml" vs "application/*+xml".
	 */
	String SELECTED_CONTENT_TYPE = View.class.getName() + ".selectedContentType";// org.springframework.web.servlet.View.selectedContentType

	// 视图对应的MIME类型，如text/html、image/jpeg等等
	String getContentType();

	// 将模型数据以某种MIME类型渲染出来
	void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
