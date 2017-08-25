package com.whz.javabase.threadpool.simpleDemo;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ExecutorTest {

    public static void main(String[] args) {
        ExecutorProcessPool pool = ExecutorProcessPool.getInstance();

        // 测试用Callable来封装任务
        testSubmitTask_Callable(pool);

        // 测试用Runnable来封装任务
//        testSubmitTask_Runnable(pool);

        //关闭线程池，如果是需要长期运行的线程池，不用调用该方法。监听程序退出的时候最好执行一下。
        pool.shutdown();
    }

    private static void testSubmitTask_Callable(ExecutorProcessPool pool) {
        for (int i = 0; i < 10; i++) {
            Future<?> future = pool.submit(new ExcuteTask1(i+""));
            try {
                // 如果接收线程返回值，future.get() 会阻塞，如果这样写主线就会停止，等待结果返回后才去提交下一个任务，
                // 那相当于一个线程一个线程的执行了。所以非特殊情况不建议使用接收返回值的。
                System.out.println(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static void testSubmitTask_Runnable(ExecutorProcessPool pool) {
        for (int i = 0; i < 10; i++) {
            // 1、使用Runnable接口封装任务，然后直接调用线程池的execute方法来执行任务
            //pool.execute(new ExcuteTask2(i+""));

            // 2、使用Runnable接口封装任务，然后将任务提交到线程池，交由线程池去执行
            Future<?> future = pool.submit(new ExcuteTask2(i+""));
            try {
                // 如果是使用Runnable封装任务，那么这里获取的返回值都是null，并且future.get()方法会阻塞
                System.out.println(future.get());
            } catch (Exception e) {e.printStackTrace();}
        }
    }

    //以Callable方式执行任务
    static class ExcuteTask1 implements Callable<String> {
        private String taskName;

        public ExcuteTask1(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public String call() throws Exception {
            long startTime = System.currentTimeMillis();
            try {
                //Java 6/7最佳的休眠方法为TimeUnit.MILLISECONDS.sleep(100);最好不要用 Thread.sleep(100);
                TimeUnit.MILLISECONDS.sleep((int)(Math.random() * 1000));// 1000毫秒以内的随机数，模拟业务逻辑处理
            } catch (Exception e) {
                e.printStackTrace();
            }
            long castTime = System.currentTimeMillis() - startTime;
            System.out.println("-------------这里执行业务逻辑，Callable TaskName = " + taskName + "耗时：" + castTime + "-------------");
            return ">>>>>>>>>>>>>线程返回值，Callable TaskName = " + taskName + "耗时：" + castTime + "<<<<<<<<<<<<<<";
        }
    }

    //以Runable方式执行任务
    static class ExcuteTask2 implements Runnable {
        private String taskName;

        public ExcuteTask2(String taskName) {
            this.taskName = taskName;
        }

        @Override
        public void run() {
            try {
                TimeUnit.MILLISECONDS.sleep((int)(Math.random() * 1000));// 1000毫秒以内的随机数，模拟业务逻辑处理
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("-------------这里执行业务逻辑，Runnable TaskName = " + taskName + "-------------");
        }

    }
}