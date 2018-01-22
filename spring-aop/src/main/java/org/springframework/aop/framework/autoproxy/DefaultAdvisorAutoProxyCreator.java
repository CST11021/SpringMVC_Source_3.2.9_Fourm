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

package org.springframework.aop.framework.autoproxy;

import org.springframework.beans.factory.BeanNameAware;

/**
 * BeanPostProcessor implementation that creates AOP proxies based on all candidate
 * Advisors in the current BeanFactory. This class is completely generic; it contains
 * no special code to handle any particular aspects, such as pooling aspects.
 *
 * <p>It's possible to filter out advisors - for example, to use multiple post processors
 * of this type in the same factory - by setting the {@code usePrefix} property
 * to true, in which case only advisors beginning with the DefaultAdvisorAutoProxyCreator's
 * bean name followed by a dot (like "aapc.") will be used. This default prefix can be
 * changed from the bean name by setting the {@code advisorBeanNamePrefix} property.
 * The separator (.) will also be used in this case.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 */

/**
 Spring提供的ProxyBeanFactory将切面织入到不同的目标类中。当然，为每一个目标类手工配置一个切面的比较烦琐的，
 Spring利用BeanPostProcessor可干涉Bean生命周期的机制，提供了一些自动创建代理，织入切面的自动代理创建器，
 其中DefaultAdvisorAutoProxyCreator是功能强大的自动代理创建器，它可以将容器中所有Advisor自动织入到目标Bean中。
 */
// DefaultAdvisorAutoProxyCreator:将对应匹配的advisor，自动添加到spring的bean。它控制的是advisor的匹配，所有的bean都会被自动代理
@SuppressWarnings("serial")
public class DefaultAdvisorAutoProxyCreator extends AbstractAdvisorAutoProxyCreator implements BeanNameAware {

	/** Separator between prefix and remainder of bean name */
	public final static String SEPARATOR = ".";

	private boolean usePrefix;
	private String advisorBeanNamePrefix;


	public void setUsePrefix(boolean usePrefix) {
		this.usePrefix = usePrefix;
	}
	public boolean isUsePrefix() {
		return this.usePrefix;
	}
	public void setAdvisorBeanNamePrefix(String advisorBeanNamePrefix) {
		this.advisorBeanNamePrefix = advisorBeanNamePrefix;
	}
	public String getAdvisorBeanNamePrefix() {
		return this.advisorBeanNamePrefix;
	}

	public void setBeanName(String name) {
		// If no infrastructure bean name prefix has been set, override it.
		if (this.advisorBeanNamePrefix == null) {
			this.advisorBeanNamePrefix = name + SEPARATOR;
		}
	}


	// 判断指定bean是否为符合要求的增强bean
	@Override
	protected boolean isEligibleAdvisorBean(String beanName) {
		return (!isUsePrefix() || beanName.startsWith(getAdvisorBeanNamePrefix()));
	}

}
