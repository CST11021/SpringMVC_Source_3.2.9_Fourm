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

package org.springframework.core;

/**
 * Interface that can be implemented by objects that should be
 * orderable, for example in a Collection.
 *
 * <p>The actual order can be interpreted as prioritization, with
 * the first object (with the lowest order value) having the highest
 * priority.
 *
 * <p>Note that there is a 'priority' marker for this interface:
 * {@link PriorityOrdered}. Order values expressed by PriorityOrdered
 * objects always apply before order values of 'plain' Ordered values.
 *
 * @author Juergen Hoeller
 * @since 07.04.2003
 * @see OrderComparator
 * @see org.springframework.core.annotation.Order
 */
public interface Ordered {

	// 用于表示最高优先级的常量
	int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;
	// 用于表示最低优先级的常量
	int LOWEST_PRECEDENCE = Integer.MAX_VALUE;


	// 返回优先级别（数值越小，优先级越高）
	int getOrder();

}
