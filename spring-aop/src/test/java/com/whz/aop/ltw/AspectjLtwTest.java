package com.whz.aop.ltw;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class AspectjLtwTest {

	// -javaagent:C:/Users/wb-whz291815/.m2/repository/org/springframework/spring-instrument/3.2.9.RELEASE/spring-instrument-3.2.9.RELEASE.jar
	// 如果使用JDK8运行需在VM options 中添加：-noverify
	// 如果使用JDK7运行需在VM options 中添加：-XX:-UseSplitVerifier
	public static void main(String[] args) {
		String configPath = "com/whz/aop/ltw/beans.xml";
		ApplicationContext ctx = new ClassPathXmlApplicationContext(configPath);
		Waiter waiter = ctx.getBean(Waiter.class);
		waiter.greetTo("John");
		waiter.serveTo("John");
	}
}
