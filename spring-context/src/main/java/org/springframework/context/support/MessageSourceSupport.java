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

package org.springframework.context.support;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.ObjectUtils;

/**
 * Base class for message source implementations, providing support infrastructure
 * such as {@link java.text.MessageFormat} handling but not implementing concrete
 * methods defined in the {@link org.springframework.context.MessageSource}.
 *
 * <p>{@link AbstractMessageSource} derives from this class, providing concrete
 * {@code getMessage} implementations that delegate to a central template
 * method for message code resolution.
 *
 * @author Juergen Hoeller
 * @since 2.5.5
 */
public abstract class MessageSourceSupport {
	protected final Log logger = LogFactory.getLog(getClass());

	private static final MessageFormat INVALID_MESSAGE_FORMAT = new MessageFormat("");

	// 标识是否总是应用MessageFormat规则
	private boolean alwaysUseMessageFormat = false;

	/**
	 * Cache to hold already generated MessageFormats per message.
	 * 缓存保存已生成的每个消息的messageformat。
	 * Used for passed-in default messages.
	 * 用于传递的默认消息。
	 * MessageFormats for resolved codes are cached on a specific basis in subclasses.
	 * 解析代码的消息格式缓存在子类中特定的基础上。
	 */
	private final Map<String, Map<Locale, MessageFormat>> messageFormatsPerMessage = new HashMap<String, Map<Locale, MessageFormat>>();


	public void setAlwaysUseMessageFormat(boolean alwaysUseMessageFormat) {
		this.alwaysUseMessageFormat = alwaysUseMessageFormat;
	}
	protected boolean isAlwaysUseMessageFormat() {
		return this.alwaysUseMessageFormat;
	}

	// 返回给定的默认消息字符串。默认的消息是由调用者指定的，并且可以被呈现为一个完全格式化的默认消息，显示给用户。
	protected String renderDefaultMessage(String defaultMessage, Object[] args, Locale locale) {
		return formatMessage(defaultMessage, args, locale);
	}

	// 使用缓存的messageformat来格式化给定的消息字符串。
	// 默认情况下，在默认情况下调用默认消息，以解决在其中找到的任何参数占位符。
	protected String formatMessage(String msg, Object[] args, Locale locale) {
		if (msg == null || (!this.alwaysUseMessageFormat && ObjectUtils.isEmpty(args))) {
			return msg;
		}
		MessageFormat messageFormat = null;
		synchronized (this.messageFormatsPerMessage) {
			Map<Locale, MessageFormat> messageFormatsPerLocale = this.messageFormatsPerMessage.get(msg);
			if (messageFormatsPerLocale != null) {
				messageFormat = messageFormatsPerLocale.get(locale);
			}
			else {
				messageFormatsPerLocale = new HashMap<Locale, MessageFormat>();
				this.messageFormatsPerMessage.put(msg, messageFormatsPerLocale);
			}
			if (messageFormat == null) {
				try {
					messageFormat = createMessageFormat(msg, locale);
				}
				catch (IllegalArgumentException ex) {
					// invalid message format - probably not intended for formatting,
					// rather using a message structure with no arguments involved
					if (this.alwaysUseMessageFormat) {
						throw ex;
					}
					// silently proceed with raw message if format not enforced
					messageFormat = INVALID_MESSAGE_FORMAT;
				}
				messageFormatsPerLocale.put(locale, messageFormat);
			}
		}
		if (messageFormat == INVALID_MESSAGE_FORMAT) {
			return msg;
		}
		synchronized (messageFormat) {
			return messageFormat.format(resolveArguments(args, locale));
		}
	}

	/**
	 * Create a MessageFormat for the given message and Locale.
	 * @param msg the message to create a MessageFormat for
	 * @param locale the Locale to create a MessageFormat for
	 * @return the MessageFormat instance
	 */
	protected MessageFormat createMessageFormat(String msg, Locale locale) {
		return new MessageFormat((msg != null ? msg : ""), locale);
	}

	// 模板方法，用于被子类重写
	protected Object[] resolveArguments(Object[] args, Locale locale) {
		return args;
	}

}
