package com.whz.designPattern.prototypePattern;

/**
 原型模式
 通常我们创建一个对象都是通过new操作来实现的，但是，当对象的构造函数非常复杂，在生成新对象的时候非常耗时间、耗资源的情况下，比如你可能需要读取外部文件，进行数据库查询又或是进行复杂算法，使用new这种方式是很耗性能的，在这种情况下，我们使用原型模式的设计思想来实现快速创建一个对象而不需要提供专门的new()操作就可以快速完成对象的创建。Prototype模式中实现起来最困难的地方就是内存复制操作，所幸在Java中提供了clone()方法替我们做了绝大部分事情。

 原型对象的概念
 通过复制（克隆）一个指定类型的对象来创建更多同类型的对象，我们称这个指定的对象可被称为“原型”对象。

 克隆和new的区别
 克隆类似于new，但是不同于new。new创建新的对象属性采用的是默认值，克隆出的对象的属性值完全和原型对象相同，并且改变克隆出的新对象是不会影响原型对象的。 使用原型模式创建对象比直接new一个对象在性能上要好的多，因为Object类的clone方法是一个本地方法，它直接操作内存中的二进制流，特别是复制大对象时，性能的差别非常明显。

 原型模式的注意事项
 原型模式很少单独出现，一般是和工厂方法模式一起出现，通过clone的方法创建一个对象，然后由工厂方法提供给调用者。
 使用原型模式复制对象时不会调用类的构造方法，因为对象的复制是通过调用Object类的clone方法来完成的，它直接在内存中复制数据，因此不会调用到类的构造方法。不但构造方法中的代码不会执行，甚至连访问权限都对原型模式无效。在单例模式中，只要将构造方法的访问权限设置为private型，就可以实现单例。但是clone方法直接无视构造方法的权限，所以，单例模式与原型模式是冲突的，在使用时要特别注意。

 克隆分为浅克隆和深克隆两种
 Object类的clone方法只会拷贝对象中的基本的数据类型，对于数组、容器对象、引用对象等都不会拷贝，这就是浅拷贝。如果要实现深拷贝，必须将原型模式中的数组、容器对象、引用对象等另行拷贝。例如：
 public class Prototype implements Cloneable {
     private ArrayList list = new ArrayList();
     public Prototype clone() {
         Prototype prototype = null;
         try {
             prototype = (Prototype) super.clone();
             prototype.list = (ArrayList) this.list.clone();
         } catch (CloneNotSupportedException e) {
            e.printStackTrace();
         }
         return prototype;
     }
 }


 */
public class PrototypePatternDemo {

    public static void main(String[] args) {
        ShapeCache.loadCache();

        Shape clonedShape = ShapeCache.getShape("1");
        System.out.println("Shape : " + clonedShape.getType());

        Shape clonedShape2 = ShapeCache.getShape("2");
        System.out.println("Shape : " + clonedShape2.getType());

        Shape clonedShape3 = ShapeCache.getShape("3");
        Shape clonedShape4 = ShapeCache.getShape("3");
        // 它们指向的内存地址不一样的
        System.out.println(clonedShape3 == clonedShape4);
    }
}