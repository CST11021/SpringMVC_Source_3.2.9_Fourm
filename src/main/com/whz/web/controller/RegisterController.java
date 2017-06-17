
package com.whz.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.whz.domain.User;
import com.whz.exception.UserExistException;
import com.whz.service.UserService;

/**
 * 
 * <br>
 * <b>类描述:</b>
 * 
 * <pre>
 * 用户注册的Action
 * </pre>
 *
 */
@Controller                   
public class RegisterController extends BaseController {
	/**
	 * 自动注入
	 */
	@Autowired
	private UserService userService;


	
	/**
	 * 用户登录
	 */
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView register(HttpServletRequest request,User user){
		ModelAndView view = new ModelAndView();
		view.setViewName("/success");
		try {
			userService.register(user);
		} catch (UserExistException e) {
			view.addObject("errorMsg", "用户名已经存在，请选择其它的名字。");
			view.setViewName("forward:/register.jsp");
		}
		setSessionUser(request,user);
		return view;
	}
	
}
