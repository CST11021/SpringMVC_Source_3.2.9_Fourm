
package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

// 解析xml配置文件，并完成向IOC的注入
public class XmlReaderContext extends ReaderContext {

	// 载入配置文件和解析配置文件
	private final XmlBeanDefinitionReader reader;
	// 对一些特殊的命名空间做处理
	private final NamespaceHandlerResolver namespaceHandlerResolver;


	public XmlReaderContext(Resource resource,
							ProblemReporter problemReporter,
							ReaderEventListener eventListener,
							SourceExtractor sourceExtractor,
							XmlBeanDefinitionReader reader,
							NamespaceHandlerResolver namespaceHandlerResolver) {

		super(resource, problemReporter, eventListener, sourceExtractor);
		this.reader = reader;
		this.namespaceHandlerResolver = namespaceHandlerResolver;
	}


	public final XmlBeanDefinitionReader getReader() {
		return this.reader;
	}
	public final BeanDefinitionRegistry getRegistry() {
		return this.reader.getRegistry();
	}
	public final ResourceLoader getResourceLoader() {
		return this.reader.getResourceLoader();
	}
	public final ClassLoader getBeanClassLoader() {
		return this.reader.getBeanClassLoader();
	}
	public final NamespaceHandlerResolver getNamespaceHandlerResolver() {
		return this.namespaceHandlerResolver;
	}

	// 获取bean生成的类名，该类名由Spring生成
	public String generateBeanName(BeanDefinition beanDefinition) {
		return this.reader.getBeanNameGenerator().generateBeanName(beanDefinition, getRegistry());
	}
	// 使用生产的BeanName将BeanDefinition注册到IOC容器中
	public String registerWithGeneratedName(BeanDefinition beanDefinition) {
		String generatedName = generateBeanName(beanDefinition);
		getRegistry().registerBeanDefinition(generatedName, beanDefinition);
		return generatedName;
	}

}
