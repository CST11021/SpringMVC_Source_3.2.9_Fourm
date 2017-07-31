package com.whz.aop.aspectJ;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/com/whz/aop/aspectJ/aspectJConfig.xml"})
public class InterceptorTest {

    @Resource
    private TestBean test;

    @Test
    public void testUmp(){
        test.test();
    }
}