
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

// 实例化之前，对注册表中的BeanDefinition的属性进行后期加工。 Application Contexts会通过注册表首先检测该工厂后处理器并使用它。 主要用于后期要覆盖配置文件中<bean>的属性

// 在加载XML，注册bean definition之后，在实例化bean definition之前，必要的时候要用到BeanFactoryPostProcessor。它负责把XML中有些占位符式的属性还原成真实值。
// 意思是说，有时候，XML中<bean>的属性值不固定，会随着外界因素变化，这时候，在<bean>中配置占位符，而另外定义一个属性文件来控制<bean>的属性。
public interface BeanFactoryPostProcessor {

	// BeanFactoryPostProcessor是在spring容器加载了bean的定义文件之后，在bean实例化之前执行的。接口方法的入参是ConfigurrableListableBeanFactory，使用该参数，可以获取到相关bean的定义信息
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
