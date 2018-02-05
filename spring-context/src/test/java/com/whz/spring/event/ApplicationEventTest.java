package com.whz.spring.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApplicationEventTest {

	public static void main(String[] args) {
		String res = "com/whz/spring/event/spring-event.xml";
		ApplicationContext ctx = new ClassPathXmlApplicationContext(res);
		MailSender mailSender = ctx.getBean(MailSender.class);
		// 邮件发送器发送完一封邮件后会，会通过Spring容器向所有监听器发送一个MailSendEvent事件
		mailSender.sendMail("王小明");
	    System.out.println("done.");
	}

}
