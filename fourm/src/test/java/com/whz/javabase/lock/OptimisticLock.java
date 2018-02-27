package com.whz.javabase.lock;

/**
 * 乐观锁
 * <p>
 * 场景:有一个对象value,需要被两个线程调用,由于是共享数据,存在脏数据的问题
 * 悲观锁可以利用synchronized实现,这里不提.现在用乐观锁来解决这个脏数据问题
 */
public class OptimisticLock {
    // 多线程同时调用的操作对象
    public static int value = 0;

    // A线程要执行的方法
    public static void invoke(int Avalue, String i) throws InterruptedException {
        Thread.sleep(1000L);
        if (Avalue != value) {
            System.out.println(Avalue + ":" + value + "A版本不一致,不执行");
            value--;
        } else {
            Avalue++;
            value = Avalue;
            System.out.println(i + ":" + value);
        }
    }

    // B线程要执行的方法
    public static void invoke2(int Bvalue, String i) throws InterruptedException {
        Thread.sleep(1000L);
        // 使用乐观锁定方式，在每次使用value的时候判断一下，value是否被修改，如果没有被修改就可以执行了，如果被修改了就不继续向下执行
        if (Bvalue != value) {
            System.out.println(Bvalue + ":" + value + "B版本不一致,不执行");
        } else {
            System.out.println("B:利用value运算,value=" + Bvalue);
        }
    }

    // 测试,期待结果:B线程执行的时候value数据总是当前最新的
    public static void main(String[] args) throws InterruptedException {
        // A 线程
        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        int Avalue = OptimisticLock.value;// A获取的value
                        OptimisticLock.invoke(Avalue, "A");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // B 线程
        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) {
                        int Bvalue = OptimisticLock.value;// B获取的value
                        OptimisticLock.invoke2(Bvalue, "B");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

/*
        B:利用value运算,value=0
        A:1
        B:利用value运算,value=1
        A:2
        A:3
        B:利用value运算,value=2

分析：上面是可能出现的运行结果，从结果看我们发现，A 将value的值改成了3,但是 B 却没有检查出来,利用错误的value值进行了操作，为什么会这样呢?
其实，这里就回到前面说的乐观锁是有一定的不安全性的,B在检查版本的时候A还没有修改,在B检查完版本后更新数据前(例子中的输出语句),A更改了value值,这时B执行更新数据(例子中的输出语句)就发生了与现存value不一致的现象.





补充：

悲观锁和乐观锁的概念

    悲观锁（Pessimistic Lock）：
        悲观锁，又被称作悲观并发控制，是数据库中一种非常典型且非常严格的并发控制策略。悲观锁具有强烈的独占和排他特性，能
    够有效地避免不同事物对同一数据并发更新而造成的数据一致性问题。在悲观锁的实现原理中，如果一个事务（假定事务A）正在对数
    据进行处理，那么在整个处理过程中，都会将数据出锁定状态，在这期间，其他事务将无法对这个数据进行更新操作，直到事务A完成
    对该数据的处理，释放了对应的锁之后，其他事务才能够重新竞争来对数据进行更新操作。也就是说，对于一份独立的数据，系统只分
    配了一把唯一的钥匙，谁获得了这把钥匙，谁就有权力更新这份数据。一般我们认为，在实际生产应用中，悲观锁策略适合解决那些对
    于数据更新竞争十分激烈的场景，在这类场景中，通常采用简单粗暴的悲观锁机制来解决并发控制问题。

    乐观锁（Optimistic Lock）：
        乐观锁，又被称作乐观并发控制，也是一种常见的并发控制策略。相对于悲观锁而言，乐观锁机制显得更加宽松与友好。从上面
    对悲观锁的讲解中我们可以看到，悲观锁假定不同事物之间的处理一定会出现互相干扰，从而需要在一个事务从头到尾的过程中都对
    数据进行加锁处理。而乐观锁则正好相反，它假定多个事务在处理过程中不会彼此影响，因此在事务处理的绝大部分时间里不需要进
    行加锁处理。当然，既然有并发，就一定会存在数据更新冲突的可能。在乐观锁机制中，在更新请求提交之前，每个事务都会首先检
    查当前事务读取数据后，是否有其他事务对该数据进行了修改。如果其他事务有更新的话，那么正在提交的事务就需要回滚。乐观锁
    通常适合使用在数据并发竞争不大，事务冲突较少的应用场景中。

    总结
        悲观锁每次获取数据的时候都会进行加锁，确保在自己使用的过程中数据不会被别人修改，使用完成后进行数据解锁。由于数据
    进行加锁，期间对该数据进行读写的其他线程都会进行等待。
        乐观锁每次获取数据的时候都不会进行加锁，但是在更新数据的时候需要判断该数据是否被别人修改过。如果数据被其他线程修
    改，则不进行数据更新，如果数据没有被其他线程修改，则进行数据更新。由于数据没有进行加锁，期间该数据可以被其他线程进行
    读写操作。

 */