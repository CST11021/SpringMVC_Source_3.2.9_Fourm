package com.whz.javabase.concurrent;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

public class Producer implements Runnable {
    private final TransferQueue<String> queue;

    public Producer(TransferQueue<String> queue) {
        this.queue = queue;
    }

    // 生产者用来生成幸运数字，并将生成的数字放到队列里
    @Override
    public void run() {
        try {
            while (true) {
                if (queue.hasWaitingConsumer()) {
                    // 注意：这里使用的transfer()方法
                    queue.transfer(produce());
                }
                TimeUnit.SECONDS.sleep(1);//生产者睡眠一秒钟,这样可以看出程序的执行过程  
            }
        } catch (InterruptedException e) {
        }
    }
    private String produce() {
        return " your lucky number " + (new Random().nextInt(100));
    }

}  