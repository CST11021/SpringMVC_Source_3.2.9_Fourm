package com.whz.javabase.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class OperationImpl extends UnicastRemoteObject implements IOperation {

    /**
     * UnicastRemoteObject类的构造函数抛出了RemoteException，故其继承类不能使用默认构造函数，继承类的构造函数必须也抛出RemoteException
     * @throws RemoteException
     */
    public OperationImpl() throws RemoteException {
        super();
    }

    @Override
    public int add(int a, int b) throws RemoteException {
        return a + b;
    }

}