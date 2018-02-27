package com.whz.javabase.atomic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// 该代码使用static变量exists用来实现同一时间只有一个worker在工作
public class Work_ByAtomicBoolean implements Runnable {
    private static AtomicBoolean exists = new AtomicBoolean(false);
    private String name;
    public Work_ByAtomicBoolean(String name) {
        this.name = name;
    }

    public static void main(String[] args) {
        Work_ByAtomicBoolean bar1 = new Work_ByAtomicBoolean("worker1");
        Work_ByAtomicBoolean bar2 = new Work_ByAtomicBoolean("worker2");
        new Thread(bar1).start();
        new Thread(bar2).start();
    }

    @Override
    public void run() {
        // 如果 exists 是 false 则更新为true，然后执行if里的代码块
        // 注意：比较和执行两个操作是作为一个原子性的事务操作，中间不会出现线程暂停的情况，主要为多线程的控制提供解决的方案。
        if (exists.compareAndSet(false, true)) {
            enter();
            working();
            sleep(2);
            leave();
            exists.set(false);
        } else {
            System.out.println(name + " give up");
        }
    }
    public void sleep(long time) {
        try {
            TimeUnit.SECONDS.sleep(time);
        } catch (InterruptedException e1) {}
    }
    public void enter() {
        System.out.println(name + " enter");
    }
    public void working() {
        System.out.println(name + " working");
    }
    public void leave() {
        System.out.println(name + " leave");
    }

}  