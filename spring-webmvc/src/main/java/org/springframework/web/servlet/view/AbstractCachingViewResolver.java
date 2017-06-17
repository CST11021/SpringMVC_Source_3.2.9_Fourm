
package org.springframework.web.servlet.view;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

// 这是一个抽象类，这种视图解析器会把它曾经解析过的视图保存起来，然后每次要解析视图的时候先从缓存里面找，如果找到了对应的视图就直接返回，
// 如果没有就创建一个新的视图对象，然后把它放到一个用于缓存的map中，接着再把新建的视图返回。使用这种视图缓存的方式可以把解析视图的性能问题降到最低。
public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

	//默认缓存的最大个数
	public static final int DEFAULT_CACHE_LIMIT = 1024;

	// Dummy marker object for unresolved views in the cache Maps
	private static final View UNRESOLVED_VIEW = new View() {
		public String getContentType() {
			return null;
		}
		public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
		}
	};


	//能缓存的最大个数
	private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

	// Whether we should refrain from resolving views again if unresolved once
	private boolean cacheUnresolved = true;

	// 不使用加锁机制来实现快速访问视图，返回已经解析过的的视图对象
	private final Map<Object, View> viewAccessCache = new ConcurrentHashMap<Object, View>(DEFAULT_CACHE_LIMIT);

	// Map from view key to View instance, synchronized for View creation
	// 缓存已经创建的视图对象
	@SuppressWarnings("serial")
	private final Map<Object, View> viewCreationCache = new LinkedHashMap<Object, View>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
				@Override
				protected boolean removeEldestEntry(Map.Entry<Object, View> eldest) {
					if (size() > getCacheLimit()) {
						viewAccessCache.remove(eldest.getKey());
						return true;
					}
					else {
						return false;
					}
				}
			};



	// 指定最大缓存的最大个数，默认1024
	public void setCacheLimit(int cacheLimit) {
		this.cacheLimit = cacheLimit;
	}
	public int getCacheLimit() {
		return this.cacheLimit;
	}

	// 设置是否启用缓存
	public void setCache(boolean cache) {
		this.cacheLimit = (cache ? DEFAULT_CACHE_LIMIT : 0);
	}
	// 判断是否启用缓存
	public boolean isCache() {
		return (this.cacheLimit > 0);
	}

	/**
	 * Whether a view name once resolved to {@code null} should be cached and
	 * automatically resolved to {@code null} subsequently.
	 * <p>Default is "true": unresolved view names are being cached, as of Spring 3.1.
	 * Note that this flag only applies if the general {@link #setCache "cache"}
	 * flag is kept at its default of "true" as well.
	 * <p>Of specific interest is the ability for some AbstractUrlBasedView
	 * implementations (FreeMarker, Velocity, Tiles) to check if an underlying
	 * resource exists via {@link AbstractUrlBasedView#checkResource(Locale)}.
	 * With this flag set to "false", an underlying resource that re-appears
	 * is noticed and used. With the flag set to "true", one check is made only.
	 */
	public void setCacheUnresolved(boolean cacheUnresolved) {
		this.cacheUnresolved = cacheUnresolved;
	}
	/**
	 * Return if caching of unresolved views is enabled.
	 */
	public boolean isCacheUnresolved() {
		return this.cacheUnresolved;
	}

	// //核心方法 根据视图名解析为具体的视图最对象
	public View resolveViewName(String viewName, Locale locale) throws Exception {

		// 如果没有启用视图缓存功能就直接创建一个视图
		if (!isCache()) {
			return createView(viewName, locale);
		}
		else {
			// 计算缓存的视图key
			Object cacheKey = getCacheKey(viewName, locale);
			// 尝试从解析过的视图缓存中获取 View 对象
			View view = this.viewAccessCache.get(cacheKey);

			if (view == null) {
				// //如果已经解析的视图缓存中没有，那么从已经创建的视图缓存中获取
				synchronized (this.viewCreationCache) {
					view = this.viewCreationCache.get(cacheKey);

					// 如果 viewCreationCache 中没有，就进行创建
					if (view == null) {
						// Ask the subclass to create the View object.
						view = createView(viewName, locale);
						if (view == null && this.cacheUnresolved) {
							view = UNRESOLVED_VIEW;
						}
						if (view != null) {
							// 创建后放入两个Cache中
							this.viewAccessCache.put(cacheKey, view);
							this.viewCreationCache.put(cacheKey, view);
							if (logger.isTraceEnabled()) {
								logger.trace("Cached view [" + cacheKey + "]");
							}
						}
					}
				}
			}
			return (view != UNRESOLVED_VIEW ? view : null);
		}
	}

	// 根据视图名获取缓存的视图 key
	protected Object getCacheKey(String viewName, Locale locale) {
		return viewName + "_" + locale;
	}

	// 从缓存中清除视图
	public void removeFromCache(String viewName, Locale locale) {
		if (!isCache()) {
			logger.warn("View caching is SWITCHED OFF -- removal not necessary");
		}
		else {
			Object cacheKey = getCacheKey(viewName, locale);
			Object cachedView;
			synchronized (this.viewCreationCache) {
				this.viewAccessCache.remove(cacheKey);
				cachedView = this.viewCreationCache.remove(cacheKey);
			}
			if (logger.isDebugEnabled()) {
				// Some debug output might be useful...
				if (cachedView == null) {
					logger.debug("No cached instance for view '" + cacheKey + "' was found");
				}
				else {
					logger.debug("Cache for view " + cacheKey + " has been cleared");
				}
			}
		}
	}
	// 清除缓存中所有的视图
	public void clearCache() {
		logger.debug("Clearing entire view cache");
		synchronized (this.viewCreationCache) {
			this.viewAccessCache.clear();
			this.viewCreationCache.clear();
		}
	}


	// 创建一个视图，具体的实现由子类实现 loadView(...)方法来实现
	protected View createView(String viewName, Locale locale) throws Exception {
		return loadView(viewName, locale);
	}
	protected abstract View loadView(String viewName, Locale locale) throws Exception;

}
