
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

// 对BeanDefinition进一步包装:封装BeanDefinition与alias的关系
public class BeanDefinitionHolder implements BeanMetadataElement {

	private final BeanDefinition beanDefinition;
	private final String beanName;
	private final String[] aliases;


	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName) {
		this(beanDefinition, beanName, null);
	}
	public BeanDefinitionHolder(BeanDefinition beanDefinition, String beanName, String[] aliases) {
		Assert.notNull(beanDefinition, "BeanDefinition must not be null");
		Assert.notNull(beanName, "Bean name must not be null");
		this.beanDefinition = beanDefinition;
		this.beanName = beanName;
		this.aliases = aliases;
	}
	public BeanDefinitionHolder(BeanDefinitionHolder beanDefinitionHolder) {
		Assert.notNull(beanDefinitionHolder, "BeanDefinitionHolder must not be null");
		this.beanDefinition = beanDefinitionHolder.getBeanDefinition();
		this.beanName = beanDefinitionHolder.getBeanName();
		this.aliases = beanDefinitionHolder.getAliases();
	}


	// 返回这个bean的配置源（这个bean是在那个配置文件定义的）
	public Object getSource() {
		return this.beanDefinition.getSource();
	}

	// 判断给定的名称candidateName 是为这个bean的beanName或是这个Bean的别名
	public boolean matchesName(String candidateName) {
		return (candidateName != null &&
				(candidateName.equals(this.beanName) || ObjectUtils.containsElement(this.aliases, candidateName)));
	}

	// 返回这个Bean的简单描述，包括beanName和别名信息
	public String getShortDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Bean definition with name '").append(this.beanName).append("'");
		if (this.aliases != null) {
			sb.append(" and aliases [").append(StringUtils.arrayToCommaDelimitedString(this.aliases)).append("]");
		}
		return sb.toString();
	}

	public String getLongDescription() {
		StringBuilder sb = new StringBuilder(getShortDescription());
		sb.append(": ").append(this.beanDefinition);
		return sb.toString();
	}

	// getter ...
	public BeanDefinition getBeanDefinition() {
		return this.beanDefinition;
	}
	public String getBeanName() {
		return this.beanName;
	}
	public String[] getAliases() {
		return this.aliases;
	}

	@Override
	public String toString() {
		return getLongDescription();
	}
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanDefinitionHolder)) {
			return false;
		}
		BeanDefinitionHolder otherHolder = (BeanDefinitionHolder) other;
		return this.beanDefinition.equals(otherHolder.beanDefinition) &&
				this.beanName.equals(otherHolder.beanName) &&
				ObjectUtils.nullSafeEquals(this.aliases, otherHolder.aliases);
	}
	@Override
	public int hashCode() {
		int hashCode = this.beanDefinition.hashCode();
		hashCode = 29 * hashCode + this.beanName.hashCode();
		hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.aliases);
		return hashCode;
	}

}
