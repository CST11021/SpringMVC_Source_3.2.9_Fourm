package com.whz.javabase.map;

import org.junit.Test;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by whz on 2017/8/26.
 */


public class WeakHashMapTest {

    @Test
    public void test() {
        String a = new String("a");
        String b = new String("b");

        WeakHashMap<String,String> weakmap = new WeakHashMap();
        weakmap.put(a, "aaa");
        weakmap.put(b, "bbb");

        System.gc();

//        a = null;
//        b = null;
        System.gc();

        System.out.println(weakmap.isEmpty());// false
        System.out.println(weakmap.size());// 0
        for(Map.Entry<String,String> entry : weakmap.entrySet()) {
            System.out.println("weakmap:" + entry.getKey() + " : " + entry.getValue());
        }
    }
}
