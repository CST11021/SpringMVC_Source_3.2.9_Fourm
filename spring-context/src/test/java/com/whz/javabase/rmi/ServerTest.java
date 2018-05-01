package com.whz.javabase.rmi;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerTest {

    public static void main(String args[]) throws Exception {

        // 以1099作为LocateRegistry接收客户端请求的端口，并注册服务的映射关系
        Registry registry = LocateRegistry.createRegistry(1099);

        IOperation iOperation = new OperationImpl();
        Naming.rebind("rmi://localhost:1099/Operation", iOperation);

        System.out.println("service running...");
    }

}