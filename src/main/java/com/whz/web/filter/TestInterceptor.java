package com.whz.web.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


public class TestInterceptor implements HandlerInterceptor {

    private final static Logger log = Logger.getLogger(TestInterceptor.class);

    // 处理程序请求之前调用
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        log.info("处理请求[" + req.getRequestURI() + "]前调用TestInterceptor#preHandle方法");
        return true;
    }

    // 处理程序请求之后调用
    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object arg2, ModelAndView arg3) throws Exception {
        log.info("处理请求[" + req.getRequestURI() + "]后调用TestInterceptor#postHandle");
    }

    // 在所有请求处理完成之后被调用的（如视图呈现之后）
    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object arg2, Exception arg3) throws Exception {
        log.info("在所有请求处理完成之后调用TestInterceptor#afterCompletion");
    }

}  