
package org.springframework.beans.factory.config;

import org.springframework.beans.BeanMetadataElement;

// 暴露了bean名称的引用接口
public interface BeanReference extends BeanMetadataElement {

	// 返回此引用指向的目标bean名称
	String getBeanName();

}
