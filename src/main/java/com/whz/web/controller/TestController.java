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
        mav.setViewName("redirect:/index.html");
        return mav;
    }

}