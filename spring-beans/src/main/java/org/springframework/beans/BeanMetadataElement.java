
package org.springframework.beans;

// 用于获取元素配置源的接口
public interface BeanMetadataElement {

	// Return the configuration source {@code Object} for this metadata element (may be {@code null}).
	// 返回这个元素的配置源（就是这个元素是在那个文件中定义的）,可能返回null
	Object getSource();

}
