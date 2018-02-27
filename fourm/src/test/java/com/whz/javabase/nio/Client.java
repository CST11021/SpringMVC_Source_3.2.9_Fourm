package com.whz.javabase.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class Client {

    public static Selector selector = null;

    public static void main(String[] args) throws IOException {
        selector = Selector.open();
        SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 30000);
        SocketChannel socketChannel = SocketChannel.open(socketAddress);
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        // 启动读取服务器端数据的线程
        new ClientThread().start();

        // 创建键盘输入流
        Scanner scan = new Scanner(System.in);
        while (scan.hasNextLine()) {
            // 读取键盘输入
            String line = scan.nextLine();
            // 将键盘输入的内容输出到SocketChannel中
            socketChannel.write(StandardCharsets.UTF_8.encode(line));
        }
    }

    // 定义读取服务器数据的线程
    private static class ClientThread extends Thread {
        public void run() {
            try {
                while (selector.select() > 0) {

                    for (SelectionKey key : selector.selectedKeys()) {
                        selector.selectedKeys().remove(key);

                        if (key.isReadable()) {
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buff = ByteBuffer.allocate(1024);
                            String content = "";
                            while (sc.read(buff) > 0) {
                                sc.read(buff);
                                buff.flip();
                                content += StandardCharsets.UTF_8.decode(buff);
                            }
                            System.out.println("服务端：" + new Date() + "\n\t" + content);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}