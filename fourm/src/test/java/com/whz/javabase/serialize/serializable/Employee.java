package com.whz.javabase.serialize.serializable;

// 当一个父类实现序列化，子类自动实现序列化，不需要显式实现Serializable接口
public class Employee implements java.io.Serializable {
    /**
     * 一个类可序列化,serialVersionUID建议给一个确定的值,不要由系统自动生成,否则在增减字段(或修改字段类型及长度)时,如果两边的类
     * 的版本不同会导致反序列化抛异常
     *
     * 序列化时为了保持版本的兼容性，即在版本升级时反序列化仍保持对象的唯一性。有两种生成方式
     *  一个是默认的1L，比如：
     *  private static final long serialVersionUID = 1L;
     *
     *  一个是根据类名、接口名、成员方法及属性等来生成一个64位的哈希字段，比如：
     *  private static final long serialVersionUID = xxxxL;
     *
     *  如何没有声明serialVersionUID，则java会使用第二种方式生成一个值，这样会导致在版本升级（比如修改字段等）时，反序列化时生
     *  成的serialVersionUID和升级前的serialVersionUID不一致的情况，这样会导致反序列化时抛异常，另外，如果声明了serialVersionUID
     *  反序列化不会抛出异常，但是修改或新增字段的值也无法被反序列。
     */
    private static final long serialVersionUID = -8427931390159016894L;
    public String name;
    public String address;
    public transient int SSN;
    public int number;

    public void mailCheck() {
        System.out.println("Mailing a check to " + name + " " + address);
    }
}