package com.whz.i18n;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.GregorianCalendar;
import java.util.Locale;

public class I18nGreeting {

	@Test
	public void rsrBdlMessageResource(){
		String[] configs = {"com/whz/i18n/spring-i18n.xml"};
		ApplicationContext ctx = new ClassPathXmlApplicationContext(configs);
		
		MessageSource ms = (MessageSource)ctx.getBean("myResource1");
		Object[] params = {"John", new GregorianCalendar().getTime()};
		
		String str1 = ms.getMessage("greeting.common",params, Locale.US);
		String str2 = ms.getMessage("greeting.morning",params, Locale.CHINA);
		String str3 = ms.getMessage("greeting.afternoon",params, Locale.CHINA);
		System.out.println(str1);
		System.out.println(str2);
		System.out.println(str3);
		
	}

	@Test
	public void rrsrBdlMessageResource() throws Exception {
		String[] configs = {"com/whz/i18n/spring-i18n.xml"};
		ApplicationContext ctx = new ClassPathXmlApplicationContext(configs);
		
		MessageSource ms = (MessageSource)ctx.getBean("myResource2");
		Object[] params = {"John", new GregorianCalendar().getTime()};
		
		for (int i = 0; i < 2; i++) {
			String str1 = ms.getMessage("greeting.common",params, Locale.US);
			System.out.println(str1);
			Thread.currentThread().sleep(20000);
		}	
	}

	@Test
	public void ctxMessageResource() throws Exception {
		String[] configs = {"com/whz/i18n/spring-i18n.xml"};
		ApplicationContext ctx = new ClassPathXmlApplicationContext(configs);
		Object[] params = {"John", new GregorianCalendar().getTime()};
		
		String str1 = ctx.getMessage("greeting.common",params, Locale.US);
		String str2 = ctx.getMessage("greeting.morning",params, Locale.CHINA);
		System.out.println(str1);
		System.out.println(str2);
	}	
	

}
