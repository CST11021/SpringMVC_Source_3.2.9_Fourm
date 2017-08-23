package com.whz.designPattern.observerPattern;

/*
    定义对象间的一种一对多的依赖关系，当一个对象的状态发生改变时，所有依赖于它的对象都得到通知并被自动更新。
    当目标类（被观察者）的某一个状态发生改变时，我们不必在目标类里再调用每个依赖类实例的方法进行更新操作，而是使用触发机
 制，通知所有观察者，通过这样方式类来做到易用行和低耦合，并且保证高度的协作。
 */
public class ObserverPatternDemo {
    public static void main(String[] args) {
        Subject subject = new Subject();

        new Observer1(subject);
        new Observer2(subject);
        new Observer3(subject);

        System.out.println("First state change: 15");
        subject.setState(15);
        System.out.println("Second state change: 10");
        subject.setState(10);
    }
}