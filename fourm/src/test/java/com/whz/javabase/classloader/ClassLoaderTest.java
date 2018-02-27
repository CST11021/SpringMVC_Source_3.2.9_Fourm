package com.whz.javabase.classloader;

public class ClassLoaderTest {

    public static void main(String args[]) throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        // 以下三个ClassLoader是同一个对象
        ClassLoader curThreadClassLoader = Thread.currentThread().getContextClassLoader();// 当前线程的类加载器
        ClassLoader curClassLoader = ClassLoaderTest.class.getClassLoader();// 当前类的类加载器
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();// 系统初始的类加载器

        System.out.println("加载当前执行线程的类加载器：: " + curThreadClassLoader);
        System.out.println("加载当前ClassLoaderTest类的 类加载器: " + curClassLoader);
        System.out.println("系统初始的类加载器：: " + sysClassLoader);
        System.out.println("加载当前ClassLoaderTest类的 类加载器的父类加载器：: " + ClassLoaderTest.class.getClassLoader().getParent());

        // 使用当前类加载器，加载ClassLoaderTest
        ClassLoaderTest t1 = (ClassLoaderTest) Class.forName(
                "com.whz.javabase.classloader.ClassLoaderTest", true, ClassLoaderTest.class.getClassLoader()).newInstance();
        // 使用当前类加载器的父加载器，加载ClassLoaderTest
        ClassLoaderTest t2 = (ClassLoaderTest) Class.forName(
                "com.whz.javabase.classloader.ClassLoaderTest", true, ClassLoaderTest.class.getClassLoader().getParent()).newInstance();




    }

}