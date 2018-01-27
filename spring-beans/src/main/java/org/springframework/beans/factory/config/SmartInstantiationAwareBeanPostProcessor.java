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

import java.lang.reflect.Constructor;

import org.springframework.beans.BeansException;

/**
 * Extension of the {@link InstantiationAwareBeanPostProcessor} interface,
 * adding a callback for predicting the eventual type of a processed bean.
 *
 * <p><b>NOTE:</b> This interface is a special purpose interface, mainly for
 * internal use within the framework. In general, application-provided
 * post-processors should simply implement the plain {@link BeanPostProcessor}
 * interface or derive from the {@link InstantiationAwareBeanPostProcessorAdapter}
 * class. New methods might be added to this interface even in point releases.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see InstantiationAwareBeanPostProcessorAdapter
 */
public interface SmartInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessor {

	/** 在调用{@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation}前预测要返回bean的类型*/
	Class<?> predictBeanType(Class<?> beanClass, String beanName) throws BeansException;

	// 确定一个实例化时要用的构造器方法
	Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException;

	/**
	 该方法用于返回早期的Bean引用，即半成品的Bean，已经实例化但是还没有注入属性
	 比如：CircularityA引用CircularityB，CircularityB引用CircularityC，CircularityC引用CircularityA
	 （1）Spring容器创建单例“circularityA” Bean：首先依据无參构造器创建“circularityA”Bean， 并暴露一个ObjectFactory，
	 这个ObjectFactory用于返回提前暴露的circularityA，然后将“circularityA”放到“当前创建的Bean缓存池”中。
	 然后进行setter注入“circularityB”；

	 （2）Spring容器创建单例“circularityB” Bean：首先依据无參构造器创建“circularityB" Bean，并暴露一个ObjectFactory，
	 于返回提前暴露的circularityB。然后将 circularityB 放入“当前创建的Bean缓存池”中，然后进行setter注入 circularityC ；

	 （3）Spring容器创建单例“circularityC” Bean：首先依据无參构造器创建“circularityC”Bean，并暴露一个ObjectFactory，
	 用于返回提前暴露的circularityC。并将 circularityC 放入“当前创建的Bean缓存池”中， 然后进行setter注入 circularityA ；
	 进行注入“circularityA”时因为步骤（1）提前暴露了 circularityA 所以从之前的Cache里面拿BeanA，而不用反复创建。

	 （4）最后在依赖注入“circularityB”和“circularityA”也是从catch里面拿提前暴露的bean。 完毕setter注入。

	 	该方法中，如果入参bean是 circularityA 这个Bean，则在第一次创建circularityA时会返回一个半成品的Bean，已经实例化但
	 是还没有注入属性，我们称这个半成品的bean为exposedObject，即早期暴露的Bean。当circularityC创建时，会先注入这个半成品
	 beanA，这样就先完成了BeanC的创建，接着会完成BeanC的创建，到最后BeanA时，BeanC已经完成了创建，所以BeanA也就可以顺利完
	 成。
	 	此外，对于“prototype”作用域Bean。Spring容器无法完毕依赖注入，由于“prototype”作用域的Bean，Spring容器不进行缓
	 存，因此无法提前暴露一个创建中的Bean。
	 	还有就是，构造函数循环依赖注入时，也会抛异常。
	 */
	Object getEarlyBeanReference(Object bean, String beanName) throws BeansException;

}
