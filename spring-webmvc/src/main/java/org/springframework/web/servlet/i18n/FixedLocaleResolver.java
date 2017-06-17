
package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FixedLocaleResolver extends AbstractLocaleResolver {

	public FixedLocaleResolver() {
	}
	public FixedLocaleResolver(Locale locale) {
		setDefaultLocale(locale);
	}

	public Locale resolveLocale(HttpServletRequest request) {
		Locale locale = getDefaultLocale();
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return locale;
	}

	//抛异常，不支持更改Locale
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException("Cannot change fixed locale - use a different locale resolution strategy");
	}

}
