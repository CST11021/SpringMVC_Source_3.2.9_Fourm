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
// 应用及其作用：Application Contexts会通过注册表首先检测该工厂后处理器并使用它。对注册表中的BeanDefinition的属性进行后期加工，
// 负责把XML中有些占位符式的属性还原成真实值。意思是说，有时候，XML中<bean>的属性值不固定，会随着外界因素变化，这时候，在<bean>
// 中配置占位符，而另外定义一个属性文件来控制<bean>的属性。

// BeanDefinitionRegistryPostProcessor 的一个典型应用是扫描指定包及其子包下面拥有指定注解的类，你会发现在BeanFactory中并没有
// 使用到该后处理器，该后处理器为Spring容器扩展而设计的，IOC容器只加载一些常规的Bean配置，而像@Service、@Repository等这些注解
// 定义的Bean是Spring容器中才扩展出来的，其中 BeanDefinitionRegistryPostProcessor 还有一个典型的应用是Mybatis中的@Mapper.
public interface BeanDefinitionRegistryPostProcessor extends BeanFactoryPostProcessor {

	/**
	 * Modify the application context's internal bean definition registry after its standard initialization.
	 * 在标准初始化之后修改应用程序上下文的内部bean定义注册表。
	 * All regular bean definitions will have been loaded, but no beans will have been instantiated yet.
	 * 所有的常规bean定义都已经加载，但是还没有实例化bean。
	 * This allows for adding further bean definitions before the next post-processing phase kicks in.
	 * 这允许在下一个后处理阶段开始之前添加更多bean定义。
	 * @param registry the bean definition registry used by the application context
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	// 在完成 BeanDefinition 注册后，实例化bean之前调用，允许修改BeanDefinition的信息，registry用于被ApplicationContext调用
	void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException;

}
