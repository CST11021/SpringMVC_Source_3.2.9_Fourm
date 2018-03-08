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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for {@link org.springframework.jdbc.core.JdbcTemplate} and
 * other JDBC-accessing DAO helpers, defining common properties such as
 * DataSource and exception translator.
 *
 * <p>Not intended to be used directly.
 * See {@link org.springframework.jdbc.core.JdbcTemplate}.
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public abstract class JdbcAccessor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	// 数据源对象
	private DataSource dataSource;
	// 异常转换器
	private SQLExceptionTranslator exceptionTranslator;
	// 是否延迟初始化异常转换器
	private boolean lazyInit = true;

	// 该方法会在 JdbcTemplate 设置完数据源属性后被调用，数据源不允许为空
	public void afterPropertiesSet() {
		if (getDataSource() == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}


	// getter and setter ...

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public DataSource getDataSource() {
		return this.dataSource;
	}
	// 根据 dbName(数据库名称) 设置异常转换器
	public void setDatabaseProductName(String dbName) {
		this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dbName);
	}
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}
	// 获取异常SQL异常转换器，如果数据源不为空则使用“SQL错误代码转换器”，否则使用“SQL状态码转换器”
	public synchronized SQLExceptionTranslator getExceptionTranslator() {
		if (this.exceptionTranslator == null) {
			DataSource dataSource = getDataSource();
			if (dataSource != null) {
				this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			}
			else {
				this.exceptionTranslator = new SQLStateSQLExceptionTranslator();
			}
		}
		return this.exceptionTranslator;
	}
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}
	public boolean isLazyInit() {
		return this.lazyInit;
	}


}
