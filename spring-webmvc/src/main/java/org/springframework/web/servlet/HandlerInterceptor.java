
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;


// Servlet API定义的servlet过滤器可以在servlet处理每个Web请求的前后分别对它进行前置处理和后置处理。
// 此外，有些时候，你可能指向处理由某些SpringMVC处理程序处理的Web请求，并在这些处理程序返回的模型属性被传递到视图之前，对它们进行一些操作。
//
// SpringMVC允许你通过处理拦截Web请求，进行前置处理和后置处理。处理拦截是在Spring的Web应用程序上下文中配置的，
// 因此它们可以利用各种容器特性，并引用容器中声明的任何bean。处理拦截是针对特殊的处理程序映射进行注册的，
// 因此它只拦截通过这些处理程序映射的请求。每个处理拦截都必须实现HandlerIntercept接口，它包含三个需要你实现的回调方法：
// preHandler()、postHandle()和afterCompletion()。第一个和第二个方法是在处理程序请求之前和之后被调用的。第二个方法还允许访问返回的ModelAndView对象，因此可以在它里面操作模型属性。
// 最后一个方法是在所有请求处理完成之后被调用的（如视图呈现之后）
public interface HandlerInterceptor {

	// 处理程序请求之前调用
	boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	// 处理程序请求之后调用
	void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception;

	// 在所有请求处理完成之后被调用的（如视图呈现之后）
	void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception;

}
