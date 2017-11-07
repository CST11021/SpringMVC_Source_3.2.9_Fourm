/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AspectJTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

/**
 * Parser for the {@code <context:component-scan/>} element.
 *
 * @author Mark Fisher
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @since 2.5
 */
// 该类主要用来解析<context:component-scan/>标签。例如，扫描com.whz.service包下所有使用Spring的注解，如@Service，则需配置：<context:component-scan base-package="com.whz.service"/>
public class ComponentScanBeanDefinitionParser implements BeanDefinitionParser {

	// 表示要扫描的包路径
	private static final String BASE_PACKAGE_ATTRIBUTE = "base-package";
	// 可以使用resource-pattern来过滤出特定的类，如：<context:component-scan base-package="cn.lovepi.spring" resource-pattern="anno/*.class"/>
	// 默认情况下加载的是package下的*.class即扫描全部类，在使用了resource-pattern之后，则只扫描package下的anno子包下的所有类。
	private static final String RESOURCE_PATTERN_ATTRIBUTE = "resource-pattern";
	// 在context:component-scan可以添加use-default-filters，spring配置中的use-default-filters用来指示是否自动扫描带
	// 有@Component、@Repository、@Service和@Controller的类。默认为true，即默认扫描。
	private static final String USE_DEFAULT_FILTERS_ATTRIBUTE = "use-default-filters";
	// 当我们需要使用BeanPostProcessor时，直接在Spring配置文件中定义这些Bean显得比较笨拙，例如：
	// 使用@Autowired注解，必须事先在Spring容器中声明AutowiredAnnotationBeanPostProcessor的Bean：
	//	<bean class="org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor "/>
	//
	//使用 @Required注解，就必须声明RequiredAnnotationBeanPostProcessor的Bean：
	//<bean class="org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor"/>
	//		　　
	//类似地，使用@Resource、@PostConstruct、@PreDestroy等注解就必须声明 CommonAnnotationBeanPostProcessor；使用@PersistenceContext注解，
	//就必须声明 PersistenceAnnotationBeanPostProcessor的Bean。这样的声明未免太不优雅，而Spring为我们提供了一种极为方便注册这些BeanPostProcessor的方式，
	//即使用<context:annotation- config/>隐式地向 Spring容器注册AutowiredAnnotationBeanPostProcessor、RequiredAnnotationBeanPostProcessor、
	//CommonAnnotationBeanPostProcessor以及PersistenceAnnotationBeanPostProcessor这4个BeanPostProcessor。如下：
    //
	//<context:annotation-config/>
	//		　　
	//另外，在我们使用注解时一般都会配置扫描包路径选项：
    //
	//<context:component-scan base-package="pack.pack"/>
	//该配置项其实也包含了自动注入上述processor的功能，因此当使用<context:component-scan/>后，即可将<context:annotation-config/>省去。
	private static final String ANNOTATION_CONFIG_ATTRIBUTE = "annotation-config";
	// 为注解的bean生成命名规则，该属性具体作用请参考：http://blog.csdn.net/lsm135/article/details/52756683
	private static final String NAME_GENERATOR_ATTRIBUTE = "name-generator";
	// 为注解的bean配置作用域规则，该属性具体作用请参考：http://doc.okbase.net/alanzyy/archive/94368.html
	private static final String SCOPE_RESOLVER_ATTRIBUTE = "scope-resolver";
	//
	private static final String SCOPED_PROXY_ATTRIBUTE = "scoped-proxy";
	private static final String EXCLUDE_FILTER_ELEMENT = "exclude-filter";
	private static final String INCLUDE_FILTER_ELEMENT = "include-filter";
	private static final String FILTER_TYPE_ATTRIBUTE = "type";
	private static final String FILTER_EXPRESSION_ATTRIBUTE = "expression";

	// 解析节点
	// element表示 <context:component-scan/> 配置节点
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		String[] basePackages = StringUtils.tokenizeToStringArray(element.getAttribute(BASE_PACKAGE_ATTRIBUTE),
				ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);

