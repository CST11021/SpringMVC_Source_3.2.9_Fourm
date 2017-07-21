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

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 在使用spring编程时，常常会遇到想根据bean的名称来获取相应的bean对象，这时候，就可以通过实现BeanFactoryAware来满足需求，代码很简单：
 @Service
 public class BeanFactoryHelper implements BeanFactoryAware {
	 private static BeanFactory beanFactory;

	 @Override
	 public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
	 	this.beanFactory = beanFactory;
	 }

	 public static Object getBean(String beanName){
		 if(beanFactory == null){
	 		throw new NullPointerException("BeanFactory is null!");
	 	 }
	 　　return beanFactory.getBean(beanName);
	 }
 }

 还有一种方式是实现ApplicationContextAware接口，代码也很简单：
 @Service
 public class ApplicationContextHelper implements ApplicationContextAware {
 	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

 	public static Object getBean(String beanName){
 		if(applicationContext == null){
 			throw new NullPointerException("ApplicationContext is null!");
 		}
 		return applicationContext.getBean(beanName);
 	}
 }


 上面两种方法，只有容器启动的时候，才会把BeanFactory和ApplicationContext注入到自定义的helper类中，如果在本地junit测试的时候，如果需要根据bean的名称获取bean对象，
 则可以通过ClassPathXmlApplicationContext来获取一个ApplicationContext，代码如下：
 @Test
 public void test() throws SQLException {
	 //通过从classpath中加载spring-mybatis.xml实现bean的获取
	 ApplicationContext context = new ClassPathXmlApplicationContext("spring-mybatis.xml");
	 IUserService userService = (IUserService) context.getBean("userService");

	 User user = new User();
	 user.setName("test");
	 user.setAge(20);
	 userService.addUser(user);
 }

 */






// 容器启动的时候会调用setBeanFactory方法，将当前容器注入到实现该接口的类（比如A）中，这样A便拥有的容器的功能
public interface BeanFactoryAware extends Aware {

	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
