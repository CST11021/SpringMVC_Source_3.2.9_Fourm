
package org.springframework.core.io;

import org.springframework.util.ResourceUtils;


public interface ResourceLoader {

	// CLASSPATH_URL_PREFIX -> "classpath:"
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

	// 根据给定的路径获取Resource
	Resource getResource(String location);

	ClassLoader getClassLoader();

}
