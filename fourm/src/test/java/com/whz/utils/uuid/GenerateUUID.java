package com.whz.utils.uuid;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * UUID生成的是length=32的16进制格式的字符串，如果回退为byte数组共16个byte元素，即UUID是一个128bit长的数字，一般用16进制表示。
 * 算法的核心思想是结合机器的网卡、当地时间、一个随即数来生成UUID。
 * 从理论上讲，如果一台机器每秒产生10000000个GUID，则可以保证（概率意义上）3240年不重复
 * 优点：
 * （1）本地生成ID，不需要进行远程调用，时延低
 * （2）扩展性好，基本可以认为没有性能上限
 * 缺点：
 * （1）无法保证趋势递增
 * （2）uuid过长，往往用字符串表示，作为主键建立索引查询效率低，常见优化方案为“转化为两个uint64整数存储”或者“折半存储”（折半后不能保证唯一性）
 */
public class GenerateUUID {

    public static String getId() {
        return UUID.randomUUID().toString();
    }


    public static void main(String[] args) {
        final int num = 50;
        final CountDownLatch begin = new CountDownLatch(1);
        final CountDownLatch end = new CountDownLatch(num);

        for (int i = 0; i < num; i++) {
            new Thread(new Worker(begin, end)).start();
        }

        System.out.println(num + "个线程开始并发生成UUID：");
        begin.countDown();
        long startTime = System.currentTimeMillis();
        try {
            end.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("生成完毕");
            System.out.println("耗时: " + (endTime - startTime));
        }

    }

    static class Worker implements Runnable {
        final CountDownLatch begin;
        final CountDownLatch end;

        public Worker(CountDownLatch begin, CountDownLatch end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {
            try {
                begin.await();
                System.out.println(getId());
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                end.countDown();
            }


        }
    }

} 