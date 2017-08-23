package com.whz.designPattern.decoratorPattern;

/*

定义：装饰模式是在不必改变原类文件和使用继承的情况下，动态的扩展一个对象的功能，它是通过创建一个包装对象，也就是装饰或包
    装真实的对象，并在保持类方法签名完整性的前提下，提供了额外的功能。

    用以下三点来概括装饰器模式的特点：
    不改变原类文件。
    不使用继承。
    动态扩展。

    首先我们有一个顶级的抽象类（或是接口），被装饰对象和装饰器都继承自这个抽象类（或实现这个接口），然后装饰器中包含这个
抽象类（或接口）的引用，这样装饰器便可以再不改变被装饰类的同时，动态的扩展被装饰对象的行为。

    JDK中的java.io包中就是使用这种设计模式来实现不同类型输入输出流的。这种设计模式可以防止类爆炸增长。

    下面我们以咖啡店结算为例进行说明，通常我们去喝咖啡需要选一种类型的咖啡，比如拿铁、摩卡或卡布奇若等，然后服务员会问你
是否加糖、加奶等等，这里糖、奶就是用来修饰咖啡的装饰器，最后结算的时候要根据不同的咖啡和配料结算。
 */
public class DecoratorPatternDemo {
    public static void main(String[] args) {
        ICoffee coffee_A = new Coffee_A();
        System.out.println(coffee_A.getDescription() + " $" + coffee_A.cost());

        ICoffee coffee_B = new Coffee_B();
        coffee_B = new Decorator1(coffee_B);
        coffee_B = new Decorator1(coffee_B);
        coffee_B = new Decorator2(coffee_B);
        System.out.println(coffee_B.getDescription() + " $" + coffee_B.cost());
    }
}