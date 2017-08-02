package com.whz.aop.aspectJ;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/com/whz/aop/aspectJ/aspectJConfig.xml"})
public class Test {

    @Resource
    private TestBean testBean;
    @Resource
    private Waiter waiter;

    // 测试一般类型的增强
    @org.junit.Test
    public void testInterceptor1(){
        testBean.printStr();
    }

    // 测试引介增强
    @org.junit.Test
    public void testInterceptor2() {
        waiter.greetTo("John");
        Seller seller = (Seller) waiter;
        seller.sell("Beer", "John");
    }
}