
package org.springframework.beans.factory.xml;

// 命名空间解析处理器,NamespaceHandlerResolver 接口只定义了一个方法，并且Spring给出了一个默认的实现，那就是DefaultNamespaceHandlerResolver类
public interface NamespaceHandlerResolver {

	// 解析命名空间的URI字符串并返回一个对应的NamespaceHandler对象，如果没有找到，则返回null
	NamespaceHandler resolve(String namespaceUri);

}
