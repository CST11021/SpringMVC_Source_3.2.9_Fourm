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

import org.springframework.transaction.TransactionStatus;

/**
 * Callback interface for transactional code. Used with {@link TransactionTemplate}'s
 * {@code execute} method, often as anonymous class within a method implementation.
 *
 * <p>Typically used to assemble various calls to transaction-unaware data access
 * services into a higher-level service method with transaction demarcation. As an
 * alternative, consider the use of declarative transaction demarcation (e.g. through
 * Spring's {@link org.springframework.transaction.annotation.Transactional} annotation).
 *
 * @author Juergen Hoeller
 * @since 17.03.2003
 * @see TransactionTemplate
 * @see CallbackPreferringPlatformTransactionManager
 */
public interface TransactionCallback<T> {

	// 事务模板类的回调接口，可以通过实现该接口执行一系列的持久化操作
	T doInTransaction(TransactionStatus status);

}
