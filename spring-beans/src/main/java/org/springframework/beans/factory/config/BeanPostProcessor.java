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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

// 该接口作用是：如果我们需要在Spring容器完成Bean的实例化，配置和其他的初始化后添加一些自己的逻辑处理，我们就可以定义一个或者多个BeanPostProcessor接口的实现
public interface BeanPostProcessor {

	// Bean 实例化之前执行该方法
	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	// Bean 实例化之后执行该方法
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}
