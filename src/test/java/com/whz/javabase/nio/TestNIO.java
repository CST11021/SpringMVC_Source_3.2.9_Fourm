package com.whz.javabase.nio;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Map;
import java.util.Set;

/**
 * Created by wb-whz291815 on 2017/8/18.
 */
public class TestNIO {

    // 测试：创建一个Buffer实例
    @org.junit.Test
    public void testCreateBuffer() {

        ByteBuffer buffer = ByteBuffer.allocate( 1024 );

//        byte array[] = new byte[1024];
//        ByteBuffer buffer = ByteBuffer.wrap( array );

        buffer.put( (byte)'a' );
        buffer.put( (byte)'b' );
        buffer.put( (byte)'c' );

        buffer.flip();

        System.out.println( (char)buffer.get() );
        System.out.println( (char)buffer.get() );
        System.out.println( (char)buffer.get() );

    }



    // 测试：读取文件内容
    @org.junit.Test
    public void testReadFile1() throws IOException {

        String inputPath = "D:" + File.separator + "temp1.txt";
        FileChannel inChannel = new FileInputStream(inputPath).getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        String str = "";
        while (true) {
            buffer.clear();
            // 将数据从通道读到缓冲区中
            int r = inChannel.read(buffer);
            if (r == -1) break;
            buffer.flip();
            str += getStringFromBufferByGBK(buffer);
        }
        System.out.println(str);
    }
    // 使用GBK编码，将ByteBuffer转为String
    private String getStringFromBufferByGBK(ByteBuffer buffer) {
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
    // 测试：读取文件内容，一个字符一个字符的读
    @org.junit.Test
    public void testReadFile2() throws IOException {
        String inputPath = "D:" + File.separator + "temp1.txt";
        FileInputStream fin = new FileInputStream(inputPath);
        FileChannel fc = fin.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate( 1024 );
        fc.read( buffer );

        buffer.flip();

        int i=0;
        while (buffer.remaining()>0) {
            byte b = buffer.get();
            System.out.println( "Character "+i+": "+((char)b) );
            i++;
        }

        fin.close();
    }


    // 测试：写数据到文件中
    @org.junit.Test
    public void testWriteFile1() throws IOException {
        String outputPath = "D:" + File.separator + "temp1.txt";
        FileChannel outChannel = new FileOutputStream(outputPath).getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        byte[] message = "仅仅只是一段测试".getBytes();
        buffer.put(message);
        buffer.flip();
        outChannel.write(buffer);
    }
    // 测试：写数据到文件中
    @org.junit.Test
    public void testWriteFile2() throws IOException {
        // “Some bytes.”的Unicode编码
        byte message[] = {
                83, 111, 109, 101, 32,
                98, 121, 116, 101, 115, 46 };

        FileOutputStream fout = new FileOutputStream( "writesomebytes.txt" );
        FileChannel fc = fout.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate( 1024 );
        for (int i=0; i<message.length; ++i) {
            buffer.put( message[i] );
        }

        buffer.flip();
        fc.write( buffer );
        fout.close();
    }

    // 测试：文件复制
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
    // 测试：使用直接缓冲区进行文件复制（比testCopyFile效率高）
    @org.junit.Test
    public void testFastCopyFile() throws IOException {

        String inputPath = "D:" + File.separator + "temp1.txt";
        String outputPath = "D:" + File.separator + "temp2.txt";
        FileChannel inChannel = new FileInputStream(inputPath).getChannel();
        FileChannel outChannel = new FileOutputStream(outputPath).getChannel();

        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        while (true) {
            buffer.clear();
            int r = inChannel.read(buffer);
            if (r == -1) break;
            buffer.flip();
            outChannel.write(buffer);
        }
    }



    // 测试：使用类型化的get/put方法
    @org.junit.Test
    public void testTypesInByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(64);

        buffer.putInt(30);
        buffer.putLong(7000000000000L);
        buffer.putDouble(Math.PI);

        buffer.flip();

        System.out.println(buffer.getInt());
        System.out.println(buffer.getLong());
        System.out.println(buffer.getDouble());
    }

