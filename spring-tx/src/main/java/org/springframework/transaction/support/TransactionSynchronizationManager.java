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

package org.springframework.transaction.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.NamedThreadLocal;
import org.springframework.core.OrderComparator;
import org.springframework.util.Assert;

/**
 Spring将JDBC的Connection、Hibernate的Session等访问数据库的连接或会话对象统称为资源，这些资源在同一时刻是不能多线程共享的。
 为了让DAO、Service类可能做到singleton，Spring的事务同步管理器类SynchronizationManager使用ThreadLocal为不同事务线程提供了
 独立的资源副本，同时维护事务配置的属性和运行状态信息。事务同步管理器是Spring事务管理的基石，不管用户使用的编程式事务管理，
 还是声明式事务管理，都离不开事务同步管理器。
 Spring框架为不同的持久化技术提供了一套从TransactionSynchronizationManager中获取对应线程绑定资源的工具类，如下表：
 ------------------------------------------------------------
 Spring JDBC或Mybatis	DataSourceUtils
 Hibernate X.0			SessionFactoryUtils
 JPA					EntityManagerFactoryUtils
 JDO					PersistenceManagerFactoryUtils
-------------------------------------------------------------
 	这些工具类都提供了静态的方法，通过这些方法可以获取和当前线程绑定的资源，如DataSourceUtils.getConnection(DataSource dataSource)
 方法可以从指定的数据源中获取和当前线程绑定的Connection，而Hibernate的SessionFactoryUtils.getSession(SessionFactory sessionFactory, boolean allowCreate)
 方法则可以从指定的SessionFactory中获取和当前线程绑定的Session。
 	当需要脱离模板类，手工操作底层持久化技术的元素API时，就需要通过这些工具类获取线程绑定的资源，而不应该直接从DataSource
 或SessionFactory中获取。因为后者不能获得与本线程相关的资源，因此无法让数据操作参与到与本线程相关的事务环境中。
 	这些工具类还有另外一个重要的用途：将特定异常转换为Spring的DAO异常。
 	Spring为不同的持久化技术提供模板类，模板类在内部通过资源获取工具间接访问TransactionSynchronizationManager中的线程绑定
 资源。所以，如果DAO使用模板类进行持久化操作，这些DAO就可以配置成singleton。如果不使用模板类，也可以直接通过资源获取工具
 类访问线程相关的资源

 TransactionSynchronizationManager将DAO、Service类中影响线程安全的所有“状态”统一抽取到该类中，并用ThreadLocal进行替换，
 从DAO（必须是基于模板类或资源获取工具类创建的DAO）和Service（必须采用Spring事务管理机制）摘掉了非线程安全的帽子，完成了
 脱胎换骨式的身份转变。
 */
public abstract class TransactionSynchronizationManager {

	private static final Log logger = LogFactory.getLog(TransactionSynchronizationManager.class);

	// 用于保存每个事务线程对应的Connection或Session等类型的资源
	private static final ThreadLocal<Map<Object, Object>> resources = new NamedThreadLocal<Map<Object, Object>>("Transactional resources");
	//
	private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations = new NamedThreadLocal<Set<TransactionSynchronization>>("Transaction synchronizations");
	// 用于保存每个事务线程对应事务的名称
	private static final ThreadLocal<String> currentTransactionName = new NamedThreadLocal<String>("Current transaction name");
	// 用于保存每个事务线程对应事务的read-only状态
	private static final ThreadLocal<Boolean> currentTransactionReadOnly = new NamedThreadLocal<Boolean>("Current transaction read-only status");
	// 用于保存每个事务线程对应事务的隔离级别
	private static final ThreadLocal<Integer> currentTransactionIsolationLevel = new NamedThreadLocal<Integer>("Current transaction isolation level");
	// 用于保存每个事务线程对应事务的激活状态
	private static final ThreadLocal<Boolean> actualTransactionActive = new NamedThreadLocal<Boolean>("Actual transaction active");



