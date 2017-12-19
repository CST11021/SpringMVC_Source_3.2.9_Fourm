package com.whz.aop.aspectJ;


import lombok.Getter;
import lombok.Setter;

/**
 * Created by wb-whz291815 on 2017/6/30.
 */
public class TestBean {

    @Getter
    @Setter
    private String testStr = "testStr";

    public void printStr() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("print test ... ");
    }


}
