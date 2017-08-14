
package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;

// 根据Session中特定的属性值确定本地化类型
/*
	SessionLocaleResolver查找Session中属性名为SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME的属性，并将其转换为Locale
	对象，一次作为客户端的本地化类型。如果应用程序使用Session维护客户的信息，SessionLocaleResolver是最合适不过的。用于仅
	需要将SessionLocaleResolver的配置添加到Spring MVC上下文的配置文件中即可：
	<bean id="localeResolver" class="org.springframework.web.servlet.i18n.SessionLocaleResolver"/>

	SessionLocaleResolver和CookieLocaleResolver的区别是，前者一般要求用户登录后生产相应的用户会话才有效，而后者只要浏览器有Cookie存在即可生效。
 */
public class SessionLocaleResolver extends AbstractLocaleResolver {

	public static final String LOCALE_SESSION_ATTRIBUTE_NAME = SessionLocaleResolver.class.getName() + ".LOCALE";


	//从request获取Locale，如果为空使用默认的Locale
	public Locale resolveLocale(HttpServletRequest request) {
		Locale locale = (Locale) WebUtils.getSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME);
		if (locale == null) {
			locale = determineDefaultLocale(request);
		}
		return locale;
	}

	//获取默认的Locale
	protected Locale determineDefaultLocale(HttpServletRequest request) {
		Locale defaultLocale = getDefaultLocale();
		if (defaultLocale == null) {
			defaultLocale = request.getLocale();
		}
		return defaultLocale;
	}

	//为request设置Locale
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		WebUtils.setSessionAttribute(request, LOCALE_SESSION_ATTRIBUTE_NAME, locale);
	}

}
