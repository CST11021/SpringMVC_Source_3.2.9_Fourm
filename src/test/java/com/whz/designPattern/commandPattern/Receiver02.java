package com.whz.designPattern.commandPattern;

// 具体接收者02，实现自己真正的业务逻辑
public class Receiver02 extends AbstractReceiver {
    public void doJob() {  
        System.out.println("接收者02 完成工作 ...\n");  
    }  
} 