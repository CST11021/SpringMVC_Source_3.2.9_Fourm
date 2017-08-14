/*
 * Copyright 2002-2013 the original author or authors.
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

package org.springframework.web.servlet;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
    一般情况下，Web应用根据客户端浏览器的设置判断客户端的本地化类型，用于可以通过IE菜单：工具->Internet选项->语言...在
    打开的“语言首选项”对话框中选择本地化类型。浏览器中设置的本地化类型会包含在HTML请求报文头中发送给web服务器，确切地
    说是通过报文头的Accept-Language参数将“语言首选项”对话框中选择的语言发送到服务器，成为服务器判断客户端本地化类型的依据。

    如果Web应用基于这种方式提供本地化页面，用户只得通过改变浏览器的设置进行本地化类型的切换。读者是否见过一些网站在页面的
    右上角提供了语言切换的图标：如英文、中文、繁体等字样。这种语言切换功能不要求用户更改浏览器的设置，它们通过Cookie、Session
    或请求参数即可切换本地化类型。

    在默认情况下，SpringMVC根据Accept-Language参数判断客户端的本地化类型，此外它还提供了多种指定客户端本地化类型的方式：
    通过Cookie、Session指定。事实上当收到请求时，SpringMVC在上下文中寻找一个本地化解析器（LocaleResolver），找到后使用它
    获取请求所对应的本地化类型信息。

    除此之外，Spring MVC还允许装配一个动态更改本地化类型的拦截器，这样通过指定一个请求参数就可以控制单个请求的本地化类型。
    本地化解析器和拦截器都定义在org.springframework.web.servlet.i18n包中，用户可以在DispatcherServlet上下文中配置它们。

    Spring提供了以下4个本地化解析器：
    AcceptHeaderLocaleResolver：根据HTTP报文头的Accept-Language参数确定本地化类型，如果没有显示定义本地化解析器，SpringMVC参数采用AcceptHeaderLocaleResolver
    CookieLocaleResolver：根据指定Cookie值确定本地化类型
    SessionLocaleResolver：根据Session中特定的属性值确定本地化类型
    LocaleChangeInterceptor：从请求参数中获取本次请求对应的本地化类型
 */
public interface LocaleResolver {

    //用于获取这个请求的区域代码，返回一个Locale对象
	Locale resolveLocale(HttpServletRequest request);

    //设置Locale
	void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale);

}
