
package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;


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
