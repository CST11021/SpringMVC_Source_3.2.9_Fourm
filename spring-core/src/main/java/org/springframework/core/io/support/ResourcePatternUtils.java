
package org.springframework.core.io.support;

import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

public abstract class ResourcePatternUtils {

	// resourceLocation 是以“classpath*:”或“classpath:”开头的字符串或是一个URL地址
	public static boolean isUrl(String resourceLocation) {
		return (resourceLocation != null &&
				(resourceLocation.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX) ||
						ResourceUtils.isUrl(resourceLocation)));
	}

	public static ResourcePatternResolver getResourcePatternResolver(ResourceLoader resourceLoader) {
		Assert.notNull(resourceLoader, "ResourceLoader must not be null");
		if (resourceLoader instanceof ResourcePatternResolver) {
			return (ResourcePatternResolver) resourceLoader;
		}
		else if (resourceLoader != null) {
			return new PathMatchingResourcePatternResolver(resourceLoader);
		}
		else {
			return new PathMatchingResourcePatternResolver();
		}
	}

}
