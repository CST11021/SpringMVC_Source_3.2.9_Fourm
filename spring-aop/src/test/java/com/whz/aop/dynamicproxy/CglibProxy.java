package com.whz.aop.dynamicproxy;

import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

// 使用CGLIB动态代理，将增强代码织入目标类
public class CglibProxy implements MethodInterceptor {
    private Enhancer enhancer = new Enhancer();

	public Object getProxy(Class clazz) {
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(this);
		//通过字节码技术动态创建子类实例
		return enhancer.create();
	}
	// 这里CGLIB实际上是采用了装饰器的设计模式：在子类中添加或改变原目标类的行为
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {//拦截父类所有方法的调用
		System.out.println("do before");
		Object result = proxy.invokeSuper(obj, args);//通过代理类调用父类中的方法
		System.out.println("do after");
		return result;
	}
}