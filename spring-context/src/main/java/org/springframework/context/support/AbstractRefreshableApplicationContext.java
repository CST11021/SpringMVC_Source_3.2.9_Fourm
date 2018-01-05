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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;

/**
 * Base class for {@link org.springframework.context.ApplicationContext}
 * implementations which are supposed to support multiple calls to {@link #refresh()},
 * creating a new internal bean factory instance every time.
 * Typically (but not necessarily), such a context will be driven by
 * a set of config locations to load bean definitions from.
 *
 * <p>The only method to be implemented by subclasses is {@link #loadBeanDefinitions},
 * which gets invoked on each refresh. A concrete implementation is supposed to load
 * bean definitions into the given
 * {@link org.springframework.beans.factory.support.DefaultListableBeanFactory},
 * typically delegating to one or more specific bean definition readers.
 *
 * <p><b>Note that there is a similar base class for WebApplicationContexts.</b>
 * {@link org.springframework.web.context.support.AbstractRefreshableWebApplicationContext}
 * provides the same subclassing strategy, but additionally pre-implements
 * all context functionality for web environments. There is also a
 * pre-defined way to receive config locations for a web context.
 *
 * <p>Concrete standalone subclasses of this base class, reading in a
 * specific bean definition format, are {@link ClassPathXmlApplicationContext}
 * and {@link FileSystemXmlApplicationContext}, which both derive from the
 * common {@link AbstractXmlApplicationContext} base class;
 * {@link org.springframework.context.annotation.AnnotationConfigApplicationContext}
 * supports {@code @Configuration}-annotated classes as a source of bean definitions.
 *
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 1.1.3
 * @see #loadBeanDefinitions
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
 * @see AbstractXmlApplicationContext
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
 */
public abstract class AbstractRefreshableApplicationContext extends AbstractApplicationContext {

	private Boolean allowBeanDefinitionOverriding;
	private Boolean allowCircularReferences;

	private DefaultListableBeanFactory beanFactory;
	// 内部 BeanFactory 的同步监视器
	private final Object beanFactoryMonitor = new Object();


	public AbstractRefreshableApplicationContext() {}
	public AbstractRefreshableApplicationContext(ApplicationContext parent) {
		super(parent);
	}


	// 设置是否允许同名bean覆盖
	public void setAllowBeanDefinitionOverriding(boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
	}

	// 设置是否允许循环依赖
	public void setAllowCircularReferences(boolean allowCircularReferences) {
		this.allowCircularReferences = allowCircularReferences;
	}


	// 刷新BeanFactory，如果已经实例化，则销毁全部的bean实例，关闭容器，然后重新初始化BeanFactory容器
	@Override
	protected final void refreshBeanFactory() throws BeansException {
		if (hasBeanFactory()) {
			destroyBeans();
			closeBeanFactory();
		}
		try {
			// 1、xmlBeanFactory继承自DefaultListtableBeanFactoryBean，并提供了XMLBeanDefinitionReader类型的属性，也就是
			// 说DefaultListableBeanFactory是容器的基础，必须首先实例化。
			// 这里仅仅只是初始化一个DefaultListableBeanFactory实例，还未进行xml解析、初始化Bean等操作
			DefaultListableBeanFactory beanFactory = createBeanFactory();
			// 2、为了序列化指定id，如果需要的话，让这个BeanFactory从id反序列化到BeanFactory对象
			beanFactory.setSerializationId(getId());
			// 3、定制BeanFactory，设置相关属性，包括是否允许覆盖同名称的不同定义的对象以及循环依赖，以及设置@Autowired
			// 和@Qualifier注解的解析器 QualifierAnnotationAutowireCandidateResolver
			customizeBeanFactory(beanFactory);
			// 4、加载BeanDefinition，初始化DocumentReader，并进行XML文件读取及解析和注册BeanDefinition
			loadBeanDefinitions(beanFactory);
			synchronized (this.beanFactoryMonitor) {
				// 5、使用全局变量记录BeanFactory类实例，因为DefaultListableBeanFactory类型的变量BeanFactory是函数内的
				// 局部变量，所以要使用全局变量记录解析结果
				this.beanFactory = beanFactory;
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
		}
	}

	@Override
	protected void cancelRefresh(BeansException ex) {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory != null)
				this.beanFactory.setSerializationId(null);
		}
		super.cancelRefresh(ex);
	}

	@Override
	protected final void closeBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			this.beanFactory.setSerializationId(null);
			this.beanFactory = null;
		}
	}

	// 判断该Spring容器是否已经持有内部的IOC容器
	protected final boolean hasBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			return (this.beanFactory != null);
		}
	}

	@Override
	public final ConfigurableListableBeanFactory getBeanFactory() {
		synchronized (this.beanFactoryMonitor) {
			if (this.beanFactory == null) {
				throw new IllegalStateException("BeanFactory not initialized or already closed - " +
						"call 'refresh' before accessing beans via the ApplicationContext");
			}
			return this.beanFactory;
		}
	}


	// 创建Spring容器的内部IOC容器（BeanFactory）,默认实现创建一个DefaultListableBeanFactory实例，该实例会指定一个父容器，
	// 该父容器指向Spring容器的父容器
	protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}

	// 给内部的IOC容器设置：1、是否覆盖同名的bean；2、是否允许循环依赖；3、设置@Qualifier和@Autowire的解析处理器
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
		// 1、如果属性allowBeanDefinitionOverriding不为空，设置给BeanFactory对象相应的属性，
		// 此属性的含义：是否允许覆盖同名称的不同定义的对象
		if (this.allowBeanDefinitionOverriding != null) {
			beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}

		// 2、如果属性allowCircularReference不为空，设置给BeanFactory对象相应属性，
		// 此属性的含义：是否允许bean之间存在循环依赖
		if (this.allowCircularReferences != null) {
			beanFactory.setAllowCircularReferences(this.allowCircularReferences);
		}

		// 3、用于@Qualifier和@Autowire
		beanFactory.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
	}

	// 通过 XmlBeanDefinitionReader 将bean定义加载到给定的bean工厂中
	protected abstract void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException;

}
