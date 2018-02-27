package com.whz.designPattern.observerPattern;

import java.util.ArrayList;
import java.util.List;

//创建被观察者
public class Subject {

    private int state;
    private List<Observer> observers = new ArrayList<Observer>();

    public int getState() {
        return state;
    }

    //设置状态，并通知所有观察者
    public void setState(int state) {
        this.state = state;
        notifyAllObservers();
    }

    //添加观察者
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    //移除观察者
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    //通知所有观察者状态更新
    public void notifyAllObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
}
