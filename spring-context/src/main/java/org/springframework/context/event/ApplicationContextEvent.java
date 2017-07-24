/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

/**
 * Base class for events raised for an {@code ApplicationContext}.
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
// 该类的作用是为了让使用Spring框架的用户扩展的，当我们继承该类后，它便成为Spring中的一个事件，这样我们便可以通过Spring来发布该事件（以便通知对应的事件监听）。
// 注意，该类中的构造器 public ApplicationContextEvent(ApplicationContext source) 它需要注入一个 ApplicationContext ，这样事件的注册操作便可交由Spring类替我们完成。
// 示例:
/**
 public class MailSender implements ApplicationContextAware {

	 private ApplicationContext ctx ;

	 // 实现ApplicationContextAware的接口方法，以便容器启动时注入容器实例
	 @Override
	 public void setApplicationContext(ApplicationContext ctx) throws BeansException {
	 	this.ctx = ctx;
	 }

	 public void sendMail(String to){
	 	System.out.println("MailSender:模拟发送邮件...");
	 	MailSendEvent mse = new MailSendEvent(this.ctx,to);// 向spring注册一个事件
	 	ctx.publishEvent(mse);// spring发布事件
	 }

 }
 */
@SuppressWarnings("serial")
public abstract class ApplicationContextEvent extends ApplicationEvent {

	public ApplicationContextEvent(ApplicationContext source) {
		super(source);
	}

	public final ApplicationContext getApplicationContext() {
		return (ApplicationContext) getSource();
	}

}
