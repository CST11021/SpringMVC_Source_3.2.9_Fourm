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

// 该接口作用是：如果我们需要在Spring容器完成Bean的实例化，配置和其他的初始化后添加一些自己的逻辑处理，我们就可以定义一个
// 或者多个BeanPostProcessor接口的实现
public interface BeanPostProcessor {

	/**
	 1、BeanPostProcessor#postProcessBeforeInitialization
	 2、@PostConstruct修饰的方法
	 3、InitializingBean#afterPropertiesSet：设置完Bean的所有属性之后被调用
	 4、调用<bean>配置中的init-method方法
	 */
	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	/**
	 1、调用<bean>配置中的init-method方法
	 2、BeanPostProcessor#postProcessAfterInitialization：Bean执行初始化方法后被调用
	 3、@PreDestroy修饰的方法
	 4、DisposableBean#destroy：在bean被销毁的时候调用
	 5、调用<bean>配置中的destroy-method方法
	 */
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;

}
