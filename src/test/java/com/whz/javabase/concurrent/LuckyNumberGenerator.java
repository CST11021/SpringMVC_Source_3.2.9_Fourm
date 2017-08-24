package com.whz.javabase.concurrent;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;


/*
LinkedTransferQueue实现了一个重要的接口TransferQueue，该接口含有下面几个重要方法：
     1. transfer(E e)：若当前存在一个正在等待获取的消费者线程，即立刻移交之；否则，会插入当前元素e到队列尾部，并且等待进入阻塞状态，到有消费者线程取走该元素。
     2. tryTransfer(E e)：若当前存在一个正在等待获取的消费者线程（使用take()或者poll()函数），使用该方法会即刻转移/传输对象元素e；若不存在，则返回false，并且不进入队列。这是一个不阻塞的操作。
     3. tryTransfer(E e, long timeout, TimeUnit unit)：若当前存在一个正在等待获取的消费者线程，会立即传输给它;否则将插入元素e到队列尾部，并且等待被消费者线程获取消费掉；若在指定的时间内元素e无法被消费者线程获取，则返回false，同时该元素被移除。
     4. hasWaitingConsumer()：判断是否存在消费者线程。
     5. getWaitingConsumerCount()：获取所有等待获取元素的消费线程数量。
 */
public class LuckyNumberGenerator {

    public static void main(String[] args) {
        TransferQueue<String> queue = new LinkedTransferQueue<String>();

        // 注意生产者使用的transfer()方法
        Thread producer = new Thread(new Producer(queue));
        producer.setDaemon(true); //设置为守护进程使得线程执行结束后程序自动结束运行  
        producer.start();

        for (int i = 0; i < 10; i++) {
            Thread consumer = new Thread(new Consumer(queue));
            consumer.setDaemon(true);
            consumer.start();
            try {
                // 消费者线程休眠一秒钟，以便以便生产者获得CPU，从而生产产品
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}  