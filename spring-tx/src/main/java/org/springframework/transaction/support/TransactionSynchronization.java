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

/**
 * Interface for transaction synchronization callbacks.
 * Supported by AbstractPlatformTransactionManager.
 *
 * <p>TransactionSynchronization implementations can implement the Ordered interface
 * to influence their execution order. A synchronization that does not implement the
 * Ordered interface is appended to the end of the synchronization chain.
 *
 * <p>System synchronizations performed by Spring itself use specific order values,
 * allowing for fine-grained interaction with their execution order (if necessary).
 *
 * @author Juergen Hoeller
 * @since 02.06.2003
 * @see TransactionSynchronizationManager
 * @see AbstractPlatformTransactionManager
 * @see org.springframework.jdbc.datasource.DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
 */
// TransactionSynchronization主要用于事务同步，提供了挂起、恢复还有提交前、提交后等钩子方法：
public interface TransactionSynchronization {

	//** Completion status in case of proper commit */
	int STATUS_COMMITTED = 0;
	//** Completion status in case of proper rollback */
	int STATUS_ROLLED_BACK = 1;
	//** Completion status in case of heuristic mixed completion or system errors */
	int STATUS_UNKNOWN = 2;

	// 挂起（暂停）事务
	void suspend();
	// 恢复事务
	void resume();
	/**
	 * Flush the underlying session to the datastore, if applicable:
	 * for example, a Hibernate/JPA session.
	 * @see org.springframework.transaction.TransactionStatus#flush()
	 */
	void flush();


	// 在事务提交前调用该方法，该方法在beforeCompletion()方法前调用
	void beforeCommit(boolean readOnly);
	// 在事务提交或回滚前该方法被调用
	void beforeCompletion();
	// 该方法在事务提交后调用，该方法在afterCompletion()方法前盗用
	void afterCommit();
	// 该方法在事务提交或回滚后被调用
	void afterCompletion(int status);

}
