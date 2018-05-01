package com.whz.javabase.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IOperation extends Remote {

    /**
     * 由于远程方法调用的本质依然是网络通信，只不过隐藏了底层实现，网络通信是经常会出现异常的，所以接口的所有方法都必须抛出RemoteException以说明该方法是有风险的
     * 另外，由于方法参数与返回值最终都将在网络上传输，故必须是可序列化的
     * @param a
     * @param b
     * @return
     */
    int add(int a, int b) throws RemoteException;

}