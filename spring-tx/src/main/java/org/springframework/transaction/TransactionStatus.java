/*
 * Copyright 2002-2009 the original author or authors.
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
 TransactionStatus代表一个事务的具体运行状态。事务管理器可以通过该接口获取事务运行期的状态信息，也可以通过该接口间接地回
 滚事务，它相比于在抛出异常时回滚事务的方式更具可控性。该接口继承与SavepointManager接口
 */
public interface TransactionStatus extends SavepointManager {

	// 判断当前事务是否是一个新的事务，如果返回false，则表示当前事务是一个已经存在的事务，或者当前操作未运行在事务环境中
	boolean isNewTransaction();

	// 判断当前事务是否在内部创建了一个保存点，该保存点是为了支持Spring的嵌套事务而创建的
	boolean hasSavepoint();

	// 将当前事务设置为rollback-only。通过该标识通知事务管理器只能将事务回滚，事务管理器通过显示调用回滚命令或抛出异常的方式回滚事务
	void setRollbackOnly();

	// 判断当前事务是否已经被标识为rollback-only
	boolean isRollbackOnly();

	// 用于刷新底层会话中的修改到数据库，一般用于刷新如Hibernate/JPA的会话，可能对如JDBC类型的事务无任何影响；
	void flush();

	// 判断事务是否已经完成，比如已提交或已经回滚
	boolean isCompleted();

}
