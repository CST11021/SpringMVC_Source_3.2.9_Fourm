package com.whz.javabase.atomic;

import java.io.IOException;
import java.util.concurrent.atomic.*;

/**
 * Created by wb-whz291815 on 2017/8/24.
 */
/**

 原子更新的基本类型有以下三个：AtomicBoolean、AtomicInteger、AtomicLong
 原子更新数组，Atomic包提供了以下几个类：AtomicIntegerArray、AtomicLongArray、AtomicReferenceArray

 AtomicReference：原子更新引用类型的值，比如AtomicReference<User>

 AtomicReferenceFieldUpdater:原子更新引用类型里的字段
 AtomicMarkableReference:原子更新带有标记位的引用类型

 原子更新字段值：
 AtomicIntegerFieldUpdater:原子更新整形的字段的更新器
 AtomicLongFieldUpdater:原子更新长整形的字段的更新器
 AtomicStampedReference:原子更新带有版本号的引用类型的更新器



以下是所有的原子操作类：
     AtomicBoolean、

     AtomicInteger、AtomicIntegerArray、AtomicIntegerFieldUpdater

     AtomicLong、AtomicLongArray、AtomicLongFieldUpdater、

     AtomicReference、AtomicReferenceArray、AtomicReferenceFieldUpdater、

     AtomicMarkableReference、AtomicStampedReference

 */
public class Test {

    // 测试：AtomicBoolean 原子类的使用
    @org.junit.Test
    public void testAtomicBoolean() throws IOException {
        // 会存在并发问题
        Work.main(null);

        // 不会存在并发问题
        //Work_ByAtomicBoolean.main(null);
    }

    // 测试：AtomicInteger 和 AtomicLong 的用法类似，这里以 AtomicInteger 为例
    /**
     AtomicInteger的常用方法如下：
         int addAndGet(int delta) ：以原子方式将输入的数值与实例中的值（AtomicInteger里的value）相加，并返回结果
         boolean compareAndSet(int expect, int update) ：如果输入的数值等于预期值，则以原子方式将该值设置为输入的值。
         int getAndIncrement()：以原子方式将当前值加1，注意：这里返回的是自增前的值。
         void lazySet(int newValue)：最终会设置成newValue，使用lazySet设置值后，可能导致其他线程在之后的一小段时间内还是可以读到旧的值。关于该方法的更多信息可以参考并发网翻译的一篇文章《AtomicLong.lazySet是如何工作的？》
         int getAndSet(int newValue)：以原子方式设置为newValue的值，并返回旧值。
     */
    @org.junit.Test
    public void testAtomicInteger() throws IOException {
        AtomicInteger ai = new AtomicInteger(1);
        System.out.println(ai.getAndIncrement());
        System.out.println(ai.get());
    }

    @org.junit.Test
    public void testAtomicIntegerArray() throws IOException {
        int[] value = new int[] { 1, 2 };
        AtomicIntegerArray ai = new AtomicIntegerArray(value);
        ai.getAndSet(0, 3);
        System.out.println(ai.get(0));
        System.out.println(value[0]);
    }


    @org.junit.Test
    public void testAtomicReference() throws IOException {
        AtomicReference<User> atomicUserRef = new AtomicReference<User>();
        User user = new User("conan", 15);
        atomicUserRef.set(user);
        User updateUser = new User("Shinichi", 17);
        atomicUserRef.compareAndSet(user, updateUser);
        System.out.println(atomicUserRef.get().getName());
        System.out.println(atomicUserRef.get().getOld());
    }

    /**
        如果我们只需要某个类里的某个字段，那么就需要使用原子更新字段类，Atomic包提供了以下三个类：
     AtomicIntegerFieldUpdater：原子更新整型的字段的更新器。
     AtomicLongFieldUpdater：原子更新长整型字段的更新器。
     AtomicStampedReference：原子更新带有版本号的引用类型。该类将整数值与引用关联起来，可用于原子的更数据和数据的版本号，
                             可以解决使用CAS进行原子更新时，可能出现的ABA问题。

        原子更新字段类都是抽象类，每次使用都时候必须使用静态方法newUpdater创建一个更新器。原子更新类的字段的必须使用
     public volatile修饰符。AtomicIntegerFieldUpdater的例子代码如下：
     */
    @org.junit.Test
    public void testAtomicIntegerFieldUpdater() throws IOException {
        AtomicIntegerFieldUpdater<User> a = AtomicIntegerFieldUpdater
                .newUpdater(User.class, "old");

        User conan = new User("conan", 10);
        System.out.println(a.getAndIncrement(conan));
        System.out.println(a.get(conan));
    }


}
