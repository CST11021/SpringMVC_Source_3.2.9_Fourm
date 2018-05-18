package com.whz.javabase.serialize.serializable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class DeserializeDemo {
    public static void main(String[] args) {
        SubEmployee e = null;
        try {
            FileInputStream fileIn = new FileInputStream("D:/employee.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            e = (SubEmployee) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
            return;
        }
        System.out.println("Deserialized Employee...");
        System.out.println("Name: " + e.name);
        System.out.println("Address: " + e.address);
        System.out.println("SSN: " + e.SSN);
        System.out.println("Number: " + e.number);
        System.out.println("ID:" + e.getId());
        System.out.println("TestBean" + e.getTestBean());
    }
}