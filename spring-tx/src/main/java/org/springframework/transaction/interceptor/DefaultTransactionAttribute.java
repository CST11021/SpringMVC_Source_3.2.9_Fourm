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

package org.springframework.transaction.interceptor;

import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Transaction attribute that takes the EJB approach to rolling
 * back on runtime, but not checked, exceptions.
 *
 * @author Rod Johnson
 * @since 16.03.2003
 */
@SuppressWarnings("serial")
public class DefaultTransactionAttribute extends DefaultTransactionDefinition implements TransactionAttribute {

	private String qualifier;


	public DefaultTransactionAttribute() {
		super();
	}
	public DefaultTransactionAttribute(TransactionAttribute other) {
		super(other);
	}
	public DefaultTransactionAttribute(int propagationBehavior) {
		super(propagationBehavior);
	}

	
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	public String getQualifier() {
		return this.qualifier;
	}

	// 指定的异常发生时，事务是否回滚
	public boolean rollbackOn(Throwable ex) {
		return (ex instanceof RuntimeException || ex instanceof Error);
	}


	/**
	 * Return an identifying description for this transaction attribute.
	 * <p>Available to subclasses, for inclusion in their {@code toString()} result.
	 */
	protected final StringBuilder getAttributeDescription() {
		StringBuilder result = getDefinitionDescription();
		if (this.qualifier != null) {
			result.append("; '").append(this.qualifier).append("'");
		}
		return result;
	}

}
