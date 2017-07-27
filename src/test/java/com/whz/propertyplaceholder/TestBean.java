package com.whz.propertyplaceholder;


import lombok.Getter;
import lombok.Setter;

/**
 * Created by wb-whz291815 on 2017/6/30.
 */
public class TestBean {

    @Getter
    @Setter
    private String testStr;
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



}
