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


// bean在销毁的时候要释放资源，需要实现该接口。
// 如果处理一个缓存的单例对象，那么一个BeanFactory应该调用销毁方法。
// 应用程序上下文应该在关闭的情况下处理所有的单例。
public interface DisposableBean {

	// 该方法在单例对象被销毁的时候，会被BeanFactory调用
	void destroy() throws Exception;

}
