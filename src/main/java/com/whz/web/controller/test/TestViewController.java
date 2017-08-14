package com.whz.web.controller.test;

import com.whz.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wb-whz291815 on 2017/8/14.
 */
@Controller
public class TestViewController {

    @RequestMapping(value = "/userListFtl")
    public String showUserListInFtl(ModelMap mm) {
        List<User> userList = new ArrayList<User>();
        User user1 = new User();
        user1.setUserId(123);
        user1.setUserName("tom");
        User user2 = new User();
        user2.setUserId(456);
        user2.setUserName("john");
        userList.add(user1);
        userList.add(user2);
        mm.addAttribute("userList", userList);
        return "userListFtl";
    }

    @RequestMapping(value = "/userListVolocity")
    public String showUserListInVolocity(ModelMap mm) {
        List<User> userList = new ArrayList<User>();
        User user1 = new User();
        user1.setUserId(123);
        user1.setUserName("tom");
        User user2 = new User();
        user2.setUserId(456);
        user2.setUserName("john");
        userList.add(user1);
        userList.add(user2);
        mm.addAttribute("userList", userList);
        return "userListVolocity";
    }

    @RequestMapping(value = "/showCustomView_Excel")
    public ModelAndView showUserListInExcel(ModelMap mm) {
        List<User> userList = new ArrayList<User>();
        User user1 = new User();
        user1.setUserId(123);
        user1.setUserName("tom");
        User user2 = new User();
        user2.setUserId(456);
        user2.setUserName("john");
        userList.add(user1);
        userList.add(user2);
        mm.addAttribute("userList", userList);
        return new ModelAndView("showCustomView_Excel");
    }

    @RequestMapping(value = "/showCustomView_PDF")
    public String showUserListInPdf(ModelMap mm) {
        List<User> userList = new ArrayList<User>();
        User user1 = new User();
        user1.setUserId(123);
        user1.setUserName("tom");
        User user2 = new User();
        user2.setUserId(456);
        user2.setUserName("john");
        userList.add(user1);
        userList.add(user2);
        mm.addAttribute("userList", userList);
        return "showCustomView_PDF";
    }

    @RequestMapping(value = "/showJsonView")
    public String showUserListInJson(ModelMap mm) {
        List<User> userList = new ArrayList<User>();
        User user1 = new User();
        user1.setUserId(123);
        user1.setUserName("tom");
        User user2 = new User();
        user2.setUserId(456);
        user2.setUserName("john");
        userList.add(user1);
        userList.add(user2);
        mm.addAttribute("userList", userList);
        return "showJsonView";
    }

    @RequestMapping(value = "/showXmlView")
    public String showUserListInXml(ModelMap mm) {
        List<User> userList = new ArrayList<User>();
        User user1 = new User();
        user1.setUserId(123);
        user1.setUserName("tom");
        User user2 = new User();
        user2.setUserId(456);
        user2.setUserName("john");
        userList.add(user1);
        userList.add(user2);
        mm.addAttribute("userList", userList);
        return "showXmlView";
    }

    // http://localhost:8080/forum/showUserListMix.html?content=json
    // http://localhost:8080/forum/showUserListMix.html?content=xml
    // http://localhost:8080/forum/showUserListMix.html 或 http://localhost:8080/forum/showUserListMix.html?content=htm
    @RequestMapping(value = "/showUserListMix")
    public String showUserListMix(ModelMap mm) {
        List<User> userList = new ArrayList<User>();
        User user1 = new User();
        user1.setUserId(123);
        user1.setUserName("tom");
        User user2 = new User();
        user2.setUserId(456);
        user2.setUserName("john");
        userList.add(user1);
        userList.add(user2);
        mm.addAttribute("userList", userList);
        return "userListMix";
    }

}
