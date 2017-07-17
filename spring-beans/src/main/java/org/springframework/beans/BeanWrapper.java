/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.beans;

import java.beans.PropertyDescriptor;

// spring中不允许bean之间直接调用，BeanWrapper就是解决此类问题的
// BeanWrapper是对Bean的包装，其接口中所定义的功能很简单包括设置获取被包装的对象，获取被包装bean的属性描述器，
// 由于BeanWrapper接口是PropertyAccessor的子接口，因此其也可以设置以及访问被包装对象的属性值。
// BeanWrapper大部分情况下是在spring ioc内部进行使用，通过BeanWrapper,spring ioc容器可以用统一的方式来访问bean的属性。用户很少需要直接使用BeanWrapper进行编程。

// 有时候我们会在配置文件中使用如：p:brand="红旗CA72" 或<property name="" value=""/>的形式进行属性值注入，
// Spring 在创建一个bean的BeanWrapper时，是通过调用该bean的构造器或工厂方法进行实例化的，这时候上面的那些属性值是还没有被注入到bean实例中，
// BeanWrapper的作用就是根据BeanDefinition中的属性配置信息，对其进行注入
public interface BeanWrapper extends ConfigurablePropertyAccessor {

	// 返回由该对象包装的bean实例
	Object getWrappedInstance();

	// 返回被包装的bean的类型
	Class<?> getWrappedClass();

	// 返回这个包装对象的propertydescriptors
	PropertyDescriptor[] getPropertyDescriptors();

	// 获取包装对象的特定属性的PropertyDescriptor
	PropertyDescriptor getPropertyDescriptor(String propertyName) throws InvalidPropertyException;

	// 设置是否激活 "auto-growing"
	void setAutoGrowNestedPaths(boolean autoGrowNestedPaths);

	// "auto-growing"的嵌套路径是否被激活
	boolean isAutoGrowNestedPaths();

	// 给数组和集合的“auto-growing”指定一个限制(默认是无限制的)
	void setAutoGrowCollectionLimit(int autoGrowCollectionLimit);

	// 返回的数组和集合自动增长的极限
	int getAutoGrowCollectionLimit();

}
