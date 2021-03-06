package com.whz.javabase.lock.reentrantlock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// LockTest2.java

public class LockTest2 {
    public static void main(String[] args) {
        Depot mDepot = new Depot();
        Producer mPro = new Producer(mDepot);
        Customer mCus = new Customer(mDepot);

        mPro.produce(60);
        mPro.produce(120);
        mCus.consume(90);
        mCus.consume(150);
        mPro.produce(110);
    }

    // 仓库
    static class Depot {
        private int size;    // 仓库的实际数量

        public Depot() {
            this.size = 0;
        }

        public void produce(int val) {
            size += val;
            System.out.printf("%s produce(%d) --> size=%d\n",
                Thread.currentThread().getName(), val, size);
        }

        public void consume(int val) {

            size -= val;
            System.out.printf("%s consume(%d) <-- size=%d\n",
                Thread.currentThread().getName(), val, size);
        }
    }

    // 生产者
    static class Producer {
        private Depot depot;

        public Producer(Depot depot) {
            this.depot = depot;
        }

        // 消费产品：新建一个线程向仓库中生产产品。
        public void produce(final int val) {
            new Thread() {
                public void run() {
                    depot.produce(val);
                }
            }.start();
        }
    }

    // 消费者
    static class Customer {
        private Depot depot;

        public Customer(Depot depot) {
            this.depot = depot;
        }

        // 消费产品：新建一个线程从仓库中消费产品。
        public void consume(final int val) {
            new Thread() {
                public void run() {
                    depot.consume(val);
                }
            }.start();
        }
    }
}