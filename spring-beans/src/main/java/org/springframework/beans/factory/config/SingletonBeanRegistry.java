
package org.springframework.beans.factory.config;

// 对单例Bean的操作接口
public interface SingletonBeanRegistry {

	// 注册singletonObject
	void registerSingleton(String beanName, Object singletonObject);

	// 获取beanName对应的单例
	Object getSingleton(String beanName);

	// 判断是否包含指定的单例bean
	boolean containsSingleton(String beanName);

	// 返回所有已经注册的BeanDefinition的beanName
	String[] getSingletonNames();

	// 返回已经注册的单例bean个数
	int getSingletonCount();

}
