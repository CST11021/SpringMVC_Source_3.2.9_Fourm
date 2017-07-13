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

/**
 * Interface to be implemented by beans that need to react once all their properties have been set by a BeanFactory:
 * 要由bean来实现的接口，一旦所有属性都由BeanFactory设置，就需要对其进行响应
 * for example, to perform custom initialization, or merely to check that all mandatory properties have been set.
 * 例如，要执行自定义的初始化，或者仅仅检查所有的强制属性都已经设置好了。
 * <p>An alternative to implementing InitializingBean is specifying a custom
 * init-method, for example in an XML bean definition.
 * For a list of all bean lifecycle methods, see the BeanFactory javadocs.
 *
 * @author Rod Johnson
 * @see BeanNameAware
 * @see BeanFactoryAware
 * @see BeanFactory
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getInitMethodName
 * @see org.springframework.context.ApplicationContextAware
 */
public interface InitializingBean {

	// 在它设置了所有的bean属性之后，由BeanFactory调用
	void afterPropertiesSet() throws Exception;

}
