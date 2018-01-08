
package org.springframework.context;

import java.io.Closeable;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;


public interface ConfigurableApplicationContext extends ApplicationContext, Lifecycle, Closeable {

	// 配置文件路径分隔符
	String CONFIG_LOCATION_DELIMITERS = ",; \t\n";

	// 一系列beanName
	String CONVERSION_SERVICE_BEAN_NAME = "conversionService";
	String LOAD_TIME_WEAVER_BEAN_NAME = "loadTimeWeaver";
	String ENVIRONMENT_BEAN_NAME = "environment";
	String SYSTEM_PROPERTIES_BEAN_NAME = "systemProperties";
	String SYSTEM_ENVIRONMENT_BEAN_NAME = "systemEnvironment";



	void setId(String id);
	void setParent(ApplicationContext parent);
	ConfigurableEnvironment getEnvironment();
	void setEnvironment(ConfigurableEnvironment environment);
	// 添加一个BeanFactory后处理器：BeanFactoryPostProcessor是在解析完配置文件并注册了BeanDefinition后，并在所有的bean实例化
	// 之前执行的调用的，一个典型的应用就是解析Bean配置时使用的占位符
	void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor);
	// 添加一个上下文监听
	void addApplicationListener(ApplicationListener<?> listener);
	// 加载或刷新配置的持久性表示形式，该配置可能是xml文件、属性文件或关系数据库的schema。
	void refresh() throws BeansException, IllegalStateException;
	// 在JVM运行时注册一个关闭 hook ，关闭这个上下文，除非它已经关闭了。
	void registerShutdownHook();
	void close();
	// 确定此应用程序上下文是否处于活动状态，即它是否已刷新至少一次，尚未关闭。
	boolean isActive();
	ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;

}
