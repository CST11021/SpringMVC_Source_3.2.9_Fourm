package com.whz.event;

import org.springframework.context.ApplicationListener;

//负责监听 MailSendEvent 事件，一旦Spring容器注册了该事件，就调用onApplicationEvent()方法
public class MailSendListener implements ApplicationListener<MailSendEvent>{

	@Override
	public void onApplicationEvent(MailSendEvent event) {
		MailSendEvent mse = (MailSendEvent) event;
		System.out.println("MailSendListener:向‘" + mse.getTo() + "’发送完一封邮件");
	}

}
