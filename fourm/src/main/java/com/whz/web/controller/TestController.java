package com.whz.web.controller;

import com.whz.service.ConfigServices;
import com.whz.constant.SystemParamter;
import com.whz.support.BehaviourConfigParserHelper;
import com.whz.support.PropertyFileParserHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ConfigServices configServices;

    private final static Logger log = Logger.getLogger(TestController.class);

    @RequestMapping("/test.html")
    public ModelAndView test(HttpServletRequest request) {
        log.info("SystemParamter.TEST1:"+ SystemParamter.TEST1);
        log.info("SystemParamter.TEST2:"+ SystemParamter.TEST2);
        log.info("SystemParamter.TEST3:" + SystemParamter.TEST3);

        String tagName = BehaviourConfigParserHelper.getTagName(configServices.getRootElement());
        String timeout = PropertyFileParserHelper.get("cache.data.timeout");
        ModelAndView mav = new ModelAndView();
        mav.addObject("tagName",tagName);
        mav.addObject("timeout",timeout);
        // 关于重定向的问题：
        // 使用servlet重定向有两种方式，一种是forward，另一种就是redirect。
        // forward是服务器内部重定向，客户端并不知道服务器把你当前请求重定向到哪里去了，地址栏的url与你之前访问的url保持不变。
        // redirect则是客户端重定向，是服务器将你当前请求返回，然后给个状态标示给你，告诉你应该去重新请求另外一个url，具体表现就是地址栏的url变成了新的url。
        mav.setViewName("redirect:/index.html");
        return mav;
    }

    @RequestMapping("/testPage.html")
    public ModelAndView test1(HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("test","just a test.");
        mav.setViewName("forward:/testPage.jsp");
        return mav;

    }

}