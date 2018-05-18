package com.whz.javabase.serialize.serializable;

import java.io.Serializable;

/**
 * @author whz
 * @version : TestBean.java, v 0.1 2018-05-18 12:46 whz Exp $$
 */
public class TestBean implements Serializable {

    private static final long serialVersionUID = 5575602485602529L;

    private String test;

    public TestBean(String test) {
        this.test = test;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "test='" + test + '\'' +
                '}';
    }
}
