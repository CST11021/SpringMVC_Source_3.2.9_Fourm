package com.whz.javabase.concurrent;

import java.util.concurrent.TransferQueue;

public class Consumer implements Runnable {
    private final TransferQueue<String> queue;

    public Consumer(TransferQueue<String> queue) {
        this.queue = queue;
    }

    // 消费者线程，从队列中取幸运数字
    @Override
    public void run() {
        try {
            System.out.println(" Consumer " + Thread.currentThread().getName() + queue.take());
        } catch (InterruptedException e) {
        }
    }
}  