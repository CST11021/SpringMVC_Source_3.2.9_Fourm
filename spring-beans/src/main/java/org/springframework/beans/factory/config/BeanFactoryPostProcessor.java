
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;





// 该接口是在spring容器解析完配置文件（注册了BeanDefinition）之后，在bean实例化之前被调用的。
// 应用及其作用：Application Contexts会通过注册表首先检测该工厂后处理器并使用它。对注册表中的BeanDefinition的属性进行后期加工，负责把XML中有些占位符式的属性还原成真实值。
// 意思是说，有时候，XML中<bean>的属性值不固定，会随着外界因素变化，这时候，在<bean>中配置占位符，而另外定义一个属性文件来控制<bean>的属性。
public interface BeanFactoryPostProcessor {

	// 接口方法的入参是ConfigurrableListableBeanFactory，使用该参数，可以获取到相关bean的定义信息
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
