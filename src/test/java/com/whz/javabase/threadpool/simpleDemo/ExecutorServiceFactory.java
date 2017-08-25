package com.whz.javabase.threadpool.simpleDemo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


//线程池构造工厂
public class ExecutorServiceFactory {
    //定时任务线程池
    private ExecutorService executors;
    private static ExecutorServiceFactory executorFactory = new ExecutorServiceFactory();

    //获取ExecutorServiceFactory
    public static ExecutorServiceFactory getInstance() {
        return executorFactory;
    }


    //---------------------提供创建不同类型的线程池-------------------------
    // 创建一个线程池，它可安排在给定延迟后运行命令或者定期地执行。
    public ExecutorService createScheduledThreadPool() {
        // CPU个数
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        // 创建
        executors = Executors.newScheduledThreadPool(availableProcessors * 10, getThreadFactory());
        return executors;
    }
    // 创建一个单线程的线程池。此线程池支持定时以及周期性执行任务的需求
    public ExecutorService createSingleThreadExecutor() {
        executors = Executors.newSingleThreadExecutor(getThreadFactory());
        return executors;
    }
    // 创建一个可缓存的线程池。如果线程池的大小超过了处理任务所需要的线程，那么就会回收部分空闲（60秒不执行任务）的线程，
    // 当任务数增加时，此线程池又可以智能的添加新线程来处理任务。此线程池不会对线程池大小做限制，线程池大小完全依赖于操作
    // 系统（或者说JVM）能够创建的最大线程大小。
    public ExecutorService createCachedThreadPool() {
        executors = Executors.newCachedThreadPool(getThreadFactory());
        return executors;
    }
    // 创建固定大小的线程池。每次提交一个任务就创建一个线程，直到线程达到线程池的最大值，线程池的大小一旦达到最大值就会保
    // 持不变，如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。
    public ExecutorService createFixedThreadPool(int count) {
        executors = Executors.newFixedThreadPool(count, getThreadFactory());
        return executors;
    }


    // 获取线程工厂的方法，
    private ThreadFactory getThreadFactory() {
        return new ThreadFactory() {
            AtomicInteger sn = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                SecurityManager manager = System.getSecurityManager();
                ThreadGroup group = (manager != null) ? manager.getThreadGroup() : Thread.currentThread().getThreadGroup();
                Thread thread = new Thread(group, r);
                thread.setName("任务线程 - " + sn.incrementAndGet());
                return thread;
            }
        };
    }
}