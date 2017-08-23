package com.whz.designPattern.commandPattern;

// 具体命令类01，通过构造函数的参数决定了该命令由哪个接收者执行
public class Command01 extends AbstractCommand {
    private AbstractReceiver receiver = null;
 
    public Command01(AbstractReceiver receiver) {
        this.receiver = receiver;  
    }  
 
    public void execute() {  
        System.out.println("命令01 被发布 ...");  
        this.receiver.doJob();  
    }  
} 