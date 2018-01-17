package com.whz.aop.advice;

import org.springframework.aop.BeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Test {

    // 参考资料：https://www.jianshu.com/p/1dd6a26c881b

    // 测试使用编程的形式,织入前置增强
    @org.junit.Test
    public void TestBeforeAdvice() {
        // 首先声明一个要被代理的目标对象和增强对象
        WaiterImpl target = new WaiterImpl();
        BeforeAdvice advice = new GreetingBeforeAdvice();

        // 声明一个Spring提供的代理工厂实例，并设置代理的接口，代理的目标类和增强逻辑
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(target.getClass().getInterfaces());
        proxyFactory.setTarget(target);
        proxyFactory.addAdvice(advice);
        proxyFactory.setOptimize(true);

        //生成代理实例
        Waiter proxy = (Waiter) proxyFactory.getProxy();

        proxy.greetTo("John");
        proxy.serveTo("John");

    }

    // 使用spring配置的形式，测试前置、后置和环绕增强
    @org.junit.Test
    public void TestAdvice1() {
        String configPath = "com/whz/aop/advice/spring-aop.xml";
        ApplicationContext ctx = new ClassPathXmlApplicationContext(configPath);
        Waiter waiterProxy = (Waiter)ctx.getBean("waiterProxy");

        waiterProxy.greetTo("John");
        System.out.println();
        waiterProxy.serveTo("John");
    }

/*
    AOP的相关术语

连接点（Joinpoint）
    程序执行的某个特殊位置，如类开始初始化前、类初始化后、类的某个方法调用前、调用后、方法抛出异常后等。我们称这些特定位
    置为“连接点”。（Spring仅支持方法的连接点，即Spring AOP仅在方法调用前、调用后、方法抛出异常时以及方法调用前后这些连
    接点织入增强。）连接点由两个信息确定：第一是用方法表示的程序执行点；第二是用相对点表示的方位。例如在Test.foo()方法执
    行前的连接点，执行点就是Test.foo()，方位是该方法执行前的位置。Spring使用切点提供执行点信息，而方位信息由增强提供。

切点（Pointcut）
    切点通过org.springframework.aop.Pointcut接口进行描述，它是指用来定位到某个方法上的信息。（如果要定位到具体连接点，还
    需提供方位信息。）

增强（Advice）
    增强是指织入到目标类连接点上的程序代码，比如性能监视、事务管理的这些横切逻辑代码。增强还提供了连接点的方位信息。

目标对象（Target）
    被织入增强的类，称为目标对象（目标类）。

引介（Introduction）
    引介是一种特殊的增强，它为类添加一些属性和方法。这样，即使一个业务类原本没有实现某个接口，通过AOP的引介功能，我们可以
    动态地为该业务类添加接口的实现逻辑，让业务类成为这个接口的实现类。

织入（Weaving）
    织入是指将增强（或引介）添加到目标类具体连接点上的这个过程。AOP有三种织入方式：
    1)编译期织入，这要求使用特殊的Java编译器。
    2)类装载期织入，这要求使用特殊的类装载器。
    3)动态代理织入，在运行期为目标类添加增强生成子类的方式。

代理（Proxy）
    一个类被AOP织入增强后，就产生一个结果类，我们称这个结果类为代理（或代理类）。代理类融合了原类和增强逻辑，根据不同的
    代理方式，代理类既可能是和原类具有相同接口的类，也可能是就是原类的子类。

切面（Aspect）
    切面由切点（连接点）和增强（或引介）组成。Spring AOP就是负责实施切面的框架，它将切面所定义的横切逻辑织入到切面所指定
    的连接点中。

*/

}