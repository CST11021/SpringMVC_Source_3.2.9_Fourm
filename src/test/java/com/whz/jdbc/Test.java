package com.whz.jdbc;

import junit.framework.TestCase;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/com/whz/jdbc/spring-jdbc.xml"})
public class Test{

    @Resource
    private UserService userService;

    @org.junit.Test
    public void testSave() {
        User user = new User();
        user.setName("张三");
        user.setAge(20);
        user.setSex("男");
        userService.save(user);
    }

    @org.junit.Test
    public void testFind() {
        List<User> list = userService.getUsers();
        System.out.println(list.toString());
    }


}
