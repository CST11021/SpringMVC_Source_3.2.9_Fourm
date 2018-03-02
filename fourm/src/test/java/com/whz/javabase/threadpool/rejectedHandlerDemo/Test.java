package com.whz.javabase.threadpool.rejectedHandlerDemo;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * 测试线程池的4种拒绝策略
 * 参考链接：http://blog.csdn.net/qq_25806863/article/details/71172823
 *
 * @author wb-whz291815
 * @version $Id: Test.java, v 0.1 2018-03-02 10:21 wb-whz291815 Exp $$
 */
public class Test {

    /**
     * 说明：
     添加第一个任务时，直接执行，任务列表为空。
     添加第二个任务时，因为采用的LinkedBlockingDeque，，并且核心线程正在执行任务，所以会将第二个任务放在队列中，队列中有 线程2.
     添加第三个任务时，也一样会放在队列中，队列中有 线程2，线程3.
     添加第四个任务时，因为核心任务还在运行，而且任务队列已经满了，所以胡直接创建新线程执行第四个任务，。这时线程池中一共就有两个线程在运行了，达到了最大线程数。任务队列中还是有线程2， 线程3.
     添加第五个任务时，再也没有地方能存放和执行这个任务了，就会被线程池拒绝添加，执行拒绝策略的rejectedExecution方法，这里就是执行AbortPolicy的rejectedExecution方法直接抛出异常。
     最终，只有四个线程能完成运行。后面的都被拒绝了。
     * @throws IOException
     */
    @org.junit.Test
    public void testAbortPolicy() throws IOException {
        // 这里需要用try/catch包起来，否则AbortPolicy拒绝策略抛出异常后，其他线程无法执行，在单测中测试才需要try/catch
        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 0,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingDeque<Runnable>(2),
                new ThreadPoolExecutor.AbortPolicy());
            doExecute(executor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.in.read();
    }

    /**
     * 说明：
     *
     * 注意在添加第五个任务，任务5 的时候，同样被线程池拒绝了，因此执行了CallerRunsPolicy的rejectedExecution方法，这个方
     * 法直接执行任务的run方法。因此可以看到任务5是在main线程中执行的。从中也可以看出，因为第五个任务在主线程中运行，所以
     * 主线程就被阻塞了，以至于当第五个任务执行完，添加第六个任务时，前面两个任务已经执行完了，有了空闲线程，因此线程6又
     * 可以添加到线程池中执行了。这个策略的缺点就是可能会阻塞主线程。
     * @throws IOException
     */
    @org.junit.Test
    public void testCallerRunsPolicy() throws IOException {
        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 0,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingDeque<Runnable>(2),
                new ThreadPoolExecutor.CallerRunsPolicy());
            doExecute(executor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.in.read();
    }

    /**
     * 说明：
     *
     * 因此采用这个拒绝策略，会让被线程池拒绝的任务直接抛弃，不会抛异常也不会执行
     *
     * @throws IOException
     */
    @org.junit.Test
    public void testDiscardPolicy() throws IOException {
        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 0,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingDeque<Runnable>(2),
                new ThreadPoolExecutor.DiscardPolicy());
            doExecute(executor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.in.read();
    }

    /**
     * 说明：
     *
     * DiscardOldestPolicy策略的作用是，当任务呗拒绝添加时，会抛弃任务队列中最旧的任务也就是最先加入队列的，再把这个新任务添加进去。
     * @throws IOException
     */
    @org.junit.Test
    public void testDiscardOldestPolicy() throws IOException {
        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 2, 0,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingDeque<Runnable>(2),
                new ThreadPoolExecutor.DiscardOldestPolicy());
            doExecute(executor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.in.read();
    }

    private static void doExecute(ThreadPoolExecutor executor) {
        for (int i = 1; i < 10; i++) {
            System.out.println("添加第" + i + "个任务");
            executor.execute(new MyThread("线程" + i));
            Iterator iterator = executor.getQueue().iterator();
            while (iterator.hasNext()) {
                MyThread thread = (MyThread)iterator.next();
                System.out.println("列表：" + thread.name);
            }
        }
    }

}
