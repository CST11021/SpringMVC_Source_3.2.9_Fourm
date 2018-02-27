package com.whz.web.filter;

import com.whz.constant.CommonConstant;
import com.whz.domain.User;
import com.whz.web.controller.TestController;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.whz.constant.CommonConstant.LOGIN_TO_URL;

public class ForumFilter implements Filter {
	private final static Logger log = Logger.getLogger(ForumFilter.class);
	//是否已过滤当前URL
	private static final String ISFILTERED = "@@isFiltered";
	//不需要登录即可访问的URI资源
	private static final String[] PUBLIC_URIS = {
			"/index.jsp",
			"/index.html",
			"/login.jsp",
			"/login/doLogin.html",
			"/register.jsp",
			"/register.html",
			"/board/listBoardTopics-",
			"/board/listTopicPosts-"
	};

	public void init(FilterConfig filterConfig) throws ServletException {}
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		log.info("调用ForumFilter#doFilter方法拦截[" + ((HttpServletRequest) request).getRequestURI() + "]请求");
		//保证该过滤器在一次请求中只被调用一次
		if (request.getAttribute(ISFILTERED) == null) {
			//设置过滤标识，防止一次请求多次过滤
			request.setAttribute(ISFILTERED, Boolean.TRUE);
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			Object userInfObj = httpRequest.getSession().getAttribute(CommonConstant.CURRENT_ONLINE_USER);
			User userContext = null;
			if(userInfObj != null){
				userContext = (User) userInfObj;
			}

			// ②-3 用户未登录, 且当前URI资源需要登录才能访问
			if (userContext == null && !isPublicUri(httpRequest.getRequestURI(), httpRequest)) {
				String toUrl = httpRequest.getRequestURL().toString();
				if (!StringUtils.isEmpty(httpRequest.getQueryString())) {
					toUrl += "?" + httpRequest.getQueryString();
				}

				// ②-4将用户的请求URL保存在session中，用于登录成功之后，跳到目标URL
				httpRequest.getSession().setAttribute(LOGIN_TO_URL, toUrl);

				// ②-5转发到登录页面
				request.getRequestDispatcher("/login.jsp").forward(request,response);
				return;
			}
		}
		chain.doFilter(request, response);
	}
	public void destroy() {}

   //校验当前URI资源是否需要登录才能访问
	private boolean isPublicUri(String requestURI, HttpServletRequest request) {
		String appContextPath = request.getContextPath();
		if (appContextPath.equalsIgnoreCase(requestURI) || (appContextPath + "/").equalsIgnoreCase(requestURI)){
			return true;
		}
		for (String uri : PUBLIC_URIS) {
			if (requestURI != null && requestURI.indexOf(uri) >= 0) {
				return true;
			}
		}
		return false;
	}

	protected User getSessionUser(HttpServletRequest request) {
		return (User) request.getSession().getAttribute(CommonConstant.CURRENT_ONLINE_USER);
	}


}
