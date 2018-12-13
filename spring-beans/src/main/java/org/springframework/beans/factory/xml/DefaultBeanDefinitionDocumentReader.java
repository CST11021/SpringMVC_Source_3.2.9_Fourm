
package org.springframework.beans.factory.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

public class DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader {
	protected final Log logger = LogFactory.getLog(getClass());

	public static final String BEAN_ELEMENT = BeanDefinitionParserDelegate.BEAN_ELEMENT;
	public static final String NESTED_BEANS_ELEMENT = "beans";
	public static final String ALIAS_ELEMENT = "alias";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String ALIAS_ATTRIBUTE = "alias";
	public static final String IMPORT_ELEMENT = "import";
	public static final String RESOURCE_ATTRIBUTE = "resource";
	public static final String PROFILE_ATTRIBUTE = "profile";


	private Environment environment;
	// 解析XML文件的上下文
	private XmlReaderContext readerContext;
	// 该类主要用来解析<bean>标签，它内部也存在一个 XmlReaderContext 的引用
	private BeanDefinitionParserDelegate delegate;




	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
		this.readerContext = readerContext;
		logger.debug("Loading bean definitions");
		Element root = doc.getDocumentElement();
		// 获取xml根节点后开始对XML的真正加载解析，Spring配置文件以<beans>标签作为根节点
		doRegisterBeanDefinitions(root);
	}
	// 以XML的根节点（即beans标签）为参数开始真正解析XML，并注册BeanDefinition
	protected void doRegisterBeanDefinitions(Element root) {
		// 处理<beans>标签的profile属性：该属性配置用于配置多个环境，方便切换开发、部署环境，最常用的就是更换不同的数据库
		String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
		if (StringUtils.hasText(profileSpec)) {
			Assert.state(this.environment != null, "Environment must be set for evaluating profiles");
			// 可以设置多个profile属性值，使用",; "进行分隔
			String[] specifiedProfiles = StringUtils.tokenizeToStringArray(profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			if (!this.environment.acceptsProfiles(specifiedProfiles)) {
				return;
			}
		}

		// 配置文件会使用import来引入别的配置文件（子文件），在解析子配置时，需要将父文件的解析结果引进来
		BeanDefinitionParserDelegate parent = this.delegate;
		this.delegate = createDelegate(this.readerContext, root, parent);

		// preProcessXml和postProcessXml都是空方法，这里用到模板模式的设计模式，主要用来子类重载这两个方法，可以实现在Bean解析前后做一些处理操作
		preProcessXml(root);
		parseBeanDefinitions(root, this.delegate);// 开始解析<beans>内的元素
		postProcessXml(root);

		this.delegate = parent;
	}

	protected BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate) {

		BeanDefinitionParserDelegate delegate = createHelper(readerContext, root, parentDelegate);
		if (delegate == null) {
			delegate = new BeanDefinitionParserDelegate(readerContext, this.environment);
			delegate.initDefaults(root, parentDelegate);
		}
		return delegate;
	}
	@Deprecated
	protected BeanDefinitionParserDelegate createHelper(XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate) {

		return null;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	protected final XmlReaderContext getReaderContext() {
		return this.readerContext;
	}
	protected Object extractSource(Element ele) {
		return this.readerContext.extractSource(ele);
	}


	// 解析文档中根级别的元素:"import", "alias", "bean".
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		//对于根节点或子节点，如果是默认命名空间的话采用parseDefaultElement方法进行解析，否则使用parseCustomElement方法对自定义命名空间进行解析
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				if (node instanceof Element) {
					Element ele = (Element) node;
					if (delegate.isDefaultNamespace(ele)) {
						// 解析默认命名空间的标签
						parseDefaultElement(ele, delegate);
					}
					else {
						// 解析自定义命名空间的标签：定义命名空间的处理器都定义在spring.handlers文件中
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			delegate.parseCustomElement(root);
		}
	}
	// 解析默认命名空间，分别对import、alias、bean和beans进行处理
	private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		// 处理import标签：处理方式是直接解析配置文件，解析完后再回来继续解析下一个标签
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
		// 处理alias别名标签：处理方式是将别名注册到bean别名的注册表里，别名注册表本质就是一个 alias--> beanName 的 key-value 集合
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
		// 处理bean标签，bean标签的处理最为复杂
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
		// 处理beans标签
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// 如果是beans标签，则递归调用下
			doRegisterBeanDefinitions(ele);
		}
	}
	// 处理import标签
	protected void importBeanDefinitionResource(Element ele) {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		// 如果不存在Resource属性，则不做任何处理
		if (!StringUtils.hasText(location)) {
			getReaderContext().error("Resource location must not be empty", ele);
			return;
		}

		// 解析系统属性，如：“${user.dir}”，返回<import>标签中resource属性的路径
		location = environment.resolveRequiredPlaceholders(location);

		Set<Resource> actualResources = new LinkedHashSet<Resource>(4);

		// 判断localtion是绝对路径还是相对路径
		boolean absoluteLocation = false;
		try {
			absoluteLocation = ResourcePatternUtils.isUrl(location) || ResourceUtils.toURI(location).isAbsolute();
		}
		catch (URISyntaxException ex) {
			// cannot convert to an URI, considering the location relative unless it is the well-known Spring prefix "classpath*:"
		}

		// 如果是绝对URI则直接根据地址加载对应的配置文件，解析，并完成bean注册
		if (absoluteLocation) {
			try {
				// 如果配置了resource文件，则会直接解析配置文件里的内容
				int importCount = getReaderContext().getReader().loadBeanDefinitions(location, actualResources);
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
				}
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to import bean definitions from URL location [" + location + "]", ele, ex);
			}
		}
		//如果是相对地址则根据相对地址计算出绝对地址
		else {
			try {
				int importCount;
				// Resource存在多个实现类,如VfsResource FileSystemResource等，而每个Resource的createRelative方式实现都不一样，所以这里先使用子类的方法尝试解析
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				if (relativeResource.exists()) {
					importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
					actualResources.add(relativeResource);
				}
				else {
					// 获取对象路径是在具体的那个路径下
					String baseLocation = getReaderContext().getResource().getURL().toString();
					// StringUtils.applyRelativePath(baseLocation, location)用来返回绝对路径，然后根据绝对路径去加载对应的配置文件，解析，并完成bean注册
					importCount = getReaderContext().getReader().loadBeanDefinitions(StringUtils.applyRelativePath(baseLocation, location), actualResources);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Failed to resolve current resource location", ele, ex);
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to import bean definitions from relative location [" + location + "]", ele, ex);
			}
		}

		Resource[] actResArray = actualResources.toArray(new Resource[actualResources.size()]);
		// 没处理完一个<import>标签后进行监听器激活处理
		getReaderContext().fireImportProcessed(location, actResArray, extractSource(ele));
	}
	// 处理alias标签
	protected void processAliasRegistration(Element ele) {
		/**
		 在对bean进行定义时，除了使用id属性来指定名称之外，为了提供多个名称，可以使用alias标签来指定。而所有的这些名称都指向同一个bean，在某些情况下提供别名非常有用，
		 比如为了让应用的每一个组件能更容易的对公共组件进行引用。然而，在定义bean时就指定所有的别名并不是总是恰当的。有时我们期望能在当前位置为那些在别处定义的bean引入别名。

		 在XML配置文件中，可用单独的<alias/>元素来完成bean别名的定义。如：
		 配置文件中定义了一个JavaBean
		 <bean id="some" class="src.com.Some"/>
		 我要给这个JavaBean增加别名，以方便不同对象来调用。我们就可以这样写：
		 <bean id="some" class="src.com.Some"/>
		 <alias name="some" alias="someJava,oneBean,twoBean"/>
		 或者是用name属性来指定，如：
		 <bean id="some" name="oneBean,twoBean,threeBean" class="src.com.Some"/>

		 考虑一个更为具体的例子，组件A在XML配置文件中定义了一个名为componentA-dataSource的DataSource bean。但组件B却想在其XML文件中以componentB-dataSource的名字来引用此bean。
		 而且在主程序MyApp的XML配置文件中，希望以myApp-dataSource的名字来引用此bean。最后容器加载三个XML文件来生成最终的ApplicationContext，在此情形下，
		 可通过在MyApp XML文件中添加下列alias元素来实现：
		 <alias name="componentA-dataSource" alias="componentB-dataSource"/>
		 <alias name="componentA-dataSource" alias="myApp-dataSource" />
		 这样一来，每个组件及主程序就可通过唯一名字来引用同一个数据源而互不干扰。
		 */
		String name = ele.getAttribute(NAME_ATTRIBUTE);
		String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
		boolean valid = true;
		// valid 用来表示是否有别名，别名可以定义在name和alias属性上
		if (!StringUtils.hasText(name)) {
			getReaderContext().error("Name must not be empty", ele);
			valid = false;
		}
		if (!StringUtils.hasText(alias)) {
			getReaderContext().error("Alias must not be empty", ele);
			valid = false;
		}
		if (valid) {
			try {
				// 注册别名，别名注册表本质就是一个 alias--> beanName 的 key-value 集合
				getReaderContext().getRegistry().registerAlias(name, alias);
			}
			catch (Exception ex) {
				getReaderContext().error("Failed to register alias '" + alias + "' for bean with name '" + name + "'", ele, ex);
			}
			// 别名注册后通知监听器做相应处理
			getReaderContext().fireAliasRegistered(name, alias, extractSource(ele));
		}
	}
	// 处理bean标签
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		// 解析完一个bean标签后返回一个 BeanDefinitionHolder
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			// 对BeanDefinition进行装饰：解决当bean使用默认的标签配置，但是子元素使用自定义配置的情况，需要对标签下的自定义标签，再次解析
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// 将注册操作委托给BeanDefinitionReaderUtils
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" + bdHolder.getBeanName() + "'", ele, ex);
			}
			// 通知监听器解析已经注册完成，当程序开发人员需要对注册BeanDefinition事件进行监听时，可以通过注册监听器的方
			// 式将处理逻辑写入监听器中，目前在Spring中并没有对此事件做任何逻辑处理
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}

	// 开始解析xml的根节点前做什么
	protected void preProcessXml(Element root) {}
	// 解析完xml后做什么
	protected void postProcessXml(Element root) {}

}
