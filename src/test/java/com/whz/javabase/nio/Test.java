package com.whz.javabase.nio;

import com.sun.xml.internal.fastinfoset.util.CharArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Created by wb-whz291815 on 2017/8/18.
 */
public class Test {


    public String getString(ByteBuffer buffer) {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            charset = Charset.forName("GBK");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    @org.junit.Test
    public void testReadFile() throws IOException {
        // 获取通道
        String inputPath = "D:" + File.separator + "temp1.txt";
        FileChannel inChannel = new FileInputStream(inputPath).getChannel();
        // 创建缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        String str = "";
        while (true) {
            buffer.clear();
            // 将数据从通道读到缓冲区中
            int r = inChannel.read(buffer);
            if (r == -1) break;
            buffer.flip();
            str += getString(buffer);
        }
        System.out.println(str);
    }

    @org.junit.Test
    public void testWriteFile() throws IOException {

        String outputPath = "D:" + File.separator + "temp1.txt";
        FileChannel outChannel = new FileOutputStream(outputPath).getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        byte[] message = "仅仅只是一段测试".getBytes();
        for (int i=0; i<message.length; ++i) {
            buffer.put( message[i] );
        }
        buffer.flip();
        outChannel.write( buffer );
    }

    @org.junit.Test
    public void testCopyFile() throws IOException {
        String inputPath = "D:" + File.separator + "temp1.txt";
        String outputPath = "D:" + File.separator + "temp2.txt";

        FileChannel inChannel = new FileInputStream(inputPath).getChannel();
        FileChannel outChannel = new FileOutputStream(outputPath).getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (true) {
            buffer.clear();
            int r = inChannel.read(buffer);
            if (r == -1) break;
            buffer.flip();// flip()方法的作用是让缓冲区可以将新读入的数据写入另一个通道中
            outChannel.write(buffer);
        }
    }

}
