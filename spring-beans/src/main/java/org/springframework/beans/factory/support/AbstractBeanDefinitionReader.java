
package org.springframework.beans.factory.support;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

public abstract class AbstractBeanDefinitionReader implements EnvironmentCapable, BeanDefinitionReader {

	protected final Log logger = LogFactory.getLog(getClass());

	// BeanDefinition注册表
	private final BeanDefinitionRegistry registry;
	// 用于加载配置文件
	private ResourceLoader resourceLoader;
	// 用于获取Bean示例的类加载器
	private ClassLoader beanClassLoader;
	private Environment environment;
	// BeanName生成器
	private BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

	// 实例化一个 BeanDefinitionReader 必须指定一个BeanDefinition注册表，然后给这个 BeanDefinitionReader 指定一个ResourceLoader实现和一个Environment实现
	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		// Determine ResourceLoader to use.
		if (this.registry instanceof ResourceLoader) {
			this.resourceLoader = (ResourceLoader) this.registry;
		}
		else {
			this.resourceLoader = new PathMatchingResourcePatternResolver();
		}

		// Inherit Environment if possible
		if (this.registry instanceof EnvironmentCapable) {
			this.environment = ((EnvironmentCapable) this.registry).getEnvironment();
		}
		else {
			this.environment = new StandardEnvironment();
		}
	}




	// 根据配置文件或配置文件路径去解析和加载bean
	public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
		Assert.notNull(resources, "Resource array must not be null");
		int counter = 0;
		for (Resource resource : resources) {
			counter += loadBeanDefinitions(resource);
		}
		return counter;
	}
	public int loadBeanDefinitions(String location) throws BeanDefinitionStoreException {
		return loadBeanDefinitions(location, null);
	}
	public int loadBeanDefinitions(String... locations) throws BeanDefinitionStoreException {
		Assert.notNull(locations, "Location array must not be null");
		int counter = 0;
		for (String location : locations) {
			counter += loadBeanDefinitions(location);
		}
		return counter;
	}
	// actualResources 用来保存 根据路径解析后的Resource，可能location是一个相对路径，该路径对应了多个配置文件
	public int loadBeanDefinitions(String location, Set<Resource> actualResources) throws BeanDefinitionStoreException {

		// 首先获取一个 ResourceLoader 用来根据文件路径来获取 Resource
		ResourceLoader resourceLoader = getResourceLoader();
		if (resourceLoader == null) {
			throw new BeanDefinitionStoreException("Cannot import bean definitions from location [" + location + "]: no ResourceLoader available");
		}

		if (resourceLoader instanceof ResourcePatternResolver) {
			// Resource pattern matching available.
			try {
				// 根据文件地址获取 Resource
				Resource[] resources = ((ResourcePatternResolver) resourceLoader).getResources(location);
				//
				int loadCount = loadBeanDefinitions(resources);
				if (actualResources != null) {
					for (Resource resource : resources) {
						actualResources.add(resource);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded " + loadCount + " bean definitions from location pattern [" + location + "]");
				}
				return loadCount;
			}
			catch (IOException ex) {
				throw new BeanDefinitionStoreException("Could not resolve bean definition resource pattern [" + location + "]", ex);
			}
		}
		else {
			// Can only load single resources by absolute URL.
			Resource resource = resourceLoader.getResource(location);
			// 获取 resource 后就可以从资源文件中将配置的bean信息转化为BeanDefinition，并将其注册到一个BeanDefinitionRegistry了
			int loadCount = loadBeanDefinitions(resource);
			if (actualResources != null) {
				actualResources.add(resource);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Loaded " + loadCount + " bean definitions from location [" + location + "]");
			}
			return loadCount;
		}
	}


	// getter and setter ...

	public final BeanDefinitionRegistry getBeanFactory() {
		return this.registry;
	}
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}
	public ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}
	public ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	public Environment getEnvironment() {
		return this.environment;
	}

	public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator = (beanNameGenerator != null ? beanNameGenerator : new DefaultBeanNameGenerator());
	}
	public BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}
}
