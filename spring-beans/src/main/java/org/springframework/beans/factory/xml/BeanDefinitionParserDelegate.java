
package org.springframework.beans.factory.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.BeanMetadataAttribute;
import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.BeanEntry;
import org.springframework.beans.factory.parsing.ConstructorArgumentEntry;
import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.beans.factory.parsing.PropertyEntry;
import org.springframework.beans.factory.parsing.QualifierEntry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.ManagedArray;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

// 专门用解析bean标签
public class BeanDefinitionParserDelegate {

	// spring中默认的命名空间URI
	public static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";
	//当存在多个值时可以使用",; "三个分隔符进行分割
	public static final String MULTI_VALUE_ATTRIBUTE_DELIMITERS = ",; ";
	@Deprecated
	public static final String BEAN_NAME_DELIMITERS = MULTI_VALUE_ATTRIBUTE_DELIMITERS;

	// 通用可选值
	public static final String TRUE_VALUE = "true";
	public static final String FALSE_VALUE = "false";
	public static final String DEFAULT_VALUE = "default";


	// <bean>标签的 autowire 属性的可选值
	public static final String AUTOWIRE_NO_VALUE = "no";
	public static final String AUTOWIRE_BY_NAME_VALUE = "byName";
	public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";
	public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";
	public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";


	public static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";
	public static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";
	public static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";






	// 所有可配置的标签名-------------------------------------------------------------------------------------------------

	// 以下是<bean>标签下可配置的标签
	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
	public static final String PROPERTY_ELEMENT = "property";
	public static final String LOOKUP_METHOD_ELEMENT = "lookup-method";
	public static final String REPLACED_METHOD_ELEMENT = "replaced-method";
	public static final String QUALIFIER_ELEMENT = "qualifier";
	public static final String META_ELEMENT = "meta";
	public static final String DESCRIPTION_ELEMENT = "description";

	public static final String BEAN_ELEMENT = "bean";
	public static final String IDREF_ELEMENT = "idref";
	public static final String REF_ELEMENT = "ref";
	public static final String ARG_TYPE_ELEMENT = "arg-type";
	public static final String VALUE_ELEMENT = "value";
	public static final String NULL_ELEMENT = "null";
	public static final String ARRAY_ELEMENT = "array";
	public static final String LIST_ELEMENT = "list";
	public static final String SET_ELEMENT = "set";
	public static final String MAP_ELEMENT = "map";
	public static final String ENTRY_ELEMENT = "entry";
	public static final String KEY_ELEMENT = "key";
	public static final String PROPS_ELEMENT = "props";
	public static final String PROP_ELEMENT = "prop";
	public static final String QUALIFIER_ATTRIBUTE_ELEMENT = "attribute";
	// -----------------------------------------------------------------------------------------------------------------






	// 所有可配置的标签属性-----------------------------------------------------------------------------------------------

	// 以下<bean>标签的属性

	public static final String NAME_ATTRIBUTE = "name";// <bean>标签的 name 属性，表示bean的名称
	public static final String ID_ATTRIBUTE = "id";// <bean>标签的 id 属性，表示bean的id
	public static final String PARENT_ATTRIBUTE = "parent";// <bean>标签的 parent 属性，表示bean继承的父bean
	public static final String CLASS_ATTRIBUTE = "class";// <bean>标签的 class 属性，表示bean的类型
	public static final String ABSTRACT_ATTRIBUTE = "abstract";// <bean>标签的 abstract 属性，表示该bean是否为抽象bean
	public static final String SCOPE_ATTRIBUTE = "scope";// <bean>标签的 scope 属性
	public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";// <bean>标签的 lazy-init 属性
	public static final String AUTOWIRE_ATTRIBUTE = "autowire";// <bean>标签的 autowire 属性
	public static final String AUTOWIRE_CANDIDATE_ATTRIBUTE = "autowire-candidate";// <bean>标签的 autowire-candidate 属性
	public static final String PRIMARY_ATTRIBUTE = "primary";// <bean>标签的 primary 属性
	public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";
	public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";// <bean>标签的 depends-on 属性
	public static final String INIT_METHOD_ATTRIBUTE = "init-method";// <bean>标签的 init-method 属性
	public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";// <bean>标签的 destroy-method 属性
	public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";// <bean>标签的 factory-method 属性
	public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";// <bean>标签的 factory-bean 属性

	// 标签的其他属性

	public static final String SINGLETON_ATTRIBUTE = "singleton";
	public static final String INDEX_ATTRIBUTE = "index";
	public static final String TYPE_ATTRIBUTE = "type";
	public static final String VALUE_TYPE_ATTRIBUTE = "value-type";
	public static final String KEY_TYPE_ATTRIBUTE = "key-type";
	public static final String REF_ATTRIBUTE = "ref";
	public static final String VALUE_ATTRIBUTE = "value";
	public static final String REPLACER_ATTRIBUTE = "replacer";
	public static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";
	public static final String BEAN_REF_ATTRIBUTE = "bean";
	public static final String LOCAL_REF_ATTRIBUTE = "local";
	public static final String PARENT_REF_ATTRIBUTE = "parent";
	public static final String KEY_ATTRIBUTE = "key";
	public static final String KEY_REF_ATTRIBUTE = "key-ref";
	public static final String VALUE_REF_ATTRIBUTE = "value-ref";
	public static final String MERGE_ATTRIBUTE = "merge";
	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";
	public static final String DEFAULT_MERGE_ATTRIBUTE = "default-merge";
	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";
	public static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";
	public static final String DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE = "default-autowire-candidates";
	public static final String DEFAULT_INIT_METHOD_ATTRIBUTE = "default-init-method";
	public static final String DEFAULT_DESTROY_METHOD_ATTRIBUTE = "default-destroy-method";

	// ----------------------------------------------------------------------------------------------------------------




	protected final Log logger = LogFactory.getLog(getClass());







	// 解析xml的上下文
	private final XmlReaderContext readerContext;
	// 该对象封装了 Bean 中哪些有默认值的属性
	private final DocumentDefaultsDefinition defaults = new DocumentDefaultsDefinition();
	//用来记录当前解析的bean
	private final ParseState parseState = new ParseState();
	private Environment environment;

