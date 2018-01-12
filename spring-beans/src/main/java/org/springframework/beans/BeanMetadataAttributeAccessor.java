
package org.springframework.beans;

import org.springframework.core.AttributeAccessorSupport;

// BeanMetadataAttributeAccessor：bean元数据存储器
// 元数据 = bean定义文件资源 + bean中定义的属性集合
// 所有的BeanDefinition对象都继承自该元数据访问器
@SuppressWarnings("serial")
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

	// 表示这个bean对应的配置源对象，比如某一个Bean是定义在那个文件中的那个配置节点，该source对象就是指向相应的配置节点的
	private Object source;

	public void setSource(Object source) {
		this.source = source;
	}
	public Object getSource() {
		return this.source;
	}

	// 给Bean元数据添加一个属性元数据
	public void addMetadataAttribute(BeanMetadataAttribute attribute) {
		super.setAttribute(attribute.getName(), attribute);
	}
	public BeanMetadataAttribute getMetadataAttribute(String name) {
		return (BeanMetadataAttribute) super.getAttribute(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		super.setAttribute(name, new BeanMetadataAttribute(name, value));
	}
	@Override
	public Object getAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}
	@Override
	public Object removeAttribute(String name) {
		BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
		return (attribute != null ? attribute.getValue() : null);
	}

}
