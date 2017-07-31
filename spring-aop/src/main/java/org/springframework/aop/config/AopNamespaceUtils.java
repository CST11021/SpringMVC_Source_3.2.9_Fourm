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

package org.springframework.aop.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.ParserContext;

/**
 * Utility class for handling registration of auto-proxy creators used internally
 * by the '{@code aop}' namespace tags.
 *
 * <p>Only a single auto-proxy creator can be registered and multiple tags may wish
 * to register different concrete implementations. As such this class delegates to
 * {@link AopConfigUtils} which wraps a simple escalation protocol. Therefore classes
 * may request a particular auto-proxy creator and know that class, <i>or a subclass
 * thereof</i>, will eventually be resident in the application context.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.0
 * @see AopConfigUtils
 */
/*
	proxy-target-class:Spring AOP部分使用JDK动态代理或CGLIB来为目标对象创建代理。（建议尽量使用JDK的动态代理），如果被代理的目标对象实现了至少一个接口，则会使用JDK动态代理。所有该目标类型实现的接口都将被代理。若该目标对象没有实现任何接口，则创建CGLIB代理。如果你希望强制使用CGLIB代理，（例如希望代理目标对象的所有方法，而不只是实现自接口的方法）那也可以。但是需要考虑以下两个问题。
1、无法通知（advise）Final方法，因为它们不能被覆写。
2、你需要将CGLIB二进制发行包放在classpath下面。

	与之相较，JDK本身就提供了动态代理，强制使用CGLIB代理需要将<aop:config>的proxy-target-class属性设为true:
<aop:config proxy-target-class="true">...</aop:config>
	当需要使用CGLIB代理和@AspectJ自动代理支持，可以按照以下方法设置<aop:aspectj-autoproxy>的proxy-target-class属性：
<aop:aspectj-autoproxy proxy-target-class="true"/>
而实际使用的过程中才会发现细节问题的差别
1、JDK动态代理：其代理对象必须是某个接口的实现，它是通过在运行期间创建一个接口的实现类来完成对目标对象的代理。
2、CGLIB代理：实现原理类似于JDK动态代理，只是它在运行期间生产的代理对象是针对目标类扩展的子类。CGLIB是高效的代码生成包，底层是依靠ASM（开源的java字节码编辑类库）操作字节码实现的，性能比JDK强。
3、expose-proxy：有时候目标对象内部的自动调用将无法实施切面中的增强，如下示例：
	public interface AService{
		public void a();
		public void b();
	}
	@Servcie()
	public class AServiceImpl1 implements AService{
		@Transactional(propagation = Propagation.REQUIRED)
		public void a(){
			this.b();
		}
		@Transactional(propagation = Propagation.REQUIRES_NEW)
		public void b(){

		}
	}

	此处的this指向目标对象，因此调用this.b()将不会执行b事务切面，即不会执行事务增强，因此b方法的事务定义“@Transactional(propagation = Propagation.REQUIRES_NEW)”将不会实施，为了解决这个问题，我们可以这样做：
	<aop:aspectj-autoproxy expose-proxy="true"/>
然后将以上代码中的“this.b();”修改为“((AService)AopContext.currentProxy()).b();”即可。
通过以上的修改便可以完成对a和b方法的同时增强。
 */
public abstract class AopNamespaceUtils {

	// The {@code proxy-target-class} attribute as found on AOP-related XML tags.
	public static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";
	// The {@code expose-proxy} attribute as found on AOP-related XML tags.
	private static final String EXPOSE_PROXY_ATTRIBUTE = "expose-proxy";


