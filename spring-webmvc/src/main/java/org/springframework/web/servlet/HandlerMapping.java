
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;


// HandlerMapping映射处理器，HandlerMapping将请求映射为HandlerExecutionChain对象（包含一个Handler处理器（页面控制器）和多个HandlerInterceptor拦截器）。
// 在Spring容器初始化完成时，在上下文环境中已定义的所有 HandlerMapping 都已经被加载了，这些加载的handlerMappings被放在一个List中并被排序，存储着HTTP请求对应的映射数据。
// 这个List中的每一个元素都对应着一个具体handlerMapping的配置，一般每一个handlerMapping可以持有一系列从URL请求到Controller的映射，而SpringMVC提供了一系列的HandlerMapping实现
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
