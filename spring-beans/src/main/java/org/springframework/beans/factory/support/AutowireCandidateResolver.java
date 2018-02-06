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

package org.springframework.beans.factory.support;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;

/**
 * Strategy interface for determining whether a specific bean definition qualifies as an autowire candidate for a specific dependency.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 */
// AutowireCandidateResolver是一个策略接口，由它来决定指定的Bean是否可以作为一个被自动绑定的候选项Bean
// QualifierAnnotationAutowireCandidateResolver实现了AutowireCandidateResolver，对要自动绑定的字段、参数和Bean
// 根据@qualifier注解进行匹配。同时也支持通过@value注解来绑定表达式的值。
public interface AutowireCandidateResolver {

	// 判断bdHodler对应的Bean是否可自动注入到其他的Bean（比如：BeanA通过@Autowired修饰变量b，则该方法判断b对应的Bean是否
	// 可以被自动注入a）
	boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor);

	// 为给定的依赖项提供一个默认值
	Object getSuggestedValue(DependencyDescriptor descriptor);

}
