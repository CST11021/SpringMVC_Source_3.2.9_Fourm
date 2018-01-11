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

package org.springframework.context;

import java.util.EventListener;

// spring中的事件监听接口，该监听用来监听 E 事件，当E事件发生后，Spring会通过ApplicationEventMulticaster来广播该事件，就
// 是通知所有的监听了该事件的监听器，然后调用监听器的onApplicationEvent()方法
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	// 当该监听器监听到对应的事件发生时，会调用该方法进行相应的处理
	void onApplicationEvent(E event);

}
