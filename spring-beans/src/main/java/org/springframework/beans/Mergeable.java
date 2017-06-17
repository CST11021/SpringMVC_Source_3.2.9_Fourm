
package org.springframework.beans;

/**
 * 表示一个集合对象可以与它父类的的集合合并的接口，它有以下实现类
 *
 * @author Rob Harrop
 * @since 2.0
 * @see org.springframework.beans.factory.support.ManagedSet
 * @see org.springframework.beans.factory.support.ManagedList
 * @see org.springframework.beans.factory.support.ManagedMap
 * @see org.springframework.beans.factory.support.ManagedProperties
 */
public interface Mergeable {

	// 是否启用合并
	boolean isMergeEnabled();

	// 将当前值集与所提供对象的值合并。
	Object merge(Object parent);

}
