package com.whz.spring.jmx.notification;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

public class Jack extends NotificationBroadcasterSupport implements JackMBean {

    private int seq = 0;

    // 该方法被调用后会通过监听机制通知所有的MBean
    public void hi() {
         //创建一个信息包：通知名称；谁发起的通知；序列号；发起通知时间；发送的消息
        Notification notify = new Notification("jack.hi",this, ++seq, System.currentTimeMillis(),"jack");
        sendNotification(notify);
    }

}