package com.whz.javabase.concurrent;

import com.whz.utils.DateUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class LinkedBlockingDequeMain {

    public static void main(String[] args) throws Exception {
        LinkedBlockingDeque<String> deque = new LinkedBlockingDeque<String>(3);

        Client client = new Client(deque);
        Thread thread = new Thread(client);
        thread.start();

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                String request = deque.take();
                System.out.println("Consumer: " + request + " - " + getCurrentTime() + " Size " + deque.size());
            }
            TimeUnit.MILLISECONDS.sleep(300);
        }
        System.out.println("Main:End");
    }

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }
}