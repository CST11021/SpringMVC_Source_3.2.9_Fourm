
package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.beans.factory.parsing.NullSourceExtractor;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Constants;
import org.springframework.core.NamedThreadLocal;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleSaxErrorHandler;
import org.springframework.util.xml.XmlValidationModeDetector;

// 注意构造器的入参是一个BeanDefinition的注册表，XmlBeanDefinitionReader不仅仅是完成解析，同时也是一个IOC容器（Bean的注册表）
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	// 禁用验证
	public static final int VALIDATION_NONE = XmlValidationModeDetector.VALIDATION_NONE;
	// 自动检测验证模式
	public static final int VALIDATION_AUTO = XmlValidationModeDetector.VALIDATION_AUTO;
	// 使用DTD验证模式
	public static final int VALIDATION_DTD = XmlValidationModeDetector.VALIDATION_DTD;
	// 使用XSD验证模式
	public static final int VALIDATION_XSD = XmlValidationModeDetector.VALIDATION_XSD;

	private static final Constants constants = new Constants(XmlBeanDefinitionReader.class);

	// 验证模式默认为 自动检测验证模式
	private int validationMode = VALIDATION_AUTO;
	// 设置xml命名空间是否敏感
	private boolean namespaceAware = false;
	// XmlBeanDefinitionReader 将 Resource 转为一个Document对象后，会使用 BeanDefinitionDocumentReader 来解析XML，解析从
	// 根节点开始，这里的Class表示 BeanDefinitionDocumentReader 接口的一个实现，后续会在createBeanDefinitionDocumentReader()
	// 方法中使用反射的技术进行实例化，至于为什么要通过反射的方式进行实例化也是百思不得解？？？？？？？？？？？？？？？？
	private Class<?> documentReaderClass = DefaultBeanDefinitionDocumentReader.class;
	private ProblemReporter problemReporter = new FailFastProblemReporter();
	private ReaderEventListener eventListener = new EmptyReaderEventListener();
	private SourceExtractor sourceExtractor = new NullSourceExtractor();
	private NamespaceHandlerResolver namespaceHandlerResolver;
	// 通过DocumentLoad来将Resource转换Document
	private DocumentLoader documentLoader = new DefaultDocumentLoader();
	// sax节点解析器
	private EntityResolver entityResolver;
	// sax解析的错误处理器
	private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

	// Xml验证模式的委托器
	private final XmlValidationModeDetector validationModeDetector = new XmlValidationModeDetector();
	// 表示当前载入的XML配置文件
	private final ThreadLocal<Set<EncodedResource>> resourcesCurrentlyBeingLoaded = new NamedThreadLocal<Set<EncodedResource>>("XML bean definition resources currently being loaded");


	// 注意构造器的入参是一个BeanDefinition的注册表
	public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}




	// -------------------------- 从xml配置文件中解析并加载bean --------------------------------------------------------------------------

	// 从指定的xml文件加载BeanDefinition
	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(new EncodedResource(resource));
	}
	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
		Assert.notNull(encodedResource, "EncodedResource must not be null");
		if (logger.isInfoEnabled()) {
			logger.info("Loading XML bean definitions from " + encodedResource.getResource());
		}

		// currentResources用来记录当前加载的资源
		Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
		if (currentResources == null) {
			currentResources = new HashSet<EncodedResource>(4);
			this.resourcesCurrentlyBeingLoaded.set(currentResources);
		}

		if (!currentResources.add(encodedResource)) {
			throw new BeanDefinitionStoreException("Detected cyclic loading of " + encodedResource + " - check your import definitions!");
		}

		try {
			InputStream inputStream = encodedResource.getResource().getInputStream();
			try {
				// inputSource不是来自于Spring，全路径为是org.xml.sax.InputSource，Spring使用SAX技术来解析XML文档
				InputSource inputSource = new InputSource(inputStream);
				if (encodedResource.getEncoding() != null) {
					inputSource.setEncoding(encodedResource.getEncoding());
				}
				// 进入逻辑的核心部分
				return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
			}
			finally {
				inputStream.close();
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("IOException parsing XML document from " + encodedResource.getResource(), ex);
		}
		finally {
			currentResources.remove(encodedResource);
			if (currentResources.isEmpty()) {
				this.resourcesCurrentlyBeingLoaded.remove();
			}
		}

	}
	public int loadBeanDefinitions(InputSource inputSource) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(inputSource, "resource loaded through SAX InputSource");
	}
	public int loadBeanDefinitions(InputSource inputSource, String resourceDescription) throws BeanDefinitionStoreException {

		return doLoadBeanDefinitions(inputSource, new DescriptiveResource(resourceDescription));
	}
	// 加载资源的真正实现：加载资源文件获取Document，根据document注册bean信息
	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException {
		try {
			int validationMode = getValidationModeForResource(resource);
			// DocumentLoader对象默认为DefaultDocumentLoader；ErrorHandler对象默认为SimpleSaxErrorHandler；namespaceAware默认为false，即xml命名空间不敏感。
			Document doc = this.documentLoader.loadDocument(inputSource, getEntityResolver(), this.errorHandler, validationMode, isNamespaceAware());
			// 解析xml并注册BeanDefinitions
			return registerBeanDefinitions(doc, resource);
		}
		catch (BeanDefinitionStoreException ex) {
			throw ex;
		}
		catch (SAXParseException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(), "Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		}
		catch (SAXException ex) {
			throw new XmlBeanDefinitionStoreException(resource.getDescription(), "XML document from " + resource + " is invalid", ex);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(), "Parser configuration exception parsing XML from " + resource, ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(), "IOException parsing XML document from " + resource, ex);
		}
		catch (Throwable ex) {
			throw new BeanDefinitionStoreException(resource.getDescription(), "Unexpected exception parsing XML document from " + resource, ex);
		}
	}

	// 获取验证模式：如果设定了验证模式则使用设定的验证模式（可以通过调用XmlBeanDefinitionReader中的setValidationMode方法进行设定），否则使用自动检测的方式。
	// 而自动检测验证模式的功能是在函数 detectValidationMode() 方法中实现的。detectValidationMode 函数中又将自动检测验证模式的工作委托给专门的处理类 XMLValidationModeDetector.java
	protected int getValidationModeForResource(Resource resource) {
		int validationModeToUse = getValidationMode();
		if (validationModeToUse != VALIDATION_AUTO) {
			return validationModeToUse;
		}
		int detectedMode = detectValidationMode(resource);
		if (detectedMode != VALIDATION_AUTO) {
			return detectedMode;
		}
		// Hmm, we didn't get a clear indication... Let's assume XSD,
		// since apparently no DTD declaration has been found up until
		// detection stopped (before finding the document's root tag).
		return VALIDATION_XSD;
	}
	protected int detectValidationMode(Resource resource) {
		if (resource.isOpen()) {
			throw new BeanDefinitionStoreException(
					"Passed-in Resource [" + resource + "] contains an open stream: " +
					"cannot determine validation mode automatically. Either pass in a Resource " +
					"that is able to create fresh streams, or explicitly specify the validationMode " +
					"on your XmlBeanDefinitionReader instance.");
		}

		InputStream inputStream;
		try {
			inputStream = resource.getInputStream();
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"Unable to determine validation mode for [" + resource + "]: cannot open InputStream. " +
					"Did you attempt to load directly from a SAX InputSource without specifying the " +
					"validationMode on your XmlBeanDefinitionReader instance?", ex);
		}

		try {
			return this.validationModeDetector.detectValidationMode(inputStream);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("Unable to determine validation mode for [" +
					resource + "]: an error occurred whilst reading from the InputStream.", ex);
		}
	}

	// 解析和注册BeanDefinition
	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
		// 使用 DefaultBeanDefinitionDocumentReader 实例化
		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
		documentReader.setEnvironment(this.getEnvironment());
		// 记录统计前BeanDefinition的加载个数
		int countBefore = getRegistry().getBeanDefinitionCount();
		// 解析这个doc，并注册bean
		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
		// 返回本次加载的BeanDefinition个数
		return getRegistry().getBeanDefinitionCount() - countBefore;
	}
	protected XmlReaderContext createReaderContext(Resource resource) {
		if (this.namespaceHandlerResolver == null) {
			this.namespaceHandlerResolver = createDefaultNamespaceHandlerResolver();
		}
		return new XmlReaderContext(resource, this.problemReporter, this.eventListener, this.sourceExtractor, this, this.namespaceHandlerResolver);
	}
	protected NamespaceHandlerResolver createDefaultNamespaceHandlerResolver() {
		return new DefaultNamespaceHandlerResolver(getResourceLoader().getClassLoader());
	}
	// 使用反射的方式实例化一个 DefaultBeanDefinitionDocumentReader 对象
	protected BeanDefinitionDocumentReader createBeanDefinitionDocumentReader() {
		return BeanDefinitionDocumentReader.class.cast(BeanUtils.instantiateClass(this.documentReaderClass));
	}

	// -------------------------- 从xml配置文件中解析并加载bean --------------------------------------------------------------------------







	// 设置验证模式，如果验证模式被关闭则会切换命名空间
	public void setValidating(boolean validating) {
		this.validationMode = (validating ? VALIDATION_AUTO : VALIDATION_NONE);
		this.namespaceAware = !validating;
	}

	// 设置/获取 验证模式
	public void setValidationModeName(String validationModeName) {
		setValidationMode(constants.asNumber(validationModeName).intValue());
	}
	public void setValidationMode(int validationMode) {
		this.validationMode = validationMode;
	}
	public int getValidationMode() {
		return this.validationMode;
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}
	protected EntityResolver getEntityResolver() {
		if (this.entityResolver == null) {
			// Determine default EntityResolver to use.
			ResourceLoader resourceLoader = getResourceLoader();
			if (resourceLoader != null) {
				this.entityResolver = new ResourceEntityResolver(resourceLoader);
			}
			else {
				this.entityResolver = new DelegatingEntityResolver(getBeanClassLoader());
			}
		}
		return this.entityResolver;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
	public void setDocumentReaderClass(Class<?> documentReaderClass) {
		if (documentReaderClass == null || !BeanDefinitionDocumentReader.class.isAssignableFrom(documentReaderClass)) {
			throw new IllegalArgumentException("documentReaderClass must be an implementation of the BeanDefinitionDocumentReader interface");
		}
		this.documentReaderClass = documentReaderClass;
	}

	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}
	public boolean isNamespaceAware() {
		return this.namespaceAware;
	}
	public void setProblemReporter(ProblemReporter problemReporter) {
		this.problemReporter = (problemReporter != null ? problemReporter : new FailFastProblemReporter());
	}
	public void setEventListener(ReaderEventListener eventListener) {
		this.eventListener = (eventListener != null ? eventListener : new EmptyReaderEventListener());
	}
	public void setSourceExtractor(SourceExtractor sourceExtractor) {
		this.sourceExtractor = (sourceExtractor != null ? sourceExtractor : new NullSourceExtractor());
	}
	public void setNamespaceHandlerResolver(NamespaceHandlerResolver namespaceHandlerResolver) {
		this.namespaceHandlerResolver = namespaceHandlerResolver;
	}
	public void setDocumentLoader(DocumentLoader documentLoader) {
		this.documentLoader = (documentLoader != null ? documentLoader : new DefaultDocumentLoader());
	}


}
