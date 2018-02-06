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

package org.springframework.format.datetime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.format.Formatter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.util.StringUtils;

/**
 * A formatter for {@link java.util.Date} types.
 * Allows the configuration of an explicit date pattern and locale.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Phillip Webb
 * @since 3.0
 * @see SimpleDateFormat
 */
public class DateFormatter implements Formatter<Date> {

	private static final Map<ISO, String> ISO_PATTERNS;
	static {
		Map<ISO, String> formats = new HashMap<DateTimeFormat.ISO, String>(4);
		formats.put(ISO.DATE, "yyyy-MM-dd");
		formats.put(ISO.TIME, "HH:mm:ss.SSSZ");
		formats.put(ISO.DATE_TIME, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		ISO_PATTERNS = Collections.unmodifiableMap(formats);
	}
	private String pattern;
	private int style = DateFormat.DEFAULT;
	private String stylePattern;
	private ISO iso;
	private TimeZone timeZone;
	private boolean lenient = false;


	public DateFormatter() {
	}
	public DateFormatter(String pattern) {
		this.pattern = pattern;
	}


	public String print(Date date, Locale locale) {
		return getDateFormat(locale).format(date);
	}
	public Date parse(String text, Locale locale) throws ParseException {
		return getDateFormat(locale).parse(text);
	}


	protected DateFormat getDateFormat(Locale locale) {
		DateFormat dateFormat = createDateFormat(locale);
		if (this.timeZone != null) {
			dateFormat.setTimeZone(this.timeZone);
		}
		dateFormat.setLenient(this.lenient);
		return dateFormat;
	}
	private DateFormat createDateFormat(Locale locale) {
		if (StringUtils.hasLength(this.pattern)) {
			return new SimpleDateFormat(this.pattern, locale);
		}
		if (this.iso != null && this.iso != ISO.NONE) {
			String pattern = ISO_PATTERNS.get(this.iso);
			if (pattern == null) {
				throw new IllegalStateException("Unsupported ISO format " + this.iso);
			}
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			format.setTimeZone(TimeZone.getTimeZone("UTC"));
			return format;
		}
		if (StringUtils.hasLength(this.stylePattern)) {
			int dateStyle = getStylePatternForChar(0);
			int timeStyle = getStylePatternForChar(1);
			if (dateStyle != -1 && timeStyle != -1) {
				return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
			}
			if (dateStyle != -1) {
				return DateFormat.getDateInstance(dateStyle, locale);
			}
			if (timeStyle != -1) {
				return DateFormat.getTimeInstance(timeStyle, locale);
			}
			throw new IllegalStateException("Unsupported style pattern '"+ this.stylePattern+ "'");

		}
		return DateFormat.getDateInstance(this.style, locale);
	}
	private int getStylePatternForChar(int index) {
		if (this.stylePattern != null && this.stylePattern.length() > index) {
			switch (this.stylePattern.charAt(index)) {
				case 'S': return DateFormat.SHORT;
				case 'M': return DateFormat.MEDIUM;
				case 'L': return DateFormat.LONG;
				case 'F': return DateFormat.FULL;
				case '-': return -1;
			}
		}
		throw new IllegalStateException("Unsupported style pattern '" + this.stylePattern + "'");
	}

	// setter 方法
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public void setIso(ISO iso) {
		this.iso = iso;
	}
	public void setStyle(int style) {
		this.style = style;
	}
	public void setStylePattern(String stylePattern) {
		this.stylePattern = stylePattern;
	}
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}
	public void setLenient(boolean lenient) {
		this.lenient = lenient;
	}
}
