/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.cache.config;

import static org.springframework.context.annotation.AnnotationConfigUtils.*;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.cache.annotation.AnnotationCacheOperationSource;
import org.springframework.cache.interceptor.BeanFactoryCacheOperationSourceAdvisor;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.w3c.dom.Element;

/**
 * {@link org.springframework.beans.factory.xml.BeanDefinitionParser}
 * implementation that allows users to easily configure all the infrastructure beans required to enable annotation-driven cache demarcation.
 *
 * <p>By default, all proxies are created as JDK proxies.
 * This may cause some problems if you are injecting objects as concrete classes rather than interfaces.
 * To overcome this restriction you can set the
 * '{@code proxy-target-class}' attribute to '{@code true}', which will
 * result in class-based proxies being created.
 *
 * @author Costin Leau
 * @since 3.1
 */
// 用来解析<cache:annotation-driven>标签
class AnnotationDrivenCacheBeanDefinitionParser implements BeanDefinitionParser {

	// 解析<cache:annotation-driven>标签
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		// 根据annotation-driven标签中的model属性决定是通过代理的方式实现aop还是通过aspectj的方式进行切面拦截(默认采用
		// proxy代理方式)。对于代理方式，会一个注册缓存Advisor和一个CacheInterceptor类型的拦截器。这就实现了对业务方法
		// 的切面拦截
		String mode = element.getAttribute("mode");
		if ("aspectj".equals(mode)) {
			// mode="aspectj" 判断是否使用aspectj代理技术来实现缓存注解的拦截
			registerCacheAspect(element, parserContext);
		}
		else {
			// mode="proxy" 使用Spring自动代理方式来拦截被缓存注解修饰的方法，默认实现
			AopAutoProxyConfigurer.configureAutoProxyCreator(element, parserContext);
		}

		return null;
	}

	// 向指定的BeanDefinition添加一个cacheManager的属性
	private static void parseCacheManagerProperty(Element element, BeanDefinition def) {
		def.getPropertyValues().add("cacheManager",
				new RuntimeBeanReference(CacheNamespaceHandler.extractCacheManager(element)));
	}

	// 注册一个 internalCacheAspect 组件
	/**
	 * Registers a
	 * <pre>
	 * <bean id="cacheAspect" class="org.springframework.cache.aspectj.AnnotationCacheAspect" factory-method="aspectOf">
	 *   <property name="cacheManager" ref="cacheManager"/>
	 *   <property name="keyGenerator" ref="keyGenerator"/>
	 * </bean>
	 *
	 * </pre>
	 * @param element
	 * @param parserContext
	 */
	private void registerCacheAspect(Element element, ParserContext parserContext) {
		if (!parserContext.getRegistry().containsBeanDefinition(CACHE_ASPECT_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			def.setBeanClassName(CACHE_ASPECT_CLASS_NAME);
			def.setFactoryMethodName("aspectOf");
			// 解析 cache-manager 属性
			parseCacheManagerProperty(element, def);
			// 解析 key-generator 属性
			CacheNamespaceHandler.parseKeyGenerator(element, def);
			// 向IOC中注册一个名为“org.springframework.cache.config.internalCacheAspect”的组件
			parserContext.registerBeanComponent(new BeanComponentDefinition(def, CACHE_ASPECT_BEAN_NAME));
		}
	}

	// 该内部类用于创建CacheOperationSource、CacheInterceptor和CacheAdvisor这几个Spring内部BeanDefinition，然后封装为一个
	// CompositeComponentDefinition对象后注册到IOC
	private static class AopAutoProxyConfigurer {
		public static void configureAutoProxyCreator(Element element, ParserContext parserContext) {
			AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);

			// 判断BeanDefinition注册表是否注册了缓存增强Bean（cache_advisor_bean）
			if (!parserContext.getRegistry().containsBeanDefinition(CACHE_ADVISOR_BEAN_NAME)) {
				// 获取该标签的所在配置源，通常情况下返回空
				Object eleSource = parserContext.extractSource(element);

				// 1、创建一个 CacheOperationSource 的 BeanDefinition 并注册到IOC容器中
				// AnnotationCacheOperationSource中创建了一个SpringCacheAnnotationParser，它是用于解析缓存注解的解析器
				RootBeanDefinition sourceDef = new RootBeanDefinition(AnnotationCacheOperationSource.class);
				sourceDef.setSource(eleSource);
				// BeanDefinition.ROLE_INFRASTRUCTURE表示该bean是Spring内部使用的bean
				sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				// 将 sourceDef 注册到BeanDefinition注册表，并返回由Spring生成的beanName
				// 这里返回的sourceName是“org.springframework.cache.annotation.AnnotationCacheOperationSource#0”
				String sourceName = parserContext.getReaderContext().registerWithGeneratedName(sourceDef);

				// 2、创建一个 CacheInterceptor 的 BeanDefinition 并注册到IOC容器中，拦截器interceptorDef#cacheOperationSources
				// 指向上面的AnnotationCacheOperationSource
				RootBeanDefinition interceptorDef = new RootBeanDefinition(CacheInterceptor.class);
				interceptorDef.setSource(eleSource);
				interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				// 向指定的BeanDefinition添加一个cacheManager的属性
				parseCacheManagerProperty(element, interceptorDef);
				// 解析<cache:annotation-driven key-generator=""/>中配置的key-generator属性，如果不为空则封装为一个
				// RuntimeBeanReference对象，并添加到BeanDefinition#propertyValues中，方便后续的属性注入
				CacheNamespaceHandler.parseKeyGenerator(element, interceptorDef);
				interceptorDef.getPropertyValues().add("cacheOperationSources", new RuntimeBeanReference(sourceName));
				String interceptorName = parserContext.getReaderContext().registerWithGeneratedName(interceptorDef);

				// 3、创建一个 CacheAdvisor 的 BeanDefinition 并注册到IOC容器中，增强advisorDef#cacheOperationSource
				// 指向上面的AnnotationCacheOperationSource，advisorDef#adviceBeanName指向上面的拦截器
				RootBeanDefinition advisorDef = new RootBeanDefinition(BeanFactoryCacheOperationSourceAdvisor.class);
				advisorDef.setSource(eleSource);
				advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
				advisorDef.getPropertyValues().add("cacheOperationSource", new RuntimeBeanReference(sourceName));
				advisorDef.getPropertyValues().add("adviceBeanName", interceptorName);
				if (element.hasAttribute("order")) {
					advisorDef.getPropertyValues().add("order", element.getAttribute("order"));
				}
				parserContext.getRegistry().registerBeanDefinition(CACHE_ADVISOR_BEAN_NAME, advisorDef);

				// 4、最后一步：将CacheOperationSource、CacheInterceptor和CacheAdvisor封装为一个组件注册到IOC
				CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(),
						eleSource);
				compositeDef.addNestedComponent(new BeanComponentDefinition(sourceDef, sourceName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(interceptorDef, interceptorName));
				compositeDef.addNestedComponent(new BeanComponentDefinition(advisorDef, CACHE_ADVISOR_BEAN_NAME));
				parserContext.registerComponent(compositeDef);
			}
		}
	}
}
