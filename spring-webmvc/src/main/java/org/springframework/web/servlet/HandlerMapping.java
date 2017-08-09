
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;

/*

 	HandlerMapping映射处理器，HandlerMapping将请求映射为HandlerExecutionChain对象（包含一个Handler处理器（页面控制器）和
 多个 HandlerInterceptor拦截器）。在Spring容器初始化完成时，在上下文环境中已定义的所有 HandlerMapping 都已经被加载了，这
 些加载的handlerMappings被放在一个List中并被排序，存储着HTTP请求对应的映射数据。这个List中的每一个元素都对应着一个具体
 handlerMapping的配置，一般每一个handlerMapping可以持有一系列从URL请求到Controller的映射，而SpringMVC提供了一系列的HandlerMapping实现。

	HandlerMapping帮助DispatcherServlet进行Web请求的URL到具体处理类的匹配。之所以称为HandlerMapping是因为，在SpringMVC中，
并不知道局限于使用Controller作为DispatcherServlet的次级控制器。实际上，我们也可以使用其他类型的次级控制器，包括SpringMVC
提供的除了Controller之外的次级控制器类型，或者第三方Web开发框架中的Page Controller组件（如Struts的Action），而所有这些次
级控制器类型，在SpringMVC中都称作Handler。HandlerMapping要处理的也就是Web请求到相应Handler之间的映射关系。如果你接触过
Struts框架的化，可以将HandlerMapping与Struts框架的ActionMapping概念进行类比。
*/

public interface HandlerMapping {

	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";
	String INTROSPECT_TYPE_LEVEL_MAPPING = HandlerMapping.class.getName() + ".introspectTypeLevelMapping";
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";
	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";


	//HandlerMapping接口唯一的一个接口方法，根据请求或取请求的处理链 HandlerExecutionChain
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
