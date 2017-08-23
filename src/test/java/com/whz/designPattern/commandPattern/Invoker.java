package com.whz.designPattern.commandPattern;

// 调用者，负责将具体的命令传送给具体的接收者
public class Invoker {

    private AbstractCommand command = null;
 
    public void setCommand(AbstractCommand command) {  
        this.command = command;  
    }
 
    public void action() {
        this.command.execute();  
    }
} 