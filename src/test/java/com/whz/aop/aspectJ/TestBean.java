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

    private String[] value;
    private String[] params;

    public String[] getValue() {
        return value;
    }

    public void setValue(String[] value) {
        this.value = value;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public void test() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("test");
    }


}
