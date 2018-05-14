package com.whz.javabase.socket.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * DatagramSocket只允许数据报发送给指定的目标地址，而MulticastSocket可以将数据报以广播的方式发送到多个客户端
 * <p>
 * 若要使用多点广播，则需要让一个数据报标有一组目标主机地址，当数据报发出后，整个组的所有所有主机都能收到该数据报。IP多点广播（或多点发送）实现了将单一信息发送到多个接受者的广播，其思想是设置一组特殊网络地址作为多点广播地址，每一个多点广播地址都被看做一个组，当客户端需要发送、接收广播信息时，加入到改组即可。
 * <p>
 * MulticastSocket既可以将数据报发送到多点广播地址，也可以接收其他主机的广播信息。
 * <p>
 * 应用程序只将数据报包发送给组播地址，路由器将确保包被发送到改组播组中的所有主机。
 * 组播地址：称为组播组的一组主机所共享的地址。组播地址的范围在224.0.0.0--- 239.255.255.255之间（都为D类地址 1110开头）。
 * <p>
 * 备注：如果现在有三台机器A、B、C，三台机器IP地址都不一样，A\B为server监听广播消息，C为客户端发送广播消息，个人理解是将A、B两台机器的MulticastSocket对象绑定在组播地址中的其中一个，然后C客户端发送消息的组播地址一致，则A、B就能够接收C发送的消息。
 * <p>
 * 如果MulticastSocket用于接收信息则使用默认地址和随机端口即可，但是如果用来接收信息，则必须要指定端口，否则发送方无法确定发送数据报的目标端口。
 */
public class MulticastSender {
    private int port;
    private String host;
    private String data;

    public MulticastSender(String data, String host, int port) {
        this.data = data;
        this.host = host;
        this.port = port;
    }

    public void send() {
        try {
            InetAddress ip = InetAddress.getByName(this.host);
            DatagramPacket packet = new DatagramPacket(this.data.getBytes(), this.data.length(), ip, this.port);
            MulticastSocket ms = new MulticastSocket();
            ms.send(packet);
            ms.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 1234;
        String host = "224.0.0.1";
        String data = "hello world.";
        System.out.println(data);
        MulticastSender ms = new MulticastSender(data, host, port);
        ms.send();
    }
}  