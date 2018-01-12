
package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderContext;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

// 该类用于解析xml配置文件，注意继承自 ReaderContext
public class XmlReaderContext extends ReaderContext {

	// 载入配置文件和解析配置文件
	private final XmlBeanDefinitionReader reader;
	// 用来解析自定义命名空间对应的处理器，例如：命名空间为：http://www.springframework.org/schema/context 对应的处理器
	// 是 ContextNamespaceHandler.java，该映射关系保存在对应jar包中/META-INF/spring.handlers文件中
	private final NamespaceHandlerResolver namespaceHandlerResolver;


	// 构造器：在解析 XmlBeanDefinitionReader 解析配置文件的时候，会实例化一个 XmlReaderContext 对象
	// ReaderEventListener默认使用：EmptyReaderEventListener
	// ProblemReporter默认使用：FailFastProblemReporter
	// SourceExtractor默认使用：NullSourceExtractor
	// NamespaceHandlerResolver默认使用：DefaultNamespaceHandlerResolver
	// 这些入参对象都在 XmlBeanDefinitionReader 中创建的
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
