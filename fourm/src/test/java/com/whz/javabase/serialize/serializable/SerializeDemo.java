package com.whz.javabase.serialize.serializable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SerializeDemo {
    public static void main(String[] args) {
        SubEmployee e = new SubEmployee();
        e.name = "Reyan Ali";
        e.address = "Phokka Kuan, Ambehta Peer";
        e.SSN = 11122333;
        e.number = 101;
        e.setId("1");
        e.setTestBean(new TestBean("test"));
        try {
            FileOutputStream fileOut = new FileOutputStream("D:/employee.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(e);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in /employee.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}