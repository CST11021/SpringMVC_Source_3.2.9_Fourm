
package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.Environment;


public interface BeanDefinitionDocumentReader {

	// 设置读取BeanDefinitions时使用的环境
	void setEnvironment(Environment environment);

	// 解析document并注册Definition
	void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) throws BeanDefinitionStoreException;

}
