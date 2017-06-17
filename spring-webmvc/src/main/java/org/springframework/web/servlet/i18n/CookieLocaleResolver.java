
package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

/**
 * 检验用户浏览器中的Cookie，用CookieLocaleResolver来解析区域。如果Cookie不存在，它会根据accept-language HTTP头部确定默认区域。
 <bean id="localeResolver" class="org.springframework.web.servlet.i18n.CookieLocaleResolver">
 <property name="cookieName" value="language"/>  
  <property name="cookieMaxAge" value="3600"/>  
 <property name="defaultLocale" value="en"/>  
 </bean>
 这个区域解析器所采用的Cookie可以通过cookieName和cookieMaxAge属性进行定制。cookieMaxAge属性表示这个Cookie应该持续多少秒，-1表示这个Cookie在浏览器关闭之后就失效。 
 如果用户浏览器中不存在该Cookie，你也可以为这个解析器设置defaultLocale属性。通过修改保存该区域的Cookie，这个区域解析器能够改变用户的区域。
 */
public class CookieLocaleResolver extends CookieGenerator implements LocaleResolver {

	public static final String LOCALE_REQUEST_ATTRIBUTE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";
	public static final String DEFAULT_COOKIE_NAME = CookieLocaleResolver.class.getName() + ".LOCALE";

	private Locale defaultLocale;


	public CookieLocaleResolver() {
		setCookieName(DEFAULT_COOKIE_NAME);
	}

	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

	protected Locale getDefaultLocale() {
		return this.defaultLocale;
	}

	public Locale resolveLocale(HttpServletRequest request) {
		// Check request for pre-parsed or preset locale.
		Locale locale = (Locale) request.getAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME);
		if (locale != null) {
			return locale;
		}

		// Retrieve and parse cookie value.
		Cookie cookie = WebUtils.getCookie(request, getCookieName());
		if (cookie != null) {
			locale = StringUtils.parseLocaleString(cookie.getValue());
			if (logger.isDebugEnabled()) {
				logger.debug("Parsed cookie value [" + cookie.getValue() + "] into locale '" + locale + "'");
			}
			if (locale != null) {
				request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
				return locale;
			}
		}

		return determineDefaultLocale(request);
	}

	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		if (locale != null) {
			// Set request attribute and add cookie.
			request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, locale);
			addCookie(response, locale.toString());
		}
		else {
			// Set request attribute to fallback locale and remove cookie.
			request.setAttribute(LOCALE_REQUEST_ATTRIBUTE_NAME, determineDefaultLocale(request));
			removeCookie(response);
		}
	}

	/**
	 * Determine the default locale for the given request,
	 * Called if no locale cookie has been found.
	 * <p>The default implementation returns the specified default locale,
	 * if any, else falls back to the request's accept-header locale.
	 * @param request the request to resolve the locale for
	 * @return the default locale (never {@code null})
	 * @see #setDefaultLocale
	 * @see javax.servlet.http.HttpServletRequest#getLocale()
	 */
	protected Locale determineDefaultLocale(HttpServletRequest request) {
		Locale defaultLocale = getDefaultLocale();
		if (defaultLocale == null) {
			defaultLocale = request.getLocale();
		}
		return defaultLocale;
	}

}
