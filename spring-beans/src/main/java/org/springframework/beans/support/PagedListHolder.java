/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.util.Assert;

/**
 * PagedListHolder is a simple state holder for handling lists of objects, separating them into pages. Page numbering starts with 0.
 *
 * <p>This is mainly targetted at usage in web UIs. Typically, an instance will be
 * instantiated with a list of beans, put into the session, and exported as model.
 * The properties can all be set/get programmatically, but the most common way will
 * be data binding, i.e. populating the bean from request parameters. The getters
 * will mainly be used by the view.
 *
 * <p>Supports sorting the underlying list via a {@link SortDefinition} implementation,
 * available as property "sort". By default, a {@link MutableSortDefinition} instance
 * will be used, toggling the ascending value on setting the same property again.
 *
 * <p>The data binding names have to be called "pageSize" and "sort.ascending",
 * as expected by BeanWrapper. Note that the names and the nesting syntax match
 * the respective JSTL EL expressions, like "myModelAttr.pageSize" and
 * "myModelAttr.sort.ascending".
 *
 * @author Juergen Hoeller
 * @since 19.05.2003
 * @see #getPageList()
 * @see org.springframework.beans.support.MutableSortDefinition
 */


// PagedListHolder是一个简单的状态持有者，用于处理对象列表，将它们分成页面。页码编号从0开始
@SuppressWarnings("serial")
public class PagedListHolder<E> implements Serializable {

	public static final int DEFAULT_PAGE_SIZE = 10;

	public static final int DEFAULT_MAX_LINKED_PAGES = 10;

	// 表示一个列表元素
	private List<E> source;

	private Date refreshDate;

	// 排序实现
	private SortDefinition sort;

	private SortDefinition sortUsed;

	private int pageSize = DEFAULT_PAGE_SIZE;

	private int page = 0;

	private boolean newPageSet;

	private int maxLinkedPages = DEFAULT_MAX_LINKED_PAGES;


	public PagedListHolder() {
		this(new ArrayList<E>(0));
	}
	public PagedListHolder(List<E> source) {
		this(source, new MutableSortDefinition(true));
	}
	public PagedListHolder(List<E> source, SortDefinition sort) {
		setSource(source);
		setSort(sort);
	}



	public void setSource(List<E> source) {
		Assert.notNull(source, "Source List must not be null");
		this.source = source;
		this.refreshDate = new Date();
		this.sortUsed = null;
	}
	public List<E> getSource() {
		return this.source;
	}

	/**
	 * Return the last time the list has been fetched from the source provider.
	 */
	public Date getRefreshDate() {
		return this.refreshDate;
	}


	public void setSort(SortDefinition sort) {
		this.sort = sort;
	}
	public SortDefinition getSort() {
		return this.sort;
	}


	public void setPageSize(int pageSize) {
		if (pageSize != this.pageSize) {
			this.pageSize = pageSize;
			if (!this.newPageSet) {
				this.page = 0;
			}
		}
	}
	public int getPageSize() {
		return this.pageSize;
	}

	public void setPage(int page) {
		this.page = page;
		this.newPageSet = true;
	}
	public int getPage() {
		this.newPageSet = false;
		if (this.page >= getPageCount()) {
			this.page = getPageCount() - 1;
		}
		return this.page;
	}

	public void setMaxLinkedPages(int maxLinkedPages) {
		this.maxLinkedPages = maxLinkedPages;
	}
	public int getMaxLinkedPages() {
		return this.maxLinkedPages;
	}


	public int getPageCount() {
		float nrOfPages = (float) getNrOfElements() / getPageSize();
		return (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages);
	}

	public boolean isFirstPage() {
		return getPage() == 0;
	}
	public boolean isLastPage() {
		return getPage() == getPageCount() -1;
	}

	public void previousPage() {
		if (!isFirstPage()) {
			this.page--;
		}
	}
	public void nextPage() {
		if (!isLastPage()) {
			this.page++;
		}
	}


	// 返回列表的总数
	public int getNrOfElements() {
		return getSource().size();
	}

	// 返回当前页面上第一个元素的元素索引。元素编号从0开始
	public int getFirstElementOnPage() {
		return (getPageSize() * getPage());
	}

	// 返回当前页面上最后一个元素的元素索引。元素编号从0开始。
	public int getLastElementOnPage() {
		int endIndex = getPageSize() * (getPage() + 1);
		int size = getNrOfElements();
		return (endIndex > size ? size : endIndex) - 1;
	}

	// 返回表示当前页面的子列表。
	public List<E> getPageList() {
		return getSource().subList(getFirstElementOnPage(), getLastElementOnPage() + 1);
	}

	// 返回在当前页面周围创建链接的第一页。
	public int getFirstLinkedPage() {
		return Math.max(0, getPage() - (getMaxLinkedPages() / 2));
	}

	// 返回在当前页面周围创建链接的最后一页。
	public int getLastLinkedPage() {
		return Math.min(getFirstLinkedPage() + getMaxLinkedPages() - 1, getPageCount() - 1);
	}


	/**
	 * Resort the list if necessary, i.e. if the current {@code sort} instance isn't equal to the backed-up {@code sortUsed} instance.
	 * <p>Calls {@code doSort} to trigger actual sorting.
	 * @see #doSort
	 */
	public void resort() {
		SortDefinition sort = getSort();
		if (sort != null && !sort.equals(this.sortUsed)) {
			this.sortUsed = copySortDefinition(sort);
			doSort(getSource(), sort);
			setPage(0);
		}
	}

	/**
	 * Create a deep copy of the given sort definition, for use as state holder to compare a modified sort definition against.
	 * <p>Default implementation creates a MutableSortDefinition instance.
	 * Can be overridden in subclasses, in particular in case of custom
	 * extensions to the SortDefinition interface. Is allowed to return
	 * null, which means that no sort state will be held, triggering
	 * actual sorting for each {@code resort} call.
	 * @param sort the current SortDefinition object
	 * @return a deep copy of the SortDefinition object
	 * @see MutableSortDefinition#MutableSortDefinition(SortDefinition)
	 */
	protected SortDefinition copySortDefinition(SortDefinition sort) {
		return new MutableSortDefinition(sort);
	}

	// 实际上根据给定的排序定义执行给定源列表的排序。默认实现使用Spring的PropertyComparator。可以在子类中重写
	protected void doSort(List<E> source, SortDefinition sort) {
		PropertyComparator.sort(source, sort);
	}

}
