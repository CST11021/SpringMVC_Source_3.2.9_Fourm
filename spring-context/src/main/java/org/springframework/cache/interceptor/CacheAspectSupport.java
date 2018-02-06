/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.cache.interceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Base class for caching aspects, such as the {@link CacheInterceptor}
 * or an AspectJ aspect.
 *
 * <p>This enables the underlying Spring caching infrastructure to be
 * used easily to implement an aspect for any aspect system.
 *
 * <p>Subclasses are responsible for calling methods in this class in
 * the correct order.
 *
 * <p>Uses the <b>Strategy</b> design pattern. A {@link CacheManager}
 * implementation will perform the actual cache management, and a
 * {@link CacheOperationSource} is used for determining caching
 * operations.
 *
 * <p>A cache aspect is serializable if its {@code CacheManager} and
 * {@code CacheOperationSource} are serializable.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Phillip Webb
 * @since 3.1
 */
public abstract class CacheAspectSupport implements InitializingBean {
	protected final Log logger = LogFactory.getLog(getClass());

	// 作为 @Cacheable 注解配置信息的key
	private static final String CACHEABLE = "cacheable";
	// 作为 @CachePut 注解配置信息的key
	private static final String UPDATE = "cacheupdate";
	// 作为 @CacheEvict 注解配置信息的key
	private static final String EVICT = "cacheevict";

	private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
	private CacheManager cacheManager;
	private CacheOperationSource cacheOperationSource;
	private KeyGenerator keyGenerator = new DefaultKeyGenerator();
	// 用于标记this.cacheManager和this.cacheOperationSource是否已经被注入，在this.afterPropertiesSet()方法中会校验
	private boolean initialized = false;

	// 校验this.cacheManager和this.cacheOperationSource，这两个对象不允许为空，并标记initialized为true
	public void afterPropertiesSet() {
		if (this.cacheManager == null) {
			throw new IllegalStateException("Property 'cacheManager' is required");
		}
		if (this.cacheOperationSource == null) {
			throw new IllegalStateException("Property 'cacheOperationSources' is required: " +
					"If there are no cacheable methods, then don't use a cache aspect.");
		}

		this.initialized = true;
	}

	// 业务方法（被缓存注解修饰的方法）被调用时，会执行该方法。Invoker封装业务方法执行逻辑；target表示业务方法的所在Bean
	// 对象；args表示业务方法入参
	protected Object execute(Invoker invoker, Object target, Method method, Object[] args) {
		// 1、如果没有配置CacheManager和CacheOperationSource，则会直接调用业务方法
		if (!this.initialized) {
			return invoker.invoke();
		}

		// get backing class
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(target);
		if (targetClass == null && target != null) {
			targetClass = target.getClass();
		}
		// 获取这个业务方法配置的缓存注解信息
		Collection<CacheOperation> cacheOp = getCacheOperationSource().getCacheOperations(method, targetClass);

		// 存在缓存注解，则解析并执行一系列对应的操作，否则直接调用业务方法
		if (!CollectionUtils.isEmpty(cacheOp)) {
			// 将配置信息进行分类
			Map<String, Collection<CacheOperationContext>> ops =
				createOperationContext(cacheOp, method, args, target, targetClass);
			// start with evictions
			inspectBeforeCacheEvicts(ops.get(EVICT));
			// follow up with cacheable
			CacheStatus status = inspectCacheables(ops.get(CACHEABLE));
			Object retVal;
			Map<CacheOperationContext, Object> updates = inspectCacheUpdates(ops.get(UPDATE));
			if (status != null) {
				if (status.updateRequired) {
					updates.putAll(status.cacheUpdates);
				}
				// return cached object
				else {
					return status.retVal;
				}
			}
			// 调用业务方法，retVal表示业务方法的返回值
			retVal = invoker.invoke();
			inspectAfterCacheEvicts(ops.get(EVICT), retVal);
			if (!updates.isEmpty()) {
				update(updates, retVal);
			}
			return retVal;
		}

		return invoker.invoke();
	}



	/**
	 * Convenience method to return a String representation of this Method
	 * for use in logging. Can be overridden in subclasses to provide a
	 * different identifier for the given method.
	 * @param method the method we're interested in
	 * @param targetClass class the method is on
	 * @return log message identifying this method
	 * @see org.springframework.util.ClassUtils#getQualifiedMethodName
	 */
	protected String methodIdentification(Method method, Class<?> targetClass) {
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		return ClassUtils.getQualifiedMethodName(specificMethod);
	}
	// 返回缓存注解配置的缓存对象，如：@Cacheable(value="accountCache")，则该方法会通过CacheManager获取一个该value值对应
	// 的一个缓存对象
	protected Collection<Cache> getCaches(CacheOperation operation) {
		// 获取缓存注解配置的value值
		Set<String> cacheNames = operation.getCacheNames();
		Collection<Cache> caches = new ArrayList<Cache>(cacheNames.size());
		for (String cacheName : cacheNames) {
			Cache cache = this.cacheManager.getCache(cacheName);
			if (cache == null) {
				throw new IllegalArgumentException("Cannot find cache named '" + cacheName + "' for " + operation);
			}
			caches.add(cache);
		}
		return caches;
	}

