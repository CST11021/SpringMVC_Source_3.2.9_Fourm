/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * Extension to the standard {@link BeanFactoryPostProcessor} SPI, allowing for
 * the registration of further bean definitions <i>before</i> regular
 * BeanFactoryPostProcessor detection kicks in. In particular,
 * BeanDefinitionRegistryPostProcessor may register further bean definitions
 * which in turn define BeanFactoryPostProcessor instances.
 *
 * @author Juergen Hoeller
 * @since 3.0.1
 * @see org.springframework.context.annotation.ConfigurationClassPostProcessor
 */
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean definition registry after its standard initialization.
	 * All regular bean definitions will have been loaded, but no beans will have been instantiated yet.
	 * This allows for adding further bean definitions before the next post-processing phase kicks in.
	 * 在标准初始化之后修改应用程序上下文的内部bean定义注册表。所有的常规bean定义都已经加载，但是还没有实例化bean。这允许
	 * 在下一个后处理阶段开始之前添加更多bean定义。
	 * @param registry the bean definition registry used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors

	说明：
		BeanDefinitionRegistryPostProcessor接口继承自BeanFactoryPostProcessor接口，该处理器接口定义在spring-bean模块中，
	但应用于ApplicationContext容器，即该处理器是为ApplicationContext容器扩展而被设计的，BeanFactoryPostProcessor处理器也
	是为扩展而设计的，但是不同的是BeanFactoryPostProcessor可以通过在BeanFactory手工设置该处理器来执行处理器方法，而
	BeanDefinitionRegistryPostProcessor即使在BeanFactory中手工设置也无法被被调用，必须在ApplicationContext中才能被调用；
	该处理器方法的调用时间是在完成 BeanDefinition 注册后，实例化bean之前被调用的，该处理主要用于修改BeanDefinition注册表
	信息，它用于被ApplicationContext调用，在bean注册到ioc后创建实例前修改bean定义和新增bean注册，这个是在context的refresh
	方法调用。BeanDefinitionRegistryPostProcessor 的一个典型应用是扫描指定包及其子包下面拥有指定注解的类，你会发现在
	BeanFactory中并没有使用到该后处理器，该后处理器为Spring容器扩展而设计的，IOC容器只加载一些常规的Bean配置，而像@Service、
	 @Repository、@Compent和@Bean等这些注解定义的Bean是ApplicationContext容器中才扩展出来的，其中
	 BeanDefinitionRegistryPostProcessor 有一个典型的应用是Mybatis中的@Mapper。此外，这里要注意的是@Service、@Repository、
	 @Compent和@Bean这些注解修饰的Bean并不是通过后处理器来注入的，而是通过自定义命名空间解析器来注入的。

	相关的应用：
		1、MapperScannerConfigurer
		在mybatis集成spring的扩展包中（mybatis-spring-xxx.jar），就是通过MapperScannerConfigurer实现
	 BeanDefinitionRegistryPostProcessor接口的postProcessBeanDefinitionRegistry方法来实现扫描@Mapper注解修饰的接口，并向
	 BeanDefinition注册表中注册一系列的AnnotatedBeanDefinition对象，这样Spring就可以在后续的启动流程中向IOC容器注册Mapper
	 接口对象实例了，从而实现Mybatis与Spring的集成。另外，Mybatis中的那些Mapper接口，会通过动态代理的方式生成一个接口的
	 代理实例，从而完成一些持久化操作，这就是为什么Mybatis只需定义Mapper接口而不用实现类的原因；并且通过MapperScannerConfigurer
	 注入的AnnotatedBeanDefinition对象，在实例化完成后其Bean对象是一个Mybatis的MapperFactoryBean对象，该MapperFactoryBean
	 实现了Spring的FactoryBean接口，然后Spring容器在返回Mapper接口对象Bean的时候，就会通过FactoryBean接口来代理这个Mapper
	 接口，该代理操作会委托Mybatis自己来完成。总之，Mybatis集成Spring中的Mapper接口，其本质是一个MapperFactoryBean，
	 MapperFactoryBean实现了FactoryBean，所以每个Mapper对象在实例化的时候会调用FactoryBean#getObject()方法，创建一个Mapper
	 的实例。
	 */
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
