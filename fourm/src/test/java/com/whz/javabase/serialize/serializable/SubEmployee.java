package com.whz.javabase.serialize.serializable;

// 当一个父类实现序列化，子类自动实现序列化，不需要显式实现Serializable接口
public class SubEmployee extends Employee {

    private String id;

    // TestBean必须显示实现Serializable接口否则报异常
    private TestBean testBean;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TestBean getTestBean() {
        return testBean;
    }

    public void setTestBean(TestBean testBean) {
        this.testBean = testBean;
    }
}
