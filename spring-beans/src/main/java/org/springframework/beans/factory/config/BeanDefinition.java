
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.core.AttributeAccessor;

//BeanDefinition接口在Spring中又三种实现:RootBeanDefinition ChildBeanDefinition以及GenericBeanDefinition.
//三种实现均继承了AbstractBeanDefinition,其中BeanDefinition是配置文件<bean>元素在容器中的内部表现形式。
//bean标签拥有class、scope、lazy-init等配置属性，BeanDefinition则提供了相应的beanClass、scope、lazyInit属性，他们是一一对应的
//GenericBeanDefinition是自2.5版本以后新加入的bean文件配置属性定义类，是一站式服务类
//父bean用RootBeanDefinition表示，子bean用ChildBeanDefinition表示，没有父bean的就用RootBeanDefinition表示
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

	boolean isAutowireCandidate();
	void setAutowireCandidate(boolean autowireCandidate);

	boolean isPrimary();
	void setPrimary(boolean primary);


	ConstructorArgumentValues getConstructorArgumentValues();
	MutablePropertyValues getPropertyValues();
	String getDescription();
	String getResourceDescription();
	BeanDefinition getOriginatingBeanDefinition();

}
