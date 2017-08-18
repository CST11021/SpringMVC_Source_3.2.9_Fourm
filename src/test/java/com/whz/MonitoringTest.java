package com.whz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by wb-whz291815 on 2017/8/18.
 */
public class MonitoringTest {

    // 线程死循环演示
    public static void createBusyThread() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {}
            }
        },"testBusyThread");
        thread.start();
    }

    // 线程锁等待演示
    public static void createLockThread(final Object lock) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    try{
                        lock.wait();
                    } catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        },"testLockThread");
        thread.start();
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        // 主线程暂停，直到用户输入，才开启 testBusyThread 线程
        br.readLine();
        createBusyThread();
        // 主线程暂停，直到用户输入，才开启 testLockThread 线程
        br.readLine();
        Object obj = new Object();
        createLockThread(obj);
        // 主线程终止
    }

}
