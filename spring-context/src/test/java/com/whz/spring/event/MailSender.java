package com.whz.spring.event;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

//模拟邮件发送器，它向目的地发送邮件时，将产生一个MailSendEvent的事件，容器中注册了监听该事件的监听器MailSendListener。
public class MailSender implements ApplicationContextAware {

	private ApplicationContext ctx ;

	//ApplicationContextAware的接口方法，以便容器启动时注入容器实例
	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		this.ctx = ctx;
	}

	public void sendMail(String to){
		System.out.println("MailSender:模拟发送邮件...");
		MailSendEvent mse = new MailSendEvent(this.ctx,to);
		//发送邮件后实例化一个发送邮件事件，然后向容器中的所有事件监听器发送事件
		ctx.publishEvent(mse);
	}

}
