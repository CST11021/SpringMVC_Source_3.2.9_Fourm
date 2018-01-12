
package org.springframework.beans.factory.xml;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;

// spring创建NamespaceHandler实现类有两种方式，
// 第一种是直接实现NamespaceHandler接口提供的方法，这种方式适合于命名空间中标签只有一个或者解析标签和属性的过程很简单，比如p命名空间；
// 第二种是继承抽象类NamespaceHandlerSupport 并实现init方法，在init方法中注册标签的解析器和装饰器以及属性的装饰器，spring
// 中大多数命名空间处理器都使用这种方式
public abstract class NamespaceHandlerSupport implements NamespaceHandler {

	// 保存相应的命名空间下对应标签的解析器，例如：ContextNamespaceHandler类中init()方法
	private final Map<String, BeanDefinitionParser> parsers = new HashMap<String, BeanDefinitionParser>();
	private final Map<String, BeanDefinitionDecorator> decorators = new HashMap<String, BeanDefinitionDecorator>();
	private final Map<String, BeanDefinitionDecorator> attributeDecorators = new HashMap<String, BeanDefinitionDecorator>();


	// 解析element标签
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return findParserForElement(element, parserContext).parse(element, parserContext);
	}
	private BeanDefinitionParser findParserForElement(Element element, ParserContext parserContext) {
		String localName = parserContext.getDelegate().getLocalName(element);
		BeanDefinitionParser parser = this.parsers.get(localName);
		if (parser == null) {
			parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionParser for element [" + localName + "]", element);
		}
		return parser;
	}

	/**
	 * Decorates the supplied {@link Node} by delegating to the {@link BeanDefinitionDecorator} that
	 * is registered to handle that {@link Node}.
	 */
	public BeanDefinitionHolder decorate(Node node, BeanDefinitionHolder definition, ParserContext parserContext) {

		return findDecoratorForNode(node, parserContext).decorate(node, definition, parserContext);
	}

	/**
	 * Locates the {@link BeanDefinitionParser} from the register implementations using
	 * the local name of the supplied {@link Node}. Supports both {@link Element Elements}
	 * and {@link Attr Attrs}.
	 */
	private BeanDefinitionDecorator findDecoratorForNode(Node node, ParserContext parserContext) {
		BeanDefinitionDecorator decorator = null;
		String localName = parserContext.getDelegate().getLocalName(node);
		if (node instanceof Element) {
			decorator = this.decorators.get(localName);
		}
		else if (node instanceof Attr) {
			decorator = this.attributeDecorators.get(localName);
		}
		else {
			parserContext.getReaderContext().fatal("Cannot decorate based on Nodes of type [" + node.getClass().getName() + "]", node);
		}
		if (decorator == null) {
			parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionDecorator for " +
					(node instanceof Element ? "element" : "attribute") + " [" + localName + "]", node);
		}
		return decorator;
	}



	// 该方法用于注册不同类型标签对应的解析器对象：一个自定义的命名空间下通常会有多个不同的配置标签，每个标签对应不同的
	// 解析器，而这些解析都需要实现 BeanDefinitionParser 接口，该方法
	protected final void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
		this.parsers.put(elementName, parser);
	}
	// 注册标签的装饰器BeanDefinitionDecorator对象
	protected final void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator dec) {
		this.decorators.put(elementName, dec);
	}
	// 注册属性的装饰器BeanDefinitionDecorator对象
	protected final void registerBeanDefinitionDecoratorForAttribute(String attrName, BeanDefinitionDecorator dec) {
		this.attributeDecorators.put(attrName, dec);
	}

}
