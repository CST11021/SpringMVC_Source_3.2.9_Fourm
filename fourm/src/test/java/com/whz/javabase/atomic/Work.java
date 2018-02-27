package com.whz.javabase.atomic;

import java.util.concurrent.TimeUnit;

public class Work implements Runnable {

    private static boolean exists = false;
    private String name;

    public Work(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        // 该代码无法保证同一时间只有一个worker在工作，因为 exists判断操作 和 exists=true;的赋值操作 之间有了其他指令
        if (!exists) {
            sleep(1);
            exists = true;
            enter();
            working();
            sleep(2);
            leave();
            exists = false;
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


    public static void main(String[] args) {

        Work bar1 = new Work("worker1");
        Work bar2 = new Work("worker2");
        new Thread(bar1).start();
        new Thread(bar2).start();

    }


}  