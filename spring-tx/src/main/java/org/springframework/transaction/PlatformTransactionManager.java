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

/** Spring的编程式事务，可以通过事务管理器来实现，如：
	PlatformTransactionManager tran = new DataSourceTransactionManager(datasource);// 事务管理器
	DefaultTransactionDefinition def = new DefaultTransactionDefinition();// 事务定义类
	TransactionStatus status = tran.getTransaction(def);// 返回事务对象
	try {
		userService.saveWithoutTransaction(zhangsanUser);
		tran.commit(status);
	} catch (Exception ex) {
		tran.rollback(status);
		System.out.println("出错了，事务回滚...");
	}
 */
public interface PlatformTransactionManager {

	// 执行持久化操作前，会先调用 getTransaction() 方法
	// 根据指定的事务信息，返回当前活动的事务或创建一个新的事务。
	TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException;

	// 执行持久化操作后，可以调用该方法进行事务提交
	void commit(TransactionStatus status) throws TransactionException;

	// 执行持久化操作异常后，可以调用该方法进行事务回滚
	void rollback(TransactionStatus status) throws TransactionException;

}
