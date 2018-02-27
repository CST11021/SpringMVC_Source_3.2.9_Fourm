package com.whz.web;

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
import java.util.Date;

@Controller
public class ControllerTest {

    private final static Logger log = Logger.getLogger(ControllerTest.class);

    @RequestMapping("/test1")
    public ModelAndView test2(FormDataVO formDataVO) {
        Date startTime = formDataVO.getStartTime();
        ModelAndView mav = new ModelAndView();
        mav.addObject("test",startTime);
        mav.setViewName("/");
        return mav;
    }
}