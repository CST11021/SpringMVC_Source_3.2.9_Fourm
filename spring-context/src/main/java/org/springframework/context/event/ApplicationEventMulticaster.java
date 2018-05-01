/*
 * Copyright 2002-2009 the original author or authors.
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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;


// 该接口用于管理Spring中的事件监听器，使用该接口可以进行事件的监听，事件的发布等功能，以便对应Spring中的监听器进行统一的管理
public interface ApplicationEventMulticaster {


	// 添加监听器
	void addApplicationListener(ApplicationListener listener);
	void addApplicationListenerBean(String listenerBeanName);

	// 移除监听器
	void removeApplicationListener(ApplicationListener listener);
	void removeApplicationListenerBean(String listenerBeanName);
	// 移除所有监听器
	void removeAllListeners();

	// 将给定Spring事件广播给所有相应的监听器，通知监听器该事件发生了
	void multicastEvent(ApplicationEvent event);

}
