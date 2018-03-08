/*
 * Copyright 2002-2008 the original author or authors.
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

/**
 * Generic interface to be implemented by resource holders.
 * Allows Spring's transaction infrastructure to introspect and reset the holder when necessary.
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 * @see ResourceHolderSupport
 * @see ResourceHolderSynchronization
 */
// 由资源持有者实现的通用接口（Spring将JDBC的Connection、Hibernate的Session等访问数据库的连接或会话对象统称为资源）
public interface ResourceHolder {

	// 重置这个holder的事务状态
	void reset();

	// 通知这个持有者，它已经从事务同步中脱离了
	void unbound();

	// 判断这个持有者是否被认为是“无效的”
	boolean isVoid();

}
