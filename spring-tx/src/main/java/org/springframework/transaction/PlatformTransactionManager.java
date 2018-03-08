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

 Spring将事务管理委托给底层具体的持久化实现框架来完成。因此，Spring为不同的持久化框架提供了PlatformTransactionManager接口的实现类，如下表：

 不同持久化技术对应的事务管理器实现类
 ---------------------------------------------------------------------------------------------
 JpaTransactionManager			使用JPA进行持久化时，使用该事务管理器
 HibernateTransactionManager	使用Hibernate X.0(X可为3,4,5）版本进行持久化时，使用该事务管理器
 DataSourceTransactionManager	使用Spring JDBC或MyBatis等基于DataSource数据源的持久化技术时，使用该事务管理器
 JdoTransactionManager			使用JDO进行持久化时，使用该事务管理器
 JtaTransactionManager			具有多个数据源的全局事务使用该事务管理器（不管采用何种持久化技术）

 这些事务管理器都是对特定事务实现框架的代理，这样就可以通过Spring所提交的高级抽象对不同种类的事务实现使用相同的方式进行管理，而不用关心具体的实现。
 要实现事务管理，首先要再Spring中配置好相应的事务管理器，为事务管理器指定数据资源及一些其他事务管理控制属性。

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
