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
// 任何spring管理的对象可能实现这个接口。那么，当ApplicationContext自身启动和停止时，它将自动调用上下文内所有生命周期的实现。通过委托给LifecycleProcessor来做这个工作。
package org.springframework.context;

/**
 * Strategy interface for processing Lifecycle beans within the ApplicationContext.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 3.0
 */
// 任何spring管理的对象可能实现这个接口。那么，当ApplicationContext自身启动和停止时，它将自动调用上下文内所有生命周期的
// 实现。通过委托给LifecycleProcessor来做这个工作。
// 注意LifecycleProcessor自身扩展了Lifecycle接口。它也增加了两个其他的方法来与上下文交互，使得可以刷新和关闭。
public interface LifecycleProcessor extends Lifecycle {

	// Notification of context refresh, e.g. for auto-starting components.
	void onRefresh();

	// Notification of context close phase, e.g. for auto-stopping components.
	void onClose();

}