	// ------------------------ Management of transaction-associated resource handles 事务资源相关方法 ------------------------

	/**
	 * Return all resources that are bound to the current thread.
	 * <p>Mainly for debugging purposes. Resource managers should always invoke
	 * {@code hasResource} for a specific resource key that they are interested in.
	 * @return a Map with resource keys (usually the resource factory) and resource
	 * values (usually the active resource object), or an empty Map if there are
	 * currently no resources bound
	 * @see #hasResource
	 */
	public static Map<Object, Object> getResourceMap() {
		Map<Object, Object> map = resources.get();
		return (map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap());
	}
	/**
	 * Check if there is a resource for the given key bound to the current thread.
	 * @param key the key to check (usually the resource factory)
	 * @return if there is a value bound to the current thread
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static boolean hasResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doGetResource(actualKey);
		return (value != null);
	}
	/**
	 * Retrieve a resource for the given key that is bound to the current thread.
	 * @param key the key to check (usually the resource factory)
	 * @return a value bound to the current thread (usually the active
	 * resource object), or {@code null} if none
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static Object getResource(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doGetResource(actualKey);
		if (value != null && logger.isTraceEnabled()) {
			logger.trace("Retrieved value [" + value + "] for key [" + actualKey + "] bound to thread [" +
					Thread.currentThread().getName() + "]");
		}
		return value;
	}
	/**
	 * Actually check the value of the resource that is bound for the given key.
	 */
	private static Object doGetResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.get(actualKey);
		// Transparently remove ResourceHolder that was marked as void...
		if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
			map.remove(actualKey);
			// Remove entire ThreadLocal if empty...
			if (map.isEmpty()) {
				resources.remove();
			}
			value = null;
		}
		return value;
	}

	// 绑定资源到当前线程，value通常就是一个资源对象
	public static void bindResource(Object key, Object value) throws IllegalStateException {
		// 如果传入的key是一个代理对象，则需要这里进行获取原始的目标对象
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Assert.notNull(value, "Value must not be null");

		Map<Object, Object> map = resources.get();
		if (map == null) {
			map = new HashMap<Object, Object>();
			resources.set(map);
		}

		Object oldValue = map.put(actualKey, value);
		// 如果是一个 ResourceHolder 对象，则这里赋值为null，以防止连接泄露
		if (oldValue instanceof ResourceHolder && ((ResourceHolder) oldValue).isVoid()) {
			oldValue = null;
		}
		if (oldValue != null) {
			throw new IllegalStateException("Already value [" + oldValue + "] for key [" +
					actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Bound value [" + value + "] for key [" + actualKey + "] to thread [" +
					Thread.currentThread().getName() + "]");
		}
	}
	/**
	 * Unbind a resource for the given key from the current thread.
	 * @param key the key to unbind (usually the resource factory)
	 * @return the previously bound value (usually the active resource object)
	 * @throws IllegalStateException if there is no value bound to the thread
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static Object unbindResource(Object key) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doUnbindResource(actualKey);
		if (value == null) {
			throw new IllegalStateException(
					"No value for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
		}
		return value;
	}
	/**
	 * Unbind a resource for the given key from the current thread.
	 * @param key the key to unbind (usually the resource factory)
	 * @return the previously bound value, or {@code null} if none bound
	 */
	public static Object unbindResourceIfPossible(Object key) {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		return doUnbindResource(actualKey);
	}
	/**
	 * Actually remove the value of the resource that is bound for the given key.
	 */
	private static Object doUnbindResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.remove(actualKey);
		// Remove entire ThreadLocal if empty...
		if (map.isEmpty()) {
			resources.remove();
		}
		// Transparently suppress a ResourceHolder that was marked as void...
		if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
			value = null;
		}
		if (value != null && logger.isTraceEnabled()) {
			logger.trace("Removed value [" + value + "] for key [" + actualKey + "] from thread [" +
					Thread.currentThread().getName() + "]");
		}
		return value;
	}



	// ------------------------------------ 管理的事务同步相关方法 -------------------------------------

	// 判断当前线程是否有正在执行的事务操作，正在执行的事务会被保存在 synchronizations 属性中
	public static boolean isSynchronizationActive() {
		return (synchronizations.get() != null);
	}
	// 为当前线程激活事务同步，在事务开始时，有事务管理器调用，如果已经激活过了，则抛出异常
	public static void initSynchronization() throws IllegalStateException {
		if (isSynchronizationActive()) {
			throw new IllegalStateException("Cannot activate transaction synchronization - already active");
		}
		logger.trace("Initializing transaction synchronization");
		synchronizations.set(new LinkedHashSet<TransactionSynchronization>());
	}
	/**
	 * Register a new transaction synchronization for the current thread.
	 * Typically called by resource management code.
	 * <p>Note that synchronizations can implement the
	 * {@link org.springframework.core.Ordered} interface.
	 * They will be executed in an order according to their order value (if any).
	 * @param synchronization the synchronization object to register
	 * @throws IllegalStateException if transaction synchronization is not active
	 * @see org.springframework.core.Ordered
	 */
	public static void registerSynchronization(TransactionSynchronization synchronization) throws IllegalStateException {

		Assert.notNull(synchronization, "TransactionSynchronization must not be null");
		if (!isSynchronizationActive()) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		synchronizations.get().add(synchronization);
	}
	/**
	 * Return an unmodifiable snapshot list of all registered synchronizations
	 * for the current thread.
	 * @return unmodifiable List of TransactionSynchronization instances
	 * @throws IllegalStateException if synchronization is not active
	 * @see TransactionSynchronization
	 */
	public static List<TransactionSynchronization> getSynchronizations() throws IllegalStateException {
		Set<TransactionSynchronization> synchs = synchronizations.get();
		if (synchs == null) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		// Return unmodifiable snapshot, to avoid ConcurrentModificationExceptions
		// while iterating and invoking synchronization callbacks that in turn
		// might register further synchronizations.
		if (synchs.isEmpty()) {
			return Collections.emptyList();
		}
		else {
			// Sort lazily here, not in registerSynchronization.
			List<TransactionSynchronization> sortedSynchs = new ArrayList<TransactionSynchronization>(synchs);
			OrderComparator.sort(sortedSynchs);
			return Collections.unmodifiableList(sortedSynchs);
		}
	}
	/**
	 * Deactivate transaction synchronization for the current thread.
	 * Called by the transaction manager on transaction cleanup.
	 * @throws IllegalStateException if synchronization is not active
	 */
	public static void clearSynchronization() throws IllegalStateException {
		if (!isSynchronizationActive()) {
			throw new IllegalStateException("Cannot deactivate transaction synchronization - not active");
		}
		logger.trace("Clearing transaction synchronization");
		synchronizations.remove();
	}



	// ------------------------------------ 暴露事务特性相关方法 -------------------------------------

	/**
	 * Expose the name of the current transaction, if any.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * @param name the name of the transaction, or {@code null} to reset it
	 * @see org.springframework.transaction.TransactionDefinition#getName()
	 */
	public static void setCurrentTransactionName(String name) {
		currentTransactionName.set(name);
	}
	/**
	 * Return the name of the current transaction, or {@code null} if none set.
	 * To be called by resource management code for optimizations per use case,
	 * for example to optimize fetch strategies for specific named transactions.
	 * @see org.springframework.transaction.TransactionDefinition#getName()
	 */
	public static String getCurrentTransactionName() {
		return currentTransactionName.get();
	}
	/**
	 * Expose a read-only flag for the current transaction.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * @param readOnly {@code true} to mark the current transaction
	 * as read-only; {@code false} to reset such a read-only marker
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 */
	public static void setCurrentTransactionReadOnly(boolean readOnly) {
		currentTransactionReadOnly.set(readOnly ? Boolean.TRUE : null);
	}
	/**
	 * Return whether the current transaction is marked as read-only.
	 * To be called by resource management code when preparing a newly
	 * created resource (for example, a Hibernate Session).
	 * <p>Note that transaction synchronizations receive the read-only flag
	 * as argument for the {@code beforeCommit} callback, to be able
	 * to suppress change detection on commit. The present method is meant
	 * to be used for earlier read-only checks, for example to set the
	 * flush mode of a Hibernate Session to "FlushMode.NEVER" upfront.
	 * @see org.springframework.transaction.TransactionDefinition#isReadOnly()
	 * @see TransactionSynchronization#beforeCommit(boolean)
	 */
	public static boolean isCurrentTransactionReadOnly() {
		return (currentTransactionReadOnly.get() != null);
	}
	/**
	 * Expose an isolation level for the current transaction.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * @param isolationLevel the isolation level to expose, according to the
	 * JDBC Connection constants (equivalent to the corresponding Spring
	 * TransactionDefinition constants), or {@code null} to reset it
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public static void setCurrentTransactionIsolationLevel(Integer isolationLevel) {
		currentTransactionIsolationLevel.set(isolationLevel);
	}
	/**
	 * Return the isolation level for the current transaction, if any.
	 * To be called by resource management code when preparing a newly
	 * created resource (for example, a JDBC Connection).
	 * @return the currently exposed isolation level, according to the
	 * JDBC Connection constants (equivalent to the corresponding Spring
	 * TransactionDefinition constants), or {@code null} if none
	 * @see java.sql.Connection#TRANSACTION_READ_UNCOMMITTED
	 * @see java.sql.Connection#TRANSACTION_READ_COMMITTED
	 * @see java.sql.Connection#TRANSACTION_REPEATABLE_READ
	 * @see java.sql.Connection#TRANSACTION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_UNCOMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_READ_COMMITTED
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_REPEATABLE_READ
	 * @see org.springframework.transaction.TransactionDefinition#ISOLATION_SERIALIZABLE
	 * @see org.springframework.transaction.TransactionDefinition#getIsolationLevel()
	 */
	public static Integer getCurrentTransactionIsolationLevel() {
		return currentTransactionIsolationLevel.get();
	}
	/**
	 * Expose whether there currently is an actual transaction active.
	 * Called by the transaction manager on transaction begin and on cleanup.
	 * @param active {@code true} to mark the current thread as being associated
	 * with an actual transaction; {@code false} to reset that marker
	 */
	public static void setActualTransactionActive(boolean active) {
		actualTransactionActive.set(active ? Boolean.TRUE : null);
	}
	/**
	 * Return whether there currently is an actual transaction active.
	 * This indicates whether the current thread is associated with an actual
	 * transaction rather than just with active transaction synchronization.
	 * <p>To be called by resource management code that wants to discriminate
	 * between active transaction synchronization (with or without backing
	 * resource transaction; also on PROPAGATION_SUPPORTS) and an actual
	 * transaction being active (with backing resource transaction;
	 * on PROPAGATION_REQUIRES, PROPAGATION_REQUIRES_NEW, etc).
	 * @see #isSynchronizationActive()
	 */
	public static boolean isActualTransactionActive() {
		return (actualTransactionActive.get() != null);
	}
	/**
	 * Clear the entire transaction synchronization state for the current thread:
	 * registered synchronizations as well as the various transaction characteristics.
	 * @see #clearSynchronization()
	 * @see #setCurrentTransactionName
	 * @see #setCurrentTransactionReadOnly
	 * @see #setCurrentTransactionIsolationLevel
	 * @see #setActualTransactionActive
	 */
	public static void clear() {
		clearSynchronization();
		setCurrentTransactionName(null);
		setCurrentTransactionReadOnly(false);
		setCurrentTransactionIsolationLevel(null);
		setActualTransactionActive(false);
	}

}
