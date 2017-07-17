/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.context;

/**
 * Interface that encapsulates event publication functionality. Serves as super-interface for ApplicationContext.
 * 封装事件发布功能的接口。作为应用上下文的超级接口。
 * @author Juergen Hoeller
 * @since 1.1.1
 * @see ApplicationContext
 * @see ApplicationEventPublisherAware
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.event.EventPublicationInterceptor
 */
// 让容器拥有发布应用上下文事件的功能，包括容器启动器事件、关闭事件等。实现了ApplicationListener事件监听接口的Bean可以接收到容器事件，并对事件进行响应处理。
// 在ApplicationContext抽象实现类AbstractApplicationContext中，我们可以发现存在一个ApplicationEventMulticaster，它负责保存所有监听器，以便在容器产生上下文事件时通知这些事件监听者。
public interface ApplicationEventPublisher {

	/**
	 * Notify all listeners registered with this application of an application event.
	 * 通知所有注册应用程序事件的侦听器
	 * Events may be framework events (such as RequestHandledEvent) or application-specific events.
	 * 事件可能是框架事件(如RequestHandledEvent)或特定于应用程序的事件。
	 * @param event the event to publish
	 * @see org.springframework.web.context.support.RequestHandledEvent
	 */
	void publishEvent(ApplicationEvent event);

}
