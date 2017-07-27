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

package org.springframework.context.config;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * Parser for the &lt;context:property-override/&gt; element.
 *
 * @author Juergen Hoeller
 * @author Dave Syer
 * @since 2.5.2
 */

/*
该类用于解析<property-override>标签

property-override和property-placeholder的不同点：
	（1）功能不一样，property-override标签的作用是为xml配置文件中的bean的属性指定最终结果；而property-placeholder标签的作用是把xml配置文件中bean 的<property>标签的value值替换成正真的值，而且<property>标签的value值必须符合特定的表达式格式，默认为“${key}”，其中key为属性文件中的key。
	（2）属性文件内容要求不一样，property-override标签加载的properties文件中的key的格式有严格的要求，必须为“bean名称.bean属性”。如果属性ignore-unresolvable的值为false，那么属性文件中的bean名称必须在当前容器中能找到对应的bean。

property-override和property-placeholder的共同点：
	（1）两者都是以properties文件作为数据来源。
	（2）两者的解析器BeanDefinitionParser类都继承自AbstractPropertyLoadingBeanDefinitionParser类。因此它们共有AbstractPropertyLoadingBeanDefinitionParser及其父类中所处理的标签属性，并且这些属性在两个标签中具有相同的作用。这其实都归于它们所代表的工厂后处理器都继承了PropertiesLoaderSupport类

 */
class PropertyOverrideBeanDefinitionParser extends AbstractPropertyLoadingBeanDefinitionParser {

	@Override
	protected Class getBeanClass(Element element) {
		return PropertyOverrideConfigurer.class;
	}

	@Override
	protected void doParse(Element element, BeanDefinitionBuilder builder) {

		super.doParse(element, builder);
		builder.addPropertyValue("ignoreInvalidKeys",
				Boolean.valueOf(element.getAttribute("ignore-unresolvable")));

	}

}
