
package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinition;

// 泛型的Bean定义：
// GenericBeanDefinition是一站式的标准bean definition，除了具有指定类、可选的构造参数值和属性参数这些其它bean definition
// 一样的特性外，它还具有通过parenetName属性来灵活设置parent bean definition。通常，GenericBeanDefinition用来注册用户可见
// 的bean definition(可见的bean definition意味着可以在该类bean definition上定义post-processor来对bean进行操作，甚至为配置
// parent name做扩展准备)。
// RootBeanDefinition / ChildBeanDefinition用来预定义具有parent/child关系的bean definition。
@SuppressWarnings("serial")
public class GenericBeanDefinition extends AbstractBeanDefinition {

	// 表示当前bean的父beanName
	private String parentName;

	public GenericBeanDefinition() {
		super();
	}
	public GenericBeanDefinition(BeanDefinition original) {
		super(original);
	}


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public String getParentName() {
		return this.parentName;
	}


	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new GenericBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof GenericBeanDefinition && super.equals(other)));
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Generic bean");
		if (this.parentName != null) {
			sb.append(" with parent '").append(this.parentName).append("'");
		}
		sb.append(": ").append(super.toString());
		return sb.toString();
	}

}
