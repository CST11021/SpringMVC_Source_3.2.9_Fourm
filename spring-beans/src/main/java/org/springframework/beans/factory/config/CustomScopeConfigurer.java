/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

// 除了直接编码调用 ConfigurableBeanFactory 的 registerScope 来注册 scope ，Spring还提供了一个专门用于
// 统一注册自定义scope的 BeanFactoryPostProcessor 实现，即 CustomScopeConfigurer 。
// 对于 ApplicationContext 来说，因为它可以自动识别并加载 BeanFactoryPostProcessor ，所以我们就可以直接在配置文件中，
// 通过这个 CustomScopeConfigurer 注册来 ThreadScope ，如：

// <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
//   <property name="scopes">
//		<map>
//			<entry key="thread" value="com.foo.ThreadScope"/>
//		</map>
//    </property>
// </bean>
//
// 在以上工作全部完成之后，我们就可以在自己的bean定义中使用这个新增加到容器的自定义scope “thread ”了，
// 如下代码演示了通常情况下“thread ”自定义scope的使用：
//<bean id="beanName" class="..." scope="thread">
//	<aop:scoped-proxy/>
//</bean>

public class CustomScopeConfigurer implements BeanFactoryPostProcessor, BeanClassLoaderAware, Ordered {

	private Map<String, Object> scopes;
	private int order = Ordered.LOWEST_PRECEDENCE;
	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	// 该方法是在spring容器解析完配置文件（注册了BeanDefinition）之后，在bean实例化之前被调用的
	@SuppressWarnings("unchecked")
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (this.scopes != null) {
			for (Map.Entry<String, Object> entry : this.scopes.entrySet()) {
				String scopeKey = entry.getKey();
				Object value = entry.getValue();
				if (value instanceof Scope) {
					beanFactory.registerScope(scopeKey, (Scope) value);
				}
				else if (value instanceof Class) {
					Class scopeClass = (Class) value;
					Assert.isAssignable(Scope.class, scopeClass);
					beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
				}
				else if (value instanceof String) {
					Class scopeClass = ClassUtils.resolveClassName((String) value, this.beanClassLoader);
					Assert.isAssignable(Scope.class, scopeClass);
					beanFactory.registerScope(scopeKey, (Scope) BeanUtils.instantiateClass(scopeClass));
				}
				else {
					throw new IllegalArgumentException("Mapped value [" + value + "] for scope key [" +
							scopeKey + "] is not an instance of required type [" + Scope.class.getName() +
							"] or a corresponding Class or String value indicating a Scope implementation");
				}
			}
		}
	}

	public void setScopes(Map<String, Object> scopes) {
		this.scopes = scopes;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public int getOrder() {
		return this.order;
	}
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

}
