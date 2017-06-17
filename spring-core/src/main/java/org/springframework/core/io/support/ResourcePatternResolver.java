
package org.springframework.core.io.support;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

// 所有ApplicationContext实现类都实现了类似于PathMatchingResourcePatternResolver的功能，可以通过带前缀的Ant风格的资源文件路径装载Spring配置文件
public interface ResourcePatternResolver extends ResourceLoader {

	String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

	// 根据Ant风格的路径获取所有配置文件
	Resource[] getResources(String locationPattern) throws IOException;

}
