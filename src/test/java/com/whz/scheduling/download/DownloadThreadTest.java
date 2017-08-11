package com.whz.scheduling.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// 负责文件下载的类
public class DownloadThreadTest extends Thread {
    
    // 待下载的文件
    private String url = null;
    // 下载到本地的文件名
    private String fileName = null;
    // 偏移量
    private long offset = 0;
    // 本线程下载的字节长度
    private long length = 0;
    private CountDownLatch end;
    private CloseableHttpClient httpClient;
    private HttpContext context;


    /**
     * @param url      下载文件地址
     * @param fileName 另存文件名
     * @param offset   本线程下载偏移量
     * @param length   本线程下载的字节长度
     * @author Angus.wang
     */
    public DownloadThreadTest(String url, String fileName, long offset, long length, CountDownLatch end, CloseableHttpClient httpClient) {
        this.url = url;
        this.fileName = fileName;
        this.offset = offset;
        this.length = length;
        this.end = end;
        this.httpClient = httpClient;
        this.context = new BasicHttpContext();
        System.out.println("偏移量=" + offset + ";字节数=" + length);
    }

    public void run() {
        try {
            HttpGet httpGet = new HttpGet(this.url);
            httpGet.addHeader("Range", "bytes=" + this.offset + "-" + (this.offset + this.length - 1));
            CloseableHttpResponse response = httpClient.execute(httpGet, context);
            BufferedInputStream bis = new BufferedInputStream(response.getEntity().getContent());
            byte[] buff = new byte[1024];
            int bytesRead;
            File newFile = new File(fileName);
            RandomAccessFile raf = new RandomAccessFile(newFile, "rw");
            while ((bytesRead = bis.read(buff, 0, buff.length)) != -1) {
                raf.seek(this.offset);
                raf.write(buff, 0, bytesRead);
                this.offset = this.offset + bytesRead;
            }
            raf.close();
            bis.close();

        } catch (ClientProtocolException e) {
            System.out.println("DownloadThread exception msg:" + ExceptionUtils.getFullStackTrace(e));
        } catch (IOException e) {
            System.out.println("DownloadThread exception msg:" + ExceptionUtils.getFullStackTrace(e));
        } finally {
            end.countDown();
            System.out.println(end.getCount() + " is go on!");
        }
    }
}