package com.whz.web.controller.test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

public class TestMultiActionController extends MultiActionController {

    public ModelAndView multiAction1(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("success");
		return mav;
	}

	public ModelAndView multiAction2(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("success");
		return mav;
	}

	public ModelAndView multiAction3(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView mav = new ModelAndView("success");
		return mav;
	}

}