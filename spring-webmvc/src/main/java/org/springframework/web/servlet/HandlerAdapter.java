
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// 在HandlerMapping返回处理请求的Controller实例后，需要一个帮助定位具体请求方法的处理类，这个类就是HandlerAdapter，HandlerAdapter是处理器适配器，
// Spring MVC通过HandlerAdapter来实际调用处理函数。例如Spring MVC自动注册的AnnotationMethodHandlerAdpater，HandlerAdapter定义了如何处理请求的策略，
// 通过请求url、请求Method和处理器的requestMapping定义，最终确定使用处理类的哪个方法来处理请求，并检查处理类相应处理方法的参数以及相关的Annotation配置，确定如何转换需要的参数传入调用方法，并最终调用返回ModelAndView。
//
// DispatcherServlet中根据HandlerMapping找到对应的handler method后，首先检查当前工程中注册的所有可用的handlerAdapter，根据handlerAdapter中的supports方法找到可以使用的handlerAdapter。
// 通过调用handlerAdapter中的handler方法来处理及准备handler method的参数及annotation(这就是spring mvc如何将request中的参数变成handle method中的输入参数的地方)，最终调用实际的handler method。
public interface HandlerAdapter {

	//support方法的作用是判断处理适配器是不是支持该Handler
	boolean supports(Object handler);

	//hanle方法，调用对应的Handler中适配到的方法，并返回一个ModelView
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	/**
	 * Same contract as for HttpServlet's {@code getLastModified} method.
	 * Can simply return -1 if there's no support in the handler class.
	 * @param request current HTTP request
	 * @param handler handler to use
	 * @return the lastModified value for the given handler
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
	 */
	long getLastModified(HttpServletRequest request, Object handler);

}
