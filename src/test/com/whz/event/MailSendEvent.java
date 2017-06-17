package com.whz.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

//由事件源 MailSender 发送完邮件后创建该实例，然后向Spring容器注册该事件
public class MailSendEvent extends ApplicationContextEvent {

	//表示发送的目的地
	private String to;
	
	public MailSendEvent(ApplicationContext source, String to) {
		super(source);
		this.to = to;
	}

	public String getTo() {
		return this.to;
	}
}
