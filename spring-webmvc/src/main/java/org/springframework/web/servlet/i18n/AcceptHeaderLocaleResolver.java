
package org.springframework.web.servlet.i18n;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;

// SpringMVC默认的Locale解析器
public class AcceptHeaderLocaleResolver implements LocaleResolver {

	public Locale resolveLocale(HttpServletRequest request) {
		return request.getLocale();
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException("Cannot change HTTP accept header - use a different locale resolution strategy");
	}

}
