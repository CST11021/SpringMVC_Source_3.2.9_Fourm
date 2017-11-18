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

package org.springframework.beans.factory.annotation;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.util.ObjectUtils;

/**
 * Convenience methods performing bean lookups related to annotations, for example
 * Spring's {@link Qualifier @Qualifier} annotation.
 *
 * @author Chris Beams
 * @since 3.1.2
 * @see BeanFactoryUtils
 */
public class BeanFactoryAnnotationUtils {

	// 从beanFactory获取beanName为qualifier，类型为beanType的bean
	public static <T> T qualifiedBeanOfType(BeanFactory beanFactory, Class<T> beanType, String qualifier) {
		if (beanFactory instanceof ConfigurableListableBeanFactory) {
			// Full qualifier matching supported.
			return qualifiedBeanOfType((ConfigurableListableBeanFactory) beanFactory, beanType, qualifier);
		}
		else if (beanFactory.containsBean(qualifier)) {
			// Fallback: target bean at least found by bean name.
			return beanFactory.getBean(qualifier, beanType);
		}
		else {
			throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
					" bean found for bean name '" + qualifier +
					"'! (Note: Qualifier matching not supported because given " +
					"BeanFactory does not implement ConfigurableListableBeanFactory.)");
		}
	}
	private static <T> T qualifiedBeanOfType(ConfigurableListableBeanFactory bf, Class<T> beanType, String qualifier) {
		// 获取beanType类型的所有bean
		Map<String, T> candidateBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(bf, beanType);
		T matchingBean = null;

		for (String beanName : candidateBeans.keySet()) {
			if (isQualifierMatch(qualifier, beanName, bf)) {
				if (matchingBean != null) {
					throw new NoSuchBeanDefinitionException(qualifier, "No unique " + beanType.getSimpleName() + " bean found for qualifier '" + qualifier + "'");
				}
				matchingBean = candidateBeans.get(beanName);
			}
		}
		if (matchingBean != null) {
			return matchingBean;
		}
		else {
			throw new NoSuchBeanDefinitionException(qualifier, "No matching " + beanType.getSimpleName() +
					" bean found for qualifier '" + qualifier + "' - neither qualifier " + "match nor bean name match!");
		}
	}
	private static boolean isQualifierMatch(String qualifier, String beanName, ConfigurableListableBeanFactory bf) {
		if (bf.containsBean(beanName)) {
			try {
				// 从工厂中获取这个Bean
				BeanDefinition bd = bf.getMergedBeanDefinition(beanName);

				if (bd instanceof AbstractBeanDefinition) {
					AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
					AutowireCandidateQualifier candidate = abd.getQualifier(Qualifier.class.getName());
					if ((candidate != null && qualifier.equals(candidate.getAttribute(AutowireCandidateQualifier.VALUE_KEY))) ||
							qualifier.equals(beanName) || ObjectUtils.containsElement(bf.getAliases(beanName), qualifier)) {
						return true;
					}
				}
				if (bd instanceof RootBeanDefinition) {
					Method factoryMethod = ((RootBeanDefinition) bd).getResolvedFactoryMethod();
					if (factoryMethod != null) {
						Qualifier targetAnnotation = factoryMethod.getAnnotation(Qualifier.class);
						if (targetAnnotation != null && qualifier.equals(targetAnnotation.value())) {
							return true;
						}
					}
				}

			}
			catch (NoSuchBeanDefinitionException ex) {
				// ignore - can't compare qualifiers for a manually registered singleton object
			}
		}
		return false;
	}

}
