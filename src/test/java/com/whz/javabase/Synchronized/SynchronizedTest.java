package com.whz.javabase.Synchronized;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.Lock;

/*
Synchronized的实现原理，Synchronized的语义底层是通过一个monitor的对象来完成，其实wait/notify等方法也依赖于monitor对象，
这就是为什么只有在同步的块或者方法中才能调用wait/notify等方法，否则会抛出java.lang.IllegalMonitorStateException的异常的原因。
 */
public class SynchronizedTest {

    // 测试：没有加同步的两个方法
    @Test
    public void testWithoutSync() throws IOException {
        final SynchronizedTest test = new SynchronizedTest();
        new Thread(new Runnable() {
            @Override
            public void run() {
                test.method1();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                test.method2();
            }
        }).start();

        System.in.read();// 如果没有添加这行代码可能会导致上面的两个线程没有执行完成就会销毁了
    }
    public void method1() {
        System.out.println("Method 1 start");
        try {
            System.out.println("Method 1 execute");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Method 1 end");
    }
    public void method2() {
        System.out.println("Method 2 start");
        try {
            System.out.println("Method 2 execute");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Method 2 end");
    }

    // 测试：同步方法
    @Test
    public void testWithSync() throws IOException {
        final SynchronizedTest test = new SynchronizedTest();

        new Thread(new Runnable() {
            @Override
            public void run() {
                test.methodSync1();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                test.methodSync2();
            }
        }).start();

        System.in.read();// 如果没有添加这行代码可能会导致上面的两个线程没有执行完成就会销毁了
    }
    public synchronized void methodSync1(){
        System.out.println("Method 1 start");
        try {
            System.out.println("Method 1 execute");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Method 1 end");
    }
    public synchronized void methodSync2(){
        System.out.println("Method 2 start");
        try {
            System.out.println("Method 2 execute");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Method 2 end");
    }

    // 测试：同步代码块
    Object objLock = new Object();
    @Test
    public void testWithSyncBlock() throws IOException {
        final SynchronizedTest test = new SynchronizedTest();

        new Thread(new Runnable() {
            @Override
            public void run() {
                methodSyncBlock1();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                methodSyncBlock2();
            }
        }).start();

        System.in.read();// 如果没有添加这行代码可能会导致上面的两个线程没有执行完成就会销毁了
    }
    public void methodSyncBlock1() {
        try {
            synchronized (objLock) {
                System.out.println("Method 1 start");
                System.out.println("Method 1 execute");
                Thread.sleep(3000);
            }
            Thread.sleep(1000);
            System.out.println("Method 1 end");
        } catch (InterruptedException e) {e.printStackTrace();}

    }
    public void methodSyncBlock2() {
        try {
            synchronized (objLock) {
                System.out.println("Method 2 start");
                System.out.println("Method 2 execute");
                Thread.sleep(3000);
            }
            System.out.println("Method 2 end");
        } catch (InterruptedException e) {e.printStackTrace();}
    }

    // 测试：静态同步方法，该测试一定是methodStaticSyn1方法先执行
    @Test
    public void testWithStaticSync() throws IOException {
        final SynchronizedTest test1 = new SynchronizedTest();
        final SynchronizedTest test2 = new SynchronizedTest();

        new Thread(new Runnable() {
            @Override
            public void run() {
                test1.methodStaticSyn1();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                test2.methodStaticSyn2();
            }
        }).start();

        System.in.read();// 如果没有添加这行代码可能会导致上面的两个线程没有执行完成就会销毁了
    }
    public static synchronized void methodStaticSyn1(){
        System.out.println("Method 1 start");
        try {
            System.out.println("Method 1 execute");
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Method 1 end");
    }
    public static synchronized void methodStaticSyn2(){
        System.out.println("Method 2 start");
        try {
            System.out.println("Method 2 execute");
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Method 2 end");
    }






}