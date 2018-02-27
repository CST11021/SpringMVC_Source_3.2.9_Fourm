package com.whz.designPattern.commandPattern;

// 具体接收者01，实现自己真正的业务逻辑
public class Receiver01 extends AbstractReceiver {
    public void doJob() {  
        System.out.println("接收者01 完成工作 ...\n");  
    }  
}