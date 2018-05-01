package com.whz.javabase.rmi;

import java.rmi.Naming;

public class ClientTest {

    public static void main(String args[]) throws Exception{
        IOperation iOperation= (IOperation) Naming.lookup("rmi://127.0.0.1:1099/Operation");
        System.out.println(iOperation.add(1,2));
    }

}