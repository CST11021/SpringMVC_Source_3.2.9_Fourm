
package org.springframework.beans.factory.xml;

import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.springframework.util.Assert;

// EntityResolver 接口声明的方法.
// public abstract InputSource resolveEntity (String publicId, String systemId) throws SAXException, IOException; 这里,他接收2个参数,publicId ,systemId ,并返回一个InputStream对象

// 如果我们在解析验证模式为xsd的配置文件,代码如下：
//		<?xml version="1.0" encoding="UTF-8"?>
//		<beans xmlns="http://www.springframework.org/schema/beans"
//		       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//		      xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd">
//		...
//		</beans>
//
//		读取得到以下参数：
//		publicId : null
//		systemId : http://www.springframework.org/schema/beans/spring-beans-2.5.xsd




// 如果我们解析的是DTD的配置文件;
//		<?xml version="1.0" encoding="UTF-8"?>
//		<!DOCTYPE beans PUBLIC  "-//SPRING//DTD BEAN//EN"  "http://www.springframework.org/dtd/spring-beans.dtd">
//		<beans>
//		...
//		</beans>
//
//		读取得到以下参数:
//		publicId : -//SPRING//DTD BEAN//EN
//		systemId : http://www.springframework.org/dtd/spring-beans.dtd

public class DelegatingEntityResolver implements EntityResolver {

	//** Suffix for DTD files */
	public static final String DTD_SUFFIX = ".dtd";
	//** Suffix for schema definition files */
	public static final String XSD_SUFFIX = ".xsd";

	private final EntityResolver dtdResolver;
	private final EntityResolver schemaResolver;


	public DelegatingEntityResolver(ClassLoader classLoader) {
		this.dtdResolver = new BeansDtdResolver();
		this.schemaResolver = new PluggableSchemaResolver(classLoader);
	}
	public DelegatingEntityResolver(EntityResolver dtdResolver, EntityResolver schemaResolver) {
		Assert.notNull(dtdResolver, "'dtdResolver' is required");
		Assert.notNull(schemaResolver, "'schemaResolver' is required");
		this.dtdResolver = dtdResolver;
		this.schemaResolver = schemaResolver;
	}


	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		//默认的寻找规则,(即:通过网络,实现上就是声明DTD的地址URI地址来下载DTD声明),这样才会造成延迟,用户体验也不好,一般的做法是将验证文件放在自己的工程里面
		if (systemId != null) {
			// 如果是DTD
			if (systemId.endsWith(DTD_SUFFIX)) {
				return this.dtdResolver.resolveEntity(publicId, systemId);
			}
			// 如果是XSD
			else if (systemId.endsWith(XSD_SUFFIX)) {
				// 通过调用META-INF/Spring.schemas解析
				return this.schemaResolver.resolveEntity(publicId, systemId);
			}
		}
		return null;
	}


	@Override
	public String toString() {
		return "EntityResolver delegating " + XSD_SUFFIX + " to " + this.schemaResolver +
				" and " + DTD_SUFFIX + " to " + this.dtdResolver;
	}

}
