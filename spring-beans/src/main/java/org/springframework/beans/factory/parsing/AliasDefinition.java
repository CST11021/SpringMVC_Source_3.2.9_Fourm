
package org.springframework.beans.factory.parsing;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.util.Assert;

// 代表在解析进程中一个已经被注册的别名
public class AliasDefinition implements BeanMetadataElement {

	private final String beanName;
	private final String alias;
	private final Object source;

	public AliasDefinition(String beanName, String alias) {
		this(beanName, alias, null);
	}
	public AliasDefinition(String beanName, String alias, Object source) {
		Assert.notNull(beanName, "Bean name must not be null");
		Assert.notNull(alias, "Alias must not be null");
		this.beanName = beanName;
		this.alias = alias;
		this.source = source;
	}


	// 返回beanName
	public final String getBeanName() {
		return this.beanName;
	}
	// 返回这个Bean已经注册的别名
	public final String getAlias() {
		return this.alias;
	}
	public final Object getSource() {
		return this.source;
	}

}
