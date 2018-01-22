/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.aop.framework;

/**
 * Marker interface that indicates a bean that is part of Spring's
 * AOP infrastructure. In particular, this implies that any such bean
 * is not subject to auto-proxying, even if a pointcut would match.
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator
 * @see org.springframework.aop.scope.ScopedProxyFactoryBean
 */
// 标记接口，表示bean是Spring AOP基础设施的一部分。特别是，这意味着任何此类bean都不受自动代理的作用，即使切入点会匹配。
public interface AopInfrastructureBean {

}
