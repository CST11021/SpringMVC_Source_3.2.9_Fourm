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

package org.springframework.jdbc.support;

import java.sql.SQLException;

import org.springframework.dao.DataAccessException;

/**
 * Strategy interface for translating between {@link SQLException SQLExceptions}
 * and Spring's data access strategy-agnostic {@link DataAccessException}
 * hierarchy.
 *
 * <p>Implementations can be generic (for example, using
 * {@link java.sql.SQLException#getSQLState() SQLState} codes for JDBC) or wholly
 * proprietary (for example, using Oracle error codes) for greater precision.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.dao.DataAccessException
 */
// SQL异常转换器接口
/*
 	传统的JDBC API在发生几乎所有的数据操作问题都抛出相同的SQLException，它将异常的细节性信息封装在异常属性中，所以如果希
 望了解异常的具体原因，用户必须分析异常对象的信息。SQLException 拥有两个代表异常具体原因的属性：错误码和SQL状态码，前者是
 数据库相关的，可通过getErrorCode()返回，其值的类型是int；而后者是一个标准的错误代码，可通过getSQLState()返回，是一个String
 类型的值，由5个字符组成。Spring中通过实现SQLExceptionTranslator 接口实现异常的转换，它根据错误码和SQL状态码信息将SQLException
 翻译成Spring DAO的异常体系。
 	该接口的两个实现类 SQLErrorCodeSQLExceptionTranslator 和 SQLStateSQLExceptionTranslator 分别负责处理SQLException中的错误
 代码和SQL状态码的翻译工作。
 	此外，SQLExceptionSubclassTranslator是Spring2.5新增加的SQLExceptionTranslator实现类，主要用于将随JDK6发布的JDBC4版本
 中新定义的异常体系转化到Spring的数据访问异常体系。对于之前的版本，该实现类自然是用不上了。



*/
public interface SQLExceptionTranslator {

	/**
	 * Translate the given {@link SQLException} into a generic {@link DataAccessException}.
	 * <p>The returned DataAccessException is supposed to contain the original
	 * {@code SQLException} as root cause. However, client code may not generally
	 * rely on this due to DataAccessExceptions possibly being caused by other resource
	 * APIs as well. That said, a {@code getRootCause() instanceof SQLException}
	 * check (and subsequent cast) is considered reliable when expecting JDBC-based
	 * access to have happened.
	 * @param task readable text describing the task being attempted
	 * @param sql SQL query or update that caused the problem (may be {@code null})
	 * @param ex the offending {@code SQLException}
	 * @return the DataAccessException, wrapping the {@code SQLException}
	 * @see org.springframework.dao.DataAccessException#getRootCause()
	 */
	DataAccessException translate(String task, String sql, SQLException ex);

}
