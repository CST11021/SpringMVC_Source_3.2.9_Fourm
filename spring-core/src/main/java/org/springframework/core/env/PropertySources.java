
package org.springframework.core.env;

//一个保存 key-value 数据结构的容器
public interface PropertySources extends Iterable<PropertySource<?>> {

	// 容器中是否包含某个key
	boolean contains(String name);

	// 根据key，从容器中返回一对 key-value
	PropertySource<?> get(String name);

}
