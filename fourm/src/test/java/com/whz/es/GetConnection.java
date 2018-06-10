package com.whz.es;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;


public class GetConnection {

    public final static String HOST = "localhost";
    // http请求的端口是9200，客户端是9300
    public final static int PORT = 9300;

    /**
     * getConnection:(获取es连接).
     *
     * @return
     * @throws Exception
     * @author xbq Date:2018年3月21日上午11:52:02
     */
    @SuppressWarnings({"resource", "unchecked"})
    public static TransportClient getConnection() throws Exception {
        // 设置集群名称
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
        // 创建client
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddresses(new TransportAddress(InetAddress.getByName(HOST), PORT));

        client.close();
        return client;
    }

    public static void main(String[] args) throws Exception {
        TransportClient client = getConnection();
        System.out.println("client==" + client.toString());
    }
}