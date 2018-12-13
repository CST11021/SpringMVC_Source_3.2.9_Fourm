
package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;

// Spring 配置解析上下文：主要是解析过程中，一系列的事件监听机制，XmlReaderContext 就是继承自该类
// 而 XmlReaderContext 封装了 XmlBeanDefinitionReader 和 NamespaceHandlerResolver 对象
public class ReaderContext {

	// 表示一个要解析的配置文件
	private final Resource resource;
	// SPI接口：允许工具和其他外部进程处理bean定义解析过程中报告的错误和警告。
	private final ProblemReporter problemReporter;
	// 在读取BeanDefinition进程中注册组件、别名、import时的回调接口
	private final ReaderEventListener eventListener;
	// 资源提取器：SourceExtractor 这是个提取解析document后返回的原生bean定义，如果你需要则可以实现这个接口，spring提供了
	// NullSourceExtractor空实现，PassThroughSourceExtractor简单实现返回了对象资源。
	private final SourceExtractor sourceExtractor;


	public ReaderContext(Resource resource, ProblemReporter problemReporter, ReaderEventListener eventListener, SourceExtractor sourceExtractor) {
		this.resource = resource;
		this.problemReporter = problemReporter;
		this.eventListener = eventListener;
		this.sourceExtractor = sourceExtractor;
	}

	public final Resource getResource() {
		return this.resource;
	}


	public void fatal(String message, Object source) {
		fatal(message, source, null, null);
	}
	public void fatal(String message, Object source, Throwable ex) {
		fatal(message, source, null, ex);
	}
	public void fatal(String message, Object source, ParseState parseState) {
		fatal(message, source, parseState, null);
	}
	public void fatal(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.fatal(new Problem(message, location, parseState, cause));
	}

	public void error(String message, Object source) {
		error(message, source, null, null);
	}
	public void error(String message, Object source, Throwable ex) {
		error(message, source, null, ex);
	}
	public void error(String message, Object source, ParseState parseState) {
		error(message, source, parseState, null);
	}
	public void error(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.error(new Problem(message, location, parseState, cause));
	}

	public void warning(String message, Object source) {
		warning(message, source, null, null);
	}
	public void warning(String message, Object source, Throwable ex) {
		warning(message, source, null, ex);
	}
	public void warning(String message, Object source, ParseState parseState) {
		warning(message, source, parseState, null);
	}
	public void warning(String message, Object source, ParseState parseState, Throwable cause) {
		Location location = new Location(getResource(), source);
		this.problemReporter.warning(new Problem(message, location, parseState, cause));
	}


	public void fireDefaultsRegistered(DefaultsDefinition defaultsDefinition) {
		this.eventListener.defaultsRegistered(defaultsDefinition);
	}
	// 通知监听器解析已经注册完成
	public void fireComponentRegistered(ComponentDefinition componentDefinition) {
		this.eventListener.componentRegistered(componentDefinition);
	}
	// 每次为bean注册一个别名后都要通知监听器做相应处理
	public void fireAliasRegistered(String beanName, String alias, Object source) {
		this.eventListener.aliasRegistered(new AliasDefinition(beanName, alias, source));
	}

	public void fireImportProcessed(String importedResource, Object source) {
		this.eventListener.importProcessed(new ImportDefinition(importedResource, source));
	}
	// 每次解析完一个<import>标签（主要resource路径的解析）后，都进行监听器激活处理
	public void fireImportProcessed(String importedResource, Resource[] actualResources, Object source) {
		this.eventListener.importProcessed(new ImportDefinition(importedResource, actualResources, source));
	}


	public SourceExtractor getSourceExtractor() {
		return this.sourceExtractor;
	}

	public Object extractSource(Object sourceCandidate) {
		return this.sourceExtractor.extractSource(sourceCandidate, this.resource);
	}

}
