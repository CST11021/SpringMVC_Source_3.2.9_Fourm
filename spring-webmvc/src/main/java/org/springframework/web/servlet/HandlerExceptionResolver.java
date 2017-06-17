
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//有时候系统运行过程中出现异常，而我们并不希望就此中断对用户的服务，而是至少告知客户当前系统在处理逻辑的过程中出现了异常，
// 甚至告知他们因为什么原因导致的。Spring中的异常处理机制帮我们完成这个工作。其实，这里Spring主要的工作就是逻辑引导至HandlerExceptionResolver类的resolveException方法
public interface HandlerExceptionResolver {

	ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex);

}
