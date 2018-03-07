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

import java.sql.Connection;

/**
	一个数据库可能拥有多个客户端，这些客户端以并发的方式访问数据库。数据库中的相同数据可能同时被多个事务访问，如果没有采
取必要的隔离措施，就会导致各种并发问题，这些并发问题归结为5类，包括3类数据读取问题和2类数据更新问题：

	脏读：A事务读取了B事务尚未提交的更改数据。
	不可重复读：A事务读取了B事务已经提交的更改数据。A事务在B事务提交前读取了一次数据，然后在B事务提交后又再一次读取了同一个数据，造成两次读取的数据不一致。
	幻读：A事务读取了B事务已经提交的新增数据。
	第一类丢失更新：A事务撤销时，把B事务已经提交的更新数据覆盖了。
	第二类丢失更新：A事务覆盖B事务已经提交的数据。

	不可重复读和幻读的区别，前者是已经提交的更改（或删除）数据，后者是已经提交新增数据，为了避免这两种情况，采取的对策是
不同的，防止读取更改数据只需对操作的数据添加行级别锁，而防止读取新增数据则须添加表级别锁。



Spring中7种事务传播级别
	事务传播行为的概念：
		事务传播行为即事务嵌套，例如当我们调用一个基于Spring的Service接口方法（如UserService.addUser()）时，它将运行于Spring管理的事务环境中，Service接口方法可能会在内部调用其他的Service接口方法以共同完成一个完整的业务操作，因此就会产生服务接口方法嵌套调用的情况，Spring通过事务传播行为控制当前的事务如何传播到被嵌套调用的目标服务接口方法中。

	Spring在TransactionDefinition接口中定义7种事务传播级别：
		Propagation_Required：如果上下文存在事务，那么就加入到事务中执行，否则新建事务执行（这个级别通常能满足处理大多数的业务场景，是默认的spring事务传播级别）。
		Propagation_Supports：如果上下文存在事务，则支持事务加入事务，否则使用非事务的方式执行（所以说，并非所有的包在transactionTemplate.execute中的代码都会有事务支持。这个通常是用来处理那些并非原子性的非核心业务逻辑操作。应用场景较少）。
		Propagation_Mandatory：该级别的事务要求上下文中必须要存在事务，否则就会抛出异常（配置该方式的传播级别是有效的控制上下文调用代码遗漏添加事务控制的保证手段。比如一段代码不能单独被调用执行，但是一旦被调用，就必须有事务包含的情况）。
		Propagation_Requires_New：每次都会新建一个事务，并且同时将上下文中的事务挂起，执行当前新建事务完成以后，上下文事务恢复再执行。
		Propagation_Not_Supported：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
		Propagation_Never：以非事务方式执行，如果当前存在事务，则抛出异常。
		Propagation_Nested：如果上下文中存在事务，则嵌套事务执行，如果不存在事务，则新建事务。

	那么什么是嵌套事务呢？很多人的理解都有些偏差。
		嵌套是子事务套在父事务中执行，子事务是父事务的一部分，在进入子事务之前，父事务建立一个回滚点，叫save point，然后执行子事务，这个子事务的执行也算是父事务的一部分，然后子事务执行结束，父事务继续执行。重点就在于那个save point。看几个问题就明了了。
	如果子事务回滚，会发生什么？ 
		父事务会回滚到进入子事务前建立的save point，然后尝试其他的事务或者其他的业务逻辑，父事务之前的操作不会受到影响，更不会自动回滚。
	如果父事务回滚，会发生什么？ 
		父事务回滚，子事务也会跟着回滚！为什么呢，因为父事务结束之前，子事务是不会提交的，我们说子事务是父事务的一部分，正是这个道理。那么：
	事务的提交，是什么情况？ 
		是父事务先提交，然后子事务提交，还是子事务先提交，父事务再提交？答案是第二种情况，还是那句话，子事务是父事务的一部分，由父事务统一提交。

 */
public interface TransactionDefinition {

	/* ---------------- Spring中的7种事务传播级别 ---------------- */
	int PROPAGATION_REQUIRED = 0;
	int PROPAGATION_SUPPORTS = 1;
	int PROPAGATION_MANDATORY = 2;
	int PROPAGATION_REQUIRES_NEW = 3;
	int PROPAGATION_NOT_SUPPORTED = 4;
	int PROPAGATION_NEVER = 5;
	int PROPAGATION_NESTED = 6;


	
	
	/* ---------------- Spring中5种事务隔离级别 ------------------------------------ */
	// 使用底层数据库的默认隔离级别
	int ISOLATION_DEFAULT = -1;
	// 隔离级别最低，只保证不会出现第一类丢失更新
	int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;
	// 允许出现：不可重复读、幻读和第二类丢失更新；不允许出现：第一类丢失更新、脏读
	int ISOLATION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;
	// 允许出现：幻读；不允许出现：第一类丢失更新、第二类丢失更新、脏读、不可重复读
	int ISOLATION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;
	// 事务界别最高，5种并发事务问题都不会出现
	int ISOLATION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;


	// Use the default timeout of the underlying transaction system,or none if timeouts are not supported.
	int TIMEOUT_DEFAULT = -1;


	// 获取事务传播行为（级别）
	int getPropagationBehavior();
	// 获取事务隔离级别
	int getIsolationLevel();
	// 返回设置的事务超时时间
	int getTimeout();
	// 返回是否优化为只读事务
	boolean isReadOnly();
	// 返回该事务的名称
	String getName();

}