	// 将 CacheOperation 对象封装为一个 CacheOperationContext
	protected CacheOperationContext getOperationContext(CacheOperation operation, Method method, Object[] args,
			Object target, Class<?> targetClass) {

		return new CacheOperationContext(operation, method, args, target, targetClass);
	}

	// evictions表示@CacheEvict注解配置的信息
	private void inspectBeforeCacheEvicts(Collection<CacheOperationContext> evictions) {
		inspectCacheEvicts(evictions, true, ExpressionEvaluator.NO_RESULT);
	}
	private void inspectAfterCacheEvicts(Collection<CacheOperationContext> evictions, Object result) {
		inspectCacheEvicts(evictions, false, result);
	}
	private void inspectCacheEvicts(Collection<CacheOperationContext> evictions, boolean beforeInvocation, Object result) {
		if (!evictions.isEmpty()) {
			boolean log = logger.isTraceEnabled();
			for (CacheOperationContext context : evictions) {
				CacheEvictOperation evictOp = (CacheEvictOperation) context.operation;

				if (beforeInvocation == evictOp.isBeforeInvocation()) {
					if (context.isConditionPassing(result)) {
						// for each cache lazy key initialization
						Object key = null;
						for (Cache cache : context.getCaches()) {
							// cache-wide flush
							if (evictOp.isCacheWide()) {
								cache.clear();
								if (log) {
									logger.trace("Invalidating entire cache for operation " + evictOp + " on method " + context.method);
								}
							}
							else {
								// check key
								if (key == null) {
									key = context.generateKey();
								}
								if (log) {
									logger.trace("Invalidating cache key " + key + " for operation " + evictOp + " on method " + context.method);
								}
								cache.evict(key);
							}
						}
					}
					else {
						if (log) {
							logger.trace("Cache condition failed on method " + context.method + " for operation " + context.operation);
						}
					}
				}
			}
		}
	}
	private CacheStatus inspectCacheables(Collection<CacheOperationContext> cacheables) {
		Map<CacheOperationContext, Object> cacheUpdates = new LinkedHashMap<CacheOperationContext, Object>(cacheables.size());
		boolean cacheHit = false;
		Object retVal = null;

		if (!cacheables.isEmpty()) {
			boolean log = logger.isTraceEnabled();
			boolean atLeastOnePassed = false;
			for (CacheOperationContext context : cacheables) {
				if (context.isConditionPassing()) {
					atLeastOnePassed = true;
					Object key = context.generateKey();
					if (log) {
						logger.trace("Computed cache key " + key + " for operation " + context.operation);
					}
					if (key == null) {
						throw new IllegalArgumentException("Null key returned for cache operation (maybe you " +
								"are using named params on classes without debug info?) " + context.operation);
					}
					// add op/key (in case an update is discovered later on)
					cacheUpdates.put(context, key);
					// check whether the cache needs to be inspected or not (the method will be invoked anyway)
					if (!cacheHit) {
						for (Cache cache : context.getCaches()) {
							Cache.ValueWrapper wrapper = cache.get(key);
							if (wrapper != null) {
								retVal = wrapper.get();
								cacheHit = true;
								break;
							}
						}
					}
				}
				else {
					if (log) {
						logger.trace("Cache condition failed on method " + context.method + " for operation " + context.operation);
					}
				}
			}

			// return a status only if at least one cacheable matched
			if (atLeastOnePassed) {
				return new CacheStatus(cacheUpdates, !cacheHit, retVal);
			}
		}

		return null;
	}
	private Map<CacheOperationContext, Object> inspectCacheUpdates(Collection<CacheOperationContext> updates) {
		Map<CacheOperationContext, Object> cacheUpdates = new LinkedHashMap<CacheOperationContext, Object>(updates.size());
		if (!updates.isEmpty()) {
			boolean log = logger.isTraceEnabled();
			for (CacheOperationContext context : updates) {
				if (context.isConditionPassing()) {
					Object key = context.generateKey();
					if (log) {
						logger.trace("Computed cache key " + key + " for operation " + context.operation);
					}
					if (key == null) {
						throw new IllegalArgumentException("Null key returned for cache operation (maybe you " +
								"are using named params on classes without debug info?) " + context.operation);
					}
					// add op/key (in case an update is discovered later on)
					cacheUpdates.put(context, key);
				}
				else {
					if (log) {
						logger.trace("Cache condition failed on method " + context.method + " for operation " + context.operation);
					}
				}
			}
		}
		return cacheUpdates;
	}
	private void update(Map<CacheOperationContext, Object> updates, Object retVal) {
		for (Map.Entry<CacheOperationContext, Object> entry : updates.entrySet()) {
			CacheOperationContext operationContext = entry.getKey();
			if (operationContext.canPutToCache(retVal)) {
				for (Cache cache : operationContext.getCaches()) {
					cache.put(entry.getValue(), retVal);
				}
			}
		}
	}

