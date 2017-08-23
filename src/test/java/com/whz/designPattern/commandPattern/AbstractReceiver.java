package com.whz.designPattern.commandPattern;

//抽象接收者，定义了每个接收者应该完成的业务逻辑
public abstract class AbstractReceiver {
    public abstract void doJob();  
} 