	public static void registerAutoProxyCreatorIfNecessary(ParserContext parserContext, Element sourceElement) {
		BeanDefinition beanDefinition = AopConfigUtils.registerAutoProxyCreatorIfNecessary(parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	public static void registerAspectJAutoProxyCreatorIfNecessary(ParserContext parserContext, Element sourceElement) {
		BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAutoProxyCreatorIfNecessary(parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	// 注册 AnnotationAwareAspectJAutoProxyCreator
	public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(ParserContext parserContext, Element sourceElement) {
		// 注册一个beanName为“org.Springframework.aop.config.internalAutoProxyCreator”的BeanDefinition，
		// 该Bean对应的类型是 AnnotationAwareAspectJAutoProxyCreator，
		// 对于AOP的实现，基本上都是靠 AnnotationAwareAspectJAutoProxyCreator 去完成的，它可以根据@Point注解定义的切点来自动代理相匹配的bean。
		// 但是为了配置简便，Spring使用了自定义配置来帮助我们自动注册 AnnotationAwareAspectJAutoProxyCreator ，其注册过程就是在这里实现的。
		BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext.getRegistry(), parserContext.extractSource(sourceElement));
		// 处理 proxy-target-class 和 expose-proxy 属性
		useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
		// 注册组件并通知，便于监听器做进一步处理
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	/**
	 * @deprecated since Spring 2.5, in favor of
	 * {@link #registerAutoProxyCreatorIfNecessary(ParserContext, Element)} and
	 * {@link AopConfigUtils#registerAutoProxyCreatorIfNecessary(BeanDefinitionRegistry, Object)}
	 */
	@Deprecated
	public static void registerAutoProxyCreatorIfNecessary(ParserContext parserContext, Object source) {
		BeanDefinition beanDefinition = AopConfigUtils.registerAutoProxyCreatorIfNecessary(
				parserContext.getRegistry(), source);
		registerComponentIfNecessary(beanDefinition, parserContext);
	}

	/**
	 * @deprecated since Spring 2.5, in favor of
	 * {@link AopConfigUtils#forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry)}
	 */
	@Deprecated
	public static void forceAutoProxyCreatorToUseClassProxying(BeanDefinitionRegistry registry) {
		AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
	}

	// 处理 proxy-target-class 和 expose-proxy 属性
	private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry, Element sourceElement) {
		if (sourceElement != null) {
			// 处理 proxy-target-class 属性：proxy-target-class属性值决定是基于接口的还是基于类的代理被创建。
			// 如果proxy-target-class 属性值被设置为true，那么基于类的代理将起作用（这时需要cglib库）；
			// 如果proxy-target-class 属性值被设置为false，或者这个属性被省略，那么标准的JDK 基于接口的代理将起作用。
			// 另外，即使你未声明 proxy-target-class="true" ，但运行类没有继承接口，spring也会自动使用CGLIB代理。高版本spring自动根据运行类选择 JDK 或 CGLIB 代理
			boolean proxyTargetClass = Boolean.valueOf(sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
			if (proxyTargetClass) {
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
			//
			/*
			处理 expose-proxy 属性：当前代理是否为可暴露状态,值是"ture",则为可访问。
			expose-proxy：有时候目标对象内部的自动调用将无法实施切面中的增强，如下示例：
				public interface AService{
					public void a();
					public void b();
				}
				@Servcie()
				public class AServiceImpl1 implements AService{
					@Transactional(propagation = Propagation.REQUIRED)
					public void a(){
						this.b();
					}
					@Transactional(propagation = Propagation.REQUIRES_NEW)
					public void b(){

					}
				}

				此处的this指向目标对象，因此调用this.b()将不会执行b事务切面，即不会执行事务增强，因此b方法的事务定义“@Transactional(propagation = Propagation.REQUIRES_NEW)”将不会实施，
			为了解决这个问题，我们可以这样做：<aop:aspectj-autoproxy expose-proxy="true"/>,然后将以上代码中的“this.b();”修改为“((AService)AopContext.currentProxy()).b();”即可。
			通过以上的修改便可以完成对a和b方法的同时增强。
			 */
			boolean exposeProxy = Boolean.valueOf(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
			if (exposeProxy) {
				AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
			}
		}
	}

	// Spring 中每次注册一个Bean都会通知相应的监听器
	private static void registerComponentIfNecessary(BeanDefinition beanDefinition, ParserContext parserContext) {
		if (beanDefinition != null) {
			BeanComponentDefinition componentDefinition = new BeanComponentDefinition(beanDefinition, AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
			parserContext.registerComponent(componentDefinition);
		}
	}

}
