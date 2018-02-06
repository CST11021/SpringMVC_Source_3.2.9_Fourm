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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * Interface used by the {@link DefaultBeanDefinitionDocumentReader} to handle custom, top-level (directly under {@code <beans/>}) tags.
 *
 * <p>Implementations are free to turn the metadata in the custom tag into as many
 * {@link BeanDefinition BeanDefinitions} as required.
 *
 * <p>The parser locates a {@link BeanDefinitionParser} from the associated
 * {@link NamespaceHandler} for the namespace in which the custom tag resides.
 *
 * @author Rob Harrop
 * @since 2.0
 * @see NamespaceHandler
 * @see AbstractBeanDefinitionParser
 */
// 该接口是用于解析位于<beans/>内的自定义级别的标签，一个自定义的命名空间下通常会有多个不同的配置标签，每个标签对应不同的
// 解析器，而这些解析都需要来实现 BeanDefinitionParser 接口，并调用parse()方法解析对应的标签。
// 解析自定义命名空间可以通过直接实现该接口的方式解析对应的配置，也可以通过继承 AbstractBeanDefinitionParser 或
// AbstractSingleBeanDefinitionParser 的方式来扩展解析器
public interface BeanDefinitionParser {

	// 解析自定义命名空间下对应的标签，并返回一个BeanDefinition，如果该标签不是一个Bean配置标签
	// 比如：<cache:annotation-driven/>等，则返回null
	BeanDefinition parse(Element element, ParserContext parserContext);

}
