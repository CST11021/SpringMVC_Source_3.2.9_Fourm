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

// 当某些对象的实例化过程过于烦琐，通过XML配置过于复杂，使我们宁愿使用Java代码来完成这个实例化过程的时候，
// 或者，某些第三方库不能直接注册到Spring容器的时候，就可以实现 org.springframework.beans.factory.FactoryBean 接口，
// 给出自己的对象实例化逻辑代码。如果你要在Spring的配置文件中配置 factory-bean 来生成bean实例并注入，你就需要实现该接口
public interface FactoryBean<T> {

	// 返回该工厂创建的实例对象
	T getObject() throws Exception;

	// 该工厂创建实例对象的类型
	Class<?> getObjectType();

	// 该工厂创建的实例是否已单例的形式存在
	boolean isSingleton();

}