	// 记录bean的Name以及对应的alias，用来检查唯一性
	// 存储所有使用过的bean名称，这样我们就可以在bean元素基础上强制唯一性。重复的bean ID /名称可能不存在于相同级别的bean
	// 元素嵌套中，但可以跨级别复制。
	private final Set<String> usedNames = new HashSet<String>();


	public BeanDefinitionParserDelegate(XmlReaderContext readerContext, Environment environment) {
		Assert.notNull(readerContext, "XmlReaderContext must not be null");
		Assert.notNull(readerContext, "Environment must not be null");
		this.readerContext = readerContext;
		this.environment = environment;
	}
	@Deprecated
	public BeanDefinitionParserDelegate(XmlReaderContext readerContext) {
		this(readerContext, new StandardEnvironment());
	}





	//-------- 解析<bean>标签-------------------------------------------------------------------------------------------------------------------------------------------------

	// 处理bean标签，并返回BeanDefinition的包装类BeanDefinitionHolder
	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
		return parseBeanDefinitionElement(ele, null);
	}
	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) {
		// <bean>的id属性
		String id = ele.getAttribute(ID_ATTRIBUTE);
		// <bean>的name属性
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

		// <bean>的别名，name属性可以设置多个值，用",; "进行分隔，如果name属性配置了多个名字，则第一个作为beanName，其他的作为别名
		List<String> aliases = new ArrayList<String>();
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}

		// 默认使用id作为beanName，如果有配置别名，则使用第一个别名作为beanName,其他的别名仍然作为别名
		String beanName = id;
		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
			beanName = aliases.remove(0);
			if (logger.isDebugEnabled()) {
				logger.debug("No XML 'id' specified - using '" + beanName + "' as bean name and " + aliases + " as aliases");
			}
		}

		if (containingBean == null) {
			// 验证在bean元素在当前级容器别中，这个beanName和别名没有被使用,如果没有被使用，将beanName和别名添加到 this.usedNames
			checkNameUniqueness(beanName, aliases, ele);
		}

		// 解析这个bean，如果解析出问题返回null
		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
		if (beanDefinition != null) {
			// 没有配置beanName的情况，就使用BeanName生成器生成name，然后Bean的全限定类名作为别名
			if (!StringUtils.hasText(beanName)) {
				try {
					if (containingBean != null) {
						beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, this.readerContext.getRegistry(), true);
					}
					else {
						beanName = this.readerContext.generateBeanName(beanDefinition);
						// 如果生成器返回类名加后缀，则可以为普通bean类名注册一个别名。预计在spring 1.2／2.0向后兼容
						String beanClassName = beanDefinition.getBeanClassName();
						if (beanClassName != null
								&& beanName.startsWith(beanClassName)
								&& beanName.length() > beanClassName.length()
								&& !this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
							aliases.add(beanClassName);
						}
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Neither XML 'id' nor 'name' specified - " + "using generated bean name [" + beanName + "]");
					}
				}
				catch (Exception ex) {
					error(ex.getMessage(), ele);
					return null;
				}
			}
			String[] aliasesArray = StringUtils.toStringArray(aliases);
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
		}

		return null;
	}
	// 验证在bean元素在当前级容器别中，这个beanName和别名没有被使用,如果没有被使用，将beanName和别名添加到 this.usedNames
	protected void checkNameUniqueness(String beanName, List<String> aliases, Element beanElement) {
		String foundName = null;

		if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
			foundName = beanName;
		}
		if (foundName == null) {
			foundName = (String) CollectionUtils.findFirstMatch(this.usedNames, aliases);
		}
		if (foundName != null) {
			error("Bean name '" + foundName + "' is already used in this <beans> element", beanElement);
		}

		this.usedNames.add(beanName);
		this.usedNames.addAll(aliases);
	}

	// ******************重要方法：解析这个bean，如果解析出问题返回null************************
	public AbstractBeanDefinition parseBeanDefinitionElement(Element ele, String beanName, BeanDefinition containingBean) {

		// 将当前解析的bean入栈
		this.parseState.push(new BeanEntry(beanName));

		String className = null;
		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
		}

		try {
			String parent = null;
			if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
				parent = ele.getAttribute(PARENT_ATTRIBUTE);
			}
			AbstractBeanDefinition bd = createBeanDefinition(className, parent);

			// 解析这个bean的scope、singleton、abstract、lazy-init、autowire、dependency-check、depends-on、primary、init-method、
			// destory-method、factory-create、factory-bean 属性
			parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
			bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));

			parseMetaElements(ele, bd);
			// lookup-method
			parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
			// replaced-method
			parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

			parseConstructorArgElements(ele, bd);
			// 解析property标签
			parsePropertyElements(ele, bd);
			// 解析<qualifier>标签
			parseQualifierElements(ele, bd);

			bd.setResource(this.readerContext.getResource());
			bd.setSource(extractSource(ele));

			return bd;
		}
		catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", ele, ex);
		}
		catch (NoClassDefFoundError err) {
			error("Class that bean class [" + className + "] depends on not found", ele, err);
		}
		catch (Throwable ex) {
			error("Unexpected failure during bean definition parsing", ele, ex);
		}
		finally {
			// 解析完后要这个bean移出栈
			this.parseState.pop();
		}

		return null;
	}
	// 使用当前的beanClassLoader 创建一个BeanDefinition
	protected AbstractBeanDefinition createBeanDefinition(String className, String parentName) throws ClassNotFoundException {
		return BeanDefinitionReaderUtils.createBeanDefinition(parentName, className, this.readerContext.getBeanClassLoader());
	}
	// 解析这个bean的scope、singleton、abstract、lazy-init、autowire、dependency-check、depends-on、primary、init-method、destory-method、factory-create、factory-bean 属性
	public AbstractBeanDefinition parseBeanDefinitionAttributes(Element ele, String beanName, BeanDefinition containingBean, AbstractBeanDefinition bd) {

		// 解析bean的scope属性
		if (ele.hasAttribute(SCOPE_ATTRIBUTE)) {
			// Spring 2.x "scope" attribute
			bd.setScope(ele.getAttribute(SCOPE_ATTRIBUTE));
			if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
				error("Specify either 'scope' or 'singleton', not both", ele);
			}
		}

		// singleton
		else if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
			// Spring 1.x "singleton" attribute
			bd.setScope(TRUE_VALUE.equals(ele.getAttribute(SINGLETON_ATTRIBUTE)) ?
					BeanDefinition.SCOPE_SINGLETON : BeanDefinition.SCOPE_PROTOTYPE);
		}
		//
		else if (containingBean != null) {
			// Take default from containing bean in case of an inner bean definition.
			bd.setScope(containingBean.getScope());
		}
		// abstract
		if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
			bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
		}
		// lazy-init
		String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(lazyInit)) {
			lazyInit = this.defaults.getLazyInit();
		}
		bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

		// autowire
		String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
		bd.setAutowireMode(getAutowireMode(autowire));

		// dependency-check
		String dependencyCheck = ele.getAttribute(DEPENDENCY_CHECK_ATTRIBUTE);
		bd.setDependencyCheck(getDependencyCheck(dependencyCheck));

		// depends-on
		if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
			String dependsOn = ele.getAttribute(DEPENDS_ON_ATTRIBUTE);
			bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, MULTI_VALUE_ATTRIBUTE_DELIMITERS));
		}

		// autowire-candidate
		String autowireCandidate = ele.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE);
		if ("".equals(autowireCandidate) || DEFAULT_VALUE.equals(autowireCandidate)) {
			String candidatePattern = this.defaults.getAutowireCandidates();
			if (candidatePattern != null) {
				String[] patterns = StringUtils.commaDelimitedListToStringArray(candidatePattern);
				bd.setAutowireCandidate(PatternMatchUtils.simpleMatch(patterns, beanName));
			}
		}
		else {
			bd.setAutowireCandidate(TRUE_VALUE.equals(autowireCandidate));
		}

		// primary
		if (ele.hasAttribute(PRIMARY_ATTRIBUTE)) {
			bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)));
		}

		// init-method
		if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
			String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
			if (!"".equals(initMethodName)) {
				bd.setInitMethodName(initMethodName);
			}
		}
		else {
			if (this.defaults.getInitMethod() != null) {
				bd.setInitMethodName(this.defaults.getInitMethod());
				bd.setEnforceInitMethod(false);
			}
		}

		// destroy-method
		if (ele.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
			String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
			if (!"".equals(destroyMethodName)) {
				bd.setDestroyMethodName(destroyMethodName);
			}
		}
		else {
			if (this.defaults.getDestroyMethod() != null) {
				bd.setDestroyMethodName(this.defaults.getDestroyMethod());
				bd.setEnforceDestroyMethod(false);
			}
		}
		// factory-method
		if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
			bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
		}
		// factory-bean
		if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
			bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE));
		}

		return bd;
	}

	// 解析<meta>子元素
	public void parseMetaElements(Element ele, BeanMetadataAttributeAccessor attributeAccessor) {
		// 返回 ele 元素的所有子节点
		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (isCandidateElement(node) && nodeNameEquals(node, META_ELEMENT)) {
				Element metaElement = (Element) node;
				String key = metaElement.getAttribute(KEY_ATTRIBUTE);
				String value = metaElement.getAttribute(VALUE_ATTRIBUTE);
				BeanMetadataAttribute attribute = new BeanMetadataAttribute(key, value);
				attribute.setSource(extractSource(metaElement));
				attributeAccessor.addMetadataAttribute(attribute);
			}
		}
	}
	// 如果该节点的命名空间是默认名空间 或者 它的父节点不是默认命名空间，则返回 TRUE
	private boolean isCandidateElement(Node node) {
		return (node instanceof Element
				&& (isDefaultNamespace(node) || !isDefaultNamespace(node.getParentNode())));
	}
	// 判断节点名称是等于 desiredName
	public boolean nodeNameEquals(Node node, String desiredName) {
		return desiredName.equals(node.getNodeName()) || desiredName.equals(getLocalName(node));
	}
	// 获取节点的LocalName
	public String getLocalName(Node node) {
		return node.getLocalName();
	}

	// 解析 lookup-override 这个属性
	public void parseLookupOverrideSubElements(Element beanEle, MethodOverrides overrides) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (isCandidateElement(node) && nodeNameEquals(node, LOOKUP_METHOD_ELEMENT)) {
				Element ele = (Element) node;
				String methodName = ele.getAttribute(NAME_ATTRIBUTE);
				String beanRef = ele.getAttribute(BEAN_ELEMENT);
				LookupOverride override = new LookupOverride(methodName, beanRef);
				override.setSource(extractSource(ele));
				overrides.addOverride(override);
			}
		}
	}
	// 解析 replaced-method 这个属性
	public void parseReplacedMethodSubElements(Element beanEle, MethodOverrides overrides) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (isCandidateElement(node) && nodeNameEquals(node, REPLACED_METHOD_ELEMENT)) {
				Element replacedMethodEle = (Element) node;
				String name = replacedMethodEle.getAttribute(NAME_ATTRIBUTE);
				String callback = replacedMethodEle.getAttribute(REPLACER_ATTRIBUTE);
				ReplaceOverride replaceOverride = new ReplaceOverride(name, callback);
				// Look for arg-type match elements.
				List<Element> argTypeEles = DomUtils.getChildElementsByTagName(replacedMethodEle, ARG_TYPE_ELEMENT);
				for (Element argTypeEle : argTypeEles) {
					String match = argTypeEle.getAttribute(ARG_TYPE_MATCH_ATTRIBUTE);
					match = (StringUtils.hasText(match) ? match : DomUtils.getTextValue(argTypeEle));
					if (StringUtils.hasText(match)) {
						replaceOverride.addTypeIdentifier(match);
					}
				}
				replaceOverride.setSource(extractSource(replacedMethodEle));
				overrides.addOverride(replaceOverride);
			}
		}
	}
	// Parse constructor-arg sub-elements of the given bean element.
	public void parseConstructorArgElements(Element beanEle, BeanDefinition bd) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (isCandidateElement(node) && nodeNameEquals(node, CONSTRUCTOR_ARG_ELEMENT)) {
				parseConstructorArgElement((Element) node, bd);
			}
		}
	}
	// 解析 property 子标签
	public void parsePropertyElements(Element beanEle, BeanDefinition bd) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			// 判断字节点是否为<property>标签
			if (isCandidateElement(node) && nodeNameEquals(node, PROPERTY_ELEMENT)) {
				// 解析property标签
				parsePropertyElement((Element) node, bd);
			}
		}
	}
	// Parse qualifier sub-elements of the given bean element.
	public void parseQualifierElements(Element beanEle, AbstractBeanDefinition bd) {
		NodeList nl = beanEle.getChildNodes();
		// 遍历bean标签的子节点
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			// 如果是<qualifier>标签
			if (isCandidateElement(node) && nodeNameEquals(node, QUALIFIER_ELEMENT)) {
				parseQualifierElement((Element) node, bd);
			}
		}
	}
	// Parse a constructor-arg element.
	public void parseConstructorArgElement(Element ele, BeanDefinition bd) {
		String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
		if (StringUtils.hasLength(indexAttr)) {
			try {
				int index = Integer.parseInt(indexAttr);
				if (index < 0) {
					error("'index' cannot be lower than 0", ele);
				}
				else {
					try {
						this.parseState.push(new ConstructorArgumentEntry(index));
						Object value = parsePropertyValue(ele, bd, null);
						ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
						if (StringUtils.hasLength(typeAttr)) {
							valueHolder.setType(typeAttr);
						}
						if (StringUtils.hasLength(nameAttr)) {
							valueHolder.setName(nameAttr);
						}
						valueHolder.setSource(extractSource(ele));
						if (bd.getConstructorArgumentValues().hasIndexedArgumentValue(index)) {
							error("Ambiguous constructor-arg entries for index " + index, ele);
						}
						else {
							bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder);
						}
					}
					finally {
						this.parseState.pop();
					}
				}
			}
			catch (NumberFormatException ex) {
				error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele);
			}
		}
		else {
			try {
				this.parseState.push(new ConstructorArgumentEntry());
				Object value = parsePropertyValue(ele, bd, null);
				ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
				if (StringUtils.hasLength(typeAttr)) {
					valueHolder.setType(typeAttr);
				}
				if (StringUtils.hasLength(nameAttr)) {
					valueHolder.setName(nameAttr);
				}
				valueHolder.setSource(extractSource(ele));
				bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder);
			}
			finally {
				this.parseState.pop();
			}
		}
	}
	// 解析property标签
	public void parsePropertyElement(Element ele, BeanDefinition bd) {
		String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
		if (!StringUtils.hasLength(propertyName)) {
			error("Tag 'property' must have a 'name' attribute", ele);
			return;
		}
		this.parseState.push(new PropertyEntry(propertyName));
		try {
			if (bd.getPropertyValues().contains(propertyName)) {
				// 如果已经包含了该属性配置则抛异常，Spring不允许重复配置同一个属性
				error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
				return;
			}
			Object val = parsePropertyValue(ele, bd, propertyName);
			PropertyValue pv = new PropertyValue(propertyName, val);
			parseMetaElements(ele, pv);
			pv.setSource(extractSource(ele));
			bd.getPropertyValues().addPropertyValue(pv);
		}
		finally {
			this.parseState.pop();
		}
	}
	// 解析一个<qualifier>标签
	public void parseQualifierElement(Element ele, AbstractBeanDefinition bd) {
		String typeName = ele.getAttribute(TYPE_ATTRIBUTE);
		if (!StringUtils.hasLength(typeName)) {
			error("Tag 'qualifier' must have a 'type' attribute", ele);
			return;
		}
		this.parseState.push(new QualifierEntry(typeName));
		try {
			AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(typeName);
			qualifier.setSource(extractSource(ele));
			String value = ele.getAttribute(VALUE_ATTRIBUTE);
			if (StringUtils.hasLength(value)) {
				qualifier.setAttribute(AutowireCandidateQualifier.VALUE_KEY, value);
			}
			NodeList nl = ele.getChildNodes();
			// 遍历<qualifier>标签的子元素
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				// 如果是<attribute>标签
				if (isCandidateElement(node) && nodeNameEquals(node, QUALIFIER_ATTRIBUTE_ELEMENT)) {
					Element attributeEle = (Element) node;
					String attributeName = attributeEle.getAttribute(KEY_ATTRIBUTE);
					String attributeValue = attributeEle.getAttribute(VALUE_ATTRIBUTE);
					if (StringUtils.hasLength(attributeName) && StringUtils.hasLength(attributeValue)) {
						BeanMetadataAttribute attribute = new BeanMetadataAttribute(attributeName, attributeValue);
						attribute.setSource(extractSource(attributeEle));
						qualifier.addMetadataAttribute(attribute);
					}
					else {
						error("Qualifier 'attribute' tag must have a 'name' and 'value'", attributeEle);
						return;
					}
				}
			}
			bd.addQualifier(qualifier);
		}
		finally {
			this.parseState.pop();
		}
	}
	// <propery>除description和meta外只包含一个子元素；判断ele是否包含ref、value属性，ref和value不能同时存在，ref或value
	// 不能与子元素同时存在；如果包含ref属性，新建RuntimeBeanReference并返回；如果包含value属性，新建TypedStringValue并
	// 返回；如果包含子元素，调用parsePropertySubElement解析子元素
	public Object parsePropertyValue(Element ele, BeanDefinition bd, String propertyName) {
		String elementName = (propertyName != null) ?
				"<property> element for property '" + propertyName + "'" :
				"<constructor-arg> element";

		// 获取<property>标签的子节点（包括：<bean>、<value>、<ref>、<array>、<description>、<idref>、<list>、<map>、
		// <meta>、<props>、<set>、<null>）
		NodeList nl = ele.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && !nodeNameEquals(node, DESCRIPTION_ELEMENT) &&
					!nodeNameEquals(node, META_ELEMENT)) {
				// 如果子节点不是<description>，也不是<meta>
				if (subElement != null) {
					// 一个<property>标签只能有一个子节点，如果有多个则抛出异常
					error(elementName + " must not contain more than one sub-element", ele);
				}
				else {
					subElement = (Element) node;
				}
			}
		}

		// 解析<property name="" ref="" value="">是否有rel属性和value属性
		boolean hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE);
		boolean hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE);
		if ((hasRefAttribute && hasValueAttribute) ||
				((hasRefAttribute || hasValueAttribute) && subElement != null)) {
			// 如果配置了这两个属性，然后又配置了子标签也抛出异常
			error(elementName +
					" is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele);
		}

		// 如果配置了ref属性，说明该bean依赖了另一个bean，将被依赖的Bean封装为一个RuntimeBeanReference对象，
		// RuntimeBeanReference 在属性注入的时候会指向对应bean的引用
		if (hasRefAttribute) {
			String refName = ele.getAttribute(REF_ATTRIBUTE);
			if (!StringUtils.hasText(refName)) {
				error(elementName + " contains empty 'ref' attribute", ele);
			}
			RuntimeBeanReference ref = new RuntimeBeanReference(refName);
			// 设置当前<property>的所在配置源
			ref.setSource(extractSource(ele));
			return ref;
		}
		// 如果配置了<property>的value属性，则将value封装为一个TypedStringValue对象返回
		else if (hasValueAttribute) {
			TypedStringValue valueHolder = new TypedStringValue(ele.getAttribute(VALUE_ATTRIBUTE));
			valueHolder.setSource(extractSource(ele));
			return valueHolder;
		}
		// 如果配置了字标签则去解析子标签
		else if (subElement != null) {
			return parsePropertySubElement(subElement, bd);
		}
		else {
			// Neither child element nor "ref" or "value" attribute found.
			error(elementName + " must specify a ref or value", ele);
			return null;
		}
	}

	// Parse a value, ref or collection sub-element of a property or constructor-arg element.
	public Object parsePropertySubElement(Element ele, BeanDefinition bd) {
		return parsePropertySubElement(ele, bd, null);
	}
	public Object parsePropertySubElement(Element ele, BeanDefinition bd, String defaultValueType) {
		if (!isDefaultNamespace(ele)) {
			// 如果子节点不是默认的命名空间，则使用委托parseNestedCustomElement()方法解析
			return parseNestedCustomElement(ele, bd);
		}
		// 如果是<bean>
		else if (nodeNameEquals(ele, BEAN_ELEMENT)) {
			BeanDefinitionHolder nestedBd = parseBeanDefinitionElement(ele, bd);
			if (nestedBd != null) {
				nestedBd = decorateBeanDefinitionIfRequired(ele, nestedBd, bd);
			}
			return nestedBd;
		}
		// 如果是<ref>
		else if (nodeNameEquals(ele, REF_ELEMENT)) {
			// A generic reference to any name of any bean.
			String refName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			boolean toParent = false;
			if (!StringUtils.hasLength(refName)) {
				// A reference to the id of another bean in the same XML file.
				refName = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(refName)) {
					// A reference to the id of another bean in a parent context.
					refName = ele.getAttribute(PARENT_REF_ATTRIBUTE);
					toParent = true;
					if (!StringUtils.hasLength(refName)) {
						error("'bean', 'local' or 'parent' is required for <ref> element", ele);
						return null;
					}
				}
			}
			if (!StringUtils.hasText(refName)) {
				error("<ref> element contains empty target attribute", ele);
				return null;
			}
			RuntimeBeanReference ref = new RuntimeBeanReference(refName, toParent);
			ref.setSource(extractSource(ele));
			return ref;
		}
		// 如果是<idref>
		else if (nodeNameEquals(ele, IDREF_ELEMENT)) {
			return parseIdRefElement(ele);
		}
		// 如果是<value>
		else if (nodeNameEquals(ele, VALUE_ELEMENT)) {
			return parseValueElement(ele, defaultValueType);
		}
		// 如果是<null>
		else if (nodeNameEquals(ele, NULL_ELEMENT)) {
			// It's a distinguished null value. Let's wrap it in a TypedStringValue
			// object in order to preserve the source location.
			TypedStringValue nullHolder = new TypedStringValue(null);
			nullHolder.setSource(extractSource(ele));
			return nullHolder;
		}
		// 如果是<array>
		else if (nodeNameEquals(ele, ARRAY_ELEMENT)) {
			return parseArrayElement(ele, bd);
		}
		// 如果是<list>
		else if (nodeNameEquals(ele, LIST_ELEMENT)) {
			return parseListElement(ele, bd);
		}
		// 如果是<set>
		else if (nodeNameEquals(ele, SET_ELEMENT)) {
			return parseSetElement(ele, bd);
		}
		// 如果是<map>
		else if (nodeNameEquals(ele, MAP_ELEMENT)) {
			return parseMapElement(ele, bd);
		}
		// 如果是<props>
		else if (nodeNameEquals(ele, PROPS_ELEMENT)) {
			return parsePropsElement(ele);
		}
		else {
			error("Unknown property sub-element: [" + ele.getNodeName() + "]", ele);
			return null;
		}
	}
	// 当解析<property>孩子节点不是默认命名空间时，会调用该方法进行解析
	private BeanDefinitionHolder parseNestedCustomElement(Element ele, BeanDefinition containingBd) {
		BeanDefinition innerDefinition = parseCustomElement(ele, containingBd);
		if (innerDefinition == null) {
			error("Incorrect usage of element '" + ele.getNodeName() + "' in a nested manner. " + "This tag cannot be used nested inside <property>.", ele);
			return null;
		}
		String id = ele.getNodeName() + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR + ObjectUtils.getIdentityHexString(innerDefinition);
		if (logger.isDebugEnabled()) {
			logger.debug("Using generated bean name [" + id + "] for nested custom element '" + ele.getNodeName() + "'");
		}
		return new BeanDefinitionHolder(innerDefinition, id);
	}

	// Return a typed String value Object for the given 'idref' element.
	public Object parseIdRefElement(Element ele) {
		// A generic reference to any name of any bean.
		String refName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
		if (!StringUtils.hasLength(refName)) {
			// A reference to the id of another bean in the same XML file.
			refName = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
			if (!StringUtils.hasLength(refName)) {
				error("Either 'bean' or 'local' is required for <idref> element", ele);
				return null;
			}
		}
		if (!StringUtils.hasText(refName)) {
			error("<idref> element contains empty target attribute", ele);
			return null;
		}
		RuntimeBeanNameReference ref = new RuntimeBeanNameReference(refName);
		ref.setSource(extractSource(ele));
		return ref;
	}
	// Return a typed String value Object for the given value element.
	public Object parseValueElement(Element ele, String defaultTypeName) {
		// It's a literal value.
		String value = DomUtils.getTextValue(ele);
		String specifiedTypeName = ele.getAttribute(TYPE_ATTRIBUTE);
		String typeName = specifiedTypeName;
		if (!StringUtils.hasText(typeName)) {
			typeName = defaultTypeName;
		}
		try {
			TypedStringValue typedValue = buildTypedStringValue(value, typeName);
			typedValue.setSource(extractSource(ele));
			typedValue.setSpecifiedTypeName(specifiedTypeName);
			return typedValue;
		}
		catch (ClassNotFoundException ex) {
			error("Type class [" + typeName + "] not found for <value> element", ele, ex);
			return value;
		}
	}
	// Build a typed String value Object for the given raw value.
	protected TypedStringValue buildTypedStringValue(String value, String targetTypeName) throws ClassNotFoundException {

		ClassLoader classLoader = this.readerContext.getBeanClassLoader();
		TypedStringValue typedValue;
		if (!StringUtils.hasText(targetTypeName)) {
			typedValue = new TypedStringValue(value);
		}
		else if (classLoader != null) {
			Class<?> targetType = ClassUtils.forName(targetTypeName, classLoader);
			typedValue = new TypedStringValue(value, targetType);
		}
		else {
			typedValue = new TypedStringValue(value, targetTypeName);
		}
		return typedValue;
	}
	// Parse an array element.
	public Object parseArrayElement(Element arrayEle, BeanDefinition bd) {
		String elementType = arrayEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
		NodeList nl = arrayEle.getChildNodes();
		ManagedArray target = new ManagedArray(elementType, nl.getLength());
		target.setSource(extractSource(arrayEle));
		target.setElementTypeName(elementType);
		target.setMergeEnabled(parseMergeAttribute(arrayEle));
		parseCollectionElements(nl, target, bd, elementType);
		return target;
	}
	// Parse a list element.
	public List parseListElement(Element collectionEle, BeanDefinition bd) {
		String defaultElementType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedList<Object> target = new ManagedList<Object>(nl.getLength());
		target.setSource(extractSource(collectionEle));
		target.setElementTypeName(defaultElementType);
		target.setMergeEnabled(parseMergeAttribute(collectionEle));
		parseCollectionElements(nl, target, bd, defaultElementType);
		return target;
	}


	public Set parseSetElement(Element collectionEle, BeanDefinition bd) {
		String defaultElementType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedSet<Object> target = new ManagedSet<Object>(nl.getLength());
		target.setSource(extractSource(collectionEle));
		target.setElementTypeName(defaultElementType);
		target.setMergeEnabled(parseMergeAttribute(collectionEle));
		parseCollectionElements(nl, target, bd, defaultElementType);
		return target;
	}
	protected void parseCollectionElements(NodeList elementNodes, Collection<Object> target, BeanDefinition bd, String defaultElementType) {

		for (int i = 0; i < elementNodes.getLength(); i++) {
			Node node = elementNodes.item(i);
			if (node instanceof Element && !nodeNameEquals(node, DESCRIPTION_ELEMENT)) {
				target.add(parsePropertySubElement((Element) node, bd, defaultElementType));
			}
		}
	}

	public Map parseMapElement(Element mapEle, BeanDefinition bd) {
		String defaultKeyType = mapEle.getAttribute(KEY_TYPE_ATTRIBUTE);
		String defaultValueType = mapEle.getAttribute(VALUE_TYPE_ATTRIBUTE);

		List<Element> entryEles = DomUtils.getChildElementsByTagName(mapEle, ENTRY_ELEMENT);
		ManagedMap<Object, Object> map = new ManagedMap<Object, Object>(entryEles.size());
		map.setSource(extractSource(mapEle));
		map.setKeyTypeName(defaultKeyType);
		map.setValueTypeName(defaultValueType);
		map.setMergeEnabled(parseMergeAttribute(mapEle));

		for (Element entryEle : entryEles) {
			// Should only have one value child element: ref, value, list, etc.
			// Optionally, there might be a key child element.
			NodeList entrySubNodes = entryEle.getChildNodes();
			Element keyEle = null;
			Element valueEle = null;
			for (int j = 0; j < entrySubNodes.getLength(); j++) {
				Node node = entrySubNodes.item(j);
				if (node instanceof Element) {
					Element candidateEle = (Element) node;
					if (nodeNameEquals(candidateEle, KEY_ELEMENT)) {
						if (keyEle != null) {
							error("<entry> element is only allowed to contain one <key> sub-element", entryEle);
						}
						else {
							keyEle = candidateEle;
						}
					}
					else {
						// Child element is what we're looking for.
						if (nodeNameEquals(candidateEle, DESCRIPTION_ELEMENT)) {
							// the element is a <description> -> ignore it
						}
						else if (valueEle != null) {
							error("<entry> element must not contain more than one value sub-element", entryEle);
						}
						else {
							valueEle = candidateEle;
						}
					}
				}
			}

			// Extract key from attribute or sub-element.
			Object key = null;
			boolean hasKeyAttribute = entryEle.hasAttribute(KEY_ATTRIBUTE);
			boolean hasKeyRefAttribute = entryEle.hasAttribute(KEY_REF_ATTRIBUTE);
			if ((hasKeyAttribute && hasKeyRefAttribute) ||
					((hasKeyAttribute || hasKeyRefAttribute)) && keyEle != null) {
				error("<entry> element is only allowed to contain either " +
						"a 'key' attribute OR a 'key-ref' attribute OR a <key> sub-element", entryEle);
			}
			if (hasKeyAttribute) {
				key = buildTypedStringValueForMap(entryEle.getAttribute(KEY_ATTRIBUTE), defaultKeyType, entryEle);
			}
			else if (hasKeyRefAttribute) {
				String refName = entryEle.getAttribute(KEY_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'key-ref' attribute", entryEle);
				}
				RuntimeBeanReference ref = new RuntimeBeanReference(refName);
				ref.setSource(extractSource(entryEle));
				key = ref;
			}
			else if (keyEle != null) {
				key = parseKeyElement(keyEle, bd, defaultKeyType);
			}
			else {
				error("<entry> element must specify a key", entryEle);
			}

			// Extract value from attribute or sub-element.
			Object value = null;
			boolean hasValueAttribute = entryEle.hasAttribute(VALUE_ATTRIBUTE);
			boolean hasValueRefAttribute = entryEle.hasAttribute(VALUE_REF_ATTRIBUTE);
			boolean hasValueTypeAttribute = entryEle.hasAttribute(VALUE_TYPE_ATTRIBUTE);
			if ((hasValueAttribute && hasValueRefAttribute) ||
					((hasValueAttribute || hasValueRefAttribute)) && valueEle != null) {
				error("<entry> element is only allowed to contain either " +
						"'value' attribute OR 'value-ref' attribute OR <value> sub-element", entryEle);
			}
			if ((hasValueTypeAttribute && hasValueRefAttribute) ||
					(hasValueTypeAttribute && !hasValueAttribute) ||
					(hasValueTypeAttribute && valueEle != null)) {
				error("<entry> element is only allowed to contain a 'value-type' " +
						"attribute when it has a 'value' attribute", entryEle);
			}
			if (hasValueAttribute) {
				String valueType = entryEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
				if (!StringUtils.hasText(valueType)) {
					valueType = defaultValueType;
				}
				value = buildTypedStringValueForMap(entryEle.getAttribute(VALUE_ATTRIBUTE), valueType, entryEle);
			}
			else if (hasValueRefAttribute) {
				String refName = entryEle.getAttribute(VALUE_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'value-ref' attribute", entryEle);
				}
				RuntimeBeanReference ref = new RuntimeBeanReference(refName);
				ref.setSource(extractSource(entryEle));
				value = ref;
			}
			else if (valueEle != null) {
				value = parsePropertySubElement(valueEle, bd, defaultValueType);
			}
			else {
				error("<entry> element must specify a value", entryEle);
			}

			// Add final key and value to the Map.
			map.put(key, value);
		}

		return map;
	}
	// Build a typed String value Object for the given raw value.
	protected final Object buildTypedStringValueForMap(String value, String defaultTypeName, Element entryEle) {
		try {
			TypedStringValue typedValue = buildTypedStringValue(value, defaultTypeName);
			typedValue.setSource(extractSource(entryEle));
			return typedValue;
		}
		catch (ClassNotFoundException ex) {
			error("Type class [" + defaultTypeName + "] not found for Map key/value type", entryEle, ex);
			return value;
		}
	}

	protected Object parseKeyElement(Element keyEle, BeanDefinition bd, String defaultKeyTypeName) {
		NodeList nl = keyEle.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				// Child element is what we're looking for.
				if (subElement != null) {
					error("<key> element must not contain more than one value sub-element", keyEle);
				}
				else {
					subElement = (Element) node;
				}
			}
		}
		return parsePropertySubElement(subElement, bd, defaultKeyTypeName);
	}
	public Properties parsePropsElement(Element propsEle) {
		ManagedProperties props = new ManagedProperties();
		props.setSource(extractSource(propsEle));
		props.setMergeEnabled(parseMergeAttribute(propsEle));

		List<Element> propEles = DomUtils.getChildElementsByTagName(propsEle, PROP_ELEMENT);
		for (Element propEle : propEles) {
			String key = propEle.getAttribute(KEY_ATTRIBUTE);
			// Trim the text value to avoid unwanted whitespace
			// caused by typical XML formatting.
			String value = DomUtils.getTextValue(propEle).trim();
			TypedStringValue keyHolder = new TypedStringValue(key);
			keyHolder.setSource(extractSource(propEle));
			TypedStringValue valueHolder = new TypedStringValue(value);
			valueHolder.setSource(extractSource(propEle));
			props.put(keyHolder, valueHolder);
		}

		return props;
	}
	public boolean parseMergeAttribute(Element collectionElement) {
		String value = collectionElement.getAttribute(MERGE_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(value)) {
			value = this.defaults.getMerge();
		}
		return TRUE_VALUE.equals(value);
	}

	@SuppressWarnings("deprecation")
	public int getAutowireMode(String attValue) {
		String att = attValue;
		if (DEFAULT_VALUE.equals(att)) {
			att = this.defaults.getAutowire();
		}
		int autowire = AbstractBeanDefinition.AUTOWIRE_NO;
		if (AUTOWIRE_BY_NAME_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_BY_NAME;
		}
		else if (AUTOWIRE_BY_TYPE_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
		}
		else if (AUTOWIRE_CONSTRUCTOR_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
		}
		else if (AUTOWIRE_AUTODETECT_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_AUTODETECT;
		}
		// Else leave default value.
		return autowire;
	}

	public int getDependencyCheck(String attValue) {
		String att = attValue;
		if (DEFAULT_VALUE.equals(att)) {
			att = this.defaults.getDependencyCheck();
		}
		if (DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE.equals(att)) {
			return AbstractBeanDefinition.DEPENDENCY_CHECK_ALL;
		}
		else if (DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE.equals(att)) {
			return AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS;
		}
		else if (DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE.equals(att)) {
			return AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
		}
		else {
			return AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;
		}
	}

	//-------- 解析<bean>标签-------------------------------------------------------------------------------------------------------------------------------------------------















	// -------------------解析自定义命名空间的标签----------------------------------------------------------------------

	public BeanDefinition parseCustomElement(Element ele) {
		return parseCustomElement(ele, null);
	}
	public BeanDefinition parseCustomElement(Element ele, BeanDefinition containingBd) {
		// 获取这个自定义标签的命名空间
		String namespaceUri = getNamespaceURI(ele);
		// 根据命名空间URI获取相应的处理器，例如：命名空间为：http://www.springframework.org/schema/context 对应的处理器
		// 是 ContextNamespaceHandler.java，该映射关系保存在对应jar包中/META-INF/spring.handlers文件中
		NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
		if (handler == null) {
			error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
			return null;
		}
		return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
	}
	// -------------------解析自定义命名空间的标签----------------------------------------------------------------------




	// ---------------------修饰BeanDefinition-----------------------------------------------------------------------------------------------

	public BeanDefinitionHolder decorateBeanDefinitionIfRequired(Element ele, BeanDefinitionHolder definitionHolder) {
		return decorateBeanDefinitionIfRequired(ele, definitionHolder, null);
	}
	public BeanDefinitionHolder decorateBeanDefinitionIfRequired(Element ele, BeanDefinitionHolder definitionHolder, BeanDefinition containingBd) {

		BeanDefinitionHolder finalDefinition = definitionHolder;

		// 先修饰基于自定义属性
		NamedNodeMap attributes = ele.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node node = attributes.item(i);
			finalDefinition = decorateIfRequired(node, finalDefinition, containingBd);
		}

		// 修饰自定义嵌套元素
		NodeList children = ele.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				finalDefinition = decorateIfRequired(node, finalDefinition, containingBd);
			}
		}
		return finalDefinition;
	}
	// 获取相应的命名空间处理器进行修饰处理
	private BeanDefinitionHolder decorateIfRequired(Node node, BeanDefinitionHolder originalDef, BeanDefinition containingBd) {

		String namespaceUri = getNamespaceURI(node);
		if (!isDefaultNamespace(namespaceUri)) {
			NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
			if (handler != null) {
				return handler.decorate(node, originalDef, new ParserContext(this.readerContext, this, containingBd));
			}
			else if (namespaceUri != null && namespaceUri.startsWith("http://www.springframework.org/")) {
				error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", node);
			}
			else {
				// A custom namespace, not to be handled by Spring - maybe "xml:...".
				if (logger.isDebugEnabled()) {
					logger.debug("No Spring NamespaceHandler found for XML schema namespace [" + namespaceUri + "]");
				}
			}
		}
		return originalDef;
	}
	// 获取所提供节点的命名空间URI
	public String getNamespaceURI(Node node) {
		return node.getNamespaceURI();
	}
	// 判断是否是默认的命名空间："http://www.springframework.org/schema/beans"
	public boolean isDefaultNamespace(String namespaceUri) {
		return (!StringUtils.hasLength(namespaceUri) || BEANS_NAMESPACE_URI.equals(namespaceUri));
	}
	public boolean isDefaultNamespace(Node node) {
		return isDefaultNamespace(getNamespaceURI(node));
	}













	public final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}
	public final Environment getEnvironment() {
		return this.environment;
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor} to pull the
	 * source metadata from the supplied {@link Element}.
	 */
	protected Object extractSource(Element ele) {
		return this.readerContext.extractSource(ele);
	}


	// 报告一个错误消息
	protected void error(String message, Node source) {
		this.readerContext.error(message, source, this.parseState.snapshot());
	}
	protected void error(String message, Element source) {
		this.readerContext.error(message, source, this.parseState.snapshot());
	}
	protected void error(String message, Element source, Throwable cause) {
		this.readerContext.error(message, source, this.parseState.snapshot(), cause);
	}


	// Initialize the default lazy-init, autowire, dependency check settings, init-method, destroy-method and merge settings.
	// Support nested 'beans'  element use cases by falling back to the given parent in case the defaults are not explicitly set locally.
	public void initDefaults(Element root, BeanDefinitionParserDelegate parent) {
		populateDefaults(this.defaults, (parent != null ? parent.defaults : null), root);
		this.readerContext.fireDefaultsRegistered(this.defaults);
	}

	/**
	 * Initialize the default settings assuming a {@code null} parent delegate.
	 * @deprecated in Spring 3.1 in favor of
	 * {@link #initDefaults(Element, BeanDefinitionParserDelegate)}
	 */
	@Deprecated
	public void initDefaults(Element root) {
		initDefaults(root, null);
	}

	/**
	 * Populate the given DocumentDefaultsDefinition instance with the default lazy-init,
	 * autowire, dependency check settings, init-method, destroy-method and merge settings.
	 * Support nested 'beans' element use cases by falling back to
	 * <literal>parentDefaults</literal> in case the defaults are not explicitly set
	 * locally.
	 * @param defaults the defaults to populate
	 * @param parentDefaults the parent BeanDefinitionParserDelegate (if any) defaults to fall back to
	 * @param root the root element of the current bean definition document (or nested beans element)
	 */
	protected void populateDefaults(DocumentDefaultsDefinition defaults, DocumentDefaultsDefinition parentDefaults, Element root) {
		String lazyInit = root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(lazyInit)) {
			lazyInit = parentDefaults != null ? parentDefaults.getLazyInit() : FALSE_VALUE;
		}
		defaults.setLazyInit(lazyInit);

		String merge = root.getAttribute(DEFAULT_MERGE_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(merge)) {
			merge = parentDefaults != null ? parentDefaults.getMerge() : FALSE_VALUE;
		}
		defaults.setMerge(merge);

		String autowire = root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(autowire)) {
			autowire = parentDefaults != null ? parentDefaults.getAutowire() : AUTOWIRE_NO_VALUE;
		}
		defaults.setAutowire(autowire);

		// don't fall back to parentDefaults for dependency-check as it's no
		// longer supported in <beans> as of 3.0. Therefore, no nested <beans>
		// would ever need to fall back to it.
		defaults.setDependencyCheck(root.getAttribute(DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE));

		if (root.hasAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE)) {
			defaults.setAutowireCandidates(root.getAttribute(DEFAULT_AUTOWIRE_CANDIDATES_ATTRIBUTE));
		}
		else if (parentDefaults != null) {
			defaults.setAutowireCandidates(parentDefaults.getAutowireCandidates());
		}

		if (root.hasAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE)) {
			defaults.setInitMethod(root.getAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE));
		}
		else if (parentDefaults != null) {
			defaults.setInitMethod(parentDefaults.getInitMethod());
		}

		if (root.hasAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE)) {
			defaults.setDestroyMethod(root.getAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE));
		}
		else if (parentDefaults != null) {
			defaults.setDestroyMethod(parentDefaults.getDestroyMethod());
		}

		defaults.setSource(this.readerContext.extractSource(root));
	}


	public DocumentDefaultsDefinition getDefaults() {
		return this.defaults;
	}

	// 返回一个默认设置的 BeanDefinitionDefaults 对象
	public BeanDefinitionDefaults getBeanDefinitionDefaults() {
		BeanDefinitionDefaults bdd = new BeanDefinitionDefaults();
		bdd.setLazyInit("TRUE".equalsIgnoreCase(this.defaults.getLazyInit()));
		bdd.setDependencyCheck(this.getDependencyCheck(DEFAULT_VALUE));
		bdd.setAutowireMode(this.getAutowireMode(DEFAULT_VALUE));
		bdd.setInitMethodName(this.defaults.getInitMethod());
		bdd.setDestroyMethodName(this.defaults.getDestroyMethod());
		return bdd;
	}

	/**
	 * Return any patterns provided in the 'default-autowire-candidates' attribute of the top-level beans element.
	 */
	public String[] getAutowireCandidatePatterns() {
		String candidatePattern = this.defaults.getAutowireCandidates();
		return (candidatePattern != null ? StringUtils.commaDelimitedListToStringArray(candidatePattern) : null);
	}




}
