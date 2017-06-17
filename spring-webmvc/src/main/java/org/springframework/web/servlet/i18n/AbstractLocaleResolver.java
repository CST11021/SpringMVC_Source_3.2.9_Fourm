
package org.springframework.web.servlet.i18n;

import java.util.Locale;

import org.springframework.web.servlet.LocaleResolver;

//提供一个默认的Locale
public abstract class AbstractLocaleResolver implements LocaleResolver {

	private Locale defaultLocale;

	protected Locale getDefaultLocale() {
		return this.defaultLocale;
	}
	public void setDefaultLocale(Locale defaultLocale) {
		this.defaultLocale = defaultLocale;
	}

}
