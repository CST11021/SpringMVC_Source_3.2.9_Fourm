/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.scheduling.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * {@link ScheduledTaskRegistrar} subclass that redirects the actual scheduling
 * of tasks to the {@link ContextRefreshedEvent} callback. Falls back to regular
 * {@code ScheduledTaskRegistrar} behavior when not running within an ApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 3.2.1
 */
public class ContextLifecycleScheduledTaskRegistrar extends ScheduledTaskRegistrar
		implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

	private ApplicationContext applicationContext;


	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() {
		if (this.applicationContext == null) {
			scheduleTasks();
		}
	}

	// Spring定时任务的开始时间是在容器刷新的时候，通过监听机制触发的
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext() != this.applicationContext) {
			return;
		}
		scheduleTasks();
	}

}
