package com.whz.jmx;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;    
import javax.management.ObjectName;
//import com.sun.jdmk.comm.HtmlAdaptorServer;找不到

// 参考：http://tianxingzhe.blog.51cto.com/3390077/1651588/
public class HelloAgent {
    // 运行HelloAgent，打开IE输入http://localhost:8091就可以看到注册的MBean
    public static void main(String[] args) throws Exception {    
        MBeanServer server = MBeanServerFactory.createMBeanServer();    
        ObjectName helloName = new ObjectName("bcndyl:name=HelloWorld");    
        server.registerMBean(new Hello(), helloName);    
        ObjectName adapterName = new ObjectName("HelloAgent:name=htmladapter");    
//        HtmlAdaptorServer adapter = new HtmlAdaptorServer();
//        adapter.setPort(8091);
//        server.registerMBean(adapter, adapterName);
//        adapter.start();
//        System.out.println("start.....");
    }   
    
}