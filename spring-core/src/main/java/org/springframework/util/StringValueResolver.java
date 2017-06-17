
package org.springframework.util;


// 字符串的解析的策略接口
public interface StringValueResolver {

	// 解决给定的字符串值的占位符，例如解析
	String resolveStringValue(String strVal);

}