    // 测试：使用类型化的Buffer类
    @org.junit.Test
    public void testUseFloatBuffer() {
        FloatBuffer buffer = FloatBuffer.allocate(10);

        for (int i = 0; i < buffer.capacity(); ++i) {
            float f = (float) Math.sin((((float) i) / 10) * (2 * Math.PI));
            buffer.put(f);
        }

        buffer.flip();

        while (buffer.hasRemaining()) {
            float f = buffer.get();
            System.out.println(f);
        }
    }

    // 测试：缓冲区分片和数据共享
    @org.junit.Test
    public void testSliceBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        for (int i = 0; i < buffer.capacity(); ++i) {
            buffer.put((byte) i);
        }

        // 对这个缓冲区分片，以创建一个包含槽3 到槽6 的子缓冲区,子缓冲区的起始和结束位置通过设置position和limit值来指定，然后调用Buffer的slice()方法：
        buffer.position(3);
        buffer.limit(7);
        ByteBuffer slice = buffer.slice();

        // 更新子缓冲区数据
        for (int i = 0; i < slice.capacity(); ++i) {
            byte b = slice.get(i);
            b *= 11;
            slice.put(i, b);
        }

        // 输出原缓冲区中的内容
        buffer.position(0);
        buffer.limit(buffer.capacity());
        while (buffer.remaining() > 0) {
            System.out.println(buffer.get());
        }
    }

    // 测试：内存映射文件 I/O
    @org.junit.Test
    public void testUseMappedFile() throws IOException {
        final int start = 0;
        final int size = 1024;

        String filePath = "D:" + File.separator + "usemappedfile.txt";
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        FileChannel fc = raf.getChannel();

        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, start, size);

        mbb.put(0, (byte) 97);// 向文件写入“a”
        mbb.put(1023, (byte) 122);// 向文件写入“z”
        raf.close();
    }

    // 测试：文件的分散读取和聚集写入，FileChannel 实现了 ScatteringByteChannel 和 GatheringByteChannel 接口
    @org.junit.Test
    public void testUseScatterGather1() throws IOException {
        String inputPath = "D:" + File.separator + "temp1.txt";
        String outputPath = "D:" + File.separator + "temp2.txt";

        RandomAccessFile randomAccessFile1 = new RandomAccessFile(inputPath, "rw");
        ScatteringByteChannel scatteringByteChannel = randomAccessFile1.getChannel();

        ByteBuffer buf1 = ByteBuffer.allocate(3);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        // 分散读取
        ByteBuffer[] bufs = {buf1, buf2};
        scatteringByteChannel.read(bufs);

        for (ByteBuffer byteBuffer : bufs) {
            byteBuffer.flip();
        }
        System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
        System.out.println("----------------------------");
        System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));

        // 聚集写入
        RandomAccessFile randomAccessFile2 = new RandomAccessFile(outputPath, "rw");
        GatheringByteChannel gatheringByteChannel = randomAccessFile2.getChannel();
        gatheringByteChannel.write(bufs);

    }
    @org.junit.Test
    public void testUseScatterGather2() throws IOException {

        int firstHeaderLength = 2;
        int secondHeaderLength = 4;
        int bodyLength = 6;

        ServerSocketChannel ssc = ServerSocketChannel.open();
        int port = 8080;
        InetSocketAddress address = new InetSocketAddress(port);
        ssc.socket().bind(address);

        int messageLength = firstHeaderLength + secondHeaderLength + bodyLength;

        ByteBuffer buffers[] = new ByteBuffer[3];
        buffers[0] = ByteBuffer.allocate(firstHeaderLength);
        buffers[1] = ByteBuffer.allocate(secondHeaderLength);
        buffers[2] = ByteBuffer.allocate(bodyLength);

        SocketChannel sc = ssc.accept();
        while (true) {

            // Scatter-read into buffers
            int bytesRead = 0;
            while (bytesRead < messageLength) {
                long r = sc.read(buffers);
                bytesRead += r;

                System.out.println("r " + r);
                for (int i = 0; i < buffers.length; ++i) {
                    ByteBuffer bb = buffers[i];
                    System.out.println("b " + i + " " + bb.position() + " " + bb.limit());
                }
            }

            // Process message here

            // Flip buffers
            for (int i = 0; i < buffers.length; ++i) {
                ByteBuffer bb = buffers[i];
                bb.flip();
            }

            // Scatter-write back out
            long bytesWritten = 0;
            while (bytesWritten < messageLength) {
                long r = sc.write(buffers);
                bytesWritten += r;
            }

            // Clear buffers
            for (int i = 0; i < buffers.length; ++i) {
                ByteBuffer bb = buffers[i];
                bb.clear();
            }

            System.out.println(bytesRead + " " + bytesWritten + " " + messageLength);
        }
    }


    // 测试：文件锁定
    @org.junit.Test
    public void testUseFileLocks() throws IOException {
        int start = 10;
        int end = 20;

        String filePath = "D:" + File.separator + "usefilelocks.txt";
        RandomAccessFile raf = new RandomAccessFile( filePath, "rw" );
        FileChannel fc = raf.getChannel();

        // 获取文件锁
        System.out.println( "trying to get lock" );
        FileLock lock = fc.lock( start, end, false );
        System.out.println( "got lock!" );

        // 将文件锁定3秒，模拟操作文件，比如向文件写入数据
        System.out.println( "pausing" );
        try { Thread.sleep( 3000 ); } catch( InterruptedException ie ) {}

        // 释放锁
        System.out.println( "going to release lock" );
        lock.release();
        System.out.println( "released lock" );

        raf.close();
    }


    // 测试：使用编码器和解码器进行字节、字符转换
    @org.junit.Test
    public void testUseCharsets1() throws IOException {
        String inputFile = "D:" + File.separator + "samplein.txt";
        String outputFile = "D:" + File.separator + "sampleout.txt";

        RandomAccessFile inf = new RandomAccessFile(inputFile, "r");
        RandomAccessFile outf = new RandomAccessFile(outputFile, "rw");

        FileChannel inc = inf.getChannel();
        FileChannel outc = outf.getChannel();

        long inputLength = new File(inputFile).length();
        MappedByteBuffer inputData = inc.map(FileChannel.MapMode.READ_ONLY, 0, inputLength);

        // 创建 ISO-8859-1 (Latin1) 字符集的一个实例
        Charset latin1 = Charset.forName("ISO-8859-1");
        CharsetDecoder decoder = latin1.newDecoder();// 编码器
        CharsetEncoder encoder = latin1.newEncoder();// 解码器

        // 使用编码器：将 字节 转为 字符
        CharBuffer cb = decoder.decode(inputData);
        // 使用解码器：将 字符 转为 字节
        ByteBuffer outputData = encoder.encode(cb);

        // 要写回数据，我们必须使用 CharsetEncoder 将它转换回字节，在转换完成之后，我们就可以将数据写到文件中了
        outc.write(outputData);
        inf.close();
        outf.close();
    }
    @org.junit.Test
    public void testUseCharsets2() throws CharacterCodingException {

        // 打印所有的编码集
        Map<String, Charset> map = Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> set = map.entrySet();
        for (Map.Entry<String, Charset> entry : set) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }


        Charset charset_GBK = Charset.forName("GBK");
        CharsetEncoder charsetEncoder = charset_GBK.newEncoder();// 获取编码器
        CharsetDecoder charsetDecoder = charset_GBK.newDecoder();// 获取解码器

        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("hello world!");
        cBuf.flip();

        // 使用编码器，生成字节
        ByteBuffer bBuf = charsetEncoder.encode(cBuf);
        for (int i = 0; i < 12; i++) {
            System.out.println(bBuf.get());
        }

        // 使用解码器，解析字节
        bBuf.flip();
        CharBuffer cBuf2 = charsetDecoder.decode(bBuf);
        System.out.println(cBuf2.toString());


        // 直接使用 Charset 进行解码
        Charset charset_UTF8 = Charset.forName("UTF-8"); //"GBK"
        bBuf.flip();
        CharBuffer cBuf3 = charset_UTF8.decode(bBuf);
        System.out.println(cBuf3);

    }




}
