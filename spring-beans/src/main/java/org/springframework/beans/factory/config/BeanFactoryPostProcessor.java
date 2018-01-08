
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;



// 该接口是在spring容器解析完配置文件（注册了BeanDefinition）之后，在所有bean实例化之前被调用的。
public interface BeanFactoryPostProcessor {

	// 接口方法的入参是ConfigurrableListableBeanFactory，使用该参数，可以获取到相关bean的定义信息
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
