
package org.springframework.beans;

// 用于获取元素配置源的接口
public interface BeanMetadataElement {

	// 返回这个Bean的配置源（就是这个元素是在那个文件中定义的）,可能返回null
	// 这里的返回值是一个Object，因为Bean元素可能是在文件（Resource）中定义，也可能是使用注解定义的
	Object getSource();

}