	// 将配置的注解信息分类保存
	// cacheOperations：表示业务方法配置的所有缓存注解配置信息；method：表示当前调用的业务方法；
	// args：表示业务方法入参；target：业务方法的所在Bean；targetClass：目标对象类型
	private Map<String, Collection<CacheOperationContext>> createOperationContext(
			Collection<CacheOperation> cacheOperations, Method method, Object[] args, Object target, Class<?> targetClass) {

		Map<String, Collection<CacheOperationContext>> result = new LinkedHashMap<String, Collection<CacheOperationContext>>(3);
		Collection<CacheOperationContext> cacheables = new ArrayList<CacheOperationContext>();
		Collection<CacheOperationContext> evicts = new ArrayList<CacheOperationContext>();
		Collection<CacheOperationContext> updates = new ArrayList<CacheOperationContext>();


		for (CacheOperation cacheOperation : cacheOperations) {
			CacheOperationContext opContext = getOperationContext(cacheOperation, method, args, target, targetClass);
			if (cacheOperation instanceof CacheableOperation) {
				cacheables.add(opContext);
			}
			if (cacheOperation instanceof CacheEvictOperation) {
				evicts.add(opContext);
			}
			if (cacheOperation instanceof CachePutOperation) {
				updates.add(opContext);
			}
		}

		result.put(CACHEABLE, cacheables);
		result.put(EVICT, evicts);
		result.put(UPDATE, updates);
		return result;
	}



	// getter and setter ...
	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	public CacheManager getCacheManager() {
		return this.cacheManager;
	}
	public void setCacheOperationSources(CacheOperationSource... cacheOperationSources) {
		Assert.notEmpty(cacheOperationSources, "At least 1 CacheOperationSource needs to be specified");
		this.cacheOperationSource = (cacheOperationSources.length > 1 ?
			new CompositeCacheOperationSource(cacheOperationSources) : cacheOperationSources[0]);
	}
	public CacheOperationSource getCacheOperationSource() {
		return this.cacheOperationSource;
	}
	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}
	public KeyGenerator getKeyGenerator() {
		return this.keyGenerator;
	}


	// 用于封装业务方法调用的接口
	public interface Invoker {
		Object invoke();
	}

	// 该类用于封装业务方法的缓存注解配置信息及对应的目标Bean，目标方法，方法入参等信息
	protected class CacheOperationContext {
		// 表示当前业务方法配置的一个注解信息
		private final CacheOperation operation;
		// 表示当前被调用的业务方法对象
		private final Method method;
		// 表示当前被调用的业务方法参入
		private final Object[] args;
		// 表示当前被调用的业务方法的所在Bean对象
		private final Object target;
		// 目标Bean类型
		private final Class<?> targetClass;
		// 表示缓存对象，该Cache对象由CacheManager创建和管理
		private final Collection<Cache> caches;

		public CacheOperationContext(CacheOperation operation, Method method, Object[] args, Object target, Class<?> targetClass) {
			this.operation = operation;
			this.method = method;
			this.args = args;
			this.target = target;
			this.targetClass = targetClass;
			this.caches = CacheAspectSupport.this.getCaches(operation);
		}

		protected boolean isConditionPassing() {
			return isConditionPassing(ExpressionEvaluator.NO_RESULT);
		}
		protected boolean isConditionPassing(Object result) {
			if (StringUtils.hasText(this.operation.getCondition())) {
				EvaluationContext evaluationContext = createEvaluationContext(result);
				return evaluator.condition(this.operation.getCondition(), this.method, evaluationContext);
			}
			return true;
		}
		protected boolean canPutToCache(Object value) {
			String unless = "";
			if (this.operation instanceof CacheableOperation) {
				unless = ((CacheableOperation) this.operation).getUnless();
			}
			else if (this.operation instanceof CachePutOperation) {
				unless = ((CachePutOperation) this.operation).getUnless();
			}
			if (StringUtils.hasText(unless)) {
				EvaluationContext evaluationContext = createEvaluationContext(value);
				return !evaluator.unless(unless, this.method, evaluationContext);
			}
			return true;
		}
		/**
		 * Computes the key for the given caching operation.
		 * @return generated key (null if none can be generated)
		 */
		protected Object generateKey() {
			if (StringUtils.hasText(this.operation.getKey())) {
				EvaluationContext evaluationContext = createEvaluationContext(ExpressionEvaluator.NO_RESULT);
				return evaluator.key(this.operation.getKey(), this.method, evaluationContext);
			}
			return keyGenerator.generate(this.target, this.method, this.args);
		}
		private EvaluationContext createEvaluationContext(Object result) {
			return evaluator.createEvaluationContext(this.caches, this.method, this.args, this.target, this.targetClass, result);
		}
		protected Collection<Cache> getCaches() {
			return this.caches;
		}
	}
	private static class CacheStatus {
		// caches/key
		final Map<CacheOperationContext, Object> cacheUpdates;
		final boolean updateRequired;
		final Object retVal;

		CacheStatus(Map<CacheOperationContext, Object> cacheUpdates, boolean updateRequired, Object retVal) {
			this.cacheUpdates = cacheUpdates;
			this.updateRequired = updateRequired;
			this.retVal = retVal;
		}
	}

}
