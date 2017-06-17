
package org.springframework.beans.factory.xml;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

// XmlBeanFactory主要用于从XML文档中读取BeanDefinition，对于注册及获取Bean的方法都是从父类DefaultListableBeanFactory继承的，而唯一与父类不同的个性实现就增加了XMLBeanDefinitionReader类型的reader属性
@Deprecated
@SuppressWarnings({"serial", "all"})
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);


	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(resource);
	}

}
