package com.whz.javabase.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;


/**
Selector：
    可以理解为通道的注册表

    1、Selector.select()
        之前的创建、注册等准备都完成之后，就可以坐等准备好的数据到来了。这时候需要知道有多少个通道感兴趣事件已经准备好了。
        这时候有下面三个方法帮你完成这项任务，分别是：
        int selector.select()
        int selector.select(long timeout)
        int selector.selectNow()

        首先讲一下这三个方法准确的作用，它们都是返回有多少个通道已经变成就绪状态。它们的区别是：
        select()是阻塞的，它会一直等到有通道准备就绪、
        select(long timeout)也是阻塞的，它会一直等到有通道准备就绪或者已经超出给定的timeout时间并返回0。
        selectNow()是非阻塞的，如果没有通道就绪就直接返回0。

   2、Selector.selectedKeys()
    通过select()方法知道有若干个通道准备就绪，就可以调用下面的方法来返回相应若干个通道的selectedKey了
    Set<SelectionKey> selectedKeys = selector.selectedKeys()
    获得selectedKeys后，你就可以进行相应的处理了。需要强调的是，每次处理完一个selectionKey之后需要将它在Set中删除，这样下
    次它准备好以后就可以再次添加到Set中来。


SelectionKey
    当Selector发现某些channel中的感兴趣事件发生了，就会返回相对应channel的SelectionKey对象。SelectionKey对象包含着许多信
    息。比如所属通道的channel对象，通过selectionKey.channel()方法就可以得到；还有通道的附加对象，通过selectionKey.attachment()
    方法就可以得到；还可以得到通道那个感兴趣时间发生了通过下面四种方法获得：
    boolean selectionKey.isAcceptable()
    boolean selectionKey.isConnectable()
    boolean selectionKey.isReadable()
    boolean selectionKey.isWritable()




ServerSocketChannel：
    用于创建Socket，Socket用来绑定指定的IP地址和端口

    Selector注册
        让Channel和Selector配合使用需要将channel注册到selector上，这个动作是通过如下代码完成的：
        SelectionKey channel.register(Selector sel,int ops,Object att)

    register()中每一个参数的含义如下：
        第一个参数，就是要将channel注册到哪个Selector
        第二个参数，它是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣，可以监听四种不同类型的事
        件，分别是Connect、Accept、Read和Write，它们四个分别代表的含义是：
            Connect(SelectionKey.OP_CONNECT):一个channel成功连接到另一个服务器——“连接就绪”
            Accept(SelectionKey.OP_ACCEPT):一个ServerSocketchannel准备好接收新进入的连接——“接收就绪”
            Read(SelectionKey.OP_READ):一个通道的可读数据已准备好——“读就绪”
            Write(SelectionKey.OP_WRITE):一个通道的可写数据已准备好——“写就绪”

        如果你对不止一种事件感兴趣，那么可以用“位或”操作符将常量连接起来，如下：
        int ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;

        register()方法的第三个参数为附加对象，它可有可无，是一个Object对象，它可以作为每个通道的标识符，用以区别注册在同
        一个Selector上的其他通道，也可以附加其他对象。

 */
// 以下代码参考自：http://www.cnblogs.com/ironPhoenix/p/4206939.html
public class Server {

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        // 通过open方法来打开一个未绑定的ServerSocketChannel实例
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 30000);
        // 将该ServerSocketChannel绑定到指定IP地址
        serverSocketChannel.socket().bind(socketAddress);
        // 设置ServerSocket以非阻塞方式工作
        serverSocketChannel.configureBlocking(false);
        // 将server注册到指定的Selector对象
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // select()方法会阻塞，直到至少有一个已注册的事件发生，当一个或者更多的事件发生时，select()方法将返回所发生的事件的数量。
        while (selector.select() > 0) {
            // 依次处理selector上的每个已选择的SelectionKey
            for (SelectionKey key : selector.selectedKeys()) {
                // 从selector上的已选择Key集中删除正在处理的SelectionKey
                selector.selectedKeys().remove(key);

                // 如果key对应的通道包含客户端的连接请求
                if (key.isAcceptable()) {
                    handlerAccept(serverSocketChannel, selector);
                }

                // 如果key对应的通道有数据需要读取
                if (key.isReadable()) {
                    String requestContext = handlerRead(key);
                    responClient(requestContext, selector);
                }
            }
        }

    }

    public static void handlerAccept(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        // 调用accept方法接受连接，产生服务器端对应的SocketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();
        // 设置采用非阻塞模式
        socketChannel.configureBlocking(false);
        // 将该SocketChannel也注册到selector，SelectionKey.OP_ACCEPT 表示要监听accept事件，也就是在新的连接建立时所发生的事件
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    public static String handlerRead(SelectionKey key) throws IOException {
        // 获取该SelectionKey对应的Channel，该Channel中有可读的数据
        SocketChannel socketChannel = (SocketChannel) key.channel();
        // 定义准备执行读取数据的ByteBuffer
        ByteBuffer buff = ByteBuffer.allocate(1024);
        String requestContext = "";
        // 开始读取数据
        try {
            while (socketChannel.read(buff) > 0) {
                buff.flip();
                requestContext += StandardCharsets.UTF_8.decode(buff);
            }
            // 打印从该sk对应的Channel里读取到的数据
            System.out.println("客户端：" + new Date() + "\n\t" + requestContext);
            return requestContext;
        }
        // 如果捕捉到该sk对应的Channel出现了异常，即表明该Channel对应的Client出现了问题，所以从Selector中取消sk的注册
        catch (IOException ex) {
            // 从Selector中删除指定的SelectionKey
            key.cancel();
            if (key.channel() != null) {
                key.channel().close();
            }
            return null;
        }
    }

    public static void responClient(String requestContent, Selector selector) throws IOException {
        // 如果content的长度大于0，即聊天信息不为空
        if (requestContent.length() > 0) {
            // 遍历该selector里注册的所有SelectKey
            for (SelectionKey key2 : selector.keys()) {
                // 获取该key对应的Channel
                Channel targetChannel = key2.channel();
                // 如果该channel是SocketChannel对象
                if (targetChannel instanceof SocketChannel) {
                    // 将读到的内容写入该Channel中
                    SocketChannel dest = (SocketChannel) targetChannel;
                    dest.write(StandardCharsets.UTF_8.encode("你好，客户端"));
                }
            }
        }
    }


}