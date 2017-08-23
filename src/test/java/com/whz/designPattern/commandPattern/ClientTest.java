package com.whz.designPattern.commandPattern;

/*
    命令模式，就是客户端发布一个命令（也就是“请求”），而这个命令是已经被封装成一个对象的。即这个命令对象的内部可能已经
指定了该命令具体被谁负责执行。就像开发经理从客户那边获取对方的需求（命令），客户在描述具体的需求时可以明确指出该需求的执
行方。然后客户将需求（命令）给开发经理，开发经理在将任务委托给你具体的执行方。


设计实现：
    1、首先创建一个调用命令的调用者 Invoker

    2、客户端创建要执行的命令 AbstractCommand ，然后还要创建命令的执行者 AbstractReceiver ，命令实现类中，需要存在执行者的引用

    3、调用者接受（设置）一个将要执行的命令，然后将命令的具体执行委托给命令的执行者

依赖关系：Invoke --> Command --> Receiver

 */
// 测试类
public class ClientTest {
    public static void main(String[] args) {  
        // 创建调用者  
        Invoker invoker = new Invoker();

        // 创建一个具体命令，并指定该命令被执行的具体接收者  
        AbstractCommand command01 = new Command01(new Receiver01());
        // 给调用者发布一个具体命令  
        invoker.setCommand(command01);
        // 调用者执行命令，其实是将其传送给具体的接收者并让其真正执行  
        invoker.action();  
          
        AbstractCommand command02 = new Command01(new Receiver02());
        invoker.setCommand(command02);  
        invoker.action();  
    }  
}