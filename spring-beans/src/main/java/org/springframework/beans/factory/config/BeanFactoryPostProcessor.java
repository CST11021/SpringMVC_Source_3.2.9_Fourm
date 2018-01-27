
package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

public interface BeanFactoryPostProcessor {

	/**
	 说明：
	 	该接口方法是在Spring容器解析完配置文件注册了BeanDefinition之后，并在bean被实例化之前被调用的；
	 	该接口方法定义在spring-bean模块中，但是并没有在IOC层被使用（如果要使用可以手动向BeanFactory注入该处理器），而是
	 在ApplicationContext层被调用，这意味着该处理器是用于在ApplicationContext层拓展而被定义的。

	 相关的应用：
		1、CustomEditorConfigurer
		配置自定义的属性编辑器时，会配置一个“org.springframework.beans.factory.config.CustomEditorConfigurer”的bean，
		并给这个bean注入自定义的属性编辑器，CustomEditorConfigurer实现了BeanFactoryPostProcessor这个后处理器接口，因此
	 	Spring会通过该处理器，在解析完后配置文件和实例化bean前，将我们自定义的属性编辑器添加到IOC容器中，这样便可以在后
	 	面属性注入的时候使用我们自定义的属性编辑器了。

		2、PropertyPlaceholderConfigurer
		有时候，我们会在配置文件中使用占位符的方式来配置Bean，Spring在bean注入属性的时候会去解析这些占位符，该解析动作就
	 	是通过PropertyPlaceholderConfigurer来实现的。PropertyPlaceholderConfigurer实现了BeanFactoryPostProcessor这个后处理器接口，在解析完后配置文件和实例化bean前，Spring会通过该处理器访问每个已经注册到容器的BeanDefinition对象，并替换${...}占位符。
		另外，当我们配置了<property-placeholder>标签，Spring 就会自行注册了一个PropertyPlaceholderConfigurer的Bean，并且该Bean是一个处理器Bean。

		3、CustomAutowireConfigurer
		4、CustomScopeConfigurer
		5、DeprecatedBeanWarner
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}
