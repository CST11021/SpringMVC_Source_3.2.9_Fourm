
package org.springframework.core;


// Attribute 操作接口
public interface AttributeAccessor {

	void setAttribute(String name, Object value);
	Object getAttribute(String name);
	Object removeAttribute(String name);
	boolean hasAttribute(String name);
	String[] attributeNames();

}
