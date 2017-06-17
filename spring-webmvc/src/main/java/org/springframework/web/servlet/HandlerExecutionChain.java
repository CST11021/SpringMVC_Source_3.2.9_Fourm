
package org.springframework.web.servlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

// HandlerExecutionChain持有一个Interceptor链和一个handler对象，这个handler对象实际上就是HTTP请求对应的Controller，在持有这个handler对象的同时，
// 还在HandlerExecutionChain中设置了一个拦截器链，通过这个拦截器链中的拦截器，可以为handler对象提供功能的增强。要完成这些工作，需要对拦截器链和handler都进行配置，
// 这些配置都是在HandlerExecutionChain的初始化函数中完成的。
//
// 为了维护这个拦截器链和handler，HandlerExecutionChain还提供了一系列与拦截器链维护相关的一些操作，比如可以为拦截器链增加拦截器的addInterceptor方法等。
public class HandlerExecutionChain {

	private static final Log logger = LogFactory.getLog(HandlerExecutionChain.class);

	//这个handler对象实际上就是HTTP请求对应的 Controller
	private final Object handler;
	//所有的HandlerInterceptor的数组
	private HandlerInterceptor[] interceptors;
	//所有的HandlerInterceptor的链表
	private List<HandlerInterceptor> interceptorList;

	private int interceptorIndex = -1;



	public HandlerExecutionChain(Object handler) {
		this(handler, null);
	}
	public HandlerExecutionChain(Object handler, HandlerInterceptor[] interceptors) {
		if (handler instanceof HandlerExecutionChain) {
			HandlerExecutionChain originalChain = (HandlerExecutionChain) handler;
			this.handler = originalChain.getHandler();
			this.interceptorList = new ArrayList<HandlerInterceptor>();
			CollectionUtils.mergeArrayIntoCollection(originalChain.getInterceptors(), this.interceptorList);
			CollectionUtils.mergeArrayIntoCollection(interceptors, this.interceptorList);
		}
		else {
			this.handler = handler;
			this.interceptors = interceptors;
		}
	}


	// 获取处理器对象
	public Object getHandler() {
		return this.handler;
	}

	//添加 HandlerInterceptor
	public void addInterceptor(HandlerInterceptor interceptor) {
		initInterceptorList();
		this.interceptorList.add(interceptor);
	}
	public void addInterceptors(HandlerInterceptor[] interceptors) {
		if (interceptors != null) {
			initInterceptorList();
			this.interceptorList.addAll(Arrays.asList(interceptors));
		}
	}

	// 初始化HandlerInterceptor的链表
	private void initInterceptorList() {
		if (this.interceptorList == null) {
			this.interceptorList = new ArrayList<HandlerInterceptor>();
		}
		if (this.interceptors != null) {
			this.interceptorList.addAll(Arrays.asList(this.interceptors));
			this.interceptors = null;
		}
	}

	//获取所有的HandlerInterceptor
	public HandlerInterceptor[] getInterceptors() {
		if (this.interceptors == null && this.interceptorList != null) {
			this.interceptors = this.interceptorList.toArray(new HandlerInterceptor[this.interceptorList.size()]);
		}
		return this.interceptors;
	}

	//依次执行HandlerInterceptor实现类的preHandle函数
	boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (getInterceptors() != null) {
			for (int i = 0; i < getInterceptors().length; i++) {
				HandlerInterceptor interceptor = getInterceptors()[i];
				if (!interceptor.preHandle(request, response, this.handler)) {
					triggerAfterCompletion(request, response, null);
					return false;
				}
				this.interceptorIndex = i;
			}
		}
		return true;
	}
	//依次执行HandlerInterceptor实现类的postHandle函数
	void applyPostHandle(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) throws Exception {
		if (getInterceptors() == null) {
			return;
		}
		for (int i = getInterceptors().length - 1; i >= 0; i--) {
			HandlerInterceptor interceptor = getInterceptors()[i];
			interceptor.postHandle(request, response, this.handler, mv);
		}
	}

	//依次执行HandlerInterceptor实现类的afterCompletion函数
	void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response, Exception ex) throws Exception {

		if (getInterceptors() == null) {
			return;
		}
		for (int i = this.interceptorIndex; i >= 0; i--) {
			HandlerInterceptor interceptor = getInterceptors()[i];
			try {
				interceptor.afterCompletion(request, response, this.handler, ex);
			}
			catch (Throwable ex2) {
				logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
			}
		}
	}

	//这个方法会在Controller方法异步执行时开始执行
	void applyAfterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response) {
		if (getInterceptors() == null) {
			return;
		}
		for (int i = getInterceptors().length - 1; i >= 0; i--) {
			if (interceptors[i] instanceof AsyncHandlerInterceptor) {
				try {
					AsyncHandlerInterceptor asyncInterceptor = (AsyncHandlerInterceptor) this.interceptors[i];
					asyncInterceptor.afterConcurrentHandlingStarted(request, response, this.handler);
				}
				catch (Throwable ex) {
					logger.error("Interceptor [" + interceptors[i] + "] failed in afterConcurrentHandlingStarted", ex);
				}
			}
		}
	}


	@Override
	public String toString() {
		if (this.handler == null) {
			return "HandlerExecutionChain with no handler";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("HandlerExecutionChain with handler [").append(this.handler).append("]");
		if (!CollectionUtils.isEmpty(this.interceptorList)) {
			sb.append(" and ").append(this.interceptorList.size()).append(" interceptor");
			if (this.interceptorList.size() > 1) {
				sb.append("s");
			}
		}
		return sb.toString();
	}

}
