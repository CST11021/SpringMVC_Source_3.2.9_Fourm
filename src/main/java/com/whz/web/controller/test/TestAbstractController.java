package com.whz.web.controller.test;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by wb-whz291815 on 2017/8/10.
 */
public class TestAbstractController extends AbstractController {

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("处理 /testSuccess.html 的请求...");
        return new ModelAndView("success");
    }

}
