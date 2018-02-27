package com.whz.scheduling.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ResourceUtils;


// 文件下载管理类
/*
Java实现大文件下载，基于http方式，控件神马的就不说了。

思路：下载文件无非要读取文件然后写文件，主要这两个步骤，主要难点：

    1.读文件，就是硬盘到内存的过程，由于jdk内存限制，不能读的太大。
    2.写文件，就是响应到浏览器端的过程，http协议是短链接，如果写文件太慢，时间过久，会造成浏览器死掉。

知识点：

    1.org.apache.http.impl.client.CloseableHttpClient  模拟httpClient客户端发送http请求，可以控制到请求文件的字节位置。
    2.BufferedInputStream都熟悉，用它接受请求来的流信息缓存。
    3.RandomAccessFile文件随机类，可以向文件写入指定位置的流信息。

基于以上信息，我的实现思路就是首先判断下载文件大小，配合多线程分割定制http请求数量和请求内容，响应到写入到RandomAccessFile指定位置中。在俗点就是大的http分割成一个个小的http请求，相当于每次请求一个网页。
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/com/whz/scheduling/download/spring-taskExecutor.xml"})
public class DownLoadManagerTest {

    static {
        try {
            File config = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + "log4j2.xml");
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(config),config);
            Configurator.initialize(null, source);
        } catch (Exception e){

        }
    }

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownLoadManagerTest.class);

    // 每个线程下载的字节数
    private static final long UNIT_SIZE = 1000 * 1024;

    @Autowired
    private TaskExecutor taskExecutor;
    private CloseableHttpClient httpClient;
    private Long startTimes;
    private Long endTimes;

    @Before
    public void setUp() {
        startTimes = System.currentTimeMillis();
        System.out.println("测试开始....");

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }
    @After
    public void tearDown() throws Exception {
        endTimes = System.currentTimeMillis();
        System.out.println("测试结束!!");
        System.out.println("********************");
        System.out.println("下载总耗时:" + (endTimes - startTimes) / 1000 + "s");
        System.out.println("********************");
    }

    // 启动多个线程下载文件
    @Test
    public void doDownload() throws IOException {

        // 将文件如：bigFile.rar 放到 D:\Tomcat\webapps\test 目录下，然后启动Tomcat
        String remoteFileUrl = "http://localhost:8080/test/bigFile.rar";
        // 从服务器上下载到本地的路径
        String localPath = "D://downloadTemp//";
        // 获取要下载的文件
        String fileName = new URL(remoteFileUrl).getFile();
        System.out.println("远程服务器上的文件：" + fileName);
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length()).replace("%20", " ");
        String downedFileName = localPath+fileName;
        System.out.println("下载到本地获得文件：" + downedFileName);

        long fileSize = getRemoteFileSize(remoteFileUrl);
//        createFile(localPath + System.currentTimeMillis() + fileName, fileSize);

        // 计算要用多少个线程来下载文件
        Long threadCount = (fileSize / UNIT_SIZE) + (fileSize % UNIT_SIZE != 0 ? 1 : 0);
        System.out.println("需要使用" + threadCount + "个线程来下载文件");

        // 线程下载的偏移量
        long offset = 0;
        CountDownLatch end = new CountDownLatch(threadCount.intValue());

        // 如果文件大小<= UNIT_SIZE ，则只需要一个线程
        if (fileSize <= UNIT_SIZE) {
            DownloadThreadTest downloadThread = new DownloadThreadTest(remoteFileUrl, downedFileName, offset, fileSize, end, httpClient);
            taskExecutor.execute(downloadThread);
        }
        // 多线程下载
        else {
            for (int i = 1; i < threadCount; i++) {
                DownloadThreadTest downloadThread = new DownloadThreadTest(remoteFileUrl, downedFileName, offset, UNIT_SIZE, end, httpClient);
                taskExecutor.execute(downloadThread);
                offset = offset + UNIT_SIZE;
            }

            // 如果不能整除，则需要再创建一个线程下载剩余字节
            if (fileSize % UNIT_SIZE != 0) {
                DownloadThreadTest downloadThread = new DownloadThreadTest(remoteFileUrl, downedFileName, offset, fileSize - UNIT_SIZE * (threadCount - 1), end, httpClient);
                taskExecutor.execute(downloadThread);
            }
        }

        try {
            end.await();
        } catch (InterruptedException e) {
            System.out.println("DownLoadManager exception msg:"+ ExceptionUtils.getFullStackTrace(e));
            e.printStackTrace();
        }

        System.out.println("下载完成！" + localPath + fileName);
    }
    // 获取远程文件的大小
    private long getRemoteFileSize(String remoteFileUrl) throws IOException {
        long fileSize = 0;
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(remoteFileUrl).openConnection();
        httpConnection.setRequestMethod("HEAD");
        int responseCode = httpConnection.getResponseCode();
        if (responseCode >= 400) {
            System.out.println("Web服务器响应错误!");
            return 0;
        }

        String sHeader;
        for (int i = 1; ; i++) {
            sHeader = httpConnection.getHeaderFieldKey(i);
            if (sHeader != null && sHeader.equals("Content-Length")) {
                System.out.println("文件大小ContentLength:" + httpConnection.getContentLength());
                fileSize = Long.parseLong(httpConnection.getHeaderField(sHeader));
                break;
            }
        }
        return fileSize;
    }
    // 创建指定大小的文件
    private void createFile(String fileName, long fileSize) throws IOException {
        System.out.println("创建一个：" + fileName + "文件");
        File newFile = new File(fileName);
        RandomAccessFile raf = new RandomAccessFile(newFile, "rw");
        raf.setLength(fileSize);
        raf.close();
    }

    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }


}