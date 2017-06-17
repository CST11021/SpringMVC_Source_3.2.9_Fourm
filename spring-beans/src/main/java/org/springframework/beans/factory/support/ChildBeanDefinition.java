
package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.ObjectUtils;

// ChildBeanDefinition从父类继承构造参数值，属性值并可以重写父类的方法，同时也可以增加新的属性或者方法。(类同于java类的继承关系)。
// 若指定初始化方法，销毁方法或者静态工厂方法，ChildBeanDefinition将重写相应父类的设置。depends on，autowire mode，dependency check，sigleton，lazy init 一般由子类自行设定。
// 注意：从spring 2.5 开始，提供了一个更好的注册bean definition类GenericBeanDefinition，它支持动态定义父依赖，方法是GenericBeanDefinition.setParentName(java.lang.String)，GenericBeanDefinition可以有效的替代ChildBeanDefinition的绝大分部使用场合。
@SuppressWarnings("serial")
public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;


	public ChildBeanDefinition(String parentName) {
		super();
		this.parentName = parentName;
	}
	public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
		super(null, pvs);
		this.parentName = parentName;
	}
	public ChildBeanDefinition(String parentName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {

		super(cargs, pvs);
		this.parentName = parentName;
	}
	public ChildBeanDefinition(String parentName, Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {

		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClass(beanClass);
	}
	public ChildBeanDefinition(String parentName, String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {

		super(cargs, pvs);
		this.parentName = parentName;
		setBeanClassName(beanClassName);
	}
	public ChildBeanDefinition(ChildBeanDefinition original) {
		super((BeanDefinition) original);
	}


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public String getParentName() {
		return this.parentName;
	}

	@Override
	public void validate() throws BeanDefinitionValidationException {
		super.validate();
		if (this.parentName == null) {
			throw new BeanDefinitionValidationException("'parentName' must be set in ChildBeanDefinition");
		}
	}

	@Override
	public AbstractBeanDefinition cloneBeanDefinition() {
		return new ChildBeanDefinition(this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ChildBeanDefinition)) {
			return false;
		}
		ChildBeanDefinition that = (ChildBeanDefinition) other;
		return (ObjectUtils.nullSafeEquals(this.parentName, that.parentName) && super.equals(other));
	}
	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.parentName) * 29 + super.hashCode();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Child bean with parent '");
		sb.append(this.parentName).append("': ").append(super.toString());
		return sb.toString();
	}

}
