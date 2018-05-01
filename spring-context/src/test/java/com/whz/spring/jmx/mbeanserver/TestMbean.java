package com.whz.spring.jmx.mbeanserver;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description = "testMBean")
public class TestMbean {

    private String p1;

    @ManagedAttribute
    public String getP1() {
        return p1;
    }

    @ManagedOperation(description = "方法P1 的说明")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "p1", description = "参数 p1 的说明")
    })
    public void addP1(String p1) {
        this.p1 = p1;
    }
}