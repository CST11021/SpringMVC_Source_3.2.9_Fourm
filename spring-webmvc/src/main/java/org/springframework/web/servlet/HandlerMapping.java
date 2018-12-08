
package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;

/**

 	HandlerMapping映射处理器，HandlerMapping将请求映射为HandlerExecutionChain对象（包含一个Handler处理器（页面控制器）和
 多个 HandlerInterceptor拦截器）。在Spring容器初始化完成时，在上下文环境中已定义的所有 HandlerMapping 都已经被加载了，这
 些加载的handlerMappings被放在一个有序的List中，存储着HTTP请求对应的映射数据。这个List中的每一个元素都对应着一个具体
 handlerMapping的配置，一般每一个handlerMapping可以持有一系列从URL请求到Controller的映射，而SpringMVC提供了一系列的HandlerMapping实现。

	HandlerMapping帮助DispatcherServlet进行Web请求的URL到具体处理类的匹配。之所以称为HandlerMapping是因为，在SpringMVC中，
并不知道局限于使用Controller作为DispatcherServlet的次级控制器。实际上，我们也可以使用其他类型的次级控制器，包括SpringMVC
提供的除了Controller之外的次级控制器类型，或者第三方Web开发框架中的Page Controller组件（如Struts的Action），而所有这些次
级控制器类型，在SpringMVC中都称作Handler。HandlerMapping要处理的也就是Web请求到相应Handler之间的映射关系。如果你接触过
Struts框架的话，可以将HandlerMapping与Struts框架的ActionMapping概念进行类比。


实现类如下：
 RequestMappingHandlerMapping：
 ControllerBeanNameHandlerMapping：
 ControllerClassNameHandlerMapping：
 BeanNameUrlHandlerMapping：
 DefaultAnnotationHandlerMapping：
 SimpleUrlHandlerMapping：

 1. HandlerMapping:                 这个接口中定义了通过HttpServletRequest 来获取对应的 HandlerExecutionChain(PS: HandlerExecutionChain 中定义了请求的拦截器+最终调用的 Handler), 其中还有一个非常重要的变量 URI_TEMPLATE_VARIABLES_ATTRIBUTE, 这个key对应的value其实就是 uri template 变量(@PathVariable 解析属性时获取数据的来源)
 2. AbstractHandlerMapping:         这个类继承了 WebApplicationObjectSupport, 这就让其具有通过 ApplicationContextAware 接口来触发初始化的功能, 当然其也在初始化方法中获取了 MappedInterceptor, 并且定义了通过 HttpServletRequest 获取 HandlerExecutionChain 的主逻辑 <-- 其也留下了获取 Handler 的模版方法 getHandlerInternal
 3. AbstractHandlerMethodMapping:   通过 HttpServletRequest 来获取 HandlerMethod; 其通过 InitializingBean.afterPropertiesSet 来搜索获取所有 HandlerMethod <-- 这部分就是在HandlerMapping初始化时获取所有HandlerMethod; 并且留下了 getMappingForMethod 等获取 RequestMappingInfo 的模版方法
 4. AbstractUrlHandlerMapping:      与 AbstractHandlerMethodMapping 不同, AbstractUrlHandlerMapping.getHandlerInternal 返回的是个 Object(PS: 这里的Object有可能是 ApplicationContext 中的 BeanName, 或直接是个 Bean), 而且其中完成了 URI 与 handler 的注册流程 registerHandler(urlPath, beanName)
 5. RequestMappingHandlerMapping:   基于 HandlerMethod 的HandlerMapping, 主要实现了getMappingForMethod(基于Method，handlerType获取RequestMappingInfo), 而 RequestMappingInfoHandlerMapping 中主要是在查找 HandlerMethod 时对 HttpServletRequest 中的一些操作, 比如设置 uri template variable
 6. SimpleUrlHandlerMapping:        在配置 uri 与handler 之间映射关系的 HandlerMapping (PS: 这里的 handler 可以是任意类型, 方正可以有对应的 HandlerAdapter 来进行激活它)
 7. AbstractDetectingUrlHandlerMapping:  从类名中我们就可以获知, 这是一个自动获取 Handler 已经 url 的类, 这个方法的触发操作是 ApplicationContextAware.setApplicationContext
 8. BeanNameUrlHandlerMapping:           以 BeanName 为 uri 的 HandlerMapping <-- 其中 BeanName 必需以 "/" 开头
 9. ControllerBeanNameHandlerMapping:    通过 BeanName 构成 uri 的 HandlerMapping (PS: 这个类已经过期)
 10. ControllerClassNameHandlerMapping:  基于 className 生成 uri 的 HandlerMapping(PS: 这个类已经过期)
 11. DefaultAnnotationHandlerMapping：   基于 @RequestMapping 的 HandlerMapping, 这里面会出现 多个 urls 对应一个 Handler (PS: 这个类已经过期)


*/

public interface HandlerMapping {

	String PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE = HandlerMapping.class.getName() + ".pathWithinHandlerMapping";
	String BEST_MATCHING_PATTERN_ATTRIBUTE = HandlerMapping.class.getName() + ".bestMatchingPattern";
	String INTROSPECT_TYPE_LEVEL_MAPPING = HandlerMapping.class.getName() + ".introspectTypeLevelMapping";
	String URI_TEMPLATE_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".uriTemplateVariables";
	String MATRIX_VARIABLES_ATTRIBUTE = HandlerMapping.class.getName() + ".matrixVariables";
	String PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE = HandlerMapping.class.getName() + ".producibleMediaTypes";


	/**
	 * HandlerMapping接口唯一的一个接口方法，根据请求或取请求的处理链 HandlerExecutionChain
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception;

}
