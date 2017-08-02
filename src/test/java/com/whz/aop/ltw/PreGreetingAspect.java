package com.whz.aop.ltw;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

// 在 src/META-INF/aop.xml 目录中配置了相关织入信息
@Aspect
public class PreGreetingAspect{
	// 对所有greeTo方法织入增强
	@Before("execution(* greetTo(..))")
	public void beforeGreeting(){
		System.out.println("How are you");
	}

}
