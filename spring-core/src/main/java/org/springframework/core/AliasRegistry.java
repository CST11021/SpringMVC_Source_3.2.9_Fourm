
package org.springframework.core;

// 定义对别名的注册接口
public interface AliasRegistry {

	// 给定一个名称，为它注册一个别名。
	void registerAlias(String name, String alias);
	// 从注册表中移除指定的别名
	void removeAlias(String alias);
	// 判断这个名字是定义了一个别名
	boolean isAlias(String beanName);
	// 返回给定名称的别名，如果定义
	String[] getAliases(String name);

}
