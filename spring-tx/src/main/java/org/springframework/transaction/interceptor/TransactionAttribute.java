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

import org.springframework.transaction.TransactionDefinition;

/**
 * This interface adds a {@code rollbackOn} specification to {@link TransactionDefinition}.
 * As custom {@code rollbackOn} is only possible with AOP, this class resides
 * in the AOP transaction package.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 16.03.2003
 * @see DefaultTransactionAttribute
 * @see RuleBasedTransactionAttribute
 */
// 它主要添加了一个获取qualifier的方法和一个决定事务是否需要回滚的rollbackOn方法，getQualifier返回的实际是
// TransactionManager的实例的BeanName，而rollbackOn会遍历前面说到的rollbackRule列表计算出winner，如果winner为null，会判
// 断异常是否为RuntimeException或者Error类型，存在winner或者异常为RuntimeException或者Error类型的时候返回true，即进行事
// 务回滚。TransactionAttribute的标准实现类是DefaultTransactionAttribute，它继承于DefaultTransactionDefinition，主要添加
// 了getQualifier和rollbackOn的实现，另外，添加了descriptor(String类型，实际上是方法描述符，通俗来说就是”
// className.methodName”，用于获取方法描述符的降级逻辑)获取和设值方法。
public interface TransactionAttribute extends TransactionDefinition {

	/**
	 * Return a qualifier value associated with this transaction attribute.
	 * <p>This may be used for choosing a corresponding transaction manager
	 * to process this specific transaction.
	 */
	String getQualifier();

	// 指定的异常发生时，事务是否回滚
	boolean rollbackOn(Throwable ex);

}
