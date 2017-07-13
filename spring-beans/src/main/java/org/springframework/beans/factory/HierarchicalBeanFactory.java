
package org.springframework.beans.factory;


// 父子级联IOC容器的接口，子容器可以通过接口方法访问父容器
public interface HierarchicalBeanFactory extends BeanFactory {

	// 返回父容器
	BeanFactory getParentBeanFactory();

	// 只在当前容器中查找bean，忽略父子级容器中的bean
	boolean containsLocalBean(String name);

}
