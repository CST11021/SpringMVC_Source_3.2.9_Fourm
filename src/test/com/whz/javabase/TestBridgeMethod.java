package com.whz.javabase;

import java.lang.reflect.Method;
  
import junit.framework.TestCase;  

// 什么是桥接方法：
// 当父类是泛型，而子类不是泛型时，如果子类重写了父类的方法，那么子类实际上会有两个同名的方法，则子类的这个方法则是桥接方法。
public class TestBridgeMethod extends TestCase {  
  
    public void testBridgeA() {

        class Father {  
            public void test() {}
        }
        class Son extends Father {
            public void test() {}
        }

        Method[] methods = Son.class.getDeclaredMethods();  
        for (Method method : methods) {
            System.out.println(method.toString() + ", is Bridge method:" + method.isBridge());
        }

    }  
      
    public void testBridgeB() {  
        class Father<T> {  
            public void test(T t) {}
        }  
        class Son<T> extends Father<T> {  
            public void test(T t) {}
        }  
  
        Method[] methods = Son.class.getDeclaredMethods();  
        for (Method method : methods) {  
            System.out.println(method.toString() + ", is Bridge method:" + method.isBridge());
        }  
    }  
      
    public void testBridgeC() {  
        class Father<T> {  
            public void test(T t) {}
        }  
        class Son extends Father<String> {  
            public void test(String s) {}
        }  
  
        Method[] methods = Son.class.getDeclaredMethods();  
        for (Method method : methods) {  
            System.out.println(method.toString() + ", is Bridge method:" + method.isBridge());
        }  
    }  
  
} 