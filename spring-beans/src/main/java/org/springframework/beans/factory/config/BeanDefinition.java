
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;

//BeanDefinition接口在Spring中有三种实现:RootBeanDefinition、ChildBeanDefinition以及GenericBeanDefinition.
// 三种实现均继承了AbstractBeanDefinition,其中BeanDefinition是配置文件<bean>元素在容器中的内部表现形式。
// bean标签拥有class、scope、lazy-init等配置属性，BeanDefinition则提供了相应的beanClass、scope、lazyInit属性，他们是一一对应的

// 在配置文件中可以定义父<bean>和子<bean>，父<bean>用RootBeanDefinition表示，而子<bean>用ChildBeanDefiniton表示，而没有父<bean>的<bean>就使用RootBeanDefinition表示。
// RootBeanDefinition 是最常用的，它表示一个顶级的Bean，而有些Bean它可能会去继承其他的Bean的一些属性，这时候spring用ChildBeanDefinition来表示这个bean
// 一个RootBeanDefinition定义表明它是一个可合并的bean definition：即在spring beanFactory运行期间，可以返回一个特定的bean。RootBeanDefinition可以作为一个重要的通用的bean definition 视图。


// 从spring 2.5 开始，提供了一个更好的注册BeanDefinition类GenericBeanDefinition，它支持动态定义父依赖，方法是GenericBeanDefinition.setParentName(java.lang.String)，
// GenericBeanDefinition 可以有效的替代ChildBeanDefinition的绝大分部使用场合，它除了具有指定类、可选的构造参数值和属性参数这些其它 BeanDefinition 一样的特性外，它还具
// 有通过parenetName属性来灵活设置parent BeanDefinition。RootBeanDefinition 用来在配置阶段进行注册BeanDefinition。然而，从spring 2.5后，编写注册bean definition有了更好
// 的方法：GenericBeanDefinition。GenericBeanDefinition支持动态定义父类依赖，而非硬编码作为RootBeanDefinition。


// 补充：spring配置被解析完后bean是以BeanDefinition的形式存储在注册表中，这里BeanDefinition中的属性配置如果引用了占位符
public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {

	//单例或原型
	String SCOPE_SINGLETON = ConfigurableBeanFactory.SCOPE_SINGLETON;
	String SCOPE_PROTOTYPE = ConfigurableBeanFactory.SCOPE_PROTOTYPE;

	//Bean角色
	int ROLE_APPLICATION = 0;
	int ROLE_SUPPORT = 1;
	int ROLE_INFRASTRUCTURE = 2;

	int getRole();

	boolean isSingleton();
	boolean isPrototype();
	boolean isAbstract();

	String getParentName();
	void setParentName(String parentName);

	String getBeanClassName();
	void setBeanClassName(String beanClassName);

	String getFactoryBeanName();
	void setFactoryBeanName(String factoryBeanName);

	String getFactoryMethodName();
	void setFactoryMethodName(String factoryMethodName);

	String getScope();
	void setScope(String scope);

	boolean isLazyInit();
	void setLazyInit(boolean lazyInit);

	String[] getDependsOn();
	void setDependsOn(String[] dependsOn);

	// 表示bean中的所有属性是否可自动注入
	boolean isAutowireCandidate();
	void setAutowireCandidate(boolean autowireCandidate);

	boolean isPrimary();
	void setPrimary(boolean primary);


	ConstructorArgumentValues getConstructorArgumentValues();
	// 用于保存配置的property标签信息
	MutablePropertyValues getPropertyValues();
	String getDescription();
	String getResourceDescription();
	BeanDefinition getOriginatingBeanDefinition();

}
