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

import java.io.Serializable;

import org.springframework.core.Constants;
import org.springframework.transaction.TransactionDefinition;

/**
 * Default implementation of the {@link TransactionDefinition} interface,
 * offering bean-style configuration and sensible default values
 * (PROPAGATION_REQUIRED, ISOLATION_DEFAULT, TIMEOUT_DEFAULT, readOnly=false).
 *
 * <p>Base class for both {@link TransactionTemplate} and
 * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute}.
 *
 * @author Juergen Hoeller
 * @since 08.05.2003
 */
// 封装事务设置的信息
@SuppressWarnings("serial")
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {

	/** 定义事务隔离界别的前缀：
	 Propagation_Required：如果上下文存在事务，那么就加入到事务中执行，否则新建事务执行（这个级别通常能满足处理大多数的业务场景，是默认的spring事务传播级别）。
	 Propagation_Supports：如果上下文存在事务，则支持事务加入事务，否则使用非事务的方式执行（所以说，并非所有的包在transactionTemplate.execute中的代码都会有事务支持。这个通常是用来处理那些并非原子性的非核心业务逻辑操作。应用场景较少）。
	 Propagation_Mandatory：该级别的事务要求上下文中必须要存在事务，否则就会抛出异常（配置该方式的传播级别是有效的控制上下文调用代码遗漏添加事务控制的保证手段。比如一段代码不能单独被调用执行，但是一旦被调用，就必须有事务包含的情况）。
	 Propagation_Requires_New：每次都会新建一个事务，并且同时将上下文中的事务挂起，执行当前新建事务完成以后，上下文事务恢复再执行。
	 Propagation_Not_Supported：以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
	 Propagation_Never：以非事务方式执行，如果当前存在事务，则抛出异常。
	 Propagation_Nested：如果上下文中存在事务，则嵌套事务执行，如果不存在事务，则新建事务。*/
	public static final String PREFIX_PROPAGATION = "PROPAGATION_";

	/** 定义事务传播级别的前缀：
	 Isolation_Read_Uncommited读未提交数据
	 Isolation_Read Commited读以提交数据
	 Isolation_Repeatable Read可重复读
	 Isolation_Serializable 串行化
	 Isolation_Default使用底层数据库的默认隔离解蔽*/
	public static final String PREFIX_ISOLATION = "ISOLATION_";

	/** 描述事务超时时间的前缀 */
	public static final String PREFIX_TIMEOUT = "timeout_";

	/** 用于标记事务只读 */
	public static final String READ_ONLY_MARKER = "readOnly";


	//** Constants instance for TransactionDefinition */
	static final Constants constants = new Constants(TransactionDefinition.class);
	private int propagationBehavior = PROPAGATION_REQUIRED;
	private int isolationLevel = ISOLATION_DEFAULT;
	private int timeout = TIMEOUT_DEFAULT;
	private boolean readOnly = false;
	private String name;

	public DefaultTransactionDefinition() {}
	public DefaultTransactionDefinition(TransactionDefinition other) {
		this.propagationBehavior = other.getPropagationBehavior();
		this.isolationLevel = other.getIsolationLevel();
		this.timeout = other.getTimeout();
		this.readOnly = other.isReadOnly();
		this.name = other.getName();
	}
	public DefaultTransactionDefinition(int propagationBehavior) {
		this.propagationBehavior = propagationBehavior;
	}


	// 设置事务传播级别的名称，如：PROPAGATION_REQUIRED
	public final void setPropagationBehaviorName(String constantName) throws IllegalArgumentException {
		if (constantName == null || !constantName.startsWith(PREFIX_PROPAGATION)) {
			throw new IllegalArgumentException("Only propagation constants allowed");
		}
		setPropagationBehavior(constants.asNumber(constantName).intValue());
	}
	public final void setPropagationBehavior(int propagationBehavior) {
		if (!constants.getValues(PREFIX_PROPAGATION).contains(propagationBehavior)) {
			throw new IllegalArgumentException("Only values of propagation constants allowed");
		}
		this.propagationBehavior = propagationBehavior;
	}
	public final int getPropagationBehavior() {
		return this.propagationBehavior;
	}


	// 设置事务隔离级别的名称，如：ISOLATION_DEFAULT
	public final void setIsolationLevelName(String constantName) throws IllegalArgumentException {
		if (constantName == null || !constantName.startsWith(PREFIX_ISOLATION)) {
			throw new IllegalArgumentException("Only isolation constants allowed");
		}
		setIsolationLevel(constants.asNumber(constantName).intValue());
	}
	public final void setIsolationLevel(int isolationLevel) {
		if (!constants.getValues(PREFIX_ISOLATION).contains(isolationLevel)) {
			throw new IllegalArgumentException("Only values of isolation constants allowed");
		}
		this.isolationLevel = isolationLevel;
	}
	public final int getIsolationLevel() {
		return this.isolationLevel;
	}

	// 设置事务超时时间
	public final void setTimeout(int timeout) {
		if (timeout < TIMEOUT_DEFAULT) {
			throw new IllegalArgumentException("Timeout must be a positive integer or TIMEOUT_DEFAULT");
		}
		this.timeout = timeout;
	}
	public final int getTimeout() {
		return this.timeout;
	}

	// 设置该事务操作是否为只读
	public final void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public final boolean isReadOnly() {
		return this.readOnly;
	}

	// 设置事务名称
	public final void setName(String name) {
		this.name = name;
	}
	public final String getName() {
		return this.name;
	}





	@Override
	public String toString() {
		return getDefinitionDescription().toString();
	}
	// 返回 DefaultTransactionDefinition 的描述信息
	protected final StringBuilder getDefinitionDescription() {
		StringBuilder result = new StringBuilder();
		result.append(constants.toCode(this.propagationBehavior, PREFIX_PROPAGATION));
		result.append(',');
		result.append(constants.toCode(this.isolationLevel, PREFIX_ISOLATION));
		if (this.timeout != TIMEOUT_DEFAULT) {
			result.append(',');
			result.append(PREFIX_TIMEOUT).append(this.timeout);
		}
		if (this.readOnly) {
			result.append(',');
			result.append(READ_ONLY_MARKER);
		}
		return result;
	}
	@Override
	public boolean equals(Object other) {
		return (other instanceof TransactionDefinition && toString().equals(other.toString()));
	}
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
