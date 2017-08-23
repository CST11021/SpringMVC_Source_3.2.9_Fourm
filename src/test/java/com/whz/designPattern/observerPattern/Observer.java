package com.whz.designPattern.observerPattern;

//创建观察者接口，每个观察者必须依赖被观察者和状态更新方法，当被观察者更新时来通知观察者更新
public abstract class Observer {
    protected Subject subject;

    public abstract void update();
}