		// Actually scan for bean definitions and register them.
		ClassPathBeanDefinitionScanner scanner = configureScanner(parserContext, element);
		// 扫描器的 doScan 是核心方法，用于扫描指定包路径下的注解，并注册BeanDefinition
		Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan(basePackages);
		registerComponents(parserContext.getReaderContext(), beanDefinitions, element);

		return null;
	}

	// 配置扫描器
	protected ClassPathBeanDefinitionScanner configureScanner(ParserContext parserContext, Element element) {
		XmlReaderContext readerContext = parserContext.getReaderContext();

		boolean useDefaultFilters = true;
		if (element.hasAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE)) {
			useDefaultFilters = Boolean.valueOf(element.getAttribute(USE_DEFAULT_FILTERS_ATTRIBUTE));
		}

		// Delegate bean definition registration to scanner class.
		ClassPathBeanDefinitionScanner scanner = createScanner(readerContext, useDefaultFilters);
		scanner.setResourceLoader(readerContext.getResourceLoader());
		scanner.setEnvironment(parserContext.getDelegate().getEnvironment());
		scanner.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());
		scanner.setAutowireCandidatePatterns(parserContext.getDelegate().getAutowireCandidatePatterns());

		if (element.hasAttribute(RESOURCE_PATTERN_ATTRIBUTE)) {
			scanner.setResourcePattern(element.getAttribute(RESOURCE_PATTERN_ATTRIBUTE));
		}

		try {
			parseBeanNameGenerator(element, scanner);
		}
		catch (Exception ex) {
			readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
		}

		try {
			parseScope(element, scanner);
		}
		catch (Exception ex) {
			readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
		}

		parseTypeFilters(element, scanner, readerContext, parserContext);

		return scanner;
	}
	// 根据 BeanDefinitionRegistry 和 use-default-filters 属性创建一个 ClassPathBeanDefinitionScanner 实例
	protected ClassPathBeanDefinitionScanner createScanner(XmlReaderContext readerContext, boolean useDefaultFilters) {
		return new ClassPathBeanDefinitionScanner(readerContext.getRegistry(), useDefaultFilters);
	}
	// 注册bean
	protected void registerComponents(XmlReaderContext readerContext, Set<BeanDefinitionHolder> beanDefinitions, Element element) {

		Object source = readerContext.extractSource(element);
		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);

		for (BeanDefinitionHolder beanDefHolder : beanDefinitions) {
			compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefHolder));
		}

		// Register annotation config processors, if necessary.
		boolean annotationConfig = true;
		if (element.hasAttribute(ANNOTATION_CONFIG_ATTRIBUTE)) {
			annotationConfig = Boolean.valueOf(element.getAttribute(ANNOTATION_CONFIG_ATTRIBUTE));
		}
		if (annotationConfig) {
			Set<BeanDefinitionHolder> processorDefinitions =
					AnnotationConfigUtils.registerAnnotationConfigProcessors(readerContext.getRegistry(), source);
			for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
				compositeDef.addNestedComponent(new BeanComponentDefinition(processorDefinition));
			}
		}

		readerContext.fireComponentRegistered(compositeDef);
	}
	// 解析 name-generator 属性，并设置名称生成器
	protected void parseBeanNameGenerator(Element element, ClassPathBeanDefinitionScanner scanner) {
		if (element.hasAttribute(NAME_GENERATOR_ATTRIBUTE)) {
			BeanNameGenerator beanNameGenerator = (BeanNameGenerator) instantiateUserDefinedStrategy(
					element.getAttribute(NAME_GENERATOR_ATTRIBUTE), BeanNameGenerator.class,
					scanner.getResourceLoader().getClassLoader());
			scanner.setBeanNameGenerator(beanNameGenerator);
		}
	}
	protected void parseScope(Element element, ClassPathBeanDefinitionScanner scanner) {
		// Register ScopeMetadataResolver if class name provided.
		if (element.hasAttribute(SCOPE_RESOLVER_ATTRIBUTE)) {
			if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
				throw new IllegalArgumentException(
						"Cannot define both 'scope-resolver' and 'scoped-proxy' on <component-scan> tag");
			}
			ScopeMetadataResolver scopeMetadataResolver = (ScopeMetadataResolver) instantiateUserDefinedStrategy(
					element.getAttribute(SCOPE_RESOLVER_ATTRIBUTE), ScopeMetadataResolver.class,
					scanner.getResourceLoader().getClassLoader());
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		}

		if (element.hasAttribute(SCOPED_PROXY_ATTRIBUTE)) {
			String mode = element.getAttribute(SCOPED_PROXY_ATTRIBUTE);
			if ("targetClass".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS);
			}
			else if ("interfaces".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.INTERFACES);
			}
			else if ("no".equals(mode)) {
				scanner.setScopedProxyMode(ScopedProxyMode.NO);
			}
			else {
				throw new IllegalArgumentException("scoped-proxy only supports 'no', 'interfaces' and 'targetClass'");
			}
		}
	}
	protected void parseTypeFilters(Element element, ClassPathBeanDefinitionScanner scanner, XmlReaderContext readerContext, ParserContext parserContext) {

		// Parse exclude and include filter elements.
		ClassLoader classLoader = scanner.getResourceLoader().getClassLoader();
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String localName = parserContext.getDelegate().getLocalName(node);
				try {
					if (INCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter((Element) node, classLoader);
						scanner.addIncludeFilter(typeFilter);
					}
					else if (EXCLUDE_FILTER_ELEMENT.equals(localName)) {
						TypeFilter typeFilter = createTypeFilter((Element) node, classLoader);
						scanner.addExcludeFilter(typeFilter);
					}
				}
				catch (Exception ex) {
					readerContext.error(ex.getMessage(), readerContext.extractSource(element), ex.getCause());
				}
			}
		}
	}
	@SuppressWarnings("unchecked")
	protected TypeFilter createTypeFilter(Element element, ClassLoader classLoader) {
		String filterType = element.getAttribute(FILTER_TYPE_ATTRIBUTE);
		String expression = element.getAttribute(FILTER_EXPRESSION_ATTRIBUTE);
		try {
			if ("annotation".equals(filterType)) {
				return new AnnotationTypeFilter((Class<Annotation>) classLoader.loadClass(expression));
			}
			else if ("assignable".equals(filterType)) {
				return new AssignableTypeFilter(classLoader.loadClass(expression));
			}
			else if ("aspectj".equals(filterType)) {
				return new AspectJTypeFilter(expression, classLoader);
			}
			else if ("regex".equals(filterType)) {
				return new RegexPatternTypeFilter(Pattern.compile(expression));
			}
			else if ("custom".equals(filterType)) {
				Class<?> filterClass = classLoader.loadClass(expression);
				if (!TypeFilter.class.isAssignableFrom(filterClass)) {
					throw new IllegalArgumentException(
							"Class is not assignable to [" + TypeFilter.class.getName() + "]: " + expression);
				}
				return (TypeFilter) BeanUtils.instantiateClass(filterClass);
			}
			else {
				throw new IllegalArgumentException("Unsupported filter type: " + filterType);
			}
		}
		catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Type filter class not found: " + expression, ex);
		}
	}
	// 使用指定的策略类型创建一个类
	@SuppressWarnings("unchecked")
	private Object instantiateUserDefinedStrategy(String className, Class<?> strategyType, ClassLoader classLoader) {
		Object result;
		try {
			result = classLoader.loadClass(className).newInstance();
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Class [" + className + "] for strategy [" +
					strategyType.getName() + "] not found", ex);
		}
		catch (Exception ex) {
			throw new IllegalArgumentException("Unable to instantiate class [" + className + "] for strategy [" +
					strategyType.getName() + "]: a zero-argument constructor is required", ex);
		}

		if (!strategyType.isAssignableFrom(result.getClass())) {
			throw new IllegalArgumentException("Provided class name must be an implementation of " + strategyType);
		}
		return result;
	}

}
