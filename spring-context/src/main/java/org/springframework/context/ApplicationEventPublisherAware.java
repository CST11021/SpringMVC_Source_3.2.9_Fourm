/*
 * Copyright 2002-2011 the original author or authors.
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

import org.springframework.beans.factory.Aware;

// 该接口继承了Aware接口，说明该接口可以在Spring启动时进行自动装配，实现该接口后就可以拥有了Spring的事件发布功能了
public interface ApplicationEventPublisherAware extends Aware {

	void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher);

}
