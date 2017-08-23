package com.whz.designPattern.observerPattern;

public class Observer2 extends Observer {

    public Observer2(Subject subject) {
        this.subject = subject;
        this.subject.addObserver(this);
    }

    @Override
    public void update() {
        System.out.println("Observer2状态更新: " + subject.getState());
    }
}