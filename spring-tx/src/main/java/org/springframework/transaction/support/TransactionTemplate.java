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

package org.springframework.transaction.support;

import java.lang.reflect.UndeclaredThrowableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;

/**
 * Template class that simplifies programmatic transaction demarcation and
 * transaction exception handling.
 *
 * <p>The central method is {@link #execute}, supporting transactional code that
 * implements the {@link TransactionCallback} interface. This template handles
 * the transaction lifecycle and possible exceptions such that neither the
 * TransactionCallback implementation nor the calling code needs to explicitly
 * handle transactions.
 *
 * <p>Typical usage: Allows for writing low-level data access objects that use
 * resources such as JDBC DataSources but are not transaction-aware themselves.
 * Instead, they can implicitly participate in transactions handled by higher-level
 * application services utilizing this class, making calls to the low-level
 * services via an inner-class callback object.
 *
 * <p>Can be used within a service implementation via direct instantiation with
 * a transaction manager reference, or get prepared in an application context
 * and passed to services as bean reference. Note: The transaction manager should
 * always be configured as bean in the application context: in the first case given
 * to the service directly, in the second case given to the prepared template.
 *
 * <p>Supports setting the propagation behavior and the isolation level by name,
 * for convenient configuration in context definitions.
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @see #execute
 * @see #setTransactionManager
 * @see org.springframework.transaction.PlatformTransactionManager
 */
/*
	鉴于使用PlatformTransactionManager进行事务管理的流程比较固定，各个事务管理期间只有部分逻辑存在差异，我们可以考虑向
Spring的数据访问层那样，使用模板方法模式与Callback相结合的方式，对直接使用PlatformTransactionManager进行事务管理的代码进
行封装。这就有了更方便的编程式事务管理方式，即使用TransactionTemplate的编程式事务管理。
 */
@SuppressWarnings("serial")
public class TransactionTemplate extends DefaultTransactionDefinition implements TransactionOperations, InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());
	private PlatformTransactionManager transactionManager;

	public TransactionTemplate() {}
	public TransactionTemplate(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	public TransactionTemplate(PlatformTransactionManager transactionManager, TransactionDefinition transactionDefinition) {
		super(transactionDefinition);
		this.transactionManager = transactionManager;
	}



	// 执行持久化操作的回调接口方法
	public <T> T execute(TransactionCallback<T> action) throws TransactionException {
		if (this.transactionManager instanceof CallbackPreferringPlatformTransactionManager) {
			return ((CallbackPreferringPlatformTransactionManager) this.transactionManager).execute(this, action);
		}
		else {
			TransactionStatus status = this.transactionManager.getTransaction(this);
			T result;
			try {
				result = action.doInTransaction(status);
			}
			catch (RuntimeException ex) {
				// Transactional code threw application exception -> rollback
				rollbackOnException(status, ex);
				throw ex;
			}
			catch (Error err) {
				// Transactional code threw error -> rollback
				rollbackOnException(status, err);
				throw err;
			}
			catch (Exception ex) {
				// Transactional code threw unexpected exception -> rollback
				rollbackOnException(status, ex);
				throw new UndeclaredThrowableException(ex, "TransactionCallback threw undeclared checked exception");
			}
			this.transactionManager.commit(status);
			return result;
		}
	}
	// 在事务范围内异常时被调用，用于事务回滚
	private void rollbackOnException(TransactionStatus status, Throwable ex) throws TransactionException {
		logger.debug("Initiating transaction rollback on application exception", ex);
		try {
			this.transactionManager.rollback(status);
		}
		catch (TransactionSystemException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			ex2.initApplicationException(ex);
			throw ex2;
		}
		catch (RuntimeException ex2) {
			logger.error("Application exception overridden by rollback exception", ex);
			throw ex2;
		}
		catch (Error err) {
			logger.error("Application exception overridden by rollback error", ex);
			throw err;
		}
	}




	// 设置事务管理器，一般该方法用于解析配置时注入事务管理器
	public void setTransactionManager(PlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
	public PlatformTransactionManager getTransactionManager() {
		return this.transactionManager;
	}



	// Bean初始化后处理器方法，此处用来校验事务模板是否已经注入了事务管理器对象
	public void afterPropertiesSet() {
		if (this.transactionManager == null) {
			throw new IllegalArgumentException("Property 'transactionManager' is required");
		}
	}



}
