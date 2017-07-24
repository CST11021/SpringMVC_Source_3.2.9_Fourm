
package org.springframework.core.env;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

// 提供一个适用于多并发环境的有序 key-value 容器
public class MutablePropertySources implements PropertySources {

	static final String NON_EXISTENT_PROPERTY_SOURCE_MESSAGE = "PropertySource named [%s] does not exist";
	static final String ILLEGAL_RELATIVE_ADDITION_MESSAGE = "PropertySource named [%s] cannot be added relative to itself";

	private final Log logger;

	// CopyOnWrite容器即写时复制的容器。通俗的理解是当我们往一个容器添加元素的时候，不直接往当前容器添加，而是先将当前容器进行Copy，复制出一个新的容器，然后新的容器里添加元素，添加完元素之后，再将原容器的引用指向新的容器。
	// 这样做的好处是我们可以对CopyOnWrite容器进行并发的读，而不需要加锁，因为当前容器不会添加任何元素。所以CopyOnWrite容器也是一种读写分离的思想，读和写不同的容器。
	private final LinkedList<PropertySource<?>> propertySourceList = new LinkedList<PropertySource<?>>();


	public MutablePropertySources() {
		this.logger = LogFactory.getLog(this.getClass());
	}
	public MutablePropertySources(PropertySources propertySources) {
		this();
		for (PropertySource<?> propertySource : propertySources) {
			this.addLast(propertySource);
		}
	}
	MutablePropertySources(Log logger) {
		this.logger = logger;
	}

	// implements PropertySoutces 接口
	public boolean contains(String name) {
		return this.propertySourceList.contains(PropertySource.named(name));
	}
	public PropertySource<?> get(String name) {
		int index = this.propertySourceList.indexOf(PropertySource.named(name));
		return index == -1 ? null : this.propertySourceList.get(index);
	}
	public Iterator<PropertySource<?>> iterator() {
		return this.propertySourceList.iterator();
	}

	// 向容器添加元素
	public void addFirst(PropertySource<?> propertySource) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Adding [%s] PropertySource with highest search precedence",
					propertySource.getName()));
		}
		removeIfPresent(propertySource);
		this.propertySourceList.addFirst(propertySource);
	}
	public void addLast(PropertySource<?> propertySource) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Adding [%s] PropertySource with lowest search precedence",
					propertySource.getName()));
		}
		removeIfPresent(propertySource);
		this.propertySourceList.addLast(propertySource);
	}
	public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Adding [%s] PropertySource with search precedence immediately higher than [%s]",
					propertySource.getName(), relativePropertySourceName));
		}
		assertLegalRelativeAddition(relativePropertySourceName, propertySource);
		removeIfPresent(propertySource);
		int index = assertPresentAndGetIndex(relativePropertySourceName);
		addAtIndex(index, propertySource);
	}
	public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Adding [%s] PropertySource with search precedence immediately lower than [%s]",
					propertySource.getName(), relativePropertySourceName));
		}
		assertLegalRelativeAddition(relativePropertySourceName, propertySource);
		removeIfPresent(propertySource);
		int index = assertPresentAndGetIndex(relativePropertySourceName);
		addAtIndex(index + 1, propertySource);
	}

	//返回元素在容器中的位置
	public int precedenceOf(PropertySource<?> propertySource) {
		return this.propertySourceList.indexOf(propertySource);
	}
	//移除容器元素
	public PropertySource<?> remove(String name) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Removing [%s] PropertySource", name));
		}
		int index = this.propertySourceList.indexOf(PropertySource.named(name));
		return index == -1 ? null : this.propertySourceList.remove(index);
	}

	//替换容器元素
	public void replace(String name, PropertySource<?> propertySource) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Replacing [%s] PropertySource with [%s]",
					name, propertySource.getName()));
		}
		int index = assertPresentAndGetIndex(name);
		this.propertySourceList.set(index, propertySource);
	}

	//返回容器大小
	public int size() {
		return this.propertySourceList.size();
	}

	@Override
	public synchronized String toString() {
		String[] names = new String[this.size()];
		for (int i=0; i < size(); i++) {
			names[i] = this.propertySourceList.get(i).getName();
		}
		return String.format("[%s]", StringUtils.arrayToCommaDelimitedString(names));
	}

	// Ensure that the given property source is not being added relative to itself.
	protected void assertLegalRelativeAddition(String relativePropertySourceName, PropertySource<?> propertySource) {
		String newPropertySourceName = propertySource.getName();
		Assert.isTrue(!relativePropertySourceName.equals(newPropertySourceName),
				String.format(ILLEGAL_RELATIVE_ADDITION_MESSAGE, newPropertySourceName));
	}

	// Log the removal of the given propertySource if it is present.
	protected void removeIfPresent(PropertySource<?> propertySource) {
		if (this.propertySourceList.contains(propertySource)) {
			this.propertySourceList.remove(propertySource);
		}
	}

	//将元素添加到容器中的指定位置
	private void addAtIndex(int index, PropertySource<?> propertySource) {
		removeIfPresent(propertySource);
		this.propertySourceList.add(index, propertySource);
	}

	//返回容器中元素的位置
	private int assertPresentAndGetIndex(String name) {
		int index = this.propertySourceList.indexOf(PropertySource.named(name));
		Assert.isTrue(index >= 0, String.format(NON_EXISTENT_PROPERTY_SOURCE_MESSAGE, name));
		return index;
	}

}
