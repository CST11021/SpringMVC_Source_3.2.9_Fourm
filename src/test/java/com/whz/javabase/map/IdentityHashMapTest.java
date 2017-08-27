package com.whz.javabase.map;

import org.junit.Test;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by whz on 2017/8/26.
 */
public class IdentityHashMapTest {


    @Test
    public void test1() {
        IdentityHashMap<String, Object> map = new IdentityHashMap<String, Object>();

        map.put(new String("key"), "1");
        map.put(new String("key"), "2");

        String key = new String("key");
        map.put(key, "3");
        map.put(key, "4");// 3会被4覆盖掉，因为key指向同一个引用

        // 打印3个键值对，并且是无序的
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "    " + entry.getValue());
        }
        System.out.println();

        System.out.println(map.containsKey(new String("key")));// false
        System.out.println(map.containsKey(new String("key")));// false
        System.out.println(map.containsKey("key"));// false
        System.out.println(map.containsKey(key));// true
        System.out.println();

        System.out.println(map.get("key"));// null
        System.out.println(map.get(key));// 4

    }

    @Test
    public void test2() {

        IdentityHashMap<String, Object> map = new IdentityHashMap<String, Object>();
        String key = new String("key");
        map.put(key, "first");
        map.put(new String("key"), "second");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.print(entry.getKey() + "    ");
            System.out.println(entry.getValue());
        }
        System.out.println("idenMap = " + map.containsKey(key));// true
        System.out.println("idenMap = " + map.containsKey("key"));// false

        System.out.println("idenMap = " + map.get(key));// first
        System.out.println("idenMap = " + map.get("key"));// null

    }

    @Test
    public void test3() {

        IdentityHashMap<String, Object> map = new IdentityHashMap<String, Object>();
        String key = new String("key");
        map.put(key, "first");
        map.put(key, "second");

        // 遍历的只有一个键值对
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.print(entry.getKey() + "    ");
            System.out.println(entry.getValue());
        }

        System.out.println("idenMap = " + map.containsKey(key));// true
        System.out.println("idenMap = " + map.get(key));// second

    }

    @Test
    public void test4() {

        IdentityHashMap<String, Object> map = new IdentityHashMap<String, Object>();
        String fsString = new String("xx");
        String secString = new String("xx");
        map.put(fsString, "first");
        map.put(secString, "second");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.print(entry.getKey() + "    ");
            System.out.println(entry.getValue());
        }
        System.out.println("idenMap = " + map.containsKey(fsString));// true
        System.out.println("idenMap = " + map.get(fsString));// first

        System.out.println("idenMap = " + map.containsKey(secString));// true
        System.out.println("idenMap = " + map.get(secString));// second

    }

}
