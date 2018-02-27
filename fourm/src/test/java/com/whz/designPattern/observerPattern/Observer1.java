package com.whz.designPattern.observerPattern;

public class Observer1 extends Observer {

    public Observer1(Subject subject) {
        this.subject = subject;
        this.subject.addObserver(this);
    }

    @Override
    public void update() {
        System.out.println("Observer1状态更新: " + subject.getState());
    }
}