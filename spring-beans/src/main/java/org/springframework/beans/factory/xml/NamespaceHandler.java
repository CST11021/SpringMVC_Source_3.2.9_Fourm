
package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

// 用于在Spring XML配置文件处理自定义命名空间的接口
public interface NamespaceHandler {

	// DefaultBeanDefinitionDocumentReader 实例化一个NamespaceHandler对象的时候会调用此方法
	// 一般该方法中都会注册一些自定义标签对应的解析器，这样Spring解析的时候就能使用不同的解析器来解析不同的自定义标签
	void init();

	// 解析指定的的Element对象，并返回一个BeanDefinition对象
	BeanDefinition parse(Element element, ParserContext parserContext);

	// 解析指定的节点，并装饰指定的BeanDefinitionHolder对象，最后返回一个已经装饰的BeanDefinitionHolder对象。这个方法在parse方法之后被调用
	BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext);

}
