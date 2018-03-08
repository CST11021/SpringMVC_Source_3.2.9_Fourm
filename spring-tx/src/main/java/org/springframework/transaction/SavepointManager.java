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

package org.springframework.transaction;

/**
 SavepointManager接口基于JDBC 3.0保存点的分段事务控制能力提供嵌套事务的机制。
 SavepointManager接口拥有以下几个方法：
 	这3个方法在底层的资源不支持保存点时，都将抛出NestedTransactionNotSupportException异常。
 */
public interface SavepointManager {

	// 创建一个保存点对象，以便在后面可以利用rollbackToSavepoint(Object savepoint)方法使事务回滚到特定的保存点上，也可以
	// 通过releaseSavepoint()方法释放一个已经不用的保存点。
	Object createSavepoint() throws TransactionException;

	// 将事务回滚到特定的保存点上，被回滚的保存点将自动释放。
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

	// 释放一个保存点。如果事务提交，则所有的保存点会被自动释放，无需手工清除。
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
