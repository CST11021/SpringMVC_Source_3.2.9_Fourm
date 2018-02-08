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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;

/**
 * Simple {@link TransactionAttributeSource} implementation that
 * allows attributes to be matched by registered name.
 *
 * 该类封装了如下的配置信息：
	<property name="transactionAttributes">
		<props>
			<prop key="saveByConfg1">PROPAGATION_REQUIRED</prop>
		</props>
	</property>
 *
 * @author Juergen Hoeller
 * @since 21.08.2003
 * @see #isMatch
 * @see MethodMapTransactionAttributeSource
 */
@SuppressWarnings("serial")
public class NameMatchTransactionAttributeSource implements TransactionAttributeSource, Serializable {

	protected static final Log logger = LogFactory.getLog(NameMatchTransactionAttributeSource.class);

	/**
	 KEY：表示方法名；value表示TransactionAttributes对象，Spring配置如下：
	 <bean id="userServiceTarget" class="org.springframework.transaction.interceptor.TransactionProxyFactoryBean"
		 p:transactionManager-ref="transactionManager"
		 p:target-ref="userService">
		 <property name="transactionAttributes">
			 <props>
			 	<prop key="saveByConfg1">PROPAGATION_REQUIRED</prop>
			 </props>
		 </property>
	 </bean>

	 这里会将 PROPAGATION_REQUIRED 转为对应的 TransactionAttribute 对象
	 */
	private Map<String, TransactionAttribute> nameMap = new HashMap<String, TransactionAttribute>();


	public void setNameMap(Map<String, TransactionAttribute> nameMap) {
		for (Map.Entry<String, TransactionAttribute> entry : nameMap.entrySet()) {
			addTransactionalMethod(entry.getKey(), entry.getValue());
		}
	}
	public void setProperties(Properties transactionAttributes) {
		// 这里通过 TransactionAttributeEditor 属性编辑器将 <prop key="saveByConfg1">PROPAGATION_REQUIRED</prop> 配置中的
		// PROPAGATION_REQUIRED 转为一个 TransactionAttribute 对象
		TransactionAttributeEditor tae = new TransactionAttributeEditor();
		Enumeration propNames = transactionAttributes.propertyNames();
		while (propNames.hasMoreElements()) {
			String methodName = (String) propNames.nextElement();
			String value = transactionAttributes.getProperty(methodName);
			tae.setAsText(value);
			TransactionAttribute attr = (TransactionAttribute) tae.getValue();
			addTransactionalMethod(methodName, attr);
		}
	}
	public void addTransactionalMethod(String methodName, TransactionAttribute attr) {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding transactional method [" + methodName + "] with attribute [" + attr + "]");
		}
		this.nameMap.put(methodName, attr);
	}
	public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
		// look for direct name match
		String methodName = method.getName();
		TransactionAttribute attr = this.nameMap.get(methodName);

		if (attr == null) {
			// Look for most specific name match.
			String bestNameMatch = null;
			for (String mappedName : this.nameMap.keySet()) {
				if (isMatch(methodName, mappedName) &&
						(bestNameMatch == null || bestNameMatch.length() <= mappedName.length())) {
					attr = this.nameMap.get(mappedName);
					bestNameMatch = mappedName;
				}
			}
		}

		return attr;
	}
	protected boolean isMatch(String methodName, String mappedName) {
		return PatternMatchUtils.simpleMatch(mappedName, methodName);
	}


	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof NameMatchTransactionAttributeSource)) {
			return false;
		}
		NameMatchTransactionAttributeSource otherTas = (NameMatchTransactionAttributeSource) other;
		return ObjectUtils.nullSafeEquals(this.nameMap, otherTas.nameMap);
	}
	@Override
	public int hashCode() {
		return NameMatchTransactionAttributeSource.class.hashCode();
	}
	@Override
	public String toString() {
		return getClass().getName() + ": " + this.nameMap;
	}

}
