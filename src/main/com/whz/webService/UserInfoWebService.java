package com.whz.webService;

import com.whz.domain.User;
import com.whz.service.UserService;
import net.sf.json.JSONArray;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by whz on 2017/2/16.
 */
public class UserInfoWebService {
    public String getUserInfoByUserId(String userId){
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        UserService userService = (UserService) wac.getBean("userService");
        int id = Integer.valueOf(userId);
        User user = userService.getUserById(id);
        String json = JSONArray.fromObject(user).toString();
        return json;
    }
}
