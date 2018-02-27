package com.whz.designPattern.observerPattern;

public class Observer3 extends Observer {

    public Observer3(Subject subject) {
        this.subject = subject;
        this.subject.addObserver(this);
    }

    @Override
    public void update() {
        System.out.println("Observer3状态更新: " + subject.getState());
    }
}