
package org.springframework.beans.factory.config;

import java.beans.PropertyEditor;
import java.security.AccessControlContext;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringValueResolver;

// ConfigurableBeanFactory：是一个重要的接口，增强IOC容器的可定制性，它定义了设置类装载器、属性编辑器、容器初始化后置处理器等方法
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {

	String SCOPE_SINGLETON = "singleton";
	String SCOPE_PROTOTYPE = "prototype";

	// 设置父容器
	void setParentBeanFactory(BeanFactory parentBeanFactory) throws IllegalStateException;

	// 设置和获取BeanClassLoader
	void setBeanClassLoader(ClassLoader beanClassLoader);
	ClassLoader getBeanClassLoader();
	void setTempClassLoader(ClassLoader tempClassLoader);
	ClassLoader getTempClassLoader();

	// 是否需要缓存bean metadata,比如bean difinition 和解析好的classes.默认开启缓存
	void setCacheBeanMetadata(boolean cacheBeanMetadata);
	boolean isCacheBeanMetadata();

	// 定义用于解析bean definition的表达式解析器
	void setBeanExpressionResolver(BeanExpressionResolver resolver);
	BeanExpressionResolver getBeanExpressionResolver();

	// 类型转换器
	void setConversionService(ConversionService conversionService);
	ConversionService getConversionService();

	// 添加一个属性编辑器
	void addPropertyEditorRegistrar(PropertyEditorRegistrar registrar);

	//BeanFactory用来转换bean属性值或者参数值的自定义转换器
	void registerCustomEditor(Class<?> requiredType, Class<? extends PropertyEditor> propertyEditorClass);
	void copyRegisteredEditorsTo(PropertyEditorRegistry registry);

	// 类型转换器
	void setTypeConverter(TypeConverter typeConverter);
	TypeConverter getTypeConverter();

	// 添加一个占位符解析器
	void addEmbeddedValueResolver(StringValueResolver valueResolver);
	// 将value占位符解析为字符串
	String resolveEmbeddedValue(String value);

	// Bean处理器
	void addBeanPostProcessor(BeanPostProcessor beanPostProcessor);
	int getBeanPostProcessorCount();

	// 注册自定义的作用域
	void registerScope(String scopeName, Scope scope);
	// 获取或有注册的作用域
	String[] getRegisteredScopeNames();
	// 根据名称获取对应的作用域对象
	Scope getRegisteredScope(String scopeName);

	// 访问权限控制
	AccessControlContext getAccessControlContext();

	// 合并其他ConfigurableBeanFactory的配置,包括上面说到的BeanPostProcessor,作用域等
	void copyConfigurationFrom(ConfigurableBeanFactory otherFactory);

	// 注册别名
	void registerAlias(String beanName, String alias) throws BeanDefinitionStoreException;
	void resolveAliases(StringValueResolver valueResolver);
	// 根据beanName获取一个BeanDefinition，如果指定的bean对应的是一个子bean，则需合并父bean的信息
	BeanDefinition getMergedBeanDefinition(String beanName) throws NoSuchBeanDefinitionException;
	// 判断这个name对应的bean是否为一个工厂bean
	boolean isFactoryBean(String name) throws NoSuchBeanDefinitionException;

	// bean创建状态控制.在解决循环依赖时有使用
	void setCurrentlyInCreation(String beanName, boolean inCreation);
	boolean isCurrentlyInCreation(String beanName);

	// 处理bean依赖问题
	void registerDependentBean(String beanName, String dependentBeanName);
	String[] getDependentBeans(String beanName);
	String[] getDependenciesForBean(String beanName);

	//bean生命周期管理 ----- 销毁bean
	//
	void destroyBean(String beanName, Object beanInstance);
	// 从作用域缓存中移除这个bean
	void destroyScopedBean(String beanName);
	// 销毁所有的单例对象
	void destroySingletons();

}